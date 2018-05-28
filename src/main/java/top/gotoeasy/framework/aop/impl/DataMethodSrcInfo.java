package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;

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
