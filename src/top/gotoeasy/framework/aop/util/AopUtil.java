package top.gotoeasy.framework.aop.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import top.gotoeasy.framework.core.util.CmnFile;
import top.gotoeasy.framework.core.util.CmnMessageDigest;

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
	 * 取得调用父方法的方法名
	 * @param desc 方法的描述 {@link Method#toGenericString()}
	 * @return 调用父方法的方法名
	 */
	public static String getCallSuperMethodName(String desc) {
		return "gotoeasy$" + CmnMessageDigest.md5(desc).replace('-', '$');
	}

	/**
	 * 生成调用父方法的源码
	 * @param method 被代理方法
	 * @return 调用父方法的源码
	 */
	public static String getCodeInvokeSuperMethod(Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append("public ");
		sb.append(getReturnType(method)).append(" ");
		sb.append(getCallSuperMethodName(method.toGenericString())).append("(");
		sb.append(getParameterDefines(method)).append("){\n");
		sb.append(isVoid(method) ? "" : "return ");
		sb.append("super.").append(method.getName());
		sb.append("(").append(getParameterNames(method)).append(");\n");
		sb.append("}\n\n");
		return sb.toString();
	}

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
			MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			return MethodHandles.lookup().findVirtual(method.getDeclaringClass(), method.getName(), methodType);  //查找方法句柄  
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 按utf-8编码读取指定文件内容
	 * @param clas 类（文件和该类在同一目录）
	 * @param fileName 文件名
	 * @return 文件内容
	 */
	public static String readText(String fileName) {
		String path = AopUtil.class.getPackage().getName().replace(".", "/") + "/";
		URL url = Thread.currentThread().getContextClassLoader().getResource(path + fileName);
		return CmnFile.readFileText(url.getPath(), "utf-8");
	}

	/**
	 * 取得代理类的类名
	 * <p>
	 * 代理类的类名 = 被代理类的类名 + "$$gotoeasy$$"
	 * </p>
	 * @param clas 被代理类
	 * @return 代理类的类名
	 */
	public static String getProxyClassName(Class<?> clas) {
		return clas.getName() + "$$gotoeasy$$";
	}

}
