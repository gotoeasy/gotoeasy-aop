package top.gotoeasy.framework.aop.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;

import top.gotoeasy.framework.core.util.CmnFile;
import top.gotoeasy.framework.core.util.CmnMessageDigest;

public class AopUtil {

	public static String getCallSuperMethodName(String desc) {
		return "gotoeasy$" + CmnMessageDigest.md5(desc).replace('-', '$');
	}

	public static String getInvokeSuperMethod(Method method) {
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

	public static String getReturnType(Method method) {
		return method.getReturnType().getName();
	}

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

	public static boolean isVoid(Method method) {
		return "void".equals(method.getReturnType().toGenericString());
	}

	public static MethodHandle getMethodHandle(Method method) {
		try {
			MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			return MethodHandles.lookup().findVirtual(method.getDeclaringClass(), method.getName(), methodType);  //查找方法句柄  
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String readText(Class<?> clas, String fileName) {
		String path = clas.getPackage().getName().replace(".", "/") + "/";
		URL url = Thread.currentThread().getContextClassLoader().getResource(path + fileName);
		if ( url == null ) {
			return null;
		}
		return CmnFile.readFileText(url.getPath(), "utf-8");
	}

}
