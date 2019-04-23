package com.github.ompc.greys.test;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * @author chengxiaojun
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        String agentJar = System.getProperty("user.dir") + '/' + "greys-agent.jar";
        String coreJar = System.getProperty("user.dir") + '/' + "greys-core.jar";
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentJar, coreJar + ";targetIp=127.0.0.1;targetPort=2345;connectTimeout=60000000");
        vm.detach();
        SpringApplication.run(Application.class, args);
    }
}
