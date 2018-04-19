package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.util.CmnBean;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * @since 2018/04
 * @author 青松
 */
public class EnhancerBuilder {

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

		String txt = AopUtil.readText("template_around.txt");
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
	 * @param tmplFile 源码模板文件
	 * @return 源码
	 */
	private String getMethodCode(Method method, String tmplFile) {
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

		String txt = AopUtil.readText(tmplFile);
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

		String txt = AopUtil.readText("template_class.txt");

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
			String tmpl = "template_method.txt";
			if ( AopUtil.isVoid(method) ) {
				tmpl = "template_void_method.txt";
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
		System.out.println(txt);
		return txt;
	}

	/**
	 * 创建代理对象
	 * @return 代理对象
	 */
	public Object build() {

		// 创建代理类源码
		String className = AopUtil.getProxyClassName(clas);
		String srcCode = createClassCode();

		MemoryJavaCompiler compiler = new MemoryJavaCompiler();
		compiler.compile(className, srcCode);
		MemoryClassLoader loader = new MemoryClassLoader();

		// 创建代理对象
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
