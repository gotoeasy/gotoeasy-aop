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
import top.gotoeasy.framework.aop.util.SourceTemplate;
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

	private static final Log				log					= LoggerFactory.getLogger(ClassScaner.class);

	private Class<?>						clas;

	private Map<Method, Method>				mapAop				= new LinkedHashMap<>();
	private Map<Method, List<AopBefore>>	mapMethodBefore		= new LinkedHashMap<>();
	private Map<Method, List<AopAfter>>		mapMethodAfter		= new LinkedHashMap<>();
	private Map<Method, List<AopThrowing>>	mapMethodThrowing	= new LinkedHashMap<>();
	private Map<Method, List<AopLast>>		mapMethodLast		= new LinkedHashMap<>();

	private Map<Method, AopAround>			mapMethodAround		= new LinkedHashMap<>();

	private int								aopObjSeq			= 1;
	private Map<Object, String>				fieldNameMap		= new LinkedHashMap<>();

	/**
	 * 生成创建器
	 * @return 创建器
	 */
	public static EnhancerBuilder get() {
		return new EnhancerBuilder();
	}

	/**
	 * 检查拦截冲突
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 */
	private void checkAround(Method method, Object aopObj) {
		if ( aopObj instanceof AopAround ) {
			if ( mapAop.containsKey(method) ) {
				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
			} else if ( mapMethodAround.containsKey(method) ) {
				throw new RuntimeException("重复的Around拦截，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
			}
		} else {
			if ( mapMethodAround.containsKey(method) ) {
				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法 (" + aopObj.getClass() + ")");
			}
		}
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
	 * 设定环绕拦截
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder setAopAround(Method method, AopAround aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		mapMethodAround.put(method, aopObj);

		return this;
	}

	/**
	 * 生成环绕拦截源码
	 * @param method 方法
	 * @return 源码
	 */
	private String getAroundCode(Method method) {
		AopAround around = mapMethodAround.get(method);
		if ( around == null ) {
			return "";
		}

		String txt = SourceTemplate.getSourceAround();
		txt = txt.replace("{methodDefine}", AopUtil.getMethodDefine(method));
		txt = txt.replace("{desc}", method.toGenericString());
		txt = txt.replace("{superClass}", clas.getName());
		txt = txt.replace("{parameterNames}", AopUtil.getParameterNames(method));
		txt = txt.replace("{return}", AopUtil.isVoid(method) ? "" : "return (" + AopUtil.getReturnType(method) + ")");
		txt = txt.replace("{aopObj}", fieldNameMap.get(around));

		return txt;
	}

	/**
	 * 设定前置拦截
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder setAopBefore(Method method, AopBefore aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		List<AopBefore> list = mapMethodBefore.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodBefore.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		mapAop.put(method, method);
		return this;
	}

	/**
	 * 设定后置拦截
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder setAopAfter(Method method, AopAfter aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		List<AopAfter> list = mapMethodAfter.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodAfter.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		mapAop.put(method, method);
		return this;
	}

	/**
	 * 设定异常拦截
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder setAopThrowing(Method method, AopThrowing aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		List<AopThrowing> list = mapMethodThrowing.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodThrowing.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		mapAop.put(method, method);
		return this;
	}

	/**
	 * 设定最终拦截
	 * @param method 方法
	 * @param aopObj 拦截处理对象
	 * @return 创建器
	 */
	public EnhancerBuilder setAopLast(Method method, AopLast aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		List<AopLast> list = mapMethodLast.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodLast.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		mapAop.put(method, method);
		return this;
	}

	/**
	 * 生成前置拦截源码
	 * @param method 方法
	 * @return 源码
	 */
	private String getBeforeCode(Method method) {
		List<AopBefore> list = mapMethodBefore.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            ((AopBefore){aopObj}).before(this, method, {parameterNames});";
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < list.size(); i++ ) {
			sb.append(txt.replace("{aopObj}", fieldNameMap.get(list.get(i)))).append("\n");
		}

		return sb.toString();
	}

	/**
	 * 生成后置拦截源码
	 * @param method 方法
	 * @return 源码
	 */
	private String getAfterCode(Method method) {
		List<AopAfter> list = mapMethodAfter.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            ((AopAfter){aopObj}).after(this, method, {parameterNames});";
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < list.size(); i++ ) {
			sb.append(txt.replace("{aopObj}", fieldNameMap.get(list.get(i)))).append("\n");
		}

		return sb.toString();
	}

	/**
	 * 生成异常拦截源码
	 * @param method 方法
	 * @return 源码
	 */
	private String getThrowingCode(Method method) {
		List<AopThrowing> list = mapMethodThrowing.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            ((AopThrowing){aopObj}).throwing(this, method, t, {parameterNames});";
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < list.size(); i++ ) {
			sb.append(txt.replace("{aopObj}", fieldNameMap.get(list.get(i)))).append("\n");
		}

		return sb.toString();
	}

	/**
	 * 生成最终拦截源码
	 * @param method 方法
	 * @return 源码
	 */
	private String getLastCode(Method method) {
		List<AopLast> list = mapMethodLast.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            ((AopLast){aopObj}).last(this, method, {parameterNames});";
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < list.size(); i++ ) {
			sb.append(txt.replace("{aopObj}", fieldNameMap.get(list.get(i)))).append("\n");
		}

		return sb.toString();
	}

	/**
	 * 生成方法拦截源码
	 * @param method 方法
	 * @param tmpl 源码模板
	 * @return 源码
	 */
	private String getMethodCode(Method method, String tmpl) {
		String desc = method.toGenericString();
		Class<?> returnType = method.getReturnType();
		String methodName = method.getName();

		String methodDefine = AopUtil.getMethodDefine(method);
		String parameterNames = AopUtil.getParameterNames(method);

		String beforeCode = getBeforeCode(method);
		String afterCode = getAfterCode(method);
		String throwingCode = getThrowingCode(method);
		String lastCode = getLastCode(method);

		String commentOut = "//";
		if ( mapMethodThrowing.containsKey(method) || mapMethodLast.containsKey(method) ) {
			commentOut = "";
		}

		String txt = tmpl;
		return txt.replace("{methodDefine}", methodDefine).replace("{returnType}", returnType.getName()).replace("{methodName}", methodName)
				.replace("{desc}", desc).replace("{beforeCode}", beforeCode).replace("{afterCode}", afterCode).replace("{throwingCode}", throwingCode)
				.replace("{lastCode}", lastCode).replace("{parameterNames}", parameterNames).replace("{superClass}", clas.getName())
				.replace("{commentOut}", commentOut);
	}

	/**
	 * 生成类源码
	 * @return 源码
	 */
	private String getClassCode() {
		String pack = clas.getPackage().getName();
		String simpleName = clas.getSimpleName();

		String txt = SourceTemplate.getSourceClass();

		return txt.replace("{pack}", pack).replace("{simpleName}", simpleName).replace("{superClass}", clas.getName());
	}

	/**
	 * 创建代理类源码
	 * @return 代理类源码
	 */
	private String createClassCode() {
		StringBuilder sbSuper = new StringBuilder();
		StringBuilder sbMethod = new StringBuilder();

		for ( Method method : mapAop.keySet() ) {
			String tmpl = SourceTemplate.getSourceMethod();
			if ( AopUtil.isVoid(method) ) {
				tmpl = SourceTemplate.getSourceVoidMethod();
			}
			sbMethod.append(getMethodCode(method, tmpl));
		}

		for ( Method method : mapMethodAround.keySet() ) {
			sbMethod.append(getAroundCode(method));
			sbSuper.append(AopUtil.getCodeInvokeSuperMethod(method));
		}

		StringBuilder sbField = new StringBuilder();
		for ( String fieldName : fieldNameMap.values() ) {
			sbField.append("    public Object " + fieldName + ";\n");
		}

		String txt = getClassCode().replace("{method}", sbMethod.toString()).replace("{field}", sbField.toString()).replace("{callSuper}",
				sbSuper.toString());

		log.trace(txt);
		return txt;
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
				if ( matchMethodBefore(method, aopObj) ) {
					setAopBefore(method, (AopBefore)aopObj);
				} else if ( matchMethodAfter(method, aopObj) ) {
					setAopAfter(method, (AopAfter)aopObj);
				} else if ( matchMethodThrowing(method, aopObj) ) {
					setAopThrowing(method, (AopThrowing)aopObj);
				} else if ( matchMethodLast(method, aopObj) ) {
					setAopLast(method, (AopLast)aopObj);
				} else if ( matchMethodAround(method, aopObj) ) {
					setAopAround(method, (AopAround)aopObj);
				}
			}
		}

		return this;
	}

	private boolean matchMethodBefore(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return false;
		}

		if ( aopObj instanceof AopBefore ) {
			String desc = method.toGenericString();
			List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
			for ( Method aopMethod : aopMethods ) {
				if ( !aopMethod.isAnnotationPresent(Before.class) ) {
					continue;
				}

				// TODO 检查@Before必须标注在AopBefore接口方法上

				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				Before aopAnno = aopMethod.getAnnotation(Before.class);
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchMethodAfter(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return false;
		}

		if ( aopObj instanceof AopAfter ) {
			String desc = method.toGenericString();
			List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
			for ( Method aopMethod : aopMethods ) {
				if ( !aopMethod.isAnnotationPresent(After.class) ) {
					continue;
				}

				// TODO 检查@After必须标注在AopAfter接口方法上

				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				After aopAnno = aopMethod.getAnnotation(After.class);
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchMethodThrowing(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return false;
		}

		if ( aopObj instanceof AopThrowing ) {
			String desc = method.toGenericString();
			List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
			for ( Method aopMethod : aopMethods ) {
				if ( !aopMethod.isAnnotationPresent(Throwing.class) ) {
					continue;
				}

				// TODO 检查@Throwing必须标注在AopThrowing接口方法上

				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				Throwing aopAnno = aopMethod.getAnnotation(Throwing.class);
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchMethodLast(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return false;
		}

		if ( aopObj instanceof AopLast ) {
			String desc = method.toGenericString();
			List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
			for ( Method aopMethod : aopMethods ) {
				if ( !aopMethod.isAnnotationPresent(Last.class) ) {
					continue;
				}

				// TODO 检查@Last必须标注在AopLast接口方法上

				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				Last aopAnno = aopMethod.getAnnotation(Last.class);
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean matchMethodAround(Method method, Object aopObj) {
		int modifiers = method.getModifiers();
		if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
			return false;
		}

		if ( aopObj instanceof AopAround ) {
			String desc = method.toGenericString();
			List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
			for ( Method aopMethod : aopMethods ) {
				if ( !aopMethod.isAnnotationPresent(Around.class) ) {
					continue;
				}

				// TODO 检查@Around必须标注在AopAround接口方法上

				// 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
				Around aopAnno = aopMethod.getAnnotation(Around.class);
				if ( CmnString.wildcardsMatch(aopAnno.value(), desc)
						&& (aopAnno.annotation().equals(Aop.class) || method.isAnnotationPresent(aopAnno.annotation())) ) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 创建代理对象
	 * @return 代理对象
	 */
	public Object build() {

		// 创建代理类源码
		String className = AopUtil.getProxyClassName(clas);
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
		for ( Object obj : fieldNameMap.keySet() ) {
			CmnBean.setFieldValue(proxyObject, fieldNameMap.get(obj), obj);
		}

		return proxyObject;
	}

}
