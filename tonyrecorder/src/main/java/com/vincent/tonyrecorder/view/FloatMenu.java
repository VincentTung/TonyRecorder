package com.vincent.tonyrecorder.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.tonyrecorder.R;
import com.vincent.tonyrecorder.activity.BlankActivity;
import com.vincent.tonyrecorder.utils.ScreenRecorder;
import com.vincent.tonyrecorder.utils.Utils;

import static com.vincent.tonyrecorder.utils.Constants.TYPE_CAPTURE_PICTURE;
import static com.vincent.tonyrecorder.utils.Constants.TYPE_SHARE_VIDEO;
import static com.vincent.tonyrecorder.utils.Constants.TYPE_VIDEO_SCREEN;


/**
 * @author vincent
 */
public class FloatMenu extends LinearLayout implements View.OnClickListener {


    private static volatile FloatMenu mInstance;
    private boolean mIsStartVideo = false;
    private float lastY;
    private OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();
                    params.y += event.getRawY() - lastY;
                    params.y = Math.max(0, params.y);
                    getWindowManager().updateViewLayout(FloatMenu.this, params);
                    lastY = event.getRawY();
                    break;
                default:
                    break;
            }
            return true;
        }
    };
    private TextView tv_float_close;
    private TextView tv_float_photo;
    private TextView tv_float_video;
    private ImageView img_float_drag;
    private LinearLayout ll_float_menu_parent;
    private View contentView;

    public boolean isVisible() {
        return mIsVisible;
    }

    private boolean mIsVisible = false;

    private FloatMenu(Context context) {
        super(context);
        init(context);
    }

    public static FloatMenu getInstance() {
        if (mInstance == null) {
            synchronized (FloatMenu.class) {
                if (mInstance == null) {
                    mInstance = new FloatMenu(Utils.getContext());
                }
            }
        }
        return mInstance;
    }

    public static void open() {
        FloatMenu floatMenu = getInstance();
        close();
        try {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSLUCENT;
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 0;
            floatMenu.getWindowManager().addView(floatMenu, params);
            floatMenu.mIsVisible = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void close() {
        try {
            FloatMenu floatMenu = getInstance();
            floatMenu.getWindowManager().removeViewImmediate(floatMenu);
            floatMenu.mIsVisible = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        contentView = LayoutInflater.from(context).inflate(R.layout.tr_float_menu, null);
        tv_float_close = contentView.findViewById(R.id.tv_float_close);
        tv_float_photo = contentView.findViewById(R.id.tv_float_picture);
        tv_float_video = contentView.findViewById(R.id.tv_float_video);
        img_float_drag = contentView.findViewById(R.id.img_float_drag);
        ll_float_menu_parent = contentView.findViewById(R.id.ll_float_menu_parent);
        tv_float_close.setOnClickListener(this);
        tv_float_photo.setOnClickListener(this);
        tv_float_video.setOnClickListener(this);
        img_float_drag.setOnTouchListener(mTouchListener);
        addView(contentView);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_float_close) {
            close();

        } else if (id == R.id.tv_float_picture) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                Intent intent = new Intent(getContext(), BlankActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("type", TYPE_CAPTURE_PICTURE);
                getContext().startActivity(intent);
            } else {
                toastNotSupportVersion();
            }

        } else if (id == R.id.tv_float_video) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                if (!mIsStartVideo) {
                    mIsStartVideo = true;
                    tv_float_video.setText("停止");
                    Intent intent = new Intent(getContext(), BlankActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", TYPE_VIDEO_SCREEN);
                    getContext().startActivity(intent);
                } else {
                    mIsStartVideo = false;
                    tv_float_video.setText("录屏");
                    String videoPath = ScreenRecorder.getInstance().stopVideo();
                    Intent intent = new Intent(getContext(), BlankActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", TYPE_SHARE_VIDEO);
                    intent.putExtra("path", videoPath);
                    getContext().startActivity(intent);
                }
            } else {
                toastNotSupportVersion();
            }
        }
    }

    private WindowManager getWindowManager() {
        return ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE));
    }

    private void toastNotSupportVersion() {
        Toast.makeText(getContext(), "只支持5.0+版本", Toast.LENGTH_SHORT).show();
    }
}
