package com.geefox.geefoxptrlib;

/**
 * Created by GeeFox
 * 2018/4/20
 */

public interface IRefreshView {


    /**
     * 重置
     */
    void onReset();


    /**
     * 下拉高度大于头部高度
     */
    void onPrepare();


    /**
     * 放手后
     */
    void onRelease();

    /**
     * 刷新完成
     */
    void onComplete();

    /**
     * 下拉高度与头部高度比例
     */
    void onPositionChange(float currentPercent);

    /**
     * 设置视图为头部或底部
     *
     * @param isHeader
     */
    void setIsHeaderOrFooter(boolean isHeader);
}
