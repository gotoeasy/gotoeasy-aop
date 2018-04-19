package top.gotoeasy.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import top.gotoeasy.framework.aop.util.AopUtil;

/**
 * 切入点类
 * @since 2018/04
 * @author 青松
 */
public class AroundPoint {

	private Object								target;
	private Class<?>							superClass;
	private Object[]							args;

	private String								desc;

	private Method								method;
	private Method								callSuper;

	private Throwable							throwable;

	private static final Map<String, Method>	mapMethod	= new HashMap<>();
	private static final Map<String, Method>	mapSuper	= new HashMap<>();

	/**
	 * 构造方法
	 * @param target 代理对象
	 * @param superClass 被代理类
	 * @param desc 被代理方法的描述 {@link Method#toGenericString()}
	 * @param args 参数
	 */
	public AroundPoint(Object target, Class<?> superClass, String desc, Object ... args) {
		this.target = target;
		this.superClass = superClass;
		this.desc = desc;
		this.args = args;

		this.method = getMethodByDesc();
	}

	/**
	 * 构造方法
	 * @param target 代理对象
	 * @param superClass 被代理类
	 * @param desc 被代理方法的描述 {@link Method#toGenericString()}
	 * @param throwable 异常
	 * @param args 参数
	 */
	public AroundPoint(Object target, Class<?> superClass, String desc, Throwable throwable, Object ... args) {
		this.target = target;
		this.superClass = superClass;
		this.desc = desc;
		this.throwable = throwable;
		this.args = args;

		this.method = getMethodByDesc();
	}

	/**
	 * 按原参数调用父类同一方法
	 * @return 处理结果
	 */
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

	/**
	 * 按指定参数调用父类同一方法
	 * <p>
	 * 参数需自行保证匹配
	 * </p>
	 * @param args 参数
	 * @return 处理结果
	 */
	public Object invokeSuperWithArgs(Object ... args) {
		try {
			if ( callSuper == null ) {
				callSuper = getCallSuperMethod();
			}
			return callSuper.invoke(target, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 方法描述
	 * @return 被代理方法的描述 {@link Method#toGenericString()}
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * 代理对象
	 * @return 代理对象
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * 被代理类
	 * @return 被代理类
	 */
	public Class<?> getSuperClass() {
		return superClass;
	}

	/**
	 * 被代理方法的参数
	 * @return 参数
	 */
	public Object[] getArgs() {
		return args;
	}

	/**
	 * 被代理方法
	 * @return 被代理方法
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * 异常对象
	 * @return 异常对象
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * 按描述取得被代理方法
	 * @return 被代理方法
	 */
	private Method getMethodByDesc() {
		if ( mapMethod.containsKey(desc) ) {
			return mapMethod.get(desc);
		}

		for ( Method th : top.gotoeasy.framework.aop.test.Test.class.getDeclaredMethods() ) {
			if ( th.toGenericString().equals(desc) ) {
				mapMethod.put(desc, th);
				return th;
			}
		}

		return null;
	}

	/**
	 * 按描述取得被代理方法的调用方法
	 * @return 调用父类同一方法的方法
	 */
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
