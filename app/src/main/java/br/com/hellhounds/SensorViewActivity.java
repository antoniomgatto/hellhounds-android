package br.com.hellhounds;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;

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
import java.util.Random;

public class SensorViewActivity extends AppCompatActivity {

    GraphView mGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        List<DataPoint> dataPoints = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        for (int i = 0; i <= 48; i++) {
            calendar.add(Calendar.MINUTE, 30);
            Logger.d("teste " + calendar.get(Calendar.HOUR_OF_DAY));
            dataPoints.add(new DataPoint(calendar.getTime(), new Random().nextInt(25)));
        }

        Calendar now = Calendar.getInstance();
        Date maxDate = now.getTime();
        now.add(Calendar.HOUR, -24);
        Date minDate = now.getTime();

        mGraphView = (GraphView) findViewById(R.id.sensor_graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints.toArray(new DataPoint[]{}));
        series.setTitle(getString(R.string.label_graph_temperatura));
        series.setBackgroundColor(Color.BLACK);
        series.setDrawDataPoints(true);
        mGraphView.addSeries(series);

        // set date label formatter
        mGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this, DateFormat.getTimeFormat(this)));
        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(4);

        // set manual x bounds to have nice steps
//        mGraphView.getViewport().setMinX(minDate.getTime());
//        mGraphView.getViewport().setMaxX(maxDate.getTime());
//        mGraphView.getViewport().setXAxisBoundsManual(true);

        mGraphView.getViewport().setMaxY(30);
        mGraphView.getViewport().setMinY(-10);
        mGraphView.getViewport().setYAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        mGraphView.getGridLabelRenderer().setHumanRounding(false);

        mGraphView.getViewport().setScalable(true);
        mGraphView.getViewport().setScalableY(true);

        mGraphView.getLegendRenderer().setVisible(true);
        mGraphView.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }
}
