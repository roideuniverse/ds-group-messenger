/**
 * 
 */
package edu.buffalo.cse.cse486586.groupmessenger.multicast;

/**
 * @author roide
 *
 */
public class UnsupportedMultiCastAlgo extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public UnsupportedMultiCastAlgo() {
        // TODO Auto-generated constructor stub
        super("Unsupported Muticast Algorithm requested.");
    }

    /**
     * @param detailMessage
     */
    public UnsupportedMultiCastAlgo(String detailMessage) {
        super(detailMessage);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param throwable
     */
    public UnsupportedMultiCastAlgo(Throwable throwable) {
        super(throwable);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param detailMessage
     * @param throwable
     */
    public UnsupportedMultiCastAlgo(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        // TODO Auto-generated constructor stub
    }

}
