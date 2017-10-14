package br.com.hellhounds;

import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.hellhounds.sensor.Sensor;
import br.com.hellhounds.sensor.SensorViewActivity;
import br.com.hellhounds.sensor.SensorsAdapter;
import br.com.hellhounds.utils.RecyclerViewClickListener;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private RecyclerView mSensorListView;
    private SensorsAdapter mAdapter;
    private List<Sensor> mSensorList = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSensorListView = findViewById(R.id.sensors_list);

        mAdapter = new SensorsAdapter(this, mSensorList, this);
        mSensorListView.setLayoutManager(new LinearLayoutManager(this));
        mSensorListView.setItemAnimator(new DefaultItemAnimator());
        mSensorListView.setAdapter(mAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {

        super.onResume();

        getData();
    }

    void getData() {
        mDatabase.child("sensors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Sensor> tmpSensorList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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

    @Override
    public void onClick(View view, int position) {
        Sensor sensor = mSensorList.get(position);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(SensorViewActivity.newIntent(MainActivity.this, sensor.getFirebaseId()), options.toBundle());
    }
}
