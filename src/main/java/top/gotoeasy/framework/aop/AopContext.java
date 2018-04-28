package top.gotoeasy.framework.aop;

/**
 * AOP拦截上下文类
 * <p>
 * 存取被拦截方法的处理结果，用于后置拦截对处理结果进行编辑<br>
 * 存取被拦截方法的执行开始时间，用于后置拦截对处理时间进行统计
 * </p>
 * 
 * @author 青松
 * @since 2018/04
 */
public class AopContext {

    private long   startTime;
    private Object result;

    /**
     * 构造方法
     * 
     * @param startTime 被拦截方法的执行开始时间
     */
    public AopContext(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 被拦截方法的处理结果
     * <p>
     * 用于后置拦截对处理结果进行编辑
     * </p>
     * 
     * @return 被拦截方法的处理结果
     */
    public Object getResult() {
        return result;
    }

    /**
     * 被拦截方法的处理结果
     * <p>
     * 用于后置拦截对处理结果进行编辑
     * </p>
     * 
     * @param result 被拦截方法的处理结果
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * 被拦截方法的执行开始时间
     * <p>
     * 用于后置拦截对处理时间进行统计
     * </p>
     * 
     * @return 被拦截方法的执行开始时间
     */
    public long getStartTime() {
        return startTime;
    }

}
