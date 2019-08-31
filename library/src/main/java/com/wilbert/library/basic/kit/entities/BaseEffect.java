package com.wilbert.library.basic.kit.entities;

import android.util.JsonReader;

import java.io.IOException;

/**
 * Created by Android Studio.
 * User: wilbert jw20082009@qq.com
 * Date: 2019/8/21 15:27
 */
public class BaseEffect {

    public static final int FILTER = 0;
    public static final int STICKER = 1;
    public static final int ACTION = 2;
    public static final int TRANSITION = 3;
    public static final int SUBTITLE = 4;

    /**
     * 0:filter,1:sticker,2:action,3:transition,4:subtitle
     */
    protected int type;

    public int getType() {
        return type;
    }

    public static BaseEffect parse(JsonReader jsonReader) throws IOException {
        BaseEffect effect = null;
        if(jsonReader != null){
            jsonReader.beginObject();
            while(jsonReader.hasNext()){
                String name = jsonReader.nextName();
                if(name.equals("type")){
                    int type = jsonReader.nextInt();
                    switch (type){
//                        case
                    }
                }
            }
            jsonReader.endObject();
        }
        return effect;
    }
}
