package top.gotoeasy.framework.aop.code;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import top.gotoeasy.framework.aop.util.AopUtil;

public class AopPoint {

	private Object								target;
	private Class<?>							superClass;
	private Object[]							args;

	private String								desc;

	private Method								method;
	private Method								callSuper;

	private Throwable							throwable;

	private static final Map<String, Method>	mapMethod	= new HashMap<>();
	private static final Map<String, Method>	mapSuper	= new HashMap<>();

	public void init(Object target, Class<?> superClass, String desc, Throwable throwable, Object ... args) {
		this.target = target;
		this.superClass = superClass;
		this.desc = desc;
		this.throwable = throwable;
		this.args = args;

		this.method = getMethodByDesc();
	}

	public void init(Object target, Class<?> superClass, String desc, Object ... args) {
		init(target, superClass, desc, null, args);
	}

	public void reset() {
		target = null;
		superClass = null;
		args = null;

		desc = null;

		method = null;
		callSuper = null;
	}

	public Object invokeSuper() {
		try {
			if ( callSuper == null ) {
				callSuper = getCallSuperMethod();
			}
			return callSuper.invoke(target, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object invokeSuper(Object[] args) {
		try {
			if ( callSuper == null ) {
				callSuper = getCallSuperMethod();
			}
			return callSuper.invoke(target, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Object getTarget() {
		return target;
	}

	public Class<?> getSuperClass() {
		return superClass;
	}

	public Object[] getArgs() {
		return args;
	}

	public Method getMethod() {
		return method;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	private Method getMethodByDesc() {
		if ( mapMethod.containsKey(desc) ) {
			return mapMethod.get(desc);
		}

		for ( Method th : top.gotoeasy.framework.aop.code.Test.class.getDeclaredMethods() ) {
			if ( th.toGenericString().equals(desc) ) {
				mapMethod.put(desc, th);
				return th;
			}
		}

		return null;
	}

	private Method getCallSuperMethod() {
		if ( mapSuper.containsKey(desc) ) {
			return mapSuper.get(desc);
		}

		String callSuperMethodName = AopUtil.getCallSuperMethodName(desc);

		for ( Method th : target.getClass().getDeclaredMethods() ) {
			if ( th.getName().equals(callSuperMethodName) ) {
				mapSuper.put(desc, th);
				return th;
			}
		}
		mapSuper.put(desc, null);
		return null;
	}
}
