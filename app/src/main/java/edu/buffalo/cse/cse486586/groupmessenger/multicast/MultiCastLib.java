/**
 * 
 */

package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.buffalo.cse.cse486586.groupmessenger.GroupMessengerActivity;
import edu.buffalo.cse.cse486586.groupmessenger.message.IMessage;
import edu.buffalo.cse.cse486586.groupmessenger.message.Message;
import edu.buffalo.cse.cse486586.groupmessenger.message.MessageType;
import edu.buffalo.cse.cse486586.groupmessenger.message.SeqMessage;

/**
 * @author roide
 */
public class MultiCastLib {
    private String TAG = MultiCastLib.class.getSimpleName();
    
    private ArrayList<Host> mMultiCastGroup = new ArrayList<Host>();
    private Sequencer mSequencer;
    private Host mLocalNode;
    private boolean mHasSequencer = false;
    private int sequenceNumber = 0;
    private int highestSeqNoSeen=-1;
    
    private PriorityQueue<Message> mDeliveryQueue;
    private ConcurrentHashMap<String, Message> mUnresolvedTextMessageBuffer;
    private ConcurrentHashMap<String, SeqMessage> mUnresolvedSeqMessageBuffer;
    
    private HashSet<String> mHistory;
    
    private Context mAppContext;
    private GroupMessengerActivity mActivity ;
    private ServerSocket mServerSocket;
    static final int SERVER_PORT = 10000;
    
    MultiCastLib(int multiCastType, GroupMessengerActivity activity, Host localHost) throws UnsupportedMultiCastAlgo {
        mActivity = activity;
        mLocalNode = localHost;
        //Log.d(TAG, "MultiCastLib Constructor");
        if (multiCastType == MultiCastAlgo.TOTAL_ORDER.ordinal()) {
            //check for total order
            //mSequencer = new Sequencer(this,multiCastType);
        } else if (multiCastType == MultiCastAlgo.CAUSAL_ORDER.ordinal()) {
            //check for causal order
            //mSequencer = new Sequencer(this,multiCastType);
        } else if (multiCastType == (MultiCastAlgo.TOTAL_ORDER.ordinal() & MultiCastAlgo.CAUSAL_ORDER
                .ordinal())) {
            //check for hybrid causal and total order(project objective)
            //mSequencer = new Sequencer(this, multiCastType);
        } else {
            //Unimplemented algorithm
            throw new UnsupportedMultiCastAlgo();
        }
        mDeliveryQueue = new PriorityQueue<Message>(50, new MessageComparator());
        mUnresolvedTextMessageBuffer = new ConcurrentHashMap<String, Message>();
        mUnresolvedSeqMessageBuffer = new ConcurrentHashMap<String, SeqMessage>();
        mHistory = new HashSet<String>();
        
        if(mLocalNode.getPort() == 11108) {
            mSequencer = new Sequencer(this, multiCastType);
            this.mHasSequencer = true;
        }
        startServer();
    }
    
    public void setLocalNode(Host node) {
        Log.d(TAG, "localHoat:" + node.getId());
        this.mLocalNode = node;
    }
    
    public Host getLocalNode() {
        return this.mLocalNode;
    }

    public void addNode(Host node) {
        Log.d(TAG, "addHost:" + node.getId());
        mMultiCastGroup.add(node);
    }

    public void removeNode(Host node) {
        if (mMultiCastGroup.contains(node))
            mMultiCastGroup.remove(node);
    }
    
    public ArrayList<Host> getMultiCastGroupList() {
        return this.mMultiCastGroup;
    }
    
   /* public int getSequenceNo() {
        return this.sequenceNumber++;
    }*/
    
    public int getMultiCastGroupCount() {
        return mMultiCastGroup.size();
    }
    
    private void startServer() {
        try {
            mServerSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mServerSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (mServerSocket != null)
            if (!mServerSocket.isClosed())
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "close_socket::" + e.toString());
                }
    }
    
    public void multicast(Message msg) {
        this.dispatch(msg);
    }
    
    public void dispatch(Message msg) {
        Log.d(TAG, " groupList=" + this.getMultiCastGroupList() );
        for (Host n : this.getMultiCastGroupList()) {
            sendMessage(msg.clone(), n);
        }
    }
    
    private void sendMessage(Message msg, Host n) {
        Log.d(TAG, "Sending Text message to:"+ n.getPort());
        msg.setDestination(n.getPort());
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg);
    }
    
    public HashSet<String> getHistory() {
        return this.mHistory;
    }
    
    private class ServerTask extends AsyncTask<ServerSocket, IMessage, Void> {
        private static final String TAG = "ServerTask";
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            Log.d(TAG, "ServerTask:doInBackground");
            ServerSocket serverSocket = sockets[0];
            try {           
                while(true) {
                    Socket client = serverSocket.accept();
                    //Log.d(TAG, "Accepted?-->" + client);
                    ObjectInputStream iStream = new ObjectInputStream(client.getInputStream());
                    IMessage msg = (IMessage) iStream.readObject();
                    Log.d(TAG, "Received:" + msg.type() +" "+ msg.getSequenceNo() + " id:" + msg.getUniqueIdentifier());
                    publishProgress(msg);
                }
            } catch (IOException e) {
                Log.d(TAG, "ServerTask:doInBackground:" + e.toString());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        @Override
        protected void onProgressUpdate(IMessage...message) {
            IMessage msg = message[0];
            if(msg.type() == MessageType.GLOBAL_SEQUENCE_ORDER) {
                resolveSeqNoAndDeliver(msg);
            } else if(msg.type() == MessageType.TEXT_MESSAGE) {
                //check if this message is a repeat message
                if(mHistory.contains(msg.getUniqueIdentifier())) {
                    Log.w(TAG, "Got Replay of delivered message-ignoring");
                    return;
                }
                //check if we already have a seq no for this message
                if(mUnresolvedSeqMessageBuffer.containsKey(msg.getUniqueIdentifier())) {
                    int seqNo = mUnresolvedSeqMessageBuffer.get(msg.getUniqueIdentifier()).getSequenceNo() ;
                    Log.d(TAG, "Got a message for which already Glob_seq no in Buffer SeqNo:" + seqNo);
                    Message tMessage = (Message) msg;
                    tMessage.setSequenceNo(seqNo);
                    mUnresolvedSeqMessageBuffer.remove(msg.getUniqueIdentifier());
                    addToDeliveryQueue(tMessage);
                    return;
                }
                Message m = (Message) msg;
                Log.d(TAG, "message=" + m.getText());
                mUnresolvedTextMessageBuffer.put(m.getUniqueIdentifier(), m);
                if(mHasSequencer) {
                    Log.d(TAG, "Sending msg to sequencer:" + m.getText());
                    mSequencer.processSequence(m);
                }
            }
        }
    }
    
    /*
     * we have received a sequence number for a message. time to deliver it.
     */
    private void resolveSeqNoAndDeliver(IMessage msg) {
        Log.d(TAG, "resolveSeqNoAndDeliver()-start");
        if(mUnresolvedTextMessageBuffer.containsKey(msg.getUniqueIdentifier())) {
            Message m = mUnresolvedTextMessageBuffer.get(msg.getUniqueIdentifier() );
            m.setSequenceNo(msg.getSequenceNo());
            mUnresolvedTextMessageBuffer.remove(msg.getUniqueIdentifier());
            addToDeliveryQueue(m);
            //Log.d(TAG, "found msg with seqNo"+ msg.getSequenceNo() + "text=" + m.getText());
        } else {
            Log.e(TAG, "Received Seq No for msg not in buffer id:" + msg.getUniqueIdentifier());
            if(mHistory.contains(msg.getUniqueIdentifier())) {
                Log.d(TAG, "Msg for this seq no already in delivery-ignoring");
            } else {
                Log.d(TAG, "Seq no has come before message-putting it in buffer");
                mUnresolvedSeqMessageBuffer.put(msg.getUniqueIdentifier(), (SeqMessage)msg);
            }
        }
        Log.d(TAG, "resolveSeqNoAndDeliver()-end");
    }
    
    /*
     * poll the messages in the delivery queue and deliver them to the application 
     */
    private void poll() {
        Log.d(TAG, "polling for delivery");
        Log.d(TAG, "unresolvedMesg::" + mUnresolvedTextMessageBuffer.size());
        Log.d(TAG, "DeliveryQueue ::" + mDeliveryQueue.size());
        
        //PriorityQueue<Message> copyQ = new PriorityQueue<Message>();
        Message m;
        while((m=mDeliveryQueue.peek()) != null) {
            Log.d(TAG, "got message from Que SeqNo=" + m.getSequenceNo());
            int diff = m.getSequenceNo() - highestSeqNoSeen ;
            if( diff == 1) {
                m=mDeliveryQueue.poll();
                mActivity.displayMessage(m);
                highestSeqNoSeen++;
                Log.d(TAG, "delQsize:" + mDeliveryQueue.size() + " seqSize:" + mUnresolvedSeqMessageBuffer.size());
            } else if( diff > 1) {
                break;
            } else {
                try {
                    throw new Exception("This is impossible-This means I have delivered a msg will less seq no");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //copyQ.add(m);
        }
        //mDeliveryQueue = copyQ ;
    }
    
    private void addToDeliveryQueue(Message m) {
        mDeliveryQueue.add(m);
        mHistory.add(m.getUniqueIdentifier());
        poll();
    }
}
