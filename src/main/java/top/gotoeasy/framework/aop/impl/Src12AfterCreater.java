package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.util.List;

import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * After拦截代码块生成类
 * 
 * @author 青松
 * @since 2018/04
 */
public class Src12AfterCreater {

    private static final String  TAB2 = "        ";

    private DataBuilderVars      dataBuilderVars;
    private AopMethodArgsMapping aopMethodArgsMapping;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public Src12AfterCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        aopMethodArgsMapping = new AopMethodArgsMapping(dataBuilderVars);
    }

    /**
     * After拦截代码块生成
     * 
     * @param method 方法
     * @return After拦截代码块
     */
    public StringBuilder getAfterSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //      {varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<DataMethodSrcInfo> list = dataBuilderVars.methodAfterSrcInfoMap.get(method);
        if ( list == null ) {
            return buf;
        }

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( DataMethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = aopMethodArgsMapping.mappingArgs(info.method, info.aopMethod, After.class, info.varMethod, "null",
                    "null");

            buf.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(").append(sbAopMethodParams);
            String parameterNames = AopUtil.getParameterNames(method, info.aopMethod);
            if ( CmnString.isNotBlank(parameterNames) ) {
                if ( sbAopMethodParams.length() > 0 ) {
                    buf.append(", ");
                }
                buf.append(parameterNames);
            }
            buf.append(");\n");
        }

        return buf;
    }

}
