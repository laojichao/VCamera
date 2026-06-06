package com.hack.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Java 反射工具类。
 * <p>
 * 封装了 {@link Field} 和 {@link Method} 的获取及操作，提供泛型包装类
 * {@link FieldRef} 和 {@link MethodRef}，简化反射调用的代码量。
 * 所有反射操作均自动设置 {@code accessible} 为 {@code true}，支持访问私有成员。
 * </p>
 */
public class RefUtils {
    /**
     * 获取指定类的字段对象。
     * <p>使用 {@link Class#getDeclaredField(String)} 获取，不会遍历父类。</p>
     *
     * @param refClass  目标类的 Class 对象
     * @param isStatic  是否为静态字段（当前参数未实际使用，预留接口）
     * @param filedName 字段名称
     * @return 对应的 {@link Field} 对象，若获取失败则返回 {@code null}
     */
    public static Field getField(Class refClass, boolean isStatic, String filedName) {
        if (refClass == null || filedName == null) {
            return null;
        }
        try {
            return refClass.getDeclaredField(filedName);
        } catch (NoSuchFieldException e) {

        }
        return null;
    }

    /**
     * 获取指定类的方法对象。
     * <p>使用 {@link Class#getDeclaredMethod(String, Class...)} 获取，不会遍历父类。</p>
     *
     * @param refClass   目标类的 Class 对象
     * @param isStatic   是否为静态方法（当前参数未实际使用，预留接口）
     * @param funcName   方法名称
     * @param paramTypes 方法参数类型数组，无参数时传入空数组
     * @return 对应的 {@link Method} 对象，若获取失败则返回 {@code null}
     */
    public static Method getMethod(Class refClass, boolean isStatic, String funcName, Class[] paramTypes) {
        if (refClass == null || funcName == null) {
            return null;
        }
        try {
            return refClass.getDeclaredMethod(funcName, paramTypes);
        } catch (NoSuchMethodException e) {

        }
        return null;
    }

    /**
     * 字段反射引用的泛型包装类。
     * <p>
     * 封装 {@link Field} 的获取与访问操作，支持通过类名字符串或 Class 对象构造。
     * 自动设置字段的可访问性，提供类型安全的 {@link #get(Object)} 和
     * {@link #set(Object, Object)} 方法。
     * </p>
     *
     * @param <T> 字段值的泛型类型
     */
    public static class FieldRef<T> {
        boolean mIsStatic;
        Field mField;
        /**
         * 通过 Class 对象构造字段引用。
         *
         * @param refClass 目标类的 Class 对象
         * @param isStatic 是否为静态字段
         * @param name     字段名称
         */
        public FieldRef(Class refClass, boolean isStatic, String name) {
            mIsStatic = isStatic;
            mField = getField(refClass, isStatic, name);
            if (mField != null) {
                mField.setAccessible(true);
            }
        }
        /**
         * 通过类名字符串构造字段引用。
         *
         * @param className 目标类的全限定名
         * @param isStatic  是否为静态字段
         * @param name      字段名称
         */
        public FieldRef(String className, boolean isStatic, String name) {
            try {
                Class targetClass = Class.forName(className);
                mField = getField(targetClass, isStatic, name);
            } catch (ClassNotFoundException e) {
            }
            mIsStatic = isStatic;
            if (mField != null) {
                mField.setAccessible(true);
            }
        }

        /**
         * 判断字段引用是否有效。
         *
         * @return {@code true} 表示底层 {@link Field} 对象不为 {@code null}
         */
        public boolean isValid() {
            return mField != null;
        }

        /**
         * 获取指定对象上该字段的值。
         * <p>对于静态字段，{@code instance} 参数将被忽略，可传入 {@code null}。</p>
         *
         * @param instance 字段所属的对象实例，静态字段时可为 {@code null}
         * @return 字段值，若获取失败则返回 {@code null}
         */
        public T get(Object instance) {
            try {
                return (T) mField.get(instance);
            } catch (Exception e) {

            }
            return null;
        }
        /**
         * 设置指定对象上该字段的值。
         * <p>对于静态字段，{@code instance} 参数将被忽略，可传入 {@code null}。</p>
         *
         * @param instance 字段所属的对象实例，静态字段时可为 {@code null}
         * @param value    要设置的新值
         */
        public void set(Object instance, T value) {
            try {
                mField.set(instance, value);
            } catch (Exception e) {

            }
        }
    }

    /**
     * 方法反射引用的泛型包装类。
     * <p>
     * 封装 {@link Method} 的获取与调用操作，支持通过类名字符串或 Class 对象构造。
     * 自动设置方法的可访问性，提供类型安全的 {@link #invoke(Object, Object...)} 方法。
     * </p>
     *
     * @param <T> 方法返回值的泛型类型
     */
    public static class MethodRef<T> {
        Method mMethod;
        /**
         * 通过类名字符串构造方法引用。
         *
         * @param className  目标类的全限定名
         * @param isStatic   是否为静态方法
         * @param funcName   方法名称
         * @param paramsTypes 方法参数类型数组
         */
        public MethodRef(String className, boolean isStatic, String funcName, Class[] paramsTypes) {
            try {
                Class targetClass = Class.forName(className);
                mMethod = getMethod(targetClass, isStatic, funcName, paramsTypes);
            } catch (Exception e) {
            }
            if (mMethod != null) {
                mMethod.setAccessible(true);
            }
        }
        /**
         * 通过 Class 对象构造方法引用。
         *
         * @param refClass    目标类的 Class 对象
         * @param isStatic    是否为静态方法
         * @param funcName    方法名称
         * @param paramsTypes 方法参数类型数组
         */
        public MethodRef(Class refClass, boolean isStatic, String funcName, Class[] paramsTypes) {
            mMethod = getMethod(refClass, isStatic, funcName, paramsTypes);
            if (mMethod != null) {
                mMethod.setAccessible(true);
            }
        }

        /**
         * 判断方法引用是否有效。
         *
         * @return {@code true} 表示底层 {@link Method} 对象不为 {@code null}
         */
        public boolean isValid() {
            return mMethod != null;
        }

        /**
         * 调用该方法并返回结果。
         * <p>对于静态方法，{@code instance} 参数将被忽略，可传入 {@code null}。
         * 若调用过程中发生异常，将静默返回 {@code null}。</p>
         *
         * @param instance 方法所属的对象实例，静态方法时可为 {@code null}
         * @param args     方法调用参数列表
         * @return 方法执行的返回值，若调用失败则返回 {@code null}
         */
        public T invoke(Object instance, Object...args) {
            try {
                return (T) mMethod.invoke(instance, args);
            } catch (Exception e) {
            }
            return null;
        }
    }
}
