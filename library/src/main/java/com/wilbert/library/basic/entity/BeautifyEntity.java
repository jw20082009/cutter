
package com.wilbert.library.basic.entity;

public class BeautifyEntity {
    private StickerEffect stickerEffect = null;

    private int strength;

    public BeautifyEntity(StickerEffect stickerEffect){
        this.stickerEffect = stickerEffect;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public StickerEffect getStickerEffect() {
        return stickerEffect;
    }

    public void setStickerEffect(StickerEffect stickerEffect) {
        this.stickerEffect = stickerEffect;
    }
}
