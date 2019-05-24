package com.github.ompc.greys.core.advisor.asm;

import com.github.ompc.greys.core.advisor.method.GaMethod;
import com.github.ompc.greys.core.util.matcher.Matcher;
import org.apache.commons.lang3.StringUtils;

/**
 * Method匹配器,用于封装Asm方法的匹配
 *
 * @author vlinux
 */
public class AsmMethodMatcher implements Matcher<AsmMethod> {

    private final GaMethod gaMethod;

    public AsmMethodMatcher(GaMethod gaMethod) {
        this.gaMethod = gaMethod;
    }

    @Override
    public boolean matching(AsmMethod target) {
        return StringUtils.equals(gaMethod.getName(), target.getName())
                && StringUtils.equals(gaMethod.getDesc(), target.getDesc());
    }

}
