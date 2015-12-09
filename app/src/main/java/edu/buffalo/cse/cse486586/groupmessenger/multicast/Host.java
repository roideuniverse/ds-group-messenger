/**
 * 
 */
package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import java.io.Serializable;

/**
 * @author roide
 *
 */
public class Host implements Serializable {

    private static final long serialVersionUID = -5416133382307995175L;
    
    private int port;
    private int id;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public Host(int id, int port) {
        this.id = id;
        this.port = port;
    }

}
