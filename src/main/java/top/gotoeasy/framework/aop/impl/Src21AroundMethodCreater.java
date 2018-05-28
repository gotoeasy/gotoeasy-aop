package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.util.List;

import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * Around拦截代码块生成类
 * 
 * @author 青松
 * @since 2018/04
 */
public class Src21AroundMethodCreater {

    private String               TAB1 = "    ";
    private String               TAB2 = TAB1 + TAB1;

    private DataBuilderVars      dataBuilderVars;
    private AopMethodArgsMapping aopMethodArgsMapping;

    public Src21AroundMethodCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.aopMethodArgsMapping = new AopMethodArgsMapping(dataBuilderVars);
    }

    public StringBuilder getAroundMethodSrc(int seq) {
        StringBuilder buf = new StringBuilder();
        dataBuilderVars.methodAroundSuperList.forEach(method -> {
            List<DataMethodSrcInfo> list = dataBuilderVars.methodAroundSrcInfoMap.get(method);
            if ( seq < list.size() ) {
                buf.append(getAroundMethodSrc(method, seq));
            }
        });
        return buf;
    }

    /**
     * Around拦截源码
     * 
     * @param isMiddleClass 是否中间类
     * @return StringBuilder
     */
    public StringBuilder getAroundMethodSrc(boolean isMiddleClass) {
        // ---------------------------------- --------------------------------------------------
        //  @Override
        //  public .....
        //      if varMethod == null
        //          {varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes})
        //          {varSuperInvoker} = (method, args) -> super.{methodName}({args})
        //      return {returnType}{varAopObj}.{aopMethodName}(this, {varMethod}, {varSuperInvoker}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder sbAroundMethod = new StringBuilder();
        dataBuilderVars.methodAroundSrcInfoMap1.keySet().forEach(method -> {
            if ( isMiddleClass && !dataBuilderVars.methodAroundSuperList.contains(method)
                    || !isMiddleClass && dataBuilderVars.methodAroundSuperList.contains(method) ) {
                return;
            }

            boolean hasReturn = !void.class.equals(method.getReturnType());
            DataMethodSrcInfo info = dataBuilderVars.methodAroundSrcInfoMap1.get(method);
            sbAroundMethod.append(TAB1).append("@Override").append("\n");
            sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "")).append(" {\n");

            // superInvoker$abc变量初始化
            if ( hasReturn ) {
                sbAroundMethod.append(TAB2).append("if (").append(info.varSuperInvoker).append(" == null ) ");
                sbAroundMethod.append(info.varSuperInvoker).append(" = (args) -> super.").append(method.getName()).append("(")
                        .append(AopUtil.getLambdaArgs(method)).append(");").append("\n");
            } else {
                sbAroundMethod.append(TAB2).append("if (").append(info.varSuperInvoker).append(" == null ) ");
                sbAroundMethod.append(info.varSuperInvoker).append(" = (args) -> {super.").append(method.getName()).append("(")
                        .append(AopUtil.getLambdaArgs(method)).append("); return null;};").append("\n");
            }
            sbAroundMethod.append("\n");

            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = aopMethodArgsMapping.mappingArgs(info.method, info.aopMethod, Around.class, info.varMethod,
                    info.varSuperInvoker, "null");

            if ( !hasReturn ) {
                // 无返回值
                sbAroundMethod.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(");
                sbAroundMethod.append(sbAopMethodParams);
            } else {
                // 有返回值
                String returnType = "";
                if ( !method.getReturnType().equals(info.aopMethodReturnType) ) {
                    // 返回类型不同时需要强制转换
                    returnType = "(" + method.getReturnType().getName() + ")";
                }
                sbAroundMethod.append(TAB2).append("return ").append(returnType).append(info.varAopObj).append(".").append(info.aopMethodName)
                        .append("(");
                sbAroundMethod.append(sbAopMethodParams);
            }

            String parameterNames = AopUtil.getParameterNames(method, info.aopMethod);
            if ( CmnString.isNotBlank(parameterNames) ) {
                if ( sbAopMethodParams.length() > 0 ) {
                    sbAroundMethod.append(", ");
                }
                sbAroundMethod.append(parameterNames);
            }
            sbAroundMethod.append(");\n");

            sbAroundMethod.append(TAB1).append("}\n\n");
        });

        return sbAroundMethod;
    }

    /**
     * Around拦截源码
     * 
     * @param isMiddleClass 是否中间类
     * @return StringBuilder
     */
    private StringBuilder getAroundMethodSrc(Method method, int seq) {

        boolean hasReturn = !void.class.equals(method.getReturnType());
        // ---------------------------------- --------------------------------------------------
        //  @Override
        //  public .....
        //      if varMethod == null
        //          {varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes})
        //          {varSuperInvoker} = (method, args) -> super.{methodName}({args})
        //      return {returnType}{varAopObj}.{aopMethodName}(this, {varMethod}, {varSuperInvoker}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder sbAroundMethod = new StringBuilder();
        DataMethodSrcInfo info = dataBuilderVars.methodAroundSrcInfoMap.get(method).get(seq);

        sbAroundMethod.append(TAB1).append("@Override").append("\n");
        sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "")).append(" {\n");
        // 使用方法参数时，初始化
        if ( dataBuilderVars.hasUseMethodArg(method) ) {
            sbAroundMethod.append(TAB2).append("if (").append(dataBuilderVars.methodFieldMap.get(method)).append(" == null ) ");
            sbAroundMethod.append(dataBuilderVars.methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
                    .append("\"");
            String parameterTypes = AopUtil.getParameterTypes(method);
            if ( CmnString.isNotBlank(parameterTypes) ) {
                sbAroundMethod.append(", ").append(parameterTypes);
            }
            sbAroundMethod.append(");\n");
        }
        // superInvoker$abc变量初始化
        if ( hasReturn ) {
            sbAroundMethod.append(TAB2).append("if (").append(info.varSuperInvoker).append(" == null ) ");
            sbAroundMethod.append(info.varSuperInvoker).append(" = (args) -> super.").append(method.getName()).append("(")
                    .append(AopUtil.getLambdaArgs(method)).append(");").append("\n");
        } else {
            sbAroundMethod.append(TAB2).append("if (").append(info.varSuperInvoker).append(" == null ) ");
            sbAroundMethod.append(info.varSuperInvoker).append(" = (args) -> {super.").append(method.getName()).append("(")
                    .append(AopUtil.getLambdaArgs(method)).append("); return null;};").append("\n");
        }
        sbAroundMethod.append("\n");

        // 前5个参数判断类型自动入参
        StringBuilder sbAopMethodParams = aopMethodArgsMapping.mappingArgs(info.method, info.aopMethod, Around.class, info.varMethod,
                info.varSuperInvoker, "null");

        if ( hasReturn ) {
            // 有返回值
            String returnType = "";
            if ( !method.getReturnType().equals(info.aopMethodReturnType) ) {
                // 返回类型不同时需要强制转换
                returnType = "(" + method.getReturnType().getName() + ")";
            }
            sbAroundMethod.append(TAB2).append("return ").append(returnType).append(info.varAopObj).append(".").append(info.aopMethodName)
                    .append("(");
            sbAroundMethod.append(sbAopMethodParams);
        } else {
            // 无返回值
            sbAroundMethod.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(");
            sbAroundMethod.append(sbAopMethodParams);
        }

        String parameterNames = AopUtil.getParameterNames(method, info.aopMethod);
        if ( CmnString.isNotBlank(parameterNames) ) {
            if ( sbAopMethodParams.length() > 0 ) {
                sbAroundMethod.append(", ");
            }
            sbAroundMethod.append(parameterNames);
        }
        sbAroundMethod.append(");\n");

        sbAroundMethod.append(TAB1).append("}\n\n");

        return sbAroundMethod;
    }
}
