package com.wilbert.library.basic.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class OutFileGenerator {
    private static final boolean INTERNAL_DIR = false;
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private static final String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    public static String generateReverseFile(Context context, String inputFile) {
        return new File((INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)), "reverse_" + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateSlowFile(Context context, String inputFile, int start, int end) {
        return new File((INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)), "slow_" + start + "_" + end + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateRepeatFile(Context context, String inputFile, int start, int end) {
        return new File((INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)), "repeat_" + start + "_" + end + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateSpeedFile(Context context, String inputFile, float speed) {
        return new File((INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)), "speed_" + (int) (speed * 100) + "_" + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateCutFile(Context context, String inputFile) {
        return new File((INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)), "cut_" + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateIFrameFile(Context context, String inputVideo) {
        return (INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)) + File.separator + "keyframe_" + new File(inputVideo).getName();
    }

    public static String generateMergeFile(Context context, List<String> inputFiles) {
        StringBuilder sbResult = new StringBuilder("merge");
        for (String file : inputFiles) {
            String filename = new File(file).getName();
            if (filename.endsWith(".mp4")) {
                filename = filename.substring(0, filename.length() - 4);
            }
            sbResult.append("_");
            if (filename.length() > 32) {
                sbResult.append(filename.substring(0, 32));
            } else {
                sbResult.append(filename);
            }
        }
        sbResult.append(".mp4");
        return new File(INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), sbResult.toString()).getAbsolutePath();
    }

    public static String generateMusicFile(Context context, String inputFile) {
        return new File(INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "music_" + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateAeFile(Context context, String inputFile) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "ae_" + new File(inputFile).getName()).getAbsolutePath();
    }

    public static String generateRecodeFile(Context context) {
        return new File(INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "record_" + getDateTimeString() + ".mp4").getAbsolutePath();
    }

    public static String generateCompressFile(Context context) {
        return new File(INTERNAL_DIR ? context.getCacheDir() : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "compress_" + getDateTimeString() + ".mp4").getAbsolutePath();
    }
}
