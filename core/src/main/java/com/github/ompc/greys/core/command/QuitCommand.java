package com.github.ompc.greys.core.command;

import com.github.ompc.greys.core.command.action.Action;
import com.github.ompc.greys.core.command.action.SilentAction;
import com.github.ompc.greys.core.command.annotation.Cmd;
import com.github.ompc.greys.core.server.Session;

import java.lang.instrument.Instrumentation;

/**
 * 退出命令
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/18
 */
@Cmd(name = "quit", sort = 8, summary = "Quit Greys console",
        eg = {
                "quit"
        })
public class QuitCommand implements Command {

    @Override
    public Action getAction() {
        return (SilentAction) (session, inst, printer) -> printer.println("Bye!").finish();
    }
}
