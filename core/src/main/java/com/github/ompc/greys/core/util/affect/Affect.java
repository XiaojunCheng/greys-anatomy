package com.github.ompc.greys.core.util.affect;

/**
 * 影响反馈
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/21
 */
public class Affect {

    private final long start = System.currentTimeMillis();

    /**
     * 影响耗时(ms)
     *
     * @return 获取耗时(ms)
     */
    public long cost() {
        return System.currentTimeMillis() - start;
    }

    @Override
    public String toString() {
        return String.format("Affect cost in %s ms.", cost());
    }

}
