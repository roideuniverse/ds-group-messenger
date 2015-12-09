/**
 * 
 */
package edu.buffalo.cse.cse486586.groupmessenger.message;

import java.io.Serializable;

/**
 * @author roide
 *
 */
public interface IMessage extends Serializable {
    public MessageType type();
    public int getDestination();
    public String getUniqueIdentifier();
    public int getSequenceNo();
    public IMessage clone();
}
