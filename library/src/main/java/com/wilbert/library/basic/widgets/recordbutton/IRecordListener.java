package com.wilbert.library.basic.widgets.recordbutton;

public interface IRecordListener {
    void onRecordPreStart();

    void onRecordStarted();

    void onRecordPreEnd();

    void onRecordEnded();

    void onProgressChanged(long progress);

    void onUpProgressChanged(float progress);
}
