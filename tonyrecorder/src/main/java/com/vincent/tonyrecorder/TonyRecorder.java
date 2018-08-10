package com.vincent.tonyrecorder;

import android.app.Application;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.Toast;

import com.vincent.tonyrecorder.utils.Utils;
import com.vincent.tonyrecorder.view.FloatMenu;


/**
 * @author vincent
 */
public class TonyRecorder {

    private static TonyRecorder INSTANCE;


    static void init(Application application) {
        INSTANCE = new TonyRecorder();
        Utils.init(application);
        Utils.registerSensor(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (!FloatMenu.getInstance().isVisible()) {
                    if (event.sensor.getType() == 1) {
                        if (Utils.checkIfShake(
                                event.values[0],
                                event.values[1],
                                event.values[2])) {
                            if (Utils.checkFloatPermission()) {
                                FloatMenu.open();
                            } else {
                                Toast.makeText(Utils.getContext(), "请开启显示浮窗权限", Toast.LENGTH_SHORT).show();
                                Utils.openSetting();
                            }
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        });
    }

    public static TonyRecorder get() {
        return INSTANCE;
    }

}
