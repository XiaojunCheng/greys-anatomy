package com.github.ompc.greys.core.advisor.asm;

import com.github.ompc.greys.core.advisor.lock.Block;
import com.github.ompc.greys.core.advisor.lock.CodeLock;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ASM代码锁<br/>
 *
 * @author oldmanpushcart@gmail.com
 * @date 15/5/28
 */
public class AsmCodeLock implements CodeLock, Opcodes {

    private final AdviceAdapter aa;

    /**
     * 锁标记
     */
    private boolean isLook;
    /**
     * 代码块开始特征数组
     */
    private final int[] beginCodeArray;
    /**
     * 代码块结束特征数组
     */
    private final int[] endCodeArray;
    /**
     * 代码匹配索引
     */
    private int index = 0;

    /**
     * 用ASM构建代码锁
     *
     * @param aa             ASM
     * @param beginCodeArray 代码块开始特征数组
     *                       字节码流要求不能破坏执行堆栈
     * @param endCodeArray   代码块结束特征数组
     *                       字节码流要求不能破坏执行堆栈
     */
    public AsmCodeLock(AdviceAdapter aa, int[] beginCodeArray, int[] endCodeArray) {
        if (null == beginCodeArray || null == endCodeArray
                || beginCodeArray.length != endCodeArray.length) {
            throw new IllegalArgumentException();
        }

        this.aa = aa;
        this.beginCodeArray = beginCodeArray;
        this.endCodeArray = endCodeArray;
    }

    /**
     * 明白了，如果连续的指令集不能和 begin或end 指令集匹配，则认为是无效的指令<br/>
     * 目的就是为了匹配特定的指令集和
     *
     * @param code
     */
    @Override
    public void code(int code) {

        final int[] codes = isLock() ? endCodeArray : beginCodeArray;
        if (index >= codes.length) {
            reset();
            return;
        }

        if (codes[index] != code) {
            reset();
            return;
        }

        if (++index == codes.length) {
            //翻转锁状态
            isLook = !isLook;
            reset();
        }

    }

    /**
     * 重置索引<br/>
     * 一般在代码序列判断失败时，则会对索引进行重置，冲头开始匹配特征序列
     */
    private void reset() {
        index = 0;
    }

    private void asm(int opcode) {
        aa.visitInsn(opcode);
    }

    /**
     * 锁定序列
     */
    private void lock() {
        for (int op : beginCodeArray) {
            asm(op);
        }
    }

    /**
     * 解锁序列
     */
    private void unLock() {
        for (int op : endCodeArray) {
            asm(op);
        }
    }

    @Override
    public boolean isLock() {
        return isLook;
    }

    @Override
    public void lock(Block block) {
        lock();
        try {
            block.code();
        } finally {
            unLock();
        }
    }

}
