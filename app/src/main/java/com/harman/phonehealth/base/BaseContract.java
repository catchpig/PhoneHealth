package com.harman.phonehealth.base;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public interface BaseContract {
    interface View {
        BaseActivity getBaseActivity();

        void closeActivity();
    }

    interface Presenter extends LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        void onCreate();

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        void onStart();

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        void onResume();

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        void onPause();

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        void onStop();

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        void onDestroy();
    }
}
