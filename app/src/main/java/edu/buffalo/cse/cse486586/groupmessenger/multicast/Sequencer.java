/**
 * 
 */

package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import edu.buffalo.cse.cse486586.groupmessenger.message.Message;
import edu.buffalo.cse.cse486586.groupmessenger.message.SeqMessage;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author roide
 */
public class Sequencer {
    private String TAG = Sequencer.class.getSimpleName();

    private MultiCastLib mMultiCastLib;
    
    private static int globalSequenceNumber = 0;

    /**
     * 
     */
    public Sequencer(MultiCastLib multicastLib, int type) {
        mMultiCastLib = multicastLib;
    }
    
    public void processSequence(Message msg) {
        SeqMessage sMsg = new SeqMessage(msg.getUniqueIdentifier(), globalSequenceNumber++);
        this.dispatch(sMsg);
    }

    public void dispatch(SeqMessage msg) {
        for (Host n : mMultiCastLib.getMultiCastGroupList())
            sendMessage(msg.clone(), n);
    }

    private void sendMessage(SeqMessage m, Host n) {
        m.setDestination(n.getPort());
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, m);
        Log.d(TAG, "Sent SEQ No:" + m.getSequenceNo()+  " to:"  +  + m.getDestination());
    }
}
