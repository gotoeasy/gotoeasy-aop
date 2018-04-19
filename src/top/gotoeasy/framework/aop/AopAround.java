package top.gotoeasy.framework.aop;

/**
 * 环绕拦截处理接口
 * @since 2018/04
 * @author 青松
 */
public interface AopAround {

	/**
	 * 环绕拦截处理
	 * @param point 切入点
	 * @return 处理结果
	 */
	public Object around(AroundPoint point);

}
