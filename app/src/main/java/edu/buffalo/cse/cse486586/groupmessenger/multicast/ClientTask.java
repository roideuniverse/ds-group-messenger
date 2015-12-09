package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import edu.buffalo.cse.cse486586.groupmessenger.message.IMessage;
import edu.buffalo.cse.cse486586.groupmessenger.message.Message;
import edu.buffalo.cse.cse486586.groupmessenger.message.MessageType;

class ClientTask extends AsyncTask<IMessage, Void, Void> {
    private final String TAG = "ClientTask";
    @Override
    protected Void doInBackground(IMessage... msgArray) {
        Log.d(TAG, "ClientTask : doInBackground");
        IMessage msgToSend = msgArray[0];
        Socket socket = null;
        if(msgToSend.type() == MessageType.TEXT_MESSAGE) 
            Log.d(TAG, "sending msgType=" + msgToSend.type() + " destination=" + msgToSend.getDestination());
        else
            Log.d(TAG, "Sent SEQ No:" + msgToSend.getSequenceNo()+  " to:"  +  + msgToSend.getDestination());
        
        try {
             socket = new Socket(InetAddress.getByAddress(new byte[] {
                    10, 0, 2, 2
            }), msgToSend.getDestination());

            //Log.d(TAG, "ClientTask : doInBackground::msg=" + msgToSend.getText());
            ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
            oStream.writeObject(msgToSend);
            oStream.close();
            closeSocket(socket);
        } catch (UnknownHostException e2) {
            e2.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, " exception:" + e);
            e.printStackTrace();
            closeSocket(socket);
        }
        return null;
    }
    private void closeSocket(Socket socket) {
        if(socket != null) {
            if(!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(TAG, "exception::closeSocket::" + e);
                    e.printStackTrace();
                }
            }
        }
    }
}
