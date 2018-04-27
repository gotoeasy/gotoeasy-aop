package top.gotoeasy.framework.aop;

/**
 * AOP拦截上下文接口
 * <p>
 * 存取被拦截方法的处理结果，用于后置拦截对处理结果进行编辑<br>
 * 存取被拦截方法的执行开始时间，用于后置拦截对处理时间进行统计
 * </p>
 * 
 * @author 青松
 * @since 2018/04
 */
public interface AopContext {

    /**
     * 被拦截方法的处理结果
     * <p>
     * 用于后置拦截对处理结果进行编辑
     * </p>
     * 
     * @return 被拦截方法的处理结果
     */
    public Object getResult();

    /**
     * 被拦截方法的处理结果
     * <p>
     * 用于后置拦截对处理结果进行编辑
     * </p>
     * 
     * @param result 被拦截方法的处理结果
     */
    public void setResult(Object result);

    /**
     * 被拦截方法的执行开始时间
     * <p>
     * 用于后置拦截对处理时间进行统计
     * </p>
     * 
     * @return 被拦截方法的执行开始时间
     */
    public long getStartTime();

    /**
     * 被拦截方法的执行开始时间
     * <p>
     * 用于后置拦截对处理时间进行统计
     * </p>
     * 
     * @param startTime 被拦截方法的执行开始时间
     */
    public void setStartTime(long startTime);

}
