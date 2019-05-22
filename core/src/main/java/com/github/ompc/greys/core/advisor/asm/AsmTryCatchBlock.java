package com.github.ompc.greys.core.advisor.asm;

import lombok.Getter;
import org.objectweb.asm.Label;

/**
 * TryCatch块,用于ExceptionsTable重排序
 *
 * @author vlinux
 */
@Getter
public class AsmTryCatchBlock {

    private final Label start;
    private final Label end;
    private final Label handler;
    private final String type;

    public AsmTryCatchBlock(Label start, Label end, Label handler, String type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

}
