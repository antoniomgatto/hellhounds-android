package br.com.hellhounds;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

public class HellHoundsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
