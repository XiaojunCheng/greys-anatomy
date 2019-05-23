package com.github.ompc.greys.core.command.action;

import com.github.ompc.greys.core.command.printer.Printer;
import com.github.ompc.greys.core.server.Session;
import com.github.ompc.greys.core.util.affect.RowAffect;

import java.lang.instrument.Instrumentation;

/**
 * 影响动作
 *
 * @author vlinux
 */
public interface RowAction extends Action {

    /**
     * 安静的执行动作
     *
     * @param session 会话
     * @param inst    inst
     * @param printer 信息发送者
     * @return 影响范围
     * @throws Throwable 动作执行出错
     */
    RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable;

}
