package br.com.hellhounds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SensorViewActivity extends AppCompatActivity {

    private static final String SENSOR_ID_PARAM = "SENSOR_ID";

    private GraphView mGraphView;
    private DatabaseReference mDatabase;
    private List<Sensor> mSensorHistoryList;
    private Date mMinDate;

    private String mSensorId;

    public static Intent newIntent(Context context, String sensorId) {
        Intent intent = new Intent(context, SensorViewActivity.class);
        intent.putExtra(SENSOR_ID_PARAM, sensorId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_view);

        mSensorId = getIntent().getStringExtra(SENSOR_ID_PARAM);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mGraphView = (GraphView) findViewById(R.id.sensor_graph);

        mSensorHistoryList = new ArrayList<Sensor>();

        setupDB();
    }

    private void showGraph() {

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
        mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, DateFormat.getTimeFormat(this)));
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(4);

//        mGraphView.getViewport().setScalableY(true);
//        mGraphView.getViewport().setScrollableY(true);

//       set manual x bounds to have nice steps
//        mGraphView.getViewport().setMinX(mMinDate.getTime());
//        mGraphView.getViewport().setMaxX(mMaxDate.getTime());
//        mGraphView.getViewport().setXAxisBoundsManual(true);
        mGraphView.invalidate();

    }

    private void setupDB() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // find min date
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, -24);
        mMinDate= now.getTime();

        Logger.d("sensorId " + mSensorId);

        Query query =  mDatabase.child("sensors_history")
                                .child(mSensorId)
                                .orderByChild("updatedAt")
                                .startAt(mMinDate.getTime());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mSensorHistoryList = new ArrayList<Sensor>();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Sensor sensor = snapshot.getValue(Sensor.class);
                    Logger.d("Data do updatedAt " + new Date(sensor.getUpdatedAt()));
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
}
