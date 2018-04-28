package top.gotoeasy.framework.aop.exception;

/**
 * AOP模块异常
 * 
 * @since 2018/03
 * @author 青松
 */
public class AopException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造方法
     */
    public AopException() {
        super();
    }

    /**
     * 构造方法
     * 
     * @param message 消息
     */
    public AopException(String message) {
        super(message);
    }

    /**
     * 构造方法
     * 
     * @param message 消息
     * @param cause 异常
     */
    public AopException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造方法
     * 
     * @param cause 异常
     */
    public AopException(Throwable cause) {
        super(cause);
    }
}
