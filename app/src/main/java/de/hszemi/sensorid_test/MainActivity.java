package de.hszemi.sensorid_test;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hszemi.sensorid_test.R;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private List<Sensor> mAccelerometers;
    private List<Sensor> mSensors;
    private List<Sensor> mAllSensors;
    private Map<String, SensorData.SensorDataMessage.Builder> mLogmap;
    SharedPreferences sharedPref;
    private int counter = -1;
    private int numberOfLoops = 20;
    private TextView mTextCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ListView sensorListView = (ListView) findViewById(R.id.sensor_list);
        mAccelerometers = new LinkedList<>();
        mSensors = new LinkedList<>();
        mAllSensors = new LinkedList<>();
        mLogmap = new HashMap<>();
        final EditText editText = (EditText) findViewById(R.id.editText);
        mTextCounter = (TextView) findViewById(R.id.textCounter);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        ListAdapter a = new SensorListViewAdapter(this, mAllSensors);

        sensorListView.setAdapter(a);
        sensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Sensor s = (Sensor) parent.getItemAtPosition(position);
                Log.d("clicked", s.toString());
                gatherSensorData(s, view, editText.getText().toString());
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //vibrateAndRecordAcceleration(view, editText.getText().toString());

//                numberOfLoops = Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_NUMBER_OF_CYCLES, "20"));
//
//                counter = numberOfLoops*mSensors.size();
//                mTextCounter.setText(""+counter);
//
//                for(int i = 0; i < numberOfLoops; i++){
//                    int k = 0;
//                    for (final Sensor s : mSensors) {
//                        Handler handler = new Handler();
//                        Log.d("handler", "Posting sensor in " + (i * mSensors.size() * 6000 + k * 6000) + "ms");
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                gatherSensorData(s, view, editText.getText().toString());
//                            }
//                        }, i * mSensors.size() * 6000 + k * 6000);
//                        k++;
//
//                    }
//                }

                Context context = getApplicationContext();
                CharSequence text = "I DO NOTHING!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            if (SensorHelper.activeSensors.contains(SensorHelper.type2string(s.getType()))) {
                mSensors.add(s);
            }
            mAllSensors.add(s);
        }

        if (mSensors.isEmpty()) {
            mSensors.add(null);
        }


    }

    private void gatherSensorData(final Sensor s, final View view, final String target) {

        Log.d("status", "I am now gathering data from: "+s.getName());
        if(mLogmap.containsKey(s.getName())){
            Snackbar.make(view, "Do not doubl touch plz", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            String display_name = sharedPref.getString(SettingsActivity.KEY_DISPLAY_NAME, "John Doe");
            mLogmap.put(s.getName(), SensorData.SensorDataMessage.newBuilder().setDisplayname(display_name).setSensorname(s.getName()));
            mSensorManager.registerListener((MainActivity) view.getContext(), s, SensorManager.SENSOR_DELAY_FASTEST);

            Handler handler = new Handler();
            Log.d("status", "Handlering! "+s.getName());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSensorManager.unregisterListener((MainActivity) view.getContext(), s);
                    Log.d("status", "Reporting! "+s.getName());
                    reportLogmap(target, s.getName(), SensorHelper.type2string(s.getType()));
                }
            }, 5000);
        }
    }

    private void reportLogmap(String target, String id, String type) {
        SensorData.SensorDataMessage.Builder data = mLogmap.get(id);
        mLogmap.remove(id);

        SensorData.SensorDataMessage sdm = data.build();
        TestData.FeatureVector.Builder featureVector = getFeatureVector(type, id, sdm);
        DataReporter dataReporter = new DataReporter(featureVector.build(), target, getApplicationContext());
        dataReporter.execute();

    }

    private TestData.FeatureVector.Builder getFeatureVector(String type, String name, SensorData.SensorDataMessage sdm) {
        List<Map<String, Double>> data = transformSensorDataMessage(sdm);

        TestData.FeatureVector.Builder fv = TestData.FeatureVector.newBuilder();

        fv.setSensortype(type);
        fv.setSensorname(name);

        fv.setMeanX(Features.mean(data, "x"));
        fv.setMeanY(Features.mean(data, "y"));
        fv.setMeanZ(Features.mean(data, "z"));

        fv.setMinX(Features.lowest(data, "x"));
        fv.setMinY(Features.lowest(data, "y"));
        fv.setMinZ(Features.lowest(data, "z"));

        fv.setMaxX(Features.highest(data, "x"));
        fv.setMaxY(Features.highest(data, "y"));
        fv.setMaxZ(Features.highest(data, "z"));

        fv.setStddevX(Features.stddev(data, "x", fv.getMeanX()));
        fv.setStddevY(Features.stddev(data, "y", fv.getMeanY()));
        fv.setStddevZ(Features.stddev(data, "z", fv.getMeanZ()));

        fv.setAvgdevX(Features.avgdev(data, "x", fv.getMeanX()));
        fv.setAvgdevY(Features.avgdev(data, "y", fv.getMeanY()));
        fv.setAvgdevZ(Features.avgdev(data, "z", fv.getMeanZ()));

        fv.setSkewnessX(Features.skewness(data, "x", fv.getMeanX(), fv.getStddevX()));
        fv.setSkewnessY(Features.skewness(data, "y", fv.getMeanY(), fv.getStddevY()));
        fv.setSkewnessZ(Features.skewness(data, "z", fv.getMeanZ(), fv.getStddevZ()));

        fv.setKurtosisX(Features.kurtosis(data, "x", fv.getMeanX(), fv.getStddevX()));
        fv.setKurtosisY(Features.kurtosis(data, "y", fv.getMeanY(), fv.getStddevY()));
        fv.setKurtosisZ(Features.kurtosis(data, "z", fv.getMeanZ(), fv.getStddevZ()));

        fv.setRmsamplitudeX(Features.rmsamplitude(data, "x"));
        fv.setRmsamplitudeY(Features.rmsamplitude(data, "y"));
        fv.setRmsamplitudeZ(Features.rmsamplitude(data, "z"));

        return fv;


    }

    private List<Map<String, Double>> transformSensorDataMessage(SensorData.SensorDataMessage sdm) {
        List<Map<String, Double>> data = new ArrayList<>();

        long initialTimestamp = sdm.getSensorreading(0).getTimestamp();

        for(SensorData.SensorReading reading : sdm.getSensorreadingList()){
            long timestamp = reading.getTimestamp() - initialTimestamp;
            double x = reading.getX();
            double y = reading.getY();
            double z = reading.getZ();

            Map<String, Double> dict = new HashMap<String, Double>();
            dict.put("timestamp", (double) timestamp);
            dict.put("x", x);
            dict.put("y", y);
            dict.put("z", z);
            data.add(dict);
        }

        return data;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        Log.d("Sensor: ", "Accuracy changed to " + accuracy);
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        //Log.d("Values: ",event.sensor.getName()+":"+event.values[0]+"-"+event.values[1]+"-"+event.values[2]);
        // Do something with this sensor value.
        SensorData.SensorDataMessage.Builder builder = mLogmap.get(event.sensor.getName());
        if (builder == null) {
            Log.e("builder", "builder for " + event.sensor.getName() + " is null!");
        } else {
            SensorData.SensorReading.Builder reading = SensorData.SensorReading.newBuilder()
                    .setTimestamp(event.timestamp);

            if(event.values != null && event.values.length > 0){
                reading.setX(event.values[0]);
            }
            if(event.values != null && event.values.length > 1) {
                reading.setY(event.values[1]);
            }
            if(event.values != null && event.values.length > 2) {
                reading.setZ(event.values[2]);
            }
            builder.addSensorreading(reading);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        for (Sensor s : mAccelerometers) {
//            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mSensorManager.unregisterListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
