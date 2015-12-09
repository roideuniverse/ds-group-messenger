package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import java.util.Comparator;

import edu.buffalo.cse.cse486586.groupmessenger.message.Message;

public class MessageComparator implements Comparator<Message>{

    public MessageComparator() {
    }

    @Override
    public int compare(Message lhs, Message rhs) {
        return lhs.getSequenceNo() - rhs.getSequenceNo();
    }

}
