package com.vincent.tonyrecorder;

import android.app.Application;
import android.support.v4.content.FileProvider;


/**
 * @author vincent
 */
public class RDFileProvider extends FileProvider {
    @Override
    public boolean onCreate() {
        if (getContext() instanceof Application) {
            TonyRecorder.init((Application) getContext());
        }
        return super.onCreate();
    }
}
