package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import top.gotoeasy.framework.aop.callback.AopAfter;
import top.gotoeasy.framework.aop.callback.AopAround;
import top.gotoeasy.framework.aop.callback.AopBefore;
import top.gotoeasy.framework.aop.callback.AopLast;
import top.gotoeasy.framework.aop.callback.AopThrowing;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnFile;

public class CodeBuilder {

	private Class<?>						clas;
	private Map<Method, List<AopBefore>>	mapMethodBefore		= new LinkedHashMap<>();
	private Map<Method, List<AopAfter>>		mapMethodAfter		= new LinkedHashMap<>();
	private Map<Method, List<AopThrowing>>	mapMethodThrowing	= new LinkedHashMap<>();
	private Map<Method, List<AopLast>>		mapMethodLast		= new LinkedHashMap<>();
	private Map<Method, AopAround>			mapMethodAround		= new LinkedHashMap<>();

	private int								seq					= 1;
	private Map<String, Object>				fieldMap			= new HashMap<>();

	/**
	 * 生成创建器
	 * @return 创建器
	 */
	public static CodeBuilder get() {
		return new CodeBuilder();
	}

	public CodeBuilder setSuperclass(Class<?> clas) {
		this.clas = clas;
		return this;
	}

	public CodeBuilder setAopBefore(Method method, AopBefore aopObj) {
		List<AopBefore> list = mapMethodBefore.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodBefore.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		return this;
	}

	public CodeBuilder setAopAfter(Method method, AopAfter aopObj) {
		List<AopAfter> list = mapMethodAfter.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodAfter.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		return this;
	}

	public CodeBuilder setAopThrowing(Method method, AopThrowing aopObj) {
		List<AopThrowing> list = mapMethodThrowing.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodThrowing.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		return this;
	}

	public CodeBuilder setAopLast(Method method, AopLast aopObj) {
		List<AopLast> list = mapMethodLast.get(method);
		if ( list == null ) {
			list = new ArrayList<>();
			mapMethodLast.put(method, list);
		}

		if ( !list.contains(aopObj) ) {
			list.add(aopObj);
		}

		return this;
	}

	public String build() {
		StringBuilder sbField = new StringBuilder();
		StringBuilder sbMethod = new StringBuilder();

		for ( Method method : mapMethodBefore.keySet() ) {
			sbMethod.append(getMethodCode(method, sbField));
		}

		String txt = getClassCode().replace("{method}", sbMethod.toString()).replace("{field}", sbField.toString());
		System.out.println(txt);
		return txt;
	}

	private String getBeforeCode(Method method, StringBuilder sbField) {
		List<AopBefore> list = mapMethodBefore.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            aopBefore{n}.before(this, method, {paramValues});";
		StringBuilder sb = new StringBuilder();
		String n;
		for ( int i = 0; i < list.size(); i++ ) {
			n = ((seq++) * 100 + i + 1) + "";
			sb.append(txt.replace("{n}", n)).append("\n");

			sbField.append("    public AopBefore aopBefore{n};".replace("{n}", n)).append("\n");
			fieldMap.put("aopBefore{n}".replace("{n}", n), list.get(i));
		}

		return sb.toString();
	}

	private String getAfterCode(Method method, StringBuilder sbField) {
		List<AopAfter> list = mapMethodAfter.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            aopAfter{n}.after(this, method, {paramValues});";
		StringBuilder sb = new StringBuilder();
		String n;
		for ( int i = 0; i < list.size(); i++ ) {
			n = ((seq++) * 100 + i + 1) + "";
			sb.append(txt.replace("{n}", n)).append("\n");

			sbField.append("    public AopAfter aopAfter{n};".replace("{n}", n)).append("\n");
			fieldMap.put("aopAfter{n}".replace("{n}", n), list.get(i));
		}

		return sb.toString();
	}

	private String getThrowingCode(Method method, StringBuilder sbField) {
		List<AopThrowing> list = mapMethodThrowing.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            aopThrowing{n}.throwing(this, method, e, {paramValues});";
		StringBuilder sb = new StringBuilder();
		String n;
		for ( int i = 0; i < list.size(); i++ ) {
			n = ((seq++) * 100 + i + 1) + "";
			sb.append(txt.replace("{n}", n)).append("\n");

			sbField.append("    public AopThrowing aopThrowing{n};".replace("{n}", n)).append("\n");
			fieldMap.put("aopThrowing{n}".replace("{n}", n), list.get(i));
		}

		return sb.toString();
	}

	private String getLastCode(Method method, StringBuilder sbField) {
		List<AopLast> list = mapMethodLast.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            aopLast{n}.last(this, method, {paramValues});";
		StringBuilder sb = new StringBuilder();
		String n;
		for ( int i = 0; i < list.size(); i++ ) {
			n = ((seq++) * 100 + i + 1) + "";
			sb.append(txt.replace("{n}", n)).append("\n");

			sbField.append("    public AopLast aopLast{n};".replace("{n}", n)).append("\n");
			fieldMap.put("aopLast{n}".replace("{n}", n), list.get(i));
		}

		return sb.toString();
	}

	private String getMethodCode(Method method, StringBuilder sbField) {
		String desc = method.toGenericString();
		Class<?> returnType = method.getReturnType();
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();
		String paramValues = "";

		String paramDefines = "";
		if ( paramTypes.length > 0 ) {
			StringBuilder sb = new StringBuilder();
			StringBuilder sbVal = new StringBuilder();
			for ( int i = 0; i < paramTypes.length; i++ ) {
				if ( i > 0 ) {
					sb.append(", ");
					sbVal.append(", ");
				}
				sb.append(paramTypes[i].getName()).append(" p" + i);
				sbVal.append("p" + i);
			}
			paramDefines = sb.toString();
			paramValues = sbVal.toString();
		}

		String beforeCode = getBeforeCode(method, sbField);
		String afterCode = getAfterCode(method, sbField);
		String throwingCode = getThrowingCode(method, sbField);
		String lastCode = getLastCode(method, sbField);

		String txt = readText("template_method.txt");
		return txt.replace("{returnType}", returnType.getName()).replace("{methodName}", methodName).replace("{desc}", desc)
				.replace("{beforeCode}", beforeCode).replace("{afterCode}", afterCode).replace("{throwingCode}", throwingCode)
				.replace("{lastCode}", lastCode).replace("{paramDefines}", paramDefines).replace("{paramValues}", paramValues)
				.replace("{superClass}", clas.getName());
	}

	private String getClassCode() {
		String pack = clas.getPackage().getName();
		String simpleName = clas.getSimpleName();

		String txt = readText("template_class.txt");

		return txt.replace("{pack}", pack).replace("{simpleName}", simpleName).replace("{superClass}", clas.getName());
	}

	private String readText(String file) {
		String path = clas.getPackage().getName().replace(".", "/") + "/";
		URL url = Thread.currentThread().getContextClassLoader().getResource(path + file);
		if ( url == null ) {
			return null;
		}
		return CmnFile.readFileText(url.getPath(), "utf-8");
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		CodeBuilder builder = CodeBuilder.get();
		builder.setSuperclass(Test.class);

		AopTest aopTest = new AopTest();
		AopTestAfter aopTestAfter = new AopTestAfter();
		AopTestThrowing aopTestThrowing = new AopTestThrowing();
		AopTestLast aopTestLast = new AopTestLast();
		for ( Method method : Test.class.getDeclaredMethods() ) {
			builder.setAopBefore(method, aopTest);
			builder.setAopAfter(method, aopTestAfter);
			builder.setAopThrowing(method, aopTestThrowing);
			//	builder.setAopLast(method, aopTestLast);
		}

		String className = Test.class.getName() + "$$gotoeasy$$";
		String srcCode = builder.build();

		MemoryJavaCompiler compiler = new MemoryJavaCompiler();
		compiler.compile(className, srcCode);
		MemoryClassLoader loader = new MemoryClassLoader();
		Class<?> aClass = loader.loadClass(className);
		Test test = (Test)aClass.newInstance();

		for ( String key : builder.fieldMap.keySet() ) {
			CmnBean.setFieldValue(test, key, builder.fieldMap.get(key));
		}

		////////////////////

		for ( int i = 0; i < 10000; i++ ) {
			test.hello("xxxxx", i);
		}

		long ss = System.currentTimeMillis();
		String xxx = null;
		for ( int i = 0; i < 1000 * 10000; i++ ) {
			xxx = test.hello("xxxxx", i);
		}
		System.err.println("MyAop: " + (System.currentTimeMillis() - ss) + "MS,   " + xxx);

		//	cglibAop();
	}

	private static void cglibAop() {
		// 增强
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(Test.class);
		enhancer.setCallback(new AopInterceptor());
		Object obj = enhancer.create(); // 返回目标类的增强子类

		Test pojo = (Test)obj;

		for ( int i = 0; i < 10000; i++ ) {
			pojo.hello("xxxxx", i);
		}

		long ss = System.currentTimeMillis();
		String xxx = null;
		for ( int i = 0; i < 1000 * 10000; i++ ) {
			xxx = pojo.hello("xxxxx", i);
		}
		System.err.println("CglibAop: " + (System.currentTimeMillis() - ss) + "MS,   " + xxx);
	}

}
