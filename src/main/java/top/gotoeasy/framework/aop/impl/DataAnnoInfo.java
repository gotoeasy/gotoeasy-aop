package top.gotoeasy.framework.aop.impl;

import java.lang.annotation.Annotation;

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
