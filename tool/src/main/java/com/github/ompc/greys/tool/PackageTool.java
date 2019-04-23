package com.github.ompc.greys.tool;

import com.github.ompc.greys.agent.AgentLauncher;
import com.github.ompc.greys.core.GreysLauncher;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author chengxiaojun
 */
public class PackageTool {

    private static final String PREMAIN_CLASS_PROPERTY = "Premain-Class";
    /**
     * The manifest property specifying the agent class.
     */
    private static final String AGENT_CLASS_PROPERTY = "Agent-Class";
    /**
     * The manifest property specifying the <i>can redefine</i> property.
     */
    private static final String CAN_REDEFINE_CLASSES_PROPERTY = "Can-Redefine-Classes";

    /**
     * The manifest property specifying the <i>can retransform</i> property.
     */
    private static final String CAN_RETRANSFORM_CLASSES_PROPERTY = "Can-Retransform-Classes";

    /**
     * The manifest property specifying the <i>can set native method prefix</i> property.
     */
    private static final String CAN_SET_NATIVE_METHOD_PREFIX = "Can-Set-Native-Method-Prefix";

    public static void main(String[] args) throws IOException {
        packageJar("greys-agent.jar", AgentLauncher.class.getPackage().getName().replace('.', '/'), generateManifest(AgentLauncher.class));
        packageJar("greys-core.jar", GreysLauncher.class.getPackage().getName().replace('.', '/'), generateEmptyManifest());
    }

    private static Manifest generateEmptyManifest() {
        return new Manifest();
    }

    private static Manifest generateManifest(Class agentClass) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.7.6.6");
        manifest.getMainAttributes().put(new Attributes.Name(PREMAIN_CLASS_PROPERTY), agentClass.getName());
        manifest.getMainAttributes().put(new Attributes.Name(AGENT_CLASS_PROPERTY), agentClass.getName());
        manifest.getMainAttributes().put(new Attributes.Name(CAN_REDEFINE_CLASSES_PROPERTY), Boolean.TRUE.toString());
        manifest.getMainAttributes().put(new Attributes.Name(CAN_RETRANSFORM_CLASSES_PROPERTY), Boolean.TRUE.toString());
        manifest.getMainAttributes().put(new Attributes.Name(CAN_SET_NATIVE_METHOD_PREFIX), Boolean.TRUE.toString());
        return manifest;
    }

    private static void packageJar(String jarFile, String packageName, Manifest manifest) throws IOException {
        File agentJar = new File(jarFile);
        //agentJar.deleteOnExit(); // Agent jar is required until VM shutdown due to lazy class loading.
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(agentJar), manifest);
        writeClass2Jar(packageName, jarOutputStream);
    }

    private static void writeClass2Jar(String packageName, JarOutputStream jarOutputStream) throws IOException {
        try {
            File dir = new File(Thread.currentThread().getContextClassLoader().getResource(packageName).getFile());
            addDirClass2Jar(jarOutputStream, dir);
        } finally {
            jarOutputStream.close();
        }
    }

    private static void addDirClass2Jar(JarOutputStream jarOutputStream, File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                addDirClass2Jar(jarOutputStream, file);
                continue;
            }

            if (file.isFile() && file.getName().endsWith(".class")) {
                addClass2Jar(jarOutputStream, file);
            }
        }
    }

    private static void addClass2Jar(JarOutputStream jarOutputStream, File file) throws IOException {
        String fileName = file.getAbsolutePath();
        int index = fileName.lastIndexOf("classes/");
        String className = fileName.substring((index < 0) ? 0 : index + "classes/".length());
        jarOutputStream.putNextEntry(new JarEntry(className));
        InputStream inputStream = AgentLauncher.class.getResourceAsStream('/' + className);
        byte[] buffer = new byte[512];
        int readByteNum;
        while ((readByteNum = inputStream.read(buffer)) != -1) {
            jarOutputStream.write(buffer, 0, readByteNum);
        }
        jarOutputStream.closeEntry();
        inputStream.close();
    }
}
