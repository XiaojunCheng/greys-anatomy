package com.github.ompc.greys.core.util.affect;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 行记录影响反馈
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/21
 */
public class RowAffect extends Affect {

    private final AtomicInteger rowCount = new AtomicInteger();

    public RowAffect() {
    }

    public RowAffect(int rowCount) {
        this.rowCount(rowCount);
    }

    /**
     * 影响行数统计
     *
     * @param addRowCount 新增行影响计数
     * @return 当前影响行个数
     */
    public int rowCount(int addRowCount) {
        return rowCount.addAndGet(addRowCount);
    }

    /**
     * 获取影响行个数
     *
     * @return 影响行个数
     */
    public int rowCount() {
        return rowCount.get();
    }

    @Override
    public String toString() {
        return String.format("Affect(row-cnt:%d) cost in %s ms.",
                rowCount(),
                cost());
    }
}
