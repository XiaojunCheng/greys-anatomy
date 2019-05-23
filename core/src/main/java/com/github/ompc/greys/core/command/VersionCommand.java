package com.github.ompc.greys.core.command;


import com.github.ompc.greys.core.command.action.Action;
import com.github.ompc.greys.core.command.action.RowAction;
import com.github.ompc.greys.core.command.annotation.Cmd;
import com.github.ompc.greys.core.util.affect.RowAffect;

import static com.github.ompc.greys.core.util.GaStringUtils.getLogo;

/**
 * 输出版本
 *
 * @author oldmanpushcart@gmail.com
 */
@Cmd(name = "version", sort = 9, summary = "Display Greys version",
        eg = {
                "version"
        })
public class VersionCommand implements Command {

    @Override
    public Action getAction() {
        return (RowAction) (session, inst, printer) -> {
            printer.print(getLogo()).finish();
            return new RowAffect(1);
        };
    }

}
