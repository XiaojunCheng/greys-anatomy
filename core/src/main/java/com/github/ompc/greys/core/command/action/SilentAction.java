package com.github.ompc.greys.core.command.action;

import com.github.ompc.greys.core.command.printer.Printer;
import com.github.ompc.greys.core.server.Session;

import java.lang.instrument.Instrumentation;

/**
 * 安静命令动作
 *
 * @author vlinux
 */
public interface SilentAction extends Action {

    /**
     * 安静的执行动作
     *
     * @param session 会话
     * @param inst    inst
     * @param printer 信息发送者
     * @throws Throwable 动作执行出错
     */
    void action(Session session, Instrumentation inst, Printer printer) throws Throwable;

}
