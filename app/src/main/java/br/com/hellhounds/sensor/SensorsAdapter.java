package br.com.hellhounds.sensor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import br.com.hellhounds.R;
import br.com.hellhounds.utils.RecyclerViewClickListener;

public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.SensorViewHolder> {

    private Context mContext;
    private List<Sensor> mSensors;
    private RecyclerViewClickListener mOnClickListener;

    public SensorsAdapter(Context context, List<Sensor> sensors) {
        this.mContext = context;
        this.mSensors = sensors;
    }

    public SensorsAdapter(Context context, List<Sensor> sensors, RecyclerViewClickListener onClickListener) {
        this.mContext = context;
        this.mSensors = sensors;
        this.mOnClickListener = onClickListener;
    }

    @Override
    public SensorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.sensor_list_row, parent, false);
        return new SensorViewHolder(rowView, mOnClickListener);
    }

    @Override
    public void onBindViewHolder(SensorViewHolder holder, int position) {
        Sensor sensor = mSensors.get(position);

        holder.sensorIdView.setText(sensor.getId());
        holder.arduinoView.setText(sensor.getArduino());
        holder.currentTemperatureView.setText(mContext.getString(R.string.label_graus_celsius, sensor.getCurrentTemperature()));
        holder.targetTemperaturaView.setText(mContext.getString(R.string.label_graus_celsius, sensor.getTargetTemperature()));
        holder.updateAtView.setText(DateFormat.getDateTimeInstance().format(new Date(sensor.getUpdatedAt())));

    }

    @Override
    public int getItemCount() {
        return mSensors.size();
    }

    public class SensorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView sensorIdView, arduinoView, currentTemperatureView, targetTemperaturaView, updateAtView;
        private RecyclerViewClickListener mOnClickListener;


        public SensorViewHolder(View view, RecyclerViewClickListener onClickListener) {
            super(view);
            sensorIdView = view.findViewById(R.id.sensorId);
            arduinoView = view.findViewById(R.id.arduino);
            currentTemperatureView = view.findViewById(R.id.currentTemperature);
            targetTemperaturaView = view.findViewById(R.id.targetTemperature);
            updateAtView = view.findViewById(R.id.updateAt);

            if (onClickListener != null) {
                mOnClickListener = onClickListener;
                view.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onClick(view, getAdapterPosition());
        }
    }
}
