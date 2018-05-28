package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;

/**
 * 方法代码信息类
 * 
 * @author 青松
 * @since 2018/04
 */
public class DataMethodSrcInfo {

    protected Method   method;
    protected String   varMethod;
    protected String   varSuperInvoker;
    protected String   varAopObj;
    protected String   aopMethodName;
    protected Method   aopMethod;
    protected Class<?> aopMethodReturnType;
    protected int      aopOrder;
}
