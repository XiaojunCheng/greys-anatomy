package com.github.ompc.greys.core;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.List;
import java.util.Optional;

import static com.github.ompc.greys.core.util.GaStringUtils.getCauseMessage;

/**
 * Greys启动器
 *
 * @author oldmanpushcart@gmail.com
 */
public class GreysLauncher {

    private final Configure configure;

    public GreysLauncher(String[] args) {
        //解析配置文件
        configure = analyzeConfigure(args);
    }

    public void launch() throws Exception {
        //加载agent
        attachAgent(configure);
    }

    /**
     * 解析Configure
     */
    private static Configure analyzeConfigure(String[] args) {
        final OptionParser parser = new OptionParser();
        parser.accepts("pid").withRequiredArg().ofType(int.class).required();
        parser.accepts("target").withOptionalArg().ofType(String.class);
        parser.accepts("multi").withOptionalArg().ofType(int.class);
        parser.accepts("core").withOptionalArg().ofType(String.class);
        parser.accepts("agent").withOptionalArg().ofType(String.class);

        final OptionSet os = parser.parse(args);
        final Configure configure = new Configure();

        if (os.has("target")) {
            final String[] strSplit = ((String) os.valueOf("target")).split(":");
            configure.setTargetIp(strSplit[0]);
            configure.setTargetPort(Integer.valueOf(strSplit[1]));
        }

        configure.setJavaPid((Integer) os.valueOf("pid"));
        configure.setGreysAgent((String) os.valueOf("agent"));
        configure.setGreysCore((String) os.valueOf("core"));
        return configure;
    }

    /**
     * 加载Agent
     */
    private void attachAgent(Configure configure) throws Exception {

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Class<?> vmdClass = loader.loadClass("com.sun.tools.attach.VirtualMachineDescriptor");
        final Class<?> vmClass = loader.loadClass("com.sun.tools.attach.VirtualMachine");

        Object attachVmdObj = null;
        for (Object obj : (List<?>) vmClass.getMethod("list", (Class<?>[]) null).invoke(null, (Object[]) null)) {
            if ((vmdClass.getMethod("id", (Class<?>[]) null).invoke(obj, (Object[]) null))
                    .equals(Integer.toString(configure.getJavaPid()))) {
                attachVmdObj = obj;
            }
        }

        if (null == attachVmdObj) {
            throw new IllegalArgumentException("pid:" + configure.getJavaPid() + " not existed.");
        }

        Object vmObj = null;
        try {
            // 使用 attach(String pid) 这种方式
            if (null == attachVmdObj) {
                vmObj = vmClass.getMethod("attach", String.class).invoke(null, "" + configure.getJavaPid());
            } else {
                vmObj = vmClass.getMethod("attach", vmdClass).invoke(null, attachVmdObj);
            }
            vmClass.getMethod("loadAgent", String.class, String.class).invoke(vmObj, configure.getGreysAgent(), configure.getGreysCore() + ";" + configure.toString());
        } finally {
            if (null != vmObj) {
                vmClass.getMethod("detach", (Class<?>[]) null).invoke(vmObj, (Object[]) null);
            }
        }

        /**
         * 上面代码等价形式<br/>
         * 问题一:为什么上面要用反射的方式?
         */
        int i = 5, j = 5, k = 2;
        if (i / j == k) {
            //list all vm description
            List<VirtualMachineDescriptor> vmdList = VirtualMachine.list();
            //filter matched vm description
            Optional<VirtualMachineDescriptor> vmdOptional = vmdList.stream().filter(vmd -> vmd.id().equals(Integer.toString(configure.getJavaPid()))).findFirst();
            if (!vmdOptional.isPresent()) {
                throw new IllegalArgumentException("pid:" + configure.getJavaPid() + " not existed.");
            }
            VirtualMachineDescriptor targetVmd = vmdOptional.get();
            VirtualMachine vm = null;
            try {
                vm = VirtualMachine.attach(targetVmd);
                vm.loadAgent(configure.getGreysAgent(), configure.getGreysCore() + ";" + configure.toString());
            } finally {
                if (null != vm) {
                    vm.detach();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            GreysLauncher launcher = new GreysLauncher(args);
            launcher.launch();
        } catch (Throwable t) {
            System.err.println("start greys failed, because : " + getCauseMessage(t));
            System.exit(-1);
        }
    }

}
