package top.gotoeasy.framework.aop.impl;

import java.lang.annotation.Annotation;

/**
 * AOP拦截处理的注解信息类
 * 
 * @author 青松
 * @since 2018/04
 */
public class DataAnnoInfo {

    protected String                        annoValue;
    protected String[]                      annoPackages;
    protected Class<? extends Annotation>[] annoTypeAnnotations;
    protected Class<?>[]                    annoClasses;
    protected Class<? extends Annotation>[] annoMethodAnnotations;
    protected boolean                       annoMatchSuperMethod;
    protected boolean                       annoMatchEquals;
    protected boolean                       annoMatchToString;
    protected boolean                       annoMatchHashCode;
    protected int                           annoOrder;

}
