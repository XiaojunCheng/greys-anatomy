package com.github.ompc.greys.debug;

import com.github.ompc.greys.agent.AgentLauncher;

/**
 * @author Xiaojun.Cheng
 * @date 2018/8/28
 */
public class CodeSourceTest {

    public static void main(String[] args) {
        System.out.println(AgentLauncher.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    }

}
