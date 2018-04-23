package top.gotoeasy.framework.aop;

@FunctionalInterface
public interface SuperInvoker {

	public Object invoke(Object ... args);

}
