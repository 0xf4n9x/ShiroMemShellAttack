package org.shiroattack;

public class JavassistClassLoader extends ClassLoader {
    public JavassistClassLoader(){
        super(Thread.currentThread().getContextClassLoader());
    }
}