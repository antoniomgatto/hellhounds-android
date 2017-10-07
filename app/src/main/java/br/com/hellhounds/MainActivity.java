package br.com.hellhounds;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orhanobut.logger.Logger;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private ListView mSensorListView;
    private List<Sensor> mSensorList = new ArrayList<Sensor>();
    private CustomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSensorListView = (ListView) findViewById(R.id.sensor_list);
        mSensorListView.setEmptyView(findViewById(android.R.id.empty));
        mSensorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Sensor sensor = mAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, SensorViewActivity.class);
                startActivity(intent);
            }
        });

        mAdapter = new CustomAdapter();
        mSensorListView.setAdapter(mAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        getData();
    }

    void getData() {
        mDatabase.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Sensor> tmpSensorList = new ArrayList<Sensor>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Logger.d(snapshot);
                    Sensor sensor = snapshot.getValue(Sensor.class);
                    tmpSensorList.add(sensor);
                }

                mSensorList.clear();
                mSensorList.addAll(tmpSensorList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, getString(R.string.message_general_firebase_problem), Toast.LENGTH_LONG).show();
            }
        });
    }

    public class CustomAdapter extends ArrayAdapter<Sensor>  {

        private class ViewHolder {
            final TextView sensorIdView;
            final TextView arduinoView;
            final TextView currentTemperatureView;
            final TextView targetTemperaturaView;
            final TextView updateAtView;

            public ViewHolder(View view) {
                sensorIdView = (TextView) view.findViewById(R.id.sensorId);
                arduinoView = (TextView) view.findViewById(R.id.arduino);
                currentTemperatureView = (TextView) view.findViewById(R.id.currentTemperature);
                targetTemperaturaView = (TextView) view.findViewById(R.id.targetTemperature);
                updateAtView = (TextView) view.findViewById(R.id.updateAt);
            }
        }


        public CustomAdapter() {
            super(MainActivity.this, R.layout.row_sensor, mSensorList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            Sensor sensor = mSensorList.get(position);

            View view;
            ViewHolder viewHolder;

            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.row_sensor, parent, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.sensorIdView.setText(sensor.getId());
            viewHolder.arduinoView.setText(sensor.getArduino());
            viewHolder.currentTemperatureView.setText(getString(R.string.label_graus_celsius, sensor.getCurrentTemperature()));
            viewHolder.targetTemperaturaView.setText(getString(R.string.label_graus_celsius, sensor.getTargetTemperature()));
            viewHolder.updateAtView.setText(DateFormat.getDateTimeInstance().format(new Date(sensor.getUpdatedAt())));

            return view;
        }
    }
}
