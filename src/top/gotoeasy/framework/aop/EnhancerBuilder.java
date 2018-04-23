package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.reflect.ClassScaner;
import top.gotoeasy.framework.core.reflect.MethodScaner;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * @since 2018/04
 * @author 青松
 */
public class EnhancerBuilder {

	private static final Log			log						= LoggerFactory.getLogger(ClassScaner.class);

	private Class<?>					clas;

	private int							aopObjSeq				= 1;
	private Map<Object, String>			aopObjFieldMap			= new LinkedHashMap<>();

	private int							methodSeq				= 1;
	private Map<Method, String>			methodFieldMap			= new LinkedHashMap<>();

	private int							superInvokerSeq			= 1;
	private Map<Method, String>			superInvokerFieldMap	= new LinkedHashMap<>();

	private Map<Method, MethodSrcInfo>	methodSrcInfoMap		= new LinkedHashMap<>();

	/**
	 * 生成创建器
	 * @return 创建器
	 */
	public static EnhancerBuilder get() {
		return new EnhancerBuilder();
	}

	//TODO 拦截检查，方法返回值检查，参数检查
//	/**
//	 * 检查拦截冲突
//	 * @param method 方法
//	 * @param aopObj 拦截处理对象
//	 */
//	private void checkAround(Method method, Object aopObj) {
//		if ( aopObj instanceof AopAround ) {
//			if ( mapAop.containsKey(method) ) {
//				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
//			} else if ( mapMethodAround.containsKey(method) ) {
//				throw new RuntimeException("重复的Around拦截，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
//			}
//		} else {
//			if ( mapMethodAround.containsKey(method) ) {
//				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
//			}
//		}
//	}

	/**
	 * 设定被代理类
	 * @param clas 被代理类
	 * @return 创建器
	 */
	public EnhancerBuilder setSuperclass(Class<?> clas) {
		this.clas = clas;
		return this;
	}

	/**
	 * 设定拦截
	 * <p>
	 * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
	 * </p>
	 * @param aops 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder matchAop(Object ... aops) {
		Method[] methods = clas.getMethods();
		for ( Method method : methods ) {
			for ( Object aopObj : aops ) {
				matchMethodAround(method, aopObj);
			}
		}

		return this;
	}

	private void matchMethodAround(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return;
		}

		String desc = method.toGenericString();
		List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
		for ( Method aopMethod : aopMethods ) {
			if ( !aopMethod.isAnnotationPresent(Around.class) ) {
				continue;
			}

			// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
			Around aopAnno = aopMethod.getAnnotation(Around.class);
			if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
					&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
				// 匹配
				log.debug("匹配【{}拦截{}】", aopMethod, method);
				String varAopObj = aopObjFieldMap.get(aopObj);
				if ( varAopObj == null ) {
					varAopObj = "aopObj" + aopObjSeq++;
					aopObjFieldMap.put(aopObj, varAopObj);
				}

				String varMethod = methodFieldMap.get(method);
				if ( varMethod == null ) {
					varMethod = "method" + methodSeq++;
					methodFieldMap.put(method, varMethod);
				}

				String varSuperInvoker = superInvokerFieldMap.get(method);
				if ( varSuperInvoker == null ) {
					varSuperInvoker = "varSuperInvoker" + superInvokerSeq++;
					superInvokerFieldMap.put(method, varSuperInvoker);
				}

				MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
				methodSrcInfo.method = method;
				methodSrcInfo.varMethod = varMethod;
				methodSrcInfo.varSuperInvoker = varSuperInvoker;
				methodSrcInfo.varAopObj = varAopObj;
				methodSrcInfo.aopMethodName = aopMethod.getName();
				methodSrcInfoMap.put(method, methodSrcInfo);
			}
		}
	}

	/**
	 * 创建代理类源码
	 * @return 代理类源码
	 */
	private String createClassCode() {
		StringBuilder sbMethodField = new StringBuilder();
		StringBuilder sbSuperInvokerField = new StringBuilder();
		StringBuilder sbAopField = new StringBuilder();
		String TAB = "    ";
		String TAB2 = "        ";
		String TAB4 = "            ";

		for ( Method method : methodFieldMap.keySet() ) {
			// private Method {varMethod};
			sbMethodField.append(TAB).append("private Method ").append(methodFieldMap.get(method)).append(";\n");
		}

		for ( Method method : superInvokerFieldMap.keySet() ) {
			// private SuperInvoker {varSuperInvoker};
			sbSuperInvokerField.append(TAB).append("private SuperInvoker ").append(superInvokerFieldMap.get(method)).append(";\n");
		}

		for ( Object aopObj : aopObjFieldMap.keySet() ) {
			// public {aopClass} {varAopObj};
			sbAopField.append(TAB).append("public ").append(aopObj.getClass().getName()).append(" ").append(aopObjFieldMap.get(aopObj)).append(";\n");
		}

		// ---------------------------------- --------------------------------------------------
		//	@Override
		//	{methodDefine} {
		//		if ( {varMethod} == null ) {
		//			{varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes});
		//			{varSuperInvoker} = (method, args) -> super.{methodName}({args});
		//		}
		//		return {varAopObj}.{aopMethodName}(this, {varMethod}, {varSuperInvoker}, {parameterNames});
		//	}
		// ---------------------------------- --------------------------------------------------
		StringBuilder sbMethod = new StringBuilder();
		MethodSrcInfo info;
		for ( Method method : methodSrcInfoMap.keySet() ) {
			info = methodSrcInfoMap.get(method);
			sbMethod.append(TAB).append("@Override").append("\n");
			sbMethod.append(TAB).append(AopUtil.getMethodDefine(method)).append(" {\n");
			sbMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) {").append("\n");
			sbMethod.append(TAB4).append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName()).append("\", ")
					.append(AopUtil.getParameterTypes(method)).append(");\n");

			if ( void.class.equals(method.getReturnType()) ) {
				// 无返回值
				sbMethod.append(TAB4).append(superInvokerFieldMap.get(method)).append(" = (args) -> {super.").append(method.getName()).append("(")
						.append("(int)args[0] ").append("); return null;};").append("\n");
				sbMethod.append(TAB2).append("}").append("\n");
				sbMethod.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod)
						.append(", ").append(info.varSuperInvoker).append(", ").append(AopUtil.getParameterNames(method)).append(");\n");
			} else {
				// 有返回值
				sbMethod.append(TAB4).append(superInvokerFieldMap.get(method)).append(" = (args) -> super.").append(method.getName()).append("(")
						.append("(int)args[0] ").append(");\n");
				sbMethod.append(TAB2).append("}").append("\n");
				sbMethod.append(TAB2).append("return ").append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ")
						.append(info.varMethod).append(", ").append(info.varSuperInvoker).append(", ").append(AopUtil.getParameterNames(method))
						.append(");\n");
			}

			sbMethod.append(TAB).append("}\n\n");
		}

		StringBuilder sbClass = new StringBuilder();
		// -------------------------------------------------------------------------
		//	package {package};
		//
		//	import java.lang.reflect.Method;
		//
		//	import top.gotoeasy.framework.aop.util.AopUtil;
		//	import top.gotoeasy.framework.aop.Enhancer;
		//	import top.gotoeasy.framework.aop.SuperInvoker;
		//
		//	public class {simpleName}$$gotoeasy$$ extends {superClass} implements Enhancer {
		//
		//		{methodField}
		//		{superInvokerField}
		//		{aopObjField}
		//
		//		{method}
		//	}
		// -------------------------------------------------------------------------
		sbClass.append("package ").append(clas.getPackage().getName()).append(";\n");
		sbClass.append("\n");
		sbClass.append("import java.lang.reflect.Method;").append("\n");
		sbClass.append("\n");
		sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
		sbClass.append("import top.gotoeasy.framework.aop.Enhancer;").append("\n");
		sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
		sbClass.append("\n");
		sbClass.append("public class ").append(AopUtil.getEnhancerSimpleName(clas)).append(" extends ").append(clas.getSimpleName())
				.append(" implements Enhancer {").append("\n");
		sbClass.append("\n");
		sbClass.append(sbMethodField);
		sbClass.append(sbSuperInvokerField);
		sbClass.append(sbAopField);
		sbClass.append("\n");
		sbClass.append(sbMethod);
		sbClass.append("}").append("\n");

		String srcCode = sbClass.toString();

		log.info("\n{}", srcCode);
		return srcCode;
	}

	/**
	 * 创建代理对象
	 * @return 代理对象
	 */
	public Object build() {

		// 创建代理类源码
		String className = AopUtil.getEnhancerName(clas);
		String srcCode = createClassCode();

		// 动态编译、创建代理对象
		MemoryJavaCompiler compiler = new MemoryJavaCompiler();
		compiler.compile(className, srcCode);
		MemoryClassLoader loader = new MemoryClassLoader();
		Class<?> proxyClass;
		Object proxyObject;
		try {
			proxyClass = loader.loadClass(className);
			proxyObject = proxyClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				loader.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// 设定拦截处理对象
		for ( Object aopObj : aopObjFieldMap.keySet() ) {
			CmnBean.setFieldValue(proxyObject, aopObjFieldMap.get(aopObj), aopObj);
		}

		return proxyObject;
	}

	@SuppressWarnings("unused")
	private static class MethodSrcInfo {

		Method	method;
		String	varMethod;
		String	varSuperInvoker;
		String	varAopObj;
		String	aopMethodName;

	}

}
