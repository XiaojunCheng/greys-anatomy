package com.github.ompc.greys.core.command;

import com.github.ompc.greys.core.command.action.Action;

/**
 * 命令
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/18
 */
public interface Command {

    /**
     * 获取命令动作
     *
     * @return 返回命令所对应的命令动作
     */
    Action getAction();

}
