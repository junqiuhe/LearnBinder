package com.sample;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Project Name：LearnBinder
 * Created by hejunqiu on 2019/12/11 15:35
 * Description:
 */
public class RefInvoke {

    public static Class createClazz(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //-------------------实例化对象.------------------
    public static Object newInstance(String className){
        return newInstance(createClazz(className));
    }

    public static Object newInstance(Class clazz){
        return newInstance(clazz, null, null);
    }

    public static Object newInstance(String className, Class[] parameterTypes, Object[] parameterValues){
        return newInstance(createClazz(className), parameterTypes, parameterValues);
    }

    public static Object newInstance(Class clazz, Class[] parameterTypes, Object[] parameterValues){
        try{
            Constructor<?> constructor = clazz.getConstructor(parameterTypes);
            return constructor.newInstance(parameterValues);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    //-------------------调用静态属性------------------

    /**
     * 获取静态属性的值.
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Object getStaticFieldObject(Class clazz, String fieldName) {
        return getFieldObject(clazz, null, fieldName);
    }

    public static Object getStaticFieldObject(String className, String fieldName) {
        return getStaticFieldObject(createClazz(className), fieldName);
    }

    /**
     * 设置静态属性值.
     *
     * @param clazz
     * @param fieldName
     * @param fieldValue
     */
    public static void setStaticFieldObject(Class clazz, String fieldName, Object fieldValue) {
        setFieldObject(clazz, null, fieldName, fieldValue);
    }

    public static void setStaticFieldObject(String className, String fieldName, Object fieldValue) {
        setStaticFieldObject(createClazz(className), fieldName, fieldValue);
    }

    //-------------------调用实例属性------------------

    /**
     * 获取对象属性的值.
     *
     * @param clazz:       对象的class
     * @param objInstance: 对象实例
     * @param fieldName:   属性名.
     * @return: 属性的值.
     */
    public static Object getFieldObject(Class clazz, Object objInstance, String fieldName) {
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            return field.get(objInstance);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldObject(String className, Object objInstance, String fieldName) {
        return getFieldObject(createClazz(className), objInstance, fieldName);
    }

    /**
     * 设置对象属性值
     *
     * @param clazz
     * @param objInstance
     * @param fieldName
     * @param fieldValue
     */
    public static void setFieldObject(Class clazz, Object objInstance, String fieldName, Object fieldValue) {
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            field.set(objInstance, fieldValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldObject(String className, Object objInstance, String fieldName, Object fieldValue) {
        setFieldObject(createClazz(className), objInstance, fieldName, fieldValue);
    }

    //-------------------调用实例方法------------------

    /**
     * 调用无参方法
     *
     * @param className
     * @param objInstance
     * @param method
     * @return
     */
    public static Object invokeInstanceMethod(String className,
                                              Object objInstance,
                                              String method) {
        return invokeInstanceMethod(createClazz(className), objInstance, method);
    }

    public static Object invokeInstanceMethod(String className,
                                              Object objInstance,
                                              String method,
                                              Class[] parameterTypes,
                                              Object[] paramsValue) {
        return invokeInstanceMethod(createClazz(className), objInstance, method, parameterTypes, paramsValue);
    }

    public static Object invokeInstanceMethod(Class clazz,
                                              Object objInstance,
                                              String method) {
        return invokeMethod(clazz, objInstance, method, null, null);
    }

    public static Object invokeInstanceMethod(Class clazz,
                                              Object objInstance,
                                              String method,
                                              Class[] parameterTypes,
                                              Object[] paramsValue) {
        return invokeMethod(clazz, objInstance, method, parameterTypes, paramsValue);
    }


    //-------------------调用静态方法------------------
    public static Object invokeStaticMethod(String className,
                                            String method) {
        return invokeStaticMethod(createClazz(className), method);
    }

    public static Object invokeStaticMethod(String className,
                                            String method,
                                            Class[] parameterTypes,
                                            Object[] paramsValue) {
        return invokeMethod(createClazz(className), null, method, parameterTypes, paramsValue);
    }

    public static Object invokeStaticMethod(Class clazz,
                                            String method) {
        return invokeStaticMethod(clazz, method, null, null);
    }

    public static Object invokeStaticMethod(Class clazz,
                                            String method,
                                            Class[] parameterTypes,
                                            Object[] paramsValue) {
        return invokeMethod(clazz, null, method, parameterTypes, paramsValue);
    }


    //------------------------------------------------------
    private static Object invokeMethod(Class clazz,
                                       Object objInstance,
                                       String method,
                                       Class[] parameterTypes,
                                       Object[] paramsValue) {
        try {
            Method m = getMethod(clazz, method, parameterTypes);
            m.setAccessible(true);
            return m.invoke(objInstance, paramsValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Class currentClazz = clazz;

        // Try getting a public method
        try {
            return currentClazz.getMethod(methodName, parameterTypes);
        }

        //Try again, getting a non-public method
        catch (NoSuchMethodException e) {
            do {
                try {
                    return currentClazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException ignore) {
                }
                currentClazz = currentClazz.getSuperclass();
            } while (currentClazz != null);

            throw new RuntimeException("NoSuchMethodException and method : " + methodName);
        }
    }

    private static Field getField(Class clazz, String fieldName) {
        Class currentClazz = clazz;

        // Try getting a public field
        try {
            return clazz.getField(fieldName);
        }
        //Try again, getting a non-public field
        catch (NoSuchFieldException e) {
            do {
                try {
                    return currentClazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignore) {
                }
                currentClazz = currentClazz.getSuperclass();
            } while (currentClazz != null);

            throw new RuntimeException("NoSuchFieldException and field : " + fieldName);
        }
    }
}
