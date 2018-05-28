package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class DataAopInfo extends DataAnnoInfo {

    protected boolean                          isAround;
    protected Map<Method, List<DataMethodSrcInfo>> methodSrcInfoMap = null;

}
