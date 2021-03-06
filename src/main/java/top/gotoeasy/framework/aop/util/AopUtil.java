package top.gotoeasy.framework.aop.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.exception.AopException;

/**
 * AOP工具类
 * <p>
 * 仅考虑模块内部使用
 * </p>
 * 
 * @since 2018/04
 * @author 青松
 */
public class AopUtil {

    private AopUtil() {
    }

    /**
     * 取得方法的声明代码
     * <p>
     * 如：public final String hello(String p0)
     * </p>
     * 
     * @param method 方法
     * @param strFinal final关键字（""/"final"）
     * @return 方法的声明代码
     */
    public static String getMethodDefine(Method method, String strFinal) {
        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(strFinal).append(" ");
        sb.append(getReturnType(method)).append(" ");
        sb.append(method.getName()).append("(");
        sb.append(getParameterDefines(method)).append(")");

        // 异常
        Class<?>[] classes = method.getExceptionTypes();
        for ( int i = 0; i < classes.length; i++ ) {
            if ( i == 0 ) {
                sb.append(" throws ");
            } else {
                sb.append(", ");
            }
            sb.append(classes[i].getName());
        }

        return sb.toString();
    }

    /**
     * 判断方法是否有参数
     * 
     * @param method 方法
     * @return true:有参数/false:无参数
     */
    public static boolean hasParameters(Method method) {
        return method.getParameterTypes().length > 0;
    }

    /**
     * 取得方法的返回类型源码
     * 
     * @param method 方法
     * @return 方法的返回类型源码
     */
    public static String getReturnType(Method method) {
        return method.getReturnType().getCanonicalName();
    }

    /**
     * 取得方法的参数类型源码
     * <p>
     * 如：String.class, int.class, int.class
     * </p>
     * 
     * @param method 方法
     * @return 方法的参数类型源码
     */
    public static String getParameterTypes(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if ( paramTypes.length == 0 ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }
            sb.append(paramTypes[i].getCanonicalName()).append(".class");
        }

        return sb.toString();
    }

    /**
     * 取得Lambda方法的参数转换源码
     * <p>
     * 如：(String)args[0], (int)args[1]
     * </p>
     * 
     * @param method 方法
     * @return Lambda方法的参数转换源码
     */
    public static String getLambdaArgs(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if ( paramTypes.length == 0 ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }

            String canonicalName = paramTypes[i].getCanonicalName();
            if ( paramTypes.length == 1 && paramTypes[i].isArray() && !paramTypes[i].getComponentType().isPrimitive() ) {
                // 仅一个参数，且参数为数组时(可变参数也是数组)，直接转换
                sb.append("(").append(canonicalName).append(")args");
            } else {
                sb.append("(").append(canonicalName).append(")args[").append(i).append("]");
            }
        }

        return sb.toString();
    }

    /**
     * 取得方法的参数定义源码
     * <p>
     * 如：String p0, String p1, String p2
     * </p>
     * 
     * @param method 方法
     * @return 方法的参数定义源码
     */
    public static String getParameterDefines(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if ( paramTypes.length == 0 ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }

            String canonicalName = paramTypes[i].getCanonicalName();
            if ( method.isVarArgs() && i == paramTypes.length - 1 ) {
                sb.append(canonicalName.substring(0, canonicalName.length() - 2)).append(" ...");
            } else {
                sb.append(canonicalName);
            }
            sb.append(" p" + i);
        }

        return sb.toString();
    }

    /**
     * 取得方法的参数定义源码
     * <p>
     * 如：String p0, String p1, String p2
     * </p>
     * 
     * @param constructor 构造方法
     * @return 方法的参数定义源码
     */
    public static String getParameterDefines(Constructor<?> constructor) {
        if ( constructor == null || constructor.getParameterCount() == 0 ) {
            return "";
        }

        Class<?>[] paramTypes = constructor.getParameterTypes();

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }
            if ( paramTypes[i].isArray() ) {
                sb.append(paramTypes[i].getComponentType().getName());
                if ( constructor.isVarArgs() && i == paramTypes.length - 1 ) {
                    sb.append(" ... p" + i);
                } else {
                    sb.append("[] p" + i);
                }
            } else {
                sb.append(paramTypes[i].getName()).append(" p" + i);
            }
        }

        return sb.toString();
    }

    /**
     * 取得方法的参数名源码
     * <p>
     * 如：p0, p1, p2
     * </p>
     * 
     * @param method 被拦截的目标方法
     * @param aopMethod 拦截处理方法
     * @return 方法的参数名源码
     */
    public static String getParameterNames(Method method, Method aopMethod) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if ( paramTypes.length == 0 || !hasArgParameter(aopMethod) ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }
            sb.append("p" + i);
        }

        return sb.toString();
    }

    /**
     * 取得构造方法的参数名源码
     * <p>
     * 如：p0, p1, p2
     * </p>
     * 
     * @param constructor 构造方法
     * @return 方法的参数名源码
     */
    public static String getParameterNames(Constructor<?> constructor) {
        if ( constructor == null ) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < constructor.getParameterCount(); i++ ) {
            if ( i > 0 ) {
                sb.append(", ");
            }
            sb.append("p" + i);
        }

        return sb.toString();
    }

    private static boolean hasArgParameter(Method aopMethod) {
        if ( aopMethod == null ) {
            return true;
        }

        Class<?>[] paramTypes = aopMethod.getParameterTypes();
        for ( Class<?> clas : paramTypes ) {
            if ( !Enhance.class.isAssignableFrom(clas) && !clas.equals(Method.class) && !clas.equals(SuperInvoker.class)
                    && !clas.equals(AopContext.class) && !Exception.class.isAssignableFrom(clas) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否为void方法
     * 
     * @param method 方法
     * @return true:是/fasle:否
     */
    public static boolean hasReturnType(Method method) {
        return !void.class.equals(method.getReturnType());
    }

    /**
     * 取得代理类的类名(含包名)
     * 
     * @param clas 被代理类
     * @return 代理类的类名(含包名)
     */
    public static String getEnhanceClassName(Class<?> clas) {
        return clas.getName() + "$$gotoeasy$$enhance";
    }

    /**
     * 取得代理类的类名(不含包名)
     * 
     * @param clas 被代理类
     * @return 代理类的类名
     */
    public static String getEnhanceSimpleName(Class<?> clas) {
        return clas.getSimpleName() + "$$gotoeasy$$enhance";
    }

    /**
     * 取得中间类的类名(含包名)
     * 
     * @param clas 被代理类
     * @param max 中间类最大数
     * @param order 中间类序号
     * @return 中间类的类名(含包名)
     */
    public static String getAroundMiddleClassName(Class<?> clas, int max, int order) {
        return clas.getName() + "$$gotoeasy$$around" + max + "$" + (order + 1);
    }

    /**
     * 取得中间类的类名(含包名)
     * 
     * @param clas 被代理类
     * @param max 中间类最大数
     * @param order 中间类序号
     * @return 中间类的类名(含包名)
     */
    public static String getAroundMiddleClassSimpleName(Class<?> clas, int max, int order) {
        if ( max == order ) {
            return getEnhanceSimpleName(clas);
        }
        return clas.getSimpleName() + "$$gotoeasy$$around" + max + "$" + (order + 1);
    }

    /**
     * 查找在增强对象的父类方法
     * 
     * @param enhance 增量对象
     * @param methodName 方法名
     * @param classes 方法参数类型
     * @return 方法
     */
    public static Method getMethod(Enhance enhance, String methodName, Class<?> ... classes) {
        try {
            Class<?> targetClass = enhance.getClass();
            while ( Enhance.class.isAssignableFrom(targetClass) ) {
                targetClass = targetClass.getSuperclass();
            }
            return targetClass.getMethod(methodName, classes);
        } catch (Exception e) {
            throw new AopException(e);
        }
    }

    /**
     * 取得方法描述
     * <p>
     * 【方法描述】
     * 包名.类名.方法名(参数类型)<br>
     * 如：top.gotoeasy.framework.aop.util.AopUtil.getMethodDesc(java.lang.Class,java.lang.reflect.Method)<br>
     * 数组及可变参数类似：top.gotoeasy.sample.check(int[],java.lang.String...)<br>
     * 父类方法也会使用指定类：top.gotoeasy.framework.aop.util.AopUtil.hashCode()
     * </p>
     * 
     * @param clas 类
     * @param method 方法
     * @return 方法描述
     */
    public static String getMethodDesc(Class<?> clas, Method method) {
        StringBuilder buf = new StringBuilder();
        Class<?> targetClass = clas;
        while ( Enhance.class.isAssignableFrom(targetClass) ) {
            targetClass = targetClass.getSuperclass();
        }

        buf.append(targetClass.getCanonicalName()).append('.').append(method.getName()).append("(");
        Class<?>[] paramTypes = method.getParameterTypes();

        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( i > 0 ) {
                buf.append(",");
            }
            if ( method.isVarArgs() && paramTypes[i].isArray() && i == paramTypes.length - 1 ) {
                buf.append(paramTypes[i].getCanonicalName()).append("...");
            } else {
                buf.append(paramTypes[i].getCanonicalName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

}
