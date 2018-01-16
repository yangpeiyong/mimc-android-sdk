package com.xiaomi.mimcdemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.xiaomi.mimcdemo.common.SystemUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.push.mimc.LoggerInterface;
import com.xiaomi.push.mimc.MimcClient;
import com.xiaomi.push.mimc.MimcException;
import com.xiaomi.push.mimc.MimcLogger;
import com.xiaomi.push.mimc.User;

public class DemoApplication extends Application {
    public static final String TAG = "com.xiaomi.MimcDemo";
    private int mCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();


        LoggerInterface newLogger = new LoggerInterface() {
            @Override
            public void setTag(String s) {

            }

            @Override
            public void log(String s) {
                Log.d(TAG, s);
            }

            @Override
            public void log(String s, Throwable throwable) {
                Log.d(TAG, s, throwable);
            }
        };
        MimcLogger.setLogger(getApplicationContext(), newLogger);
        MimcLogger.setLogLevel(MimcLogger.INFO);

        SystemUtils.initialize(this);
        MimcClient.initialize(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCount++;
                // Switch to the foreground
                if (mCount == 1) {
                    User user = UserManager.getInstance().getUser();
                    if (user != null) try {
                        user.login();
                    } catch (MimcException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                // Switch to the background
                if (mCount == 0) {
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}
