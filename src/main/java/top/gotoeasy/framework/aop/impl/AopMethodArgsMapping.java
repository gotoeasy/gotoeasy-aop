package top.gotoeasy.framework.aop.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * AOP调用参数匹配类
 * 
 * @author 青松
 * @since 2018/04
 */
public class AopMethodArgsMapping {

    private DataBuilderVars dataBuilderVars;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public AopMethodArgsMapping(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
    }

    /**
     * 针对拦截处理方法的前5个参数，进行自动匹配并入参
     * 
     * @param method 被拦截的目标方法
     * @param aopMethod 拦截处理方法
     * @param aopClass 拦截注解类
     * @param varMethodName 目标方法变量名
     * @param varSuperInvokerName 原方法调用器变量名
     * @param varExceptionName 异常对象变量名
     * @return 入参代码片段StringBuilder
     */
    public StringBuilder mappingArgs(Method method, Method aopMethod, Class<? extends Annotation> aopClass, String varMethodName,
            String varSuperInvokerName, String varExceptionName) {

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
                dataBuilderVars.argMethodMap.put(method, dataBuilderVars.methodFieldMap.get(method));
            } else if ( SuperInvoker.class.isAssignableFrom(paramClass) ) {
                var = varSuperInvokerName;
            } else if ( AopContext.class.isAssignableFrom(paramClass) ) {
                var = "context";
                setMethodContextInfo(method, aopClass.getSimpleName());
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
