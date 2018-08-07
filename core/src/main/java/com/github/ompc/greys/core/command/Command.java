package com.github.ompc.greys.core.command;

import com.github.ompc.greys.core.advisor.AdviceListener;
import com.github.ompc.greys.core.server.Session;
import com.github.ompc.greys.core.util.PointCut;
import com.github.ompc.greys.core.util.affect.RowAffect;

import java.lang.instrument.Instrumentation;

/**
 * 命令
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/18
 */
public interface Command {

    /**
     * 信息发送者
     */
    interface Printer {

        /**
         * 发送信息
         *
         * @param isFinished 是否结束打印
         * @param message    发送信息内容
         * @return
         */
        Printer print(boolean isFinished, String message);

        /**
         * 发送信息
         *
         * @param message 发送信息内容
         * @return
         */
        Printer print(String message);

        /**
         * 换行发送信息
         *
         * @param isFinished 是否结束打印
         * @param message    发送信息内容
         * @return
         */
        Printer println(boolean isFinished, String message);

        /**
         * 换行发送信息
         *
         * @param message 发送信息内容
         * @return
         */
        Printer println(String message);

        /**
         * 结束打印
         */
        void finish();
    }

    /**
     * 类增强
     */
    interface ClassEnhancer {

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

    /**
     * 命令动作
     */
    interface Action {
    }

    /**
     * 类增强动作
     */
    interface ClassEnhancerAction extends Action {

        /**
         * 执行动作
         *
         * @param session 会话
         * @param inst    inst
         * @param printer 信息发送者
         * @return 类增强
         * @throws Throwable 动作执行出错
         */
        ClassEnhancer action(Session session, Instrumentation inst, Printer printer) throws Throwable;

    }

    /**
     * 安静命令动作
     */
    interface SilentAction extends Action {

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

    /**
     * 影响动作
     */
    interface AffectAction extends Action {

        /**
         * 影响的执行动作
         *
         * @param session 会话
         * @param inst    inst
         * @param printer 信息发送者
         * @return 影响范围
         * @throws Throwable 动作执行出错
         */
        RowAffect action(Session session, Instrumentation inst, Printer printer) throws Throwable;

    }

    /**
     * 获取命令动作
     *
     * @return 返回命令所对应的命令动作
     */
    Action getAction();

}
