package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.reflect.MethodScaner;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * @author 青松
 * @since 2018/04
 */
public class EnhancerBuilder {

	private static final Log					log							= LoggerFactory.getLogger(EnhancerBuilder.class);

	private Class<?>							clas;

	private int									aopObjSeq					= 1;
	private Map<Object, String>					aopObjFieldMap				= new LinkedHashMap<>();

	private int									methodSeq					= 1;
	private Map<Method, String>					methodFieldMap				= new LinkedHashMap<>();

	private int									superInvokerSeq				= 1;
	private Map<Method, String>					superInvokerFieldMap		= new LinkedHashMap<>();

	private Map<Method, MethodSrcInfo>			methodAroundSrcInfoMap		= new LinkedHashMap<>();
	private Map<Method, List<MethodSrcInfo>>	methodBeforeSrcInfoMap		= new LinkedHashMap<>();
	private Map<Method, List<MethodSrcInfo>>	methodAfterSrcInfoMap		= new LinkedHashMap<>();
	private Map<Method, List<MethodSrcInfo>>	methodThrowingSrcInfoMap	= new LinkedHashMap<>();
	private Map<Method, List<MethodSrcInfo>>	methodLastSrcInfoMap		= new LinkedHashMap<>();

	private Map<Method, Method>					methodNormalAopMap			= new LinkedHashMap<>();
	private Map<Method, Method>					methodAroundAopMap			= new LinkedHashMap<>();

	private String								TAB1						= "    ";
	private String								TAB2						= TAB1 + TAB1;
	private String								TAB3						= TAB2 + TAB1;

	/**
	 * 生成创建器
	 * @return 创建器
	 */
	public static EnhancerBuilder get() {
		return new EnhancerBuilder();
	}

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
				if ( aopObj.getClass().isAnnotationPresent(Aop.class) ) {
					// 检查@Aop
					matchMethodAround(method, aopObj);
				}
			}
		}

		return this;
	}

	/**
	 * 拦截检查
	 * @param method 被拦截方法
	 * @param aopMethod 拦截处理方法
	 * @param isAround 是否环绕拦截
	 */
	private void checkAop(Method method, Method aopMethod, boolean isAround) {

		// 拦截冲突检查
		if ( isAround ) {
			if ( methodAroundAopMap.containsKey(method) ) {
				log.error("发现拦截冲突，目标方法：{}", method);
				log.error("   拦截处理1：{}", methodAroundAopMap.get(method));
				log.error("   拦截处理2：{}", aopMethod);
				throw new RuntimeException("重复的Around拦截，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopMethod + ")");
			} else if ( methodNormalAopMap.containsKey(method) ) {
				log.error("发现拦截冲突，目标方法：{}", method);
				log.error("   拦截处理1：{}", methodNormalAopMap.get(method));
				log.error("   拦截处理2：{}", aopMethod);
				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopMethod + ")");
			}
		} else if ( methodAroundAopMap.containsKey(method) ) {
			log.error("发现拦截冲突，目标方法：{}", method);
			log.error("   拦截处理1：{}", methodAroundAopMap.get(method));
			log.error("   拦截处理2：{}", aopMethod);
			throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopMethod + ")");
		}

		// 方法返回类型检查
		if ( isAround ) {
			if ( !AopUtil.isVoid(method) && AopUtil.isVoid(aopMethod) ) {
				log.error("拦截处理漏返回类型缺失，应和目标方法一致");
				log.error("   目标方法：{}", method);
				log.error("   拦截处理：{}", aopMethod);
				throw new RuntimeException("拦截错误，目标方法有返回值，拦截处理漏返回 (" + aopMethod + ")");
			}
		}

		// 保存
		if ( isAround ) {
			methodAroundAopMap.put(method, aopMethod);
		} else {
			methodNormalAopMap.put(method, aopMethod);
		}

	}

	private void matchMethodAround(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return;
		}

		String desc = method.toGenericString();
		List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
		for ( Method aopMethod : aopMethods ) {
			if ( aopMethod.isAnnotationPresent(Around.class) ) {
				// @Around
				Around aopAnno = aopMethod.getAnnotation(Around.class);
				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					// 拦截检查
					checkAop(method, aopMethod, true);
					// 匹配成功，保存匹配结果
					saveAroundResult(method, aopMethod, aopObj);
				}
			} else if ( aopMethod.isAnnotationPresent(Before.class) ) {
				// @Before
				Before aopAnno = aopMethod.getAnnotation(Before.class);
				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					// 拦截检查
					checkAop(method, aopMethod, false);
					// 匹配成功，保存匹配结果
					saveNormalResult(method, aopMethod, aopObj, methodBeforeSrcInfoMap);
				}
			} else if ( aopMethod.isAnnotationPresent(After.class) ) {
				// @After
				After aopAnno = aopMethod.getAnnotation(After.class);
				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					// 拦截检查
					checkAop(method, aopMethod, false);
					// 匹配成功，保存匹配结果
					saveNormalResult(method, aopMethod, aopObj, methodAfterSrcInfoMap);
				}
			} else if ( aopMethod.isAnnotationPresent(Throwing.class) ) {
				// @Throwing
				Throwing aopAnno = aopMethod.getAnnotation(Throwing.class);
				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					// 拦截检查
					checkAop(method, aopMethod, false);
					// 匹配成功，保存匹配结果
					saveNormalResult(method, aopMethod, aopObj, methodThrowingSrcInfoMap);
				}
			} else if ( aopMethod.isAnnotationPresent(Last.class) ) {
				// @Last
				Last aopAnno = aopMethod.getAnnotation(Last.class);
				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					// 拦截检查
					checkAop(method, aopMethod, false);
					// 匹配成功，保存匹配结果
					saveNormalResult(method, aopMethod, aopObj, methodLastSrcInfoMap);
				}
			}

		}
	}

	private String getAopVarName(Object aopObj) {
		return CmnString.uncapitalize(aopObj.getClass().getSimpleName()) + "$" + aopObjSeq++;
	}

	private String getMethodVarName(Method method) {
		return "method" + methodSeq++ + "$" + method.getName();
	}

	private String getSuperInvokerVarName(Method method) {
		return "superInvoker" + superInvokerSeq++ + "$" + method.getName();
	}

	private void saveNormalResult(Method method, Method aopMethod, Object aopObj, Map<Method, List<MethodSrcInfo>> methodInfoMap) {
		// 匹配成功，保存匹配结果
		log.debug("匹配【{}拦截{}】", aopMethod, method);

		// aop变量
		String varAopObj = aopObjFieldMap.get(aopObj);
		if ( varAopObj == null ) {
			varAopObj = getAopVarName(aopObj);
			aopObjFieldMap.put(aopObj, varAopObj);
		}

		// method变量
		String varMethod = methodFieldMap.get(method);
		if ( varMethod == null ) {
			varMethod = getMethodVarName(method);
			methodFieldMap.put(method, varMethod);
		}

		// 方法信息变量
		MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
		methodSrcInfo.method = method;
		methodSrcInfo.varMethod = varMethod;
		methodSrcInfo.varAopObj = varAopObj;
		methodSrcInfo.aopMethodName = aopMethod.getName();
		List<MethodSrcInfo> list = methodInfoMap.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			methodInfoMap.put(method, list);
		}
		list.add(methodSrcInfo);
	}

	private void saveAroundResult(Method method, Method aopMethod, Object aopObj) {
		// 匹配成功，保存匹配结果
		log.debug("匹配【{}拦截{}】", aopMethod, method);

		// aop变量
		String varAopObj = aopObjFieldMap.get(aopObj);
		if ( varAopObj == null ) {
			varAopObj = getAopVarName(aopObj);
			aopObjFieldMap.put(aopObj, varAopObj);
		}

		// method变量
		String varMethod = methodFieldMap.get(method);
		if ( varMethod == null ) {
			varMethod = getMethodVarName(method);
			methodFieldMap.put(method, varMethod);
		}

		// superInvoker变量
		String varSuperInvoker = superInvokerFieldMap.get(method);
		if ( varSuperInvoker == null ) {
			varSuperInvoker = getSuperInvokerVarName(method);
			superInvokerFieldMap.put(method, varSuperInvoker);
		}

		// 方法信息变量
		MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
		methodSrcInfo.method = method;
		methodSrcInfo.varMethod = varMethod;
		methodSrcInfo.varSuperInvoker = varSuperInvoker;
		methodSrcInfo.varAopObj = varAopObj;
		methodSrcInfo.aopMethodName = aopMethod.getName();
		methodAroundSrcInfoMap.put(method, methodSrcInfo);
	}

	/**
	 * 创建代理类源码
	 * @return 代理类源码
	 */
	private String createClassCode() {
		StringBuilder sbMethodField = new StringBuilder();
		StringBuilder sbSuperInvokerField = new StringBuilder();
		StringBuilder sbAopField = new StringBuilder();

		for ( Method method : methodFieldMap.keySet() ) {
			// private Method {varMethod};
			sbMethodField.append(TAB1).append("private Method ").append(methodFieldMap.get(method)).append(";\n");
		}

		for ( Method method : superInvokerFieldMap.keySet() ) {
			// private SuperInvoker {varSuperInvoker};
			sbSuperInvokerField.append(TAB1).append("private SuperInvoker ").append(superInvokerFieldMap.get(method)).append(";\n");
		}

		for ( Object aopObj : aopObjFieldMap.keySet() ) {
			// public {aopClass} {varAopObj};
			sbAopField.append(TAB1).append("public ").append(aopObj.getClass().getName()).append(" ").append(aopObjFieldMap.get(aopObj))
					.append(";\n");
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
		StringBuilder sbAroundMethod = new StringBuilder();
		MethodSrcInfo info;
		for ( Method method : methodAroundSrcInfoMap.keySet() ) {
			info = methodAroundSrcInfoMap.get(method);
			sbAroundMethod.append(TAB1).append("@Override").append("\n");
			sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method)).append(" {\n");
			sbAroundMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) {").append("\n");
			sbAroundMethod.append(TAB3).append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
					.append("\"");

			String parameterTypes = AopUtil.getParameterTypes(method);
			if ( CmnString.isNotBlank(parameterTypes) ) {
				sbAroundMethod.append(", ").append(parameterTypes);
			}
			sbAroundMethod.append(");\n");

			if ( void.class.equals(method.getReturnType()) ) {
				// 无返回值
				sbAroundMethod.append(TAB3).append(superInvokerFieldMap.get(method)).append(" = (args) -> {super.").append(method.getName())
						.append("(").append(AopUtil.getLambdaArgs(method)).append("); return null;};").append("\n");
				sbAroundMethod.append(TAB2).append("}").append("\n");
				sbAroundMethod.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod)
						.append(", ").append(info.varSuperInvoker);
			} else {
				// 有返回值
				sbAroundMethod.append(TAB3).append(superInvokerFieldMap.get(method)).append(" = (args) -> super.").append(method.getName())
						.append("(").append(AopUtil.getLambdaArgs(method)).append(");\n");
				sbAroundMethod.append(TAB2).append("}").append("\n");
				sbAroundMethod.append(TAB2).append("return ").append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ")
						.append(info.varMethod).append(", ").append(info.varSuperInvoker);
			}

			String parameterNames = AopUtil.getParameterNames(method);
			if ( CmnString.isNotBlank(parameterTypes) ) {
				sbAroundMethod.append(", ").append(parameterNames);
			}
			sbAroundMethod.append(");\n");

			sbAroundMethod.append(TAB1).append("}\n\n");
		}

		// ---------------------------------- --------------------------------------------------
		//	@Override
		//	{methodDefine} {
		//		if ( {varMethod} == null ) {
		//			{varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes});
		//		}
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		//		{returnType} rs = super.{methodName}({parameterNames});
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		//	    return rs;
		//	}
		// ---------------------------------- --------------------------------------------------
		StringBuilder sbNormalMethod = new StringBuilder();
		for ( Method method : methodNormalAopMap.keySet() ) {
			sbNormalMethod.append(TAB1).append("@Override").append("\n");
			sbNormalMethod.append(TAB1).append(AopUtil.getMethodDefine(method)).append(" {\n");
			sbNormalMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) {").append("\n");
			sbNormalMethod.append(TAB3).append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
					.append("\", ").append(AopUtil.getParameterTypes(method)).append(");\n");
			sbNormalMethod.append(TAB2).append("}").append("\n");

			sbNormalMethod.append(getBeforeSrc(method)); // Before

			boolean hasTry = methodThrowingSrcInfoMap.containsKey(method) || methodLastSrcInfoMap.containsKey(method);
			if ( hasTry ) {
				sbNormalMethod.append("try {").append("\n");
			}

			if ( void.class.equals(method.getReturnType()) ) {
				// 无返回值
				sbNormalMethod.append(TAB2).append("super.").append(method.getName()).append("(").append(AopUtil.getParameterNames(method))
						.append(");\n");
			} else {
				// 有返回值
				sbNormalMethod.append(TAB2).append(method.getReturnType().getName()).append(" rs = super.").append(method.getName()).append("(")
						.append(AopUtil.getParameterNames(method)).append(");\n");
			}

			sbNormalMethod.append(getAfterSrc(method)); // After

			if ( !void.class.equals(method.getReturnType()) ) {
				sbNormalMethod.append(TAB2).append("return rs;").append("\n");
			}

			if ( methodThrowingSrcInfoMap.containsKey(method) ) {
				sbNormalMethod.append("} catch (Exception ex) {").append("\n");
				sbNormalMethod.append(getThrowingSrc(method)); // Throwing
				sbNormalMethod.append("throw new RuntimeException(ex);").append("\n");
				sbNormalMethod.append("}");
			}
			if ( methodLastSrcInfoMap.containsKey(method) ) {
				sbNormalMethod.append(" finally {").append("\n");
				sbNormalMethod.append(getLastSrc(method)); // Last
				sbNormalMethod.append("}");
			}
			sbNormalMethod.append("\n");

			sbNormalMethod.append(TAB1).append("}\n\n");
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
		sbClass.append(sbNormalMethod);
		sbClass.append("\n");
		sbClass.append(sbAroundMethod);
		sbClass.append("}").append("\n");

		String srcCode = sbClass.toString();

		log.info("\n{}", srcCode);
		return srcCode;
	}

	private StringBuilder getBeforeSrc(Method method) {
		// ---------------------------------- --------------------------------------------------
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		// ---------------------------------- --------------------------------------------------
		StringBuilder buf = new StringBuilder();
		List<MethodSrcInfo> list = methodBeforeSrcInfoMap.get(method);
		if ( list != null ) {
			for ( MethodSrcInfo info : list ) {
				buf.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod);
				String parameterNames = AopUtil.getParameterNames(method);
				if ( CmnString.isNotBlank(parameterNames) ) {
					buf.append(", ").append(parameterNames);
				}
				buf.append(");\n");
			}
		}
		return buf;
	}

	private StringBuilder getAfterSrc(Method method) {
		// ---------------------------------- --------------------------------------------------
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		// ---------------------------------- --------------------------------------------------
		StringBuilder buf = new StringBuilder();
		List<MethodSrcInfo> list = methodAfterSrcInfoMap.get(method);
		if ( list != null ) {
			for ( MethodSrcInfo info : list ) {
				buf.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod);
				String parameterNames = AopUtil.getParameterNames(method);
				if ( CmnString.isNotBlank(parameterNames) ) {
					buf.append(", ").append(parameterNames);
				}
				buf.append(");\n");
			}
		}
		return buf;
	}

	private StringBuilder getThrowingSrc(Method method) {
		// ---------------------------------- --------------------------------------------------
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		// ---------------------------------- --------------------------------------------------
		StringBuilder buf = new StringBuilder();
		List<MethodSrcInfo> list = methodThrowingSrcInfoMap.get(method);
		if ( list != null ) {
			for ( MethodSrcInfo info : list ) {
				buf.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod);
				String parameterNames = AopUtil.getParameterNames(method);
				if ( CmnString.isNotBlank(parameterNames) ) {
					buf.append(", ").append(parameterNames);
				}
				buf.append(");\n");
			}
		}
		return buf;
	}

	private StringBuilder getLastSrc(Method method) {
		// ---------------------------------- --------------------------------------------------
		//		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames});
		// ---------------------------------- --------------------------------------------------
		StringBuilder buf = new StringBuilder();
		List<MethodSrcInfo> list = methodLastSrcInfoMap.get(method);
		if ( list != null ) {
			for ( MethodSrcInfo info : list ) {
				buf.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(this, ").append(info.varMethod);
				String parameterNames = AopUtil.getParameterNames(method);
				if ( CmnString.isNotBlank(parameterNames) ) {
					buf.append(", ").append(parameterNames);
				}
				buf.append(");\n");
			}
		}
		return buf;
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
		Object proxyObject;
		try ( MemoryClassLoader loader = new MemoryClassLoader() ) {
			proxyObject = loader.loadClass(className).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
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

//	private void saveBeforeResult(Method method, Method aopMethod, Object aopObj) {
//		// 匹配成功，保存匹配结果
//		log.debug("匹配【{}拦截{}】", aopMethod, method);
//
//		// aop变量
//		String varAopObj = aopObjFieldMap.get(aopObj);
//		if ( varAopObj == null ) {
//			varAopObj = getAopVarName(aopObj);
//			aopObjFieldMap.put(aopObj, varAopObj);
//		}
//
//		// method变量
//		String varMethod = methodFieldMap.get(method);
//		if ( varMethod == null ) {
//			varMethod = getMethodVarName(method);
//			methodFieldMap.put(method, varMethod);
//		}
//
//		// 方法信息变量
//		MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
//		methodSrcInfo.method = method;
//		methodSrcInfo.varMethod = varMethod;
//		methodSrcInfo.varAopObj = varAopObj;
//		methodSrcInfo.aopMethodName = aopMethod.getName();
//		List<MethodSrcInfo> list = methodBeforeSrcInfoMap.get(method);
//		if ( list == null ) {
//			list = new ArrayList<>();
//			methodBeforeSrcInfoMap.put(method, list);
//		}
//		list.add(methodSrcInfo);
//	}
//
//	private void saveAfterResult(Method method, Method aopMethod, Object aopObj) {
//		// 匹配成功，保存匹配结果
//		log.debug("匹配【{}拦截{}】", aopMethod, method);
//
//		// aop变量
//		String varAopObj = aopObjFieldMap.get(aopObj);
//		if ( varAopObj == null ) {
//			varAopObj = getAopVarName(aopObj);
//			aopObjFieldMap.put(aopObj, varAopObj);
//		}
//
//		// method变量
//		String varMethod = methodFieldMap.get(method);
//		if ( varMethod == null ) {
//			varMethod = getMethodVarName(method);
//			methodFieldMap.put(method, varMethod);
//		}
//
//		// 方法信息变量
//		MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
//		methodSrcInfo.method = method;
//		methodSrcInfo.varMethod = varMethod;
//		methodSrcInfo.varAopObj = varAopObj;
//		methodSrcInfo.aopMethodName = aopMethod.getName();
//		List<MethodSrcInfo> list = methodAfterSrcInfoMap.get(method);
//		if ( list == null ) {
//			list = new ArrayList<>();
//			methodAfterSrcInfoMap.put(method, list);
//		}
//		list.add(methodSrcInfo);
//	}

}
