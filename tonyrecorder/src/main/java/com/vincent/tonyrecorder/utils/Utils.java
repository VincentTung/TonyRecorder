package com.vincent.tonyrecorder.utils;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import static com.vincent.tonyrecorder.utils.Constants.REQ_SHARE_PICTURE;
import static com.vincent.tonyrecorder.utils.Constants.REQ_SHARE_VIDEO;


/**
 * @author vincent
 */
public class Utils {

    private static Context instence = null;

    public static void init(Context context) {
        instence = context;
    }

    public static Context getContext() {
        return instence;
    }

    /**
     * @param context
     * @return
     */
    public static String getSaveRootPath(Context context) {
        String directoryPath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            directoryPath = context.getApplicationContext().getExternalCacheDir().getPath();
        } else {
            directoryPath = context.getApplicationContext().getCacheDir().getPath();
        }
        return directoryPath;
    }

    /**
     * 截屏文件夹路径
     *
     * @param context
     * @return
     */
    public static String getPictureSavePath(Context context) {
        String directoryPath = getSaveRootPath(context) + File.separator + Constants.PICTURE_SAVE_FOLDER;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directoryPath;
    }

    /**
     * 录像文件夹路径
     *
     * @return
     * @par
     */
    public static String getVideoSavePath() {
        String directoryPath = getSaveRootPath(instence) + File.separator + Constants.VIDEO_SAVE_FOLDER;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directoryPath;
    }

    public static String generatePictureFilePath(Context context) {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        return getPictureSavePath(context) + File.separator + df.format(System.currentTimeMillis()) + Constants.PICTURE_POSTFIX;
    }


    public static void sharePicture(Activity context, String path) {
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);
        share_intent.setType("image/*");
        share_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        share_intent = Intent.createChooser(share_intent, "分享");
        context.startActivityForResult(share_intent, REQ_SHARE_PICTURE);
    }

    public static void shareVideo(Activity context, String path) {
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);
        share_intent.setType("video/*");
        share_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
        share_intent = Intent.createChooser(share_intent, "分享");
        context.startActivityForResult(share_intent, REQ_SHARE_VIDEO);
    }

    public static Bitmap getBitmapFromImageReader(ImageReader reader) {
        Bitmap bitmap;
        Image image = reader.acquireLatestImage();
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        reader.close();
        return bitmap;

    }

    public static boolean save(final Bitmap src,
                               final String filePath,
                               final Bitmap.CompressFormat format) {
        return save(src, getFileByPath(filePath), format, false);
    }

    public static boolean save(final Bitmap src,
                               final File file,
                               final Bitmap.CompressFormat format,
                               final boolean recycle) {
        if (isEmptyBitmap(src) || !createFileByDeleteOldFile(file)) return false;
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled()) src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    private static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        if (file.exists() && !file.delete()) return false;
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private static long lastCheckTime;
    private static float[] lastXyz = new float[3];

    public static boolean checkIfShake(float x, float y, float z) {
        long currentTime = System.currentTimeMillis();
        long diffTime = currentTime - lastCheckTime;
        if (diffTime < 100) {
            return false;
        }
        lastCheckTime = currentTime;
        float deltaX = x - lastXyz[0];
        float deltaY = y - lastXyz[1];
        float deltaZ = z - lastXyz[2];
        lastXyz[0] = x;
        lastXyz[1] = y;
        lastXyz[2] = z;
        int delta = (int) (Math.sqrt(deltaX * deltaX
                + deltaY * deltaY + deltaZ * deltaZ) / diffTime * 10000);
        if (delta > 600) {
            return true;
        }
        return false;
    }

    public static void registerSensor(SensorEventListener listener) {
        try {
            SensorManager manager = (SensorManager) instence.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static boolean checkFloatPermission() {
        Context context = instence;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    private static final String MANUFACTURER = Build.MANUFACTURER.toLowerCase();
    private static final String MANUF_MEIZU = "meizu";
    private static final String MANUF_XIAOMI = "xiaomi";
    private static final String MANUF_VIVO = "vivo";
    private static final String MANUF_HUAWEI = "huawei";


    private static Intent normalSettingIntent() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", instence.getPackageName(), null));
        return intent;
    }

    public static void openSetting() {

        Intent intent = null;
        if (MANUFACTURER.contains(MANUF_MEIZU)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                intent = normalSettingIntent();
            } else {
                intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                intent.putExtra("packageName", instence.getPackageName());
                intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
            }

        } else if (MANUFACTURER.contains(MANUF_XIAOMI)) {
            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.putExtra("extra_pkgname", instence.getPackageName());
        } else if (MANUFACTURER.contains(MANUF_VIVO)) {
            intent.putExtra("packagename", instence.getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"));
            } else {
                intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
            }
        } else if (MANUFACTURER.contains(MANUF_HUAWEI)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent = normalSettingIntent();
            } else {
                intent = new Intent();
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
            }
        }

        try {
            instence.startActivity(intent);
        } catch (Exception e) {
            instence.startActivity(normalSettingIntent());
        }
    }

}