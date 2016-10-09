package de.hszemi.sensorid_test;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * DataReporter sends a FeatureVector to the target host and receives
 * a TestResult that it then displays to the user.
 */
public class DataReporter extends AsyncTask<Void, Void, String> {
    private TestData.FeatureVector serializedData;
    private TestData.TestResult response;
    private String targetHost;
    private Context context;

    public DataReporter(TestData.FeatureVector serializedData, String targetHost, Context context) {
        this.serializedData = serializedData;
        this.targetHost = targetHost;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        Socket socket = null;
        try {

            socket = new Socket(this.targetHost, 54322);

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            // first we send the FeatureVector…
            serializedData.writeDelimitedTo(os);

            //…then we receive the TestData answer.
            response = TestData.TestResult.parseDelimitedFrom(is);

            is.close();
            os.close();
            socket.close();
            if(response == null){
                return "ERROR_RESPONSE_WAS_NULL";
            } else {
                Log.d("RESULT", response.getResultDisplayname());
                return response.getResultDisplayname();
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(socket != null){
                try{
                    socket.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        // If we reach this part of the code, an exception occurred.
        // Probably the host did not respond.
        return "NO_RESULT\nIs the server even running?";
    }

    @Override
    protected void onPostExecute(String retval){

        // Display the result to the user! Might be either a device id
        // or an internal error message.
        Toast.makeText(context, retval, Toast.LENGTH_LONG).show();
    }

    public TestData.FeatureVector getSerializedData() {
        return serializedData;
    }

    public void setSerializedData(TestData.FeatureVector serializedData) {
        this.serializedData = serializedData;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

}
