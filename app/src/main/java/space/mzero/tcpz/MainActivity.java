package space.mzero.tcpz;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "space.mzero.tcpz.MESSAGE";
    public static String msg = "default";
    public static String resp = "No Response Yet\nTry Sending Some Data.";
    public static String server_address = "10.0.0.6";
    public static Integer server_port = 3333;
    public static Boolean Abort = false;
    public static LongOperation lo = null;

    public static Socket socket = null;

    public static final String MY_PREFS_NAME = "MyPrefsFile";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String nserver_address = prefs.getString("server", null);

        if (nserver_address!= null){
            Log.d("load server",nserver_address);
            TextView server_text = (TextView) findViewById(R.id.serverText);
            server_text.setText(nserver_address);
        }
        Integer nserver_port = prefs.getInt("port", 3333);

        if (nserver_port!= 3333){
            Log.d("load port",nserver_port.toString());
            TextView port_text = (TextView) findViewById(R.id.portText);
            port_text.setText(nserver_port.toString());
        }
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText(resp);
        EditText editText = (EditText) findViewById(R.id.editText);
        Log.d("create",msg);

        editText.setText(msg);
    }



    private class LongOperation extends AsyncTask<String, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... params) {

            socket = null;
            SocketAddress address = new InetSocketAddress(server_address, server_port);

            socket = new Socket();


            try {
                socket.connect(address, 3000);
            } catch (IOException e) {
                Log.d("time","no worky X");
                e.printStackTrace();
            }
            try {
                socket.setSoTimeout(3000);
            } catch (SocketException e) {
                Log.d("timeout","server took too long to respond");

                e.printStackTrace();
                return "Can't Connect";
            }
            OutputStream out = null;
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter output = new PrintWriter(out);


            output.print(msg);
            output.flush();

//read
            String str = "waiting";
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.d("test","trying to read from server");

                String line;
                str = "";
                while ((line = br.readLine()) != null) {
                    Log.d("read line",line);
                    str = str + line;
                    str = str + "\r\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (str != null) {
                Log.d("test","trying to print what was just read");
                System.out.println(str);
            }


//read
            output.close();

//read
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//read
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("tag", "done server");
            return str;
        }

        @Override
        protected void onPostExecute(String result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            Abort = false;
            Log.d("Set Abort",Abort.toString());
            Log.d("tag","post ex");
            resp = result;
            TextView textView = (TextView) findViewById(R.id.textView2);
            textView.setText(resp);

        }

        @Override
        protected void onPreExecute() {
            TextView server_text = (TextView) findViewById(R.id.serverText);
            TextView port_text = (TextView) findViewById(R.id.portText);
            server_address = server_text.getText().toString();
            server_port = Integer.parseInt(port_text.getText().toString());
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("server", server_address);
            editor.putInt("port", server_port);
            editor.commit();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}


        protected void onCancelled(){
            Log.d("cancel","ca");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Abort = false;
        }


    }
    /** Called when the user taps the Send button */
    public void sendMessage(View view) {

        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setText("Loading...");
        msg = message;
        Log.d("msg",msg);
        // intent.putExtra(EXTRA_MESSAGE, message);
        Log.d("Check Abort",Abort.toString());
        if(Abort==true) {
            lo.cancel(false);
            Log.d("Aborting",Abort.toString());
        }
        else {
            lo = new LongOperation();
            lo.execute();
        }
        Abort = true;

        //startActivity(intent);
    }
}