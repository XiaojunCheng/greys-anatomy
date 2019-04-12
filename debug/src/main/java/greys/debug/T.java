package greys.debug;

import java.util.Date;

/**
 * @author Xiaojun.Cheng
 * @date 2018/9/1
 */
public class T {

    public static void main(String[] args) {
        System.out.println("start at " + new Date());
        execute(() -> {
            StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
            for (int i = traceElements.length - 1; i >= 0; i--) {
                System.out.println(traceElements[i]);
            }
        });
        System.out.println("end at " + new Date());
    }

    private static void execute(Runnable r) {
        r.run();
    }

}
