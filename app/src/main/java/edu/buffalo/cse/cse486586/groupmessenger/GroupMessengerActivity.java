package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.IOException;
import java.util.ArrayList;

import edu.buffalo.cse.cse486586.groupmessenger.message.Message;
import edu.buffalo.cse.cse486586.groupmessenger.multicast.MultiCastAlgo;
import edu.buffalo.cse.cse486586.groupmessenger.multicast.MultiCastLib;
import edu.buffalo.cse.cse486586.groupmessenger.multicast.MultiClassFactory;
import edu.buffalo.cse.cse486586.groupmessenger.multicast.Host;
import edu.buffalo.cse.cse486586.groupmessenger.multicast.UnsupportedMultiCastAlgo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity implements IRemoteMessageListener {
    
    private final String TAG = GroupMessengerActivity.class.getSimpleName();
    private TextView mTextView;
    private EditText mEditText;
    private Button mButton;
    private MultiCastLib mMultiCastLib;
    
    private ArrayList<Integer> mPortList = new ArrayList<Integer>();
    private Uri mUri ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        mPortList.add(11108);
        mPortList.add(11112);
        mPortList.add(11116);
        mPortList.add(11120);
        mPortList.add(11124);
        mUri = GroupMessengerProvider.CONTENT_URI;

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        mTextView = (TextView) findViewById(R.id.textView1);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(mTextView, getContentResolver()));
        //findViewById(R.id.button1).setOnClickListener(mTestOnClick);
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        mEditText = (EditText) findViewById(R.id.editText1);
        mEditText.setOnKeyListener(mKeyListener);
        
        mButton = (Button) findViewById(R.id.button4);
        mButton.setOnClickListener(mButtonOnClickListener);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final int localPort = (Integer.parseInt(portStr) * 2);
        Log.d(TAG, "local_port=" + localPort  );
        
        try {
            mMultiCastLib = MultiClassFactory.getInstance(MultiCastAlgo.TOTAL_ORDER.ordinal(), this, new Host(localPort,localPort));
            for(Integer port:mPortList) {
                Host host = new Host(port, port);
                mMultiCastLib.addNode(host);
            }
        } catch (UnsupportedMultiCastAlgo e) {
            mTextView.setText("Failed To Initialize Multicast");
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(this.isFinishing()) {
            if(mMultiCastLib != null)
                mMultiCastLib.stopServer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    OnClickListener mButtonOnClickListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            String msg = mEditText.getText().toString() + "\n";
            mEditText.setText(""); 
            
            //mTextView.setText(mTextView.getText() + "\n" + "local:" + msg);
            Message m = new Message(msg,mMultiCastLib.getLocalNode().getPort());
            mMultiCastLib.multicast(m);
        }
    };
    
    OnKeyListener mKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                
                String msg = mEditText.getText().toString() + "\n";
                mEditText.setText(""); 
                
                //mTextView.setText(mTextView.getText() + "\n" + "local:" + msg);
                Message m = new Message(msg,mMultiCastLib.getLocalNode().getPort());
                mMultiCastLib.multicast(m);
                return true;
            }
            return false;
        }
    };

    @Override
    public void displayMessage(Message message) {
        Log.d(TAG, "display message: " + message.getText());
        ContentValues values = new ContentValues();
        //values.put(""+message.getSequenceNo(), message.getText().trim());
        values.put("key", message.getSequenceNo());
        values.put("value", message.getText().trim());
        this.getContentResolver().insert(mUri, values);
        mTextView.setText(mTextView.getText() + "\n" +  message.getSource() + " : " + message.getText().trim() + "\t->" + message.getSequenceNo());
    }
    
    OnClickListener mTestOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            for(int i=1;i<=50;i++) {
                mEditText.setText("kaushik" + i);
                mButton.performClick();
            }
        }
        
    };
    
}
