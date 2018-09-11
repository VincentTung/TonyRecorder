package com.vincent.tonyrecorder.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.vincent.tonyrecorder.R;
import com.vincent.tonyrecorder.utils.Constants;

import com.vincent.tonyrecorder.utils.ScreenRecorder;
import com.vincent.tonyrecorder.utils.Utils;

import static com.vincent.tonyrecorder.utils.Constants.REQ_CAPTURE_PICTURE;
import static com.vincent.tonyrecorder.utils.Constants.REQ_SHARE_PICTURE;
import static com.vincent.tonyrecorder.utils.Constants.REQ_SHARE_VIDEO;
import static com.vincent.tonyrecorder.utils.Constants.REQ_VIDEO_SCREEN;
import static com.vincent.tonyrecorder.utils.Constants.TYPE_CAPTURE_PICTURE;
import static com.vincent.tonyrecorder.utils.Constants.TYPE_SHARE_VIDEO;

/**
 *
 * @author vincent
 */
public class BlankActivity extends Activity {
    private static final String TAG = BlankActivity.class.getSimpleName();
    private MediaProjectionManager mMediaPM;
    private FrameLayout fl_preview;
    private ImageView img_preview;
    private String mFilePath = null;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tr_activity_blank);
        img_preview = findViewById(R.id.img_preview);
        fl_preview = findViewById(R.id.fl_preview);
        fl_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.sharePicture(BlankActivity.this, mFilePath);
            }
        });

        init(getIntent());
    }

    private void init(Intent intent) {
        type = intent.getIntExtra("type", TYPE_CAPTURE_PICTURE);
        if (type == TYPE_SHARE_VIDEO) {
            String videoPath = getIntent().getStringExtra("path");
            Utils.shareVideo(this, videoPath);
        } else {
            startRecord(this);
        }
    }

    private void startRecord(Context context) {
        mMediaPM = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(new Intent(mMediaPM.createScreenCaptureIntent()), type == TYPE_CAPTURE_PICTURE ? Constants.REQ_CAPTURE_PICTURE : Constants.REQ_VIDEO_SCREEN);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CAPTURE_PICTURE) {

            if (resultCode == RESULT_OK) {
                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                int mWidth = point.x;
                int mHeight = point.y;
                DisplayMetrics outMetric = new DisplayMetrics();
                display.getMetrics(outMetric);
                int mScreenDensity = (int) outMetric.density;
                ImageReader mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
                final MediaProjection mediaProjection = mMediaPM.getMediaProjection(resultCode, data);
                final VirtualDisplay mVirtualDisplay = mediaProjection.createVirtualDisplay("mediaprojection", mWidth, mHeight,
                        mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
                mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Bitmap mBitmap = Utils.getBitmapFromImageReader(reader);
                        mVirtualDisplay.release();
                        mFilePath = Utils.generatePictureFilePath(BlankActivity.this);
                        Utils.save(mBitmap, mFilePath, Bitmap.CompressFormat.JPEG);
                        fl_preview.setVisibility(View.VISIBLE);
                        img_preview.setImageBitmap(mBitmap);

                    }
                }, new Handler());

            } else {
                finish();
            }
        } else if (requestCode == REQ_SHARE_PICTURE || requestCode == REQ_SHARE_VIDEO) {
            finish();
        } else if (requestCode == REQ_VIDEO_SCREEN) {
            MediaProjection mediaProjection = mMediaPM.getMediaProjection(resultCode, data);
            ScreenRecorder.getInstance().setMediaProjection(mediaProjection).startVideo();
            finish();
        }
    }


}