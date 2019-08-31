package com.wilbert.library.basic.entity;

import android.graphics.Color;

/**
 * 后期动作
 */
public enum Actions {

    LingHunChuQiao("灵魂出窍", Color.parseColor("#CCABABAB")), FenLie("视频分裂", Color.parseColor("#CCAB82FF")), GuangBo("动感光波", Color.parseColor("#CCD1EEEE")),
    HuanJing("暗黑幻境", Color.parseColor("#CC8B6508")), BaiYeChuang("百叶窗", Color.parseColor("#CC8EE5EE")), GuiYing("鬼影", Color.parseColor("#CC949494")),
    HuanYing("幻影", Color.parseColor("#CCB7B7B7")), YouLing("幽灵", Color.parseColor("#CCBDB76B")), ShanDian("闪电", Color.parseColor("#CCDBDBDB")),
    JingXiang("镜像", Color.parseColor("#CCEE6363")), HuanJue("幻觉", Color.parseColor("#CCEEC900"));

    public String name;
    public int color;

    Actions(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public static Actions parseActions(int ordinal) {
        Actions[] actions = Actions.values();
        for (Actions action : actions) {
            if (action.ordinal() == ordinal) {
                return action;
            }
        }
        return null;
    }
}
