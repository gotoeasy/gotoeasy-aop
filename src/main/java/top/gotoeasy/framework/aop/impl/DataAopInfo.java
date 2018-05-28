package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * AOP拦截处理的注解信息扩充类
 * 
 * @author 青松
 * @since 2018/04
 */
public class DataAopInfo extends DataAnnoInfo {

    protected boolean                              isAround;
    protected Map<Method, List<DataMethodSrcInfo>> methodSrcInfoMap = null;

}
