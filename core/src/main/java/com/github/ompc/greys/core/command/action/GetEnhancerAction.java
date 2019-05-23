package com.github.ompc.greys.core.command.action;

import com.github.ompc.greys.core.command.enhancer.GetEnhancer;
import com.github.ompc.greys.core.command.printer.Printer;
import com.github.ompc.greys.core.server.Session;

import java.lang.instrument.Instrumentation;

/**
 * 类增强动作
 *
 * @author vlinux
 */
public interface GetEnhancerAction extends Action {

    /**
     * 执行动作
     *
     * @param session 会话
     * @param inst    inst
     * @param printer 信息发送者
     * @return 类增强
     * @throws Throwable 动作执行出错
     */
    GetEnhancer action(Session session, Instrumentation inst, Printer printer) throws Throwable;

}
