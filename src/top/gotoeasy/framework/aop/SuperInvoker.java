package top.gotoeasy.framework.aop;

/**
 * 父类方法调用器
 * <p>
 * 继承增强时，提供父类相同方法名调用的接口<br>
 * </p>
 * @since 2018/04
 * @author 青松
 */
@FunctionalInterface
public interface SuperInvoker {

	/**
	 * 调用原父类方法
	 * @param args 参数
	 * @return 结果
	 */
	public Object invoke(Object ... args);

}
