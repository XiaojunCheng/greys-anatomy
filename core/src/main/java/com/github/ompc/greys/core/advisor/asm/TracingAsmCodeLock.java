package com.github.ompc.greys.core.advisor.asm;

import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 用于Tracing的代码锁
 *
 * @author vlinux
 */
public class TracingAsmCodeLock extends AsmCodeLock {

    public TracingAsmCodeLock(AdviceAdapter aa) {
        super(
                aa,
                new int[]{ICONST_0, POP},
                new int[]{ICONST_1, POP}
        );
    }
}
