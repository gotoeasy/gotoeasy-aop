package top.gotoeasy.framework.aop.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import top.gotoeasy.framework.aop.Enhancer;

/**
 * Aop工具类
 * <p>
 * 仅考虑模块内部使用
 * </p>
 * @since 2018/04
 * @author 青松
 */
public class AopUtil {

	/**
	 * 取得方法的声明代码
	 * <p>
	 * 如：public final String hello(String p0)
	 * </p>
	 * @param method 方法
	 * @return 方法的声明代码
	 */
	public static String getMethodDefine(Method method) {
		StringBuilder sb = new StringBuilder();
		int modifiers = method.getModifiers();
		if ( Modifier.isPublic(modifiers) ) {
			sb.append("public ");
		} else if ( Modifier.isProtected(modifiers) ) {
			sb.append("protected ");
		}

		if ( Modifier.isStatic(modifiers) ) {
			sb.append("static ");
		}
		if ( Modifier.isSynchronized(modifiers) ) {
			sb.append("synchronized ");
		}
		sb.append("final ");
		sb.append(getReturnType(method)).append(" ");
		sb.append(method.getName()).append("(");
		sb.append(getParameterDefines(method)).append(")");

		return sb.toString();
	}

	/**
	 * 取得方法的返回类型源码
	 * @param method 方法
	 * @return 方法的返回类型源码
	 */
	public static String getReturnType(Method method) {
		return method.getReturnType().getName();
	}

	/**
	 * 取得方法的参数类型源码
	 * <p>
	 * 如：String.class, int.class, int.class
	 * </p>
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
			if ( paramTypes[i].isArray() ) {
				sb.append(paramTypes[i].getComponentType().getName()).append("[]").append(".class");
			} else {
				sb.append(paramTypes[i].getName()).append(".class");
			}
		}

		return sb.toString();
	}

	/**
	 * 取得Lambda方法的参数转换源码
	 * <p>
	 * 如：(String)args[0], (int)args[1]
	 * </p>
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
			if ( paramTypes[i].isArray() ) {
				sb.append("(").append(paramTypes[i].getComponentType().getName()).append("[]").append(")args[").append(i).append("]");
			} else {
				sb.append("(").append(paramTypes[i].getName()).append(")args[").append(i).append("]");
			}
		}

		return sb.toString();
	}

	/**
	 * 取得方法的参数定义源码
	 * <p>
	 * 如：String p0, String p1, String p2
	 * </p>
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
			if ( paramTypes[i].isArray() ) {
				sb.append(paramTypes[i].getComponentType().getName());
				if ( method.isVarArgs() && i == paramTypes.length - 1 ) {
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
	 * @param method 方法
	 * @return 方法的参数名源码
	 */
	public static String getParameterNames(Method method) {
		Class<?>[] paramTypes = method.getParameterTypes();
		if ( paramTypes.length == 0 ) {
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
	 * 判断是否为void方法
	 * @param method 方法
	 * @return true:是/fasle:否
	 */
	public static boolean isVoid(Method method) {
		return void.class.equals(method.getReturnType());
	}

	/**
	 * 取得指定方法的方法句柄
	 * <p>
	 * 按方法句柄调用能明显提高性能，但仍需谨慎使用<br>
	 * 在Java8环境，千万次以上调用的实验结果，表现出不稳定性<br>
	 * invoke时而正常时而异常，invokeWithArguments虽没有异常但性能及其不稳定<br>
	 * </p>
	 * @param method 方法
	 * @return 方法句柄
	 */
	public static MethodHandle getMethodHandle(Method method) {
		try {
			Lookup lk = MethodHandles.lookup();
			return lk.unreflect(method);
//			MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
//			return lk.findVirtual(method.getDeclaringClass(), method.getName(), methodType);  //查找方法句柄  
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 取得代理类的类名(含包名)
	 * <p>
	 * 代理类的类名 = 被代理类的类名 + "$$gotoeasy$$"
	 * </p>
	 * @param clas 被代理类
	 * @return 代理类的类名
	 */
	public static String getEnhancerName(Class<?> clas) {
		return clas.getName() + "$$gotoeasy$$";
	}

	/**
	 * 取得代理类的类名(不含包名)
	 * <p>
	 * 代理类的类名 = 被代理类的类名 + "$$gotoeasy$$"
	 * </p>
	 * @param clas 被代理类
	 * @return 代理类的类名
	 */
	public static String getEnhancerSimpleName(Class<?> clas) {
		return clas.getSimpleName() + "$$gotoeasy$$";
	}

	/**
	 * 查找在增量对象的父类中声明的方法
	 * @param enhancer 增量对象
	 * @param methodName 方法名
	 * @param classes 方法参数类型
	 * @return 方法
	 */
	public static Method getMethod(Enhancer enhancer, String methodName, Class<?> ... classes) {
		try {
			return enhancer.getClass().getSuperclass().getDeclaredMethod(methodName, classes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
