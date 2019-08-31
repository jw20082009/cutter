package com.wilbert.library.basic.kit;

import android.text.TextUtils;
import android.util.JsonReader;

import com.wilbert.library.basic.base.IResultListener;
import com.wilbert.library.basic.kit.entities.Timeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 15:56
 */
public class KitReader {
    private static final String TAG = "KitReader";
    private static final String INDEX_FILE = "data.json";

    public static void createTimeline(final String filePath, final IResultListener<Timeline> listener) {
        if (listener == null)
            return;
        if (TextUtils.isEmpty(filePath))
            listener.onResult(null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(filePath, INDEX_FILE);
                JsonReader jsonReader = null;
                try {
                    FileReader reader = new FileReader(file);
                    jsonReader = new JsonReader(reader);

                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String name = jsonReader.nextName();
                        if (TextUtils.equals(name, "timeline")) {
                            Timeline timeline = Timeline.parse(jsonReader);
                            listener.onResult(timeline);
                            return;
                        }
                    }
                    jsonReader.endObject();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (jsonReader != null)
                            jsonReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
