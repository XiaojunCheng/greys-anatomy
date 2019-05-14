package com.github.ompc.greys.core.util.collection;

/**
 * 堆栈
 *
 * @param <E>
 * @author oldmanpushcart@gmail.com
 * @date 15/6/21
 */
public interface GaStack<E> {

    /**
     * 出栈
     *
     * @return
     */
    E pop();

    /**
     * 往栈顶放元素
     *
     * @param e
     */
    void push(E e);

    /**
     * 获取栈顶元素
     *
     * @return
     */
    E peek();

    /**
     * 是否为空
     *
     * @return
     */
    boolean isEmpty();

}
