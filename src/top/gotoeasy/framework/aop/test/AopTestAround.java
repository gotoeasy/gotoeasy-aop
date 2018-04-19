package top.gotoeasy.framework.aop.test;

import top.gotoeasy.framework.aop.AopAround;
import top.gotoeasy.framework.aop.AroundPoint;

public class AopTestAround implements AopAround {

	@Override
	public Object around(AroundPoint point) {
		// TODO Auto-generated method stub
		return point.invokeSuper();
	}

}
