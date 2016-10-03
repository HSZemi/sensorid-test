package de.hszemi.sensorid_test;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by zemanek on 30.06.16.
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

            serializedData.writeDelimitedTo(os);
            response = TestData.TestResult.parseDelimitedFrom(is);

            Log.d("RESULT", response.getResultDisplayname());

            is.close();
            os.close();
            socket.close();
            return response.getResultDisplayname();

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
        return "NO_RESULT";
    }

    @Override
    protected void onPostExecute(String retval){
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, retval, duration);
        toast.show();
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
