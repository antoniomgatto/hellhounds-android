package br.com.hellhounds;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SensorViewActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String SENSOR_ID_PARAM = "SENSOR_ID";

    private static final int PERIOD_LAST_24_HOURS_SELECTED = 0;
    private static final int PERIOD_LAST_WEEK_SELECTED = 1;
    private static final int PERIOD_ALL_SELECTED = 2;

    private Spinner mGraphPeriodSelector;
    private GraphView mGraphView;
    private TextView mSensorIdView;
    private TextView mArduinoView;
    private TextView mCurrentTemperatureView;
    private TextView mTargetTemperaturaView;
    private TextView mUpdateAtView;

    private DatabaseReference mDatabase;
    private List<Sensor> mSensorHistoryList;
    private Date mMinDate;
    private int mPeriodSelected;
    private String mSensorId;
    private Sensor mSensor;

    public static Intent newIntent(Context context, String sensorId) {
        Intent intent = new Intent(context, SensorViewActivity.class);
        intent.putExtra(SENSOR_ID_PARAM, sensorId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensor_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSensorId = getIntent().getStringExtra(SENSOR_ID_PARAM);

        mGraphView = (GraphView) findViewById(R.id.sensor_graph);
        mSensorIdView = (TextView) findViewById(R.id.sensorId);
        mArduinoView = (TextView) findViewById(R.id.arduino);
        mCurrentTemperatureView = (TextView) findViewById(R.id.currentTemperature);
        mTargetTemperaturaView = (TextView) findViewById(R.id.targetTemperature);
        mUpdateAtView = (TextView) findViewById(R.id.updateAt);

        mSensorHistoryList = new ArrayList<Sensor>();

        setupGraphSelector();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        updateSensorInfo();
        updateSensorHistory();
    }

    private void setupGraphSelector() {
        mGraphPeriodSelector = (Spinner) findViewById(R.id.sensor_graph_period_selector);
        ArrayAdapter<CharSequence> graphSelectorAdapter = ArrayAdapter.createFromResource(this,
                R.array.graph_period_options, android.R.layout.simple_spinner_dropdown_item);
        graphSelectorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mGraphPeriodSelector.setAdapter(graphSelectorAdapter);
        mGraphPeriodSelector.setOnItemSelectedListener(this);
        mPeriodSelected = PERIOD_LAST_24_HOURS_SELECTED;
    }

    private void showGraph() {

        mGraphView.removeAllSeries();
        mGraphView.clearSecondScale();

        List<DataPoint> dataPoints = new ArrayList<>();

        for(Sensor sensor : mSensorHistoryList) {
            dataPoints.add(new DataPoint(new Date(sensor.getUpdatedAt()), sensor.getCurrentTemperature()));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[]{}));
        series.setTitle(getString(R.string.label_graph_temperatura));
        series.setBackgroundColor(Color.BLACK);
        series.setDrawDataPoints(true);
        mGraphView.addSeries(series);

        mGraphView.getViewport().setMaxY(30);
        mGraphView.getViewport().setMinY(-10);
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getGridLabelRenderer().setNumVerticalLabels(5);

        mGraphView.getLegendRenderer().setVisible(true);
        mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        // set date label formatter
        if (mPeriodSelected == PERIOD_LAST_24_HOURS_SELECTED) {
            mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, DateFormat.getTimeFormat(this)));
        } else {
            mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, DateFormat.getDateFormat(this)));
        }

        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(3);

        mGraphView.getViewport().setScalable(true);
        mGraphView.getViewport().setScalableY(true);

        if (mSensorHistoryList.size() > 1) {
            Sensor minSensorHistory = mSensorHistoryList.get(0);
            Sensor maxSensorHistory = mSensorHistoryList.get(mSensorHistoryList.size() - 1);
            mGraphView.getViewport().setMinX(minSensorHistory.getUpdatedAt());
            mGraphView.getViewport().setMaxX(maxSensorHistory.getUpdatedAt());
            mGraphView.getViewport().setXAxisBoundsManual(true);
        }

    }

    private void updateSensorInfo() {
        mDatabase.child("sensors").child(mSensorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSensor = dataSnapshot.getValue(Sensor.class);

                mSensorIdView.setText(mSensor.getId());
                mArduinoView.setText(mSensor.getArduino());
                mCurrentTemperatureView.setText(getString(R.string.label_graus_celsius, mSensor.getCurrentTemperature()));
                mTargetTemperaturaView.setText(getString(R.string.label_graus_celsius, mSensor.getTargetTemperature()));
                mUpdateAtView.setText(java.text.DateFormat.getDateTimeInstance().format(new Date(mSensor.getUpdatedAt())));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SensorViewActivity.this, getString(R.string.message_general_firebase_problem), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateSensorHistory() {

        Calendar now = Calendar.getInstance();
        mMinDate = null;

        switch (mPeriodSelected) {
            case PERIOD_LAST_WEEK_SELECTED:
                now.add(Calendar.DATE, -7);
                mMinDate = now.getTime();
                break;
            case PERIOD_LAST_24_HOURS_SELECTED:
                now.add(Calendar.HOUR, -24);
                mMinDate = now.getTime();
                break;
        }

        Query query;

        if (mMinDate != null) {
            query = mDatabase.child("sensors_history")
                    .child(mSensorId)
                    .orderByChild("updatedAt")
                    .startAt(mMinDate.getTime());
        } else {
            query = mDatabase.child("sensors_history")
                    .child(mSensorId)
                    .orderByChild("updatedAt");
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mSensorHistoryList = new ArrayList<Sensor>();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Sensor sensor = snapshot.getValue(Sensor.class);
                    mSensorHistoryList.add(sensor);
                }

                showGraph();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SensorViewActivity.this, getString(R.string.message_general_firebase_problem), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        mPeriodSelected = position;
        updateSensorHistory();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sensor_view, menu);
        menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_settings_white_24dp));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_set_temperature:

                    LayoutInflater inflater = LayoutInflater.from(this);
                    View dialogView = inflater.inflate(R.layout.dialog_temperature, null);
                    final TextView temperatureView = dialogView.findViewById(R.id.temperature_view);
                    final SeekBar seekBar = dialogView.findViewById(R.id.temperature_seekbar);
                    seekBar.setMax(30);
                    seekBar.setKeyProgressIncrement(1);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            temperatureView.setText(getString(R.string.label_graus_celsius, progress));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setTitle(getString(R.string.label_dialog_new_temperature));
                    dialogBuilder.setView(dialogView);
                    dialogBuilder.setPositiveButton(getString(R.string.button_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {

                            HashMap<String, Object> payload = new HashMap<String, Object>();
                            payload.put(mSensor.getId(), seekBar.getProgress());

                            mDatabase.child("sensors_config").child(mSensor.getArduino()).setValue(payload);

                            Toast.makeText(SensorViewActivity.this, getString(R.string.message_temperature_save_success), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setNegativeButton(getString(R.string.button_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });

                    //init data
                    temperatureView.setText(getString(R.string.label_graus_celsius, mSensor.getTargetTemperature()));
                    seekBar.setProgress(mSensor.getTargetTemperature());

                    dialogBuilder.create();
                    dialogBuilder.show();
                break;
        }

        return true;
    }
}
