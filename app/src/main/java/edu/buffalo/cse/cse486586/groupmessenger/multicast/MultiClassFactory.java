/**
 * 
 */
package edu.buffalo.cse.cse486586.groupmessenger.multicast;

import edu.buffalo.cse.cse486586.groupmessenger.GroupMessengerActivity;

/**
 * @author roide
 *
 */
public class MultiClassFactory {
    private static MultiCastLib mLib ;
    public static MultiCastLib getInstance(int type, GroupMessengerActivity act, Host local) throws UnsupportedMultiCastAlgo {
        if(mLib == null) {
            mLib = new MultiCastLib(type,act, local);
        }
        return mLib;
    }
}
