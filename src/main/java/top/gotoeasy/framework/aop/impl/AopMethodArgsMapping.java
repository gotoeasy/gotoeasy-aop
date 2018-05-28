package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.util.CmnString;

public class AopMethodArgsMapping {

    private DataBuilderVars dataBuilderVars;

    public AopMethodArgsMapping(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
    }

    /**
     * 针对拦截处理方法的前5个参数，进行自动匹配并入参
     * 
     * @param method 被拦截的目标方法
     * @param aopMethod 拦截处理方法
     * @param aopType 拦截类型（Around/Before/After/Throwing/Last）
     * @param varMethodName 目标方法变量名
     * @param varSuperInvokerName 原方法调用器变量名
     * @param varExceptionName 异常对象变量名
     * @return 入参代码片段StringBuilder
     */
    public StringBuilder mappingArgs(Method method, Method aopMethod, String aopType, String varMethodName, String varSuperInvokerName,
            String varExceptionName) {

        StringBuilder buf = new StringBuilder();
        Class<?>[] aopParamClass = aopMethod.getParameterTypes();
        Class<?> paramClass;
        for ( int i = 0; i < aopParamClass.length && i < 5; i++ ) {
            paramClass = aopParamClass[i];
            String var = null;
            if ( Enhance.class.isAssignableFrom(paramClass) ) {
                var = "this";
            } else if ( Method.class.isAssignableFrom(paramClass) ) {
                var = varMethodName;
            } else if ( SuperInvoker.class.isAssignableFrom(paramClass) ) {
                var = varSuperInvokerName;
            } else if ( AopContext.class.isAssignableFrom(paramClass) ) {
                var = "context";
                setMethodContextInfo(method, aopType);
            } else if ( Exception.class.isAssignableFrom(paramClass) ) {
                var = varExceptionName;
            }

            if ( var != null ) {
                if ( buf.length() > 0 ) {
                    buf.append(", ");
                }
                buf.append(var);
            }
        }

        return buf;
    }

    private void setMethodContextInfo(Method method, String aopType) {
        // aopType: 拦截类型（Around/Before/After/Throwing/Last）
        String codes = CmnString.nullToBlank(dataBuilderVars.aopContextMap.get(method));
        if ( !Around.class.getSimpleName().equals(aopType) && !codes.contains(aopType) ) {
            dataBuilderVars.aopContextMap.put(method, codes + aopType);
        }
    }

}
