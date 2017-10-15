package br.com.hellhounds;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;

public class HellHoundsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        LeakCanary.install(this);

        Logger.addLogAdapter(new AndroidLogAdapter());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
