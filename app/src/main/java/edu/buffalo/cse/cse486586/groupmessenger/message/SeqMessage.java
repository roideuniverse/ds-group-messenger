package edu.buffalo.cse.cse486586.groupmessenger.message;

public class SeqMessage implements IMessage {

    private static final long serialVersionUID = -5209570857640742185L;
    
    private String uniqueIdentifier;
    private int sequenceNo;
    private int destination;
    
    public SeqMessage(String id, int seqNo) {
        this.uniqueIdentifier = id;
        this.sequenceNo = seqNo;
    }
    
    @Override
    public MessageType type() {
        return MessageType.GLOBAL_SEQUENCE_ORDER;
    }

    @Override
    public SeqMessage clone() {
        SeqMessage m = new SeqMessage(this.uniqueIdentifier, this.sequenceNo);
        m.destination = this.destination;
        return m;
    }
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }


}
