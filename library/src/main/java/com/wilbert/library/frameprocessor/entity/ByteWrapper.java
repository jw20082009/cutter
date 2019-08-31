package com.wilbert.library.frameprocessor.entity;

public class ByteWrapper {

    private byte[] data;

    public ByteWrapper(byte[] data){
        this.data = data;
    }

    public byte[] getData(){
        return data;
    }
}
