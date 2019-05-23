package com.github.ompc.greys.core.command.enhancer;

import com.github.ompc.greys.core.advisor.listener.AdviceListener;
import com.github.ompc.greys.core.util.PointCut;

/**
 * 类增强
 *
 * @author vlinux
 */
public interface GetEnhancer {

    /**
     * 获取增强功能点
     *
     * @return
     */
    PointCut getPointCut();

    /**
     * 获取监听器
     *
     * @return 返回监听器
     */
    AdviceListener getAdviceListener();

}
