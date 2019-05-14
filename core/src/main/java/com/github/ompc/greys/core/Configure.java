package com.github.ompc.greys.core;

import com.github.ompc.greys.core.util.FeatureCodec;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.github.ompc.greys.core.util.GaReflectUtils.*;
import static com.github.ompc.greys.core.util.GaStringUtils.newString;
import static java.lang.reflect.Modifier.isStatic;

/**
 * 配置类
 *
 * @author oldmanpushcart@gmail.com
 */
@Data
public class Configure {
    /**
     * 目标主机IP
     */
    private String targetIp;
    /**
     * 目标进程号
     */
    private int targetPort;
    /**
     * 对方java进程号
     */
    private int javaPid;
    /**
     * 连接超时时间(ms)
     */
    private int connectTimeout = 6000;
    private String greysCore;
    private String greysAgent;

    /**
     * 对象的编码解码器
     */
    private final static FeatureCodec codec = new FeatureCodec(';', '=');

    /**
     * 序列化成字符串
     *
     * @return 序列化字符串
     */
    @Override
    public String toString() {

        final Map<String, String> map = new HashMap<>(8);
        for (Field field : getFields(Configure.class)) {
            // 过滤掉静态类
            if (isStatic(field.getModifiers())) {
                continue;
            }

            // 非静态的才需要纳入非序列化过程
            try {
                map.put(field.getName(), newString(getValue(this, field)));
            } catch (Throwable t) {
                //
            }
        }

        return codec.toString(map);
    }

    /**
     * 反序列化字符串成对象
     *
     * @param toString 序列化字符串
     * @return 反序列化的对象
     */
    public static Configure toConfigure(String toString) {
        final Configure configure = new Configure();
        final Map<String, String> map = codec.toMap(toString);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                final Field field = getField(Configure.class, entry.getKey());
                if (null != field
                        && !isStatic(field.getModifiers())) {
                    setValue(field, valueOf(field.getType(), entry.getValue()), configure);
                }
            } catch (Throwable t) {
                //
            }
        }
        return configure;
    }

}
