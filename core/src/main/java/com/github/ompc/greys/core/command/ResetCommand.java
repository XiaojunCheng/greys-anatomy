package com.github.ompc.greys.core.command;

import com.github.ompc.greys.core.advisor.Enhancer;
import com.github.ompc.greys.core.command.annotation.Cmd;
import com.github.ompc.greys.core.util.affect.EnhancerAffect;
import com.github.ompc.greys.core.util.affect.RowAffect;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * 恢复所有增强类<br/>
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/29
 */
@Cmd(name = "reset", sort = 11, summary = "Reset all the enhanced classes",
        eg = {
                "reset",
                "reset *List",
                "reset -E .*List"
        })
public class ResetCommand implements Command {

    @Override
    public Action getAction() {

        return (AffectAction) (session, inst, printer) -> {

            final EnhancerAffect enhancerAffect = Enhancer.reset(inst);
            printer.print(EMPTY).finish();
            return new RowAffect(enhancerAffect.cCnt());
        };
    }

}
