package top.gotoeasy.framework.aop;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;

import top.gotoeasy.framework.aop.impl.DataBuilderVars;
import top.gotoeasy.framework.aop.impl.ProxyObjectCreater;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * 
 * @author 青松
 * @since 2018/04
 */
public class EnhanceBuilder {

    private static final Log log             = LoggerFactory.getLogger(EnhanceBuilder.class);

    private DataBuilderVars  dataBuilderVars = new DataBuilderVars();

    /**
     * 生成创建器
     * 
     * @return 创建器
     */
    public static EnhanceBuilder get() {
        return new EnhanceBuilder();
    }

    /**
     * 设定被代理类
     * 
     * @param <T> 被代理类
     * @param clas 被代理类
     * @return 创建器
     */
    public <T> EnhanceBuilder setSuperclass(Class<T> clas) {
        if ( Modifier.isFinal(clas.getModifiers()) ) {
            log.warn("无法通过继承来增强的final类：{}" + clas);
        }

        dataBuilderVars.clas = clas;
        return this;
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     * 
     * @param list 拦截处理对象列表
     * @return 创建器
     */
    public EnhanceBuilder matchAopList(List<Object> list) {
        dataBuilderVars.aopList.addAll(list);
        return this;
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     * 
     * @param aops 拦截处理对象
     * @return 创建器
     */
    public EnhanceBuilder matchAop(Object ... aops) {
        for ( Object aopObj : aops ) {
            dataBuilderVars.aopList.add(aopObj);
        }
        return this;
    }

    /**
     * 指定构造方法
     * 
     * @param constructor 构造方法
     * @param initargs 构造方法参数
     * @return 创建器
     */
    public EnhanceBuilder setConstructorArgs(Constructor<?> constructor, Object ... initargs) {
        if ( constructor != null ) {
            dataBuilderVars.constructor = constructor;
            dataBuilderVars.initargs = initargs;
        }
        return this;
    }

    /**
     * 创建代理对象
     * 
     * @param <T> 被代理类
     * @return 代理对象
     */
    public <T> T build() {
        return new ProxyObjectCreater(dataBuilderVars).create();
    }

}
