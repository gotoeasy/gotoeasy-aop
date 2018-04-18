package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import top.gotoeasy.framework.aop.callback.AopAfter;
import top.gotoeasy.framework.aop.callback.AopAround;
import top.gotoeasy.framework.aop.callback.AopBefore;
import top.gotoeasy.framework.aop.callback.AopLast;
import top.gotoeasy.framework.aop.callback.AopThrowing;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnFile;

public class CodeBuilder {

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
	public static CodeBuilder get() {
		return new CodeBuilder();
	}

	private void checkAround(Method method, Object aopObj) {
		if ( aopObj instanceof AopAround ) {
			if ( mapAop.containsKey(method) ) {
				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法");
			} else if ( mapMethodAround.containsKey(method) ) {
				throw new RuntimeException("Around拦截重复，Around拦截必须独占，不能和其他拦截共同拦截同一方法");
			}
		} else {
			if ( mapMethodAround.containsKey(method) ) {
				throw new RuntimeException("拦截冲突，Around拦截必须独占，不能和其他拦截共同拦截同一方法");
			}
		}
	}

	public CodeBuilder setSuperclass(Class<?> clas) {
		this.clas = clas;
		return this;
	}

	public CodeBuilder setAopAround(Method method, AopAround aopObj) {
		checkAround(method, aopObj);

		if ( !fieldNameMap.containsKey(aopObj) ) {
			fieldNameMap.put(aopObj, "aopObj" + aopObjSeq++);
		}

		mapMethodAround.put(method, aopObj);

		return this;
	}

	private String getAroundCode(Method method) {
		AopAround around = mapMethodAround.get(method);
		if ( around == null ) {
			return "";
		}

		String txt = AopUtil.readText(CodeBuilder.class, "template_around.txt");
		txt = txt.replace("{methodDefine}", AopUtil.getMethodDefine(method));
		txt = txt.replace("{desc}", method.toGenericString());
		txt = txt.replace("{superClass}", clas.getName());
		txt = txt.replace("{parameterNames}", AopUtil.getParameterNames(method));
		txt = txt.replace("{return}", AopUtil.isVoid(method) ? "" : "return (" + AopUtil.getReturnType(method) + ")");
		txt = txt.replace("{aopObj}", fieldNameMap.get(around));

		return txt;
	}

	public CodeBuilder setAopBefore(Method method, AopBefore aopObj) {
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

	public CodeBuilder setAopAfter(Method method, AopAfter aopObj) {
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

	public CodeBuilder setAopThrowing(Method method, AopThrowing aopObj) {
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

	public CodeBuilder setAopLast(Method method, AopLast aopObj) {
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

	private String getThrowingCode(Method method) {
		List<AopThrowing> list = mapMethodThrowing.get(method);
		if ( list == null || list.isEmpty() ) {
			return "";
		}

		String txt = "            ((AopThrowing){aopObj}).throwing(this, method, e, {parameterNames});";
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < list.size(); i++ ) {
			sb.append(txt.replace("{aopObj}", fieldNameMap.get(list.get(i)))).append("\n");
		}

		return sb.toString();
	}

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

		String txt = readText(tmplFile);
		return txt.replace("{methodDefine}", methodDefine).replace("{returnType}", returnType.getName()).replace("{methodName}", methodName)
				.replace("{desc}", desc).replace("{beforeCode}", beforeCode).replace("{afterCode}", afterCode).replace("{throwingCode}", throwingCode)
				.replace("{lastCode}", lastCode).replace("{parameterNames}", parameterNames).replace("{superClass}", clas.getName())
				.replace("{commentOut}", commentOut);
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

	public String build() {

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
			sbSuper.append(AopUtil.getInvokeSuperMethod(method));
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

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		CodeBuilder builder = CodeBuilder.get();
		builder.setSuperclass(Test.class);

		AopTestBefore aopTest = new AopTestBefore();
		AopTestAfter aopTestAfter = new AopTestAfter();
		AopTestThrowing aopTestThrowing = new AopTestThrowing();
		AopTestLast aopTestLast = new AopTestLast();
		AopTestAround aopTestAround = new AopTestAround();

		Method[] methods = Test.class.getDeclaredMethods();
		for ( int i = 0; i < methods.length; i++ ) {
			if ( i == 0 ) {
				builder.setAopAround(methods[i], aopTestAround);
				continue;
			}
//			builder.setAopBefore(methods[i], aopTest);
//			builder.setAopAfter(methods[i], aopTestAfter);
//			builder.setAopThrowing(methods[i], aopTestThrowing);
//			builder.setAopLast(methods[i], aopTestLast);
		}

		String className = Test.class.getName() + "$$gotoeasy$$";
		String srcCode = builder.build();

		MemoryJavaCompiler compiler = new MemoryJavaCompiler();
		compiler.compile(className, srcCode);
		MemoryClassLoader loader = new MemoryClassLoader();
		Class<?> aClass = loader.loadClass(className);
		Test test = (Test)aClass.newInstance();

		for ( Object obj : builder.fieldNameMap.keySet() ) {
			CmnBean.setFieldValue(test, builder.fieldNameMap.get(obj), obj);
		}

		////////////////////
		for ( int i = 0; i < 10000; i++ ) {
			test.hello("xxxxx");
		}

		long ss = System.currentTimeMillis();
		String xxx = null;
		for ( int i = 0; i < 1000 * 10000; i++ ) {
			xxx = test.hello("xxxxx");
		}
		System.err.println("MyAop: " + (System.currentTimeMillis() - ss) + "MS,   " + xxx);

		cglibAop();
	}

	private static void cglibAop() {
		// 增强
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(Test.class);
		enhancer.setCallback(new AopInterceptor());
		Object obj = enhancer.create(); // 返回目标类的增强子类

		Test pojo = (Test)obj;

		for ( int i = 0; i < 10000; i++ ) {
			pojo.hello("xxxxx");
		}

		long ss = System.currentTimeMillis();
		String xxx = null;
		for ( int i = 0; i < 1000 * 10000; i++ ) {
			xxx = pojo.hello("xxxxx");
		}
		System.err.println("CglibAop: " + (System.currentTimeMillis() - ss) + "MS,   " + xxx);
	}

}
