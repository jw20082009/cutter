package com.wilbert.library.basic.kit.entities;

import android.text.TextUtils;
import android.util.JsonReader;

import java.io.IOException;
import java.util.List;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/19 10:45
 */
public class Timeline {

    private String thumb;
    private List<BaseEffect> effects;
    private VideoTrack videotrack;
    private AudioTrack audioTrack;

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public List<BaseEffect> getEffects() {
        return effects;
    }

    public void setEffects(List<BaseEffect> effects) {
        this.effects = effects;
    }

    public VideoTrack getVideotrack() {
        return videotrack;
    }

    public void setVideotrack(VideoTrack videotrack) {
        this.videotrack = videotrack;
    }

    public AudioTrack getAudioTrack() {
        return audioTrack;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public static Timeline parse(JsonReader jsonReader) throws IOException {
        Timeline timeline = null;
        if (jsonReader != null) {
            timeline = new Timeline();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if (name.equals("thumb")) {
                    timeline.setThumb(jsonReader.nextString());
                } else if (name.equals("effects")) {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        String typename = jsonReader.nextName();
                        if (TextUtils.equals("type", typename)) {
                            int type = jsonReader.nextInt();
                            switch (type) {
                                case BaseEffect.FILTER:
                                    break;
                                case BaseEffect.STICKER:
                                    break;
                                case BaseEffect.ACTION:
                                    break;
                                case BaseEffect.TRANSITION:
                                    break;
                                case BaseEffect.SUBTITLE:
                                    break;
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                } else if (name.equals("videotrack")) {

                } else if (name.equals("audiotrack")) {

                }
            }
            jsonReader.endObject();
        }
        return timeline;
    }
}
