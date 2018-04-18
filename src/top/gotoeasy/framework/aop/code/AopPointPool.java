package top.gotoeasy.framework.aop.code;

import java.util.ArrayList;
import java.util.List;

public class AopPointPool {

	private static List<AopPoint> list = new ArrayList<>();

//	public static AopPoint create(Object target, Class<?> superClass, String desc, Throwable throwable, Object ... args) {
//		AopPoint point;
//		if ( !list.isEmpty() ) {
//			point = list.remove(0);
//			point.init(target, superClass, desc, throwable, args);
//			return point;
//		}
//		point = new AopPoint();
//		point.init(target, superClass, desc, throwable, args);
//		return point;
//	}

	public static AopPoint create(Object target, Class<?> superClass, String desc, Object ... args) {
		AopPoint point;
		if ( !list.isEmpty() ) {
			point = list.remove(0);
			point.init(target, superClass, desc, args);
			return point;
		}
		point = new AopPoint();
		point.init(target, superClass, desc, args);
		return point;
	}

	public static void back(AopPoint point) {
		point.reset();
		list.add(point);
	}
}
