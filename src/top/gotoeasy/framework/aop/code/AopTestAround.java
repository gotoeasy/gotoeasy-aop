package top.gotoeasy.framework.aop.code;

import top.gotoeasy.framework.aop.callback.AopAround;

public class AopTestAround implements AopAround {

	@Override
	public Object around(AopPoint point) {
		// TODO Auto-generated method stub
		return point.invokeSuper();
	}

}
