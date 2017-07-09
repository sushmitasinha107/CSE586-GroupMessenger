package edu.buffalo.cse.cse486586.groupmessenger1;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String [] REMOTE_PORT = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    static private int sequence = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Allow this Activity to use a layout file that defines what UI elements to use.
         * Please take a look at res/layout/main.xml to see how the UI elements are defined.
         *
         * R is an automatically generated class that contains pointers to statically declared
         * "resources" such as UI elements and strings. For example, R.layout.main refers to the
         * entire UI screen declared in res/layout/main.xml file. You can find other examples of R
         * class variables below.
         */
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));




                /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
/*
         * Retrieve a pointer to the input box (EditText) defined in the layout
         * XML file (res/layout/main.xml).
         *
         * This is another example of R class variables. R.id.edit_text refers to the EditText UI
         * element declared in res/layout/main.xml. The id of "edit_text" is given in that file by
         * the use of "android:id="@+id/edit_text""
         */
        final EditText editText = (EditText) findViewById(R.id.editText1);
        final Button myButton = (Button) findViewById(R.id.button4);
/*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View myView) {
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
//source referred
//http://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html
            try {
                //accept multiple messages using while loop
                while (true) {
                    Socket echoSocket = serverSocket.accept();
                    InputStream ipstr = echoSocket.getInputStream();
                    byte[] buffer = new byte[128];
                    ipstr.read(buffer);
                    String msg = new String(buffer);     //convert the inputstream object to string
                    publishProgress(msg);
                }
            } catch (IOException e) {
                Log.e(TAG, "ServerTask IOException");
            }

            return null;
        }

        protected void onProgressUpdate(String...strings) {
         /*
             * The following code displays what is received in doInBackground().
             */
            //display the message on the textView
            String strReceived = strings[0].trim();
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(strReceived);
            localTextView.append("\n");

            //Insert the key value pair in ContentValues
            String key = Integer.toString(sequence);
            ContentValues values = new ContentValues();
            values.put("key",key);
            values.put("value",strReceived);

            // provide the URI with the value in ContentValues
            Uri.Builder builder = new Uri.Builder();
            builder.authority("edu.buffalo.cse.cse486586.groupmessenger1.provider");
            builder.scheme("content");

            getApplicationContext().getContentResolver().insert(builder.build(),values);
            //increment sequence number for next key-value pair
            sequence++;
            return;
        }

    }


        /***
         * ClientTask is an AsyncTask that should send a string over the network.
         * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
         * an enter key press event.
         *
         * @author stevko
         *
         */
        private class ClientTask extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... msgs) {
                try {
                    for(String stringPort : REMOTE_PORT) {
                        String remotePort = stringPort;
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort));
                        String msgToSend = msgs[0];
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                        OutputStream out = socket.getOutputStream();
                        out.write(msgToSend.getBytes());
                        out.flush();
                        socket.close();
                    }
                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }

                return null;
            }
        }
}
