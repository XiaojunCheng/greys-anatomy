package com.github.ompc.greys.core.command.printer;

/**
 * 信息发送者
 *
 * @author vlinux
 */
public interface Printer {

    /**
     * 发送信息
     *
     * @param isF     是否结束打印
     * @param message 发送信息内容
     * @return
     */
    Printer print(boolean isF, String message);

    /**
     * 发送信息
     *
     * @param message 发送信息内容
     * @return
     */
    Printer print(String message);

    /**
     * 换行发送信息
     *
     * @param isF     是否结束打印
     * @param message 发送信息内容
     * @return
     */
    Printer println(boolean isF, String message);

    /**
     * 换行发送信息
     *
     * @param message 发送信息内容
     * @return
     */
    Printer println(String message);

    /**
     * 结束打印
     */
    void finish();

}
