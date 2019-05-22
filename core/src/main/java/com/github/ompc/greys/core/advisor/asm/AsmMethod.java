package com.github.ompc.greys.core.advisor.asm;

import lombok.Getter;

/**
 * Method在Asm内部的封装,用于封装Asm方法
 *
 * @author vlinux
 */
@Getter
public class AsmMethod {

    private final String name;
    private final String desc;

    public AsmMethod(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

}
