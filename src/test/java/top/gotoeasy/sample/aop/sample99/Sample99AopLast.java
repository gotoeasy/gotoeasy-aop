package top.gotoeasy.sample.aop.sample99;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample99AopLast {

    private static final Log log = LoggerFactory.getLogger(Sample99AopLast.class);

    @Last("*.Sample99Bean.mod(*)")
    public void last(Method method) {
        log.debug("@Last 拦截成功:{}", method);
    }

    @Last(value = "*Sample99BaseBean.count2(*)", matchSuperMethod = true, matchToString = true)
    public Object last2(SuperInvoker superInvoker, Object ... objects) {
        return superInvoker.invoke(objects);
    }

    @Last(value = "*Sample99BaseBean.count2(*)", matchSuperMethod = true, matchToString = true)
    public void last3(Object ... objects) {
    }

    @Last("*.Sample99BeanConstructor*.add(*)")
    public void last() {
        log.debug("@Last 拦截成功");
    }

}
