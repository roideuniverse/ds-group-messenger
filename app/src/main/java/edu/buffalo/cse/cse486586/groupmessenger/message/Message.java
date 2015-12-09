/**
 * 
 */
package edu.buffalo.cse.cse486586.groupmessenger.message;

import java.util.HashMap;
import java.util.Random;

import android.util.SparseIntArray;


/**
 * @author roide
 *
 */
public class Message implements IMessage, Comparable<Message> {
    private static final long serialVersionUID = -1113690495666390385L;
    private int source;
    private int destination;
    private String text;
    private int sequenceNo;
    private String uniqueIdentifier;
    private HashMap<Integer,Integer> vectorTimestamp = new HashMap<Integer,Integer>(5);
    
    public Message(String payload, int local) {
        this.text = payload;
        this.source = local;
        this.sequenceNo=0;
        this.destination = local;
        Random r = new Random();
        r.setSeed(source + System.currentTimeMillis());
        this.uniqueIdentifier = "" + source +  System.currentTimeMillis() + r.nextInt() ;
    }
    
    @Override
    public MessageType type() {
        return MessageType.TEXT_MESSAGE;
    }
    
    @Override
    public int getDestination() {
        return destination;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
    
    @Override
    public Message clone() {
        Message m = new Message(this.text, this.source);
        m.sequenceNo = this.sequenceNo;
        m.destination = this.destination;
        m.uniqueIdentifier = this.uniqueIdentifier;
        return m;
    }
    
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
   
    public void setDestination(int destination) {
        this.destination = destination;
    }
    public int getSource() {
        return source;
    }
    public void setSource(int source) {
        this.source = source;
    }
    public int getSequenceNo() {
        return sequenceNo;
    }
    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    @Override
    public int compareTo(Message another) {
        return (this.sequenceNo-another.sequenceNo);
    }

    public HashMap<Integer,Integer> getVectorTimestamp() {
        return vectorTimestamp;
    }

    public void setVectorTimestamp(HashMap<Integer,Integer> vectorTimestamp) {
        this.vectorTimestamp = vectorTimestamp;
    }
}
