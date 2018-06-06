package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.util.List;

import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * Around拦截方法代码生成类
 * 
 * @author 青松
 * @since 2018/04
 */
public class Src21AroundMethodCreater {

    private static final String  TAB1 = "    ";
    private static final String  TAB2 = TAB1 + TAB1;

    private DataBuilderVars      dataBuilderVars;
    private AopMethodArgsMapping aopMethodArgsMapping;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public Src21AroundMethodCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.aopMethodArgsMapping = new AopMethodArgsMapping(dataBuilderVars);
    }

    /**
     * Around拦截方法代码生成
     * 
     * @param seq 当前序号
     * @return Around拦截方法代码
     */
    public StringBuilder getAroundMethodSrc(int seq) {
        StringBuilder buf = new StringBuilder();
        dataBuilderVars.methodAroundSuperSet.forEach(method -> {
            List<DataMethodSrcInfo> list = dataBuilderVars.methodAroundSrcInfoMap.get(method);
            if ( seq < list.size() ) {
                buf.append(getAroundMethodSrc(method, seq));
            }
        });
        return buf;
    }

    /**
     * SuperInvoker变量初始化代码生成
     * 
     * @param seq 当前序号
     * @return SuperInvoker变量初始化代码
     */
    public StringBuilder getSuperInvokerInitSrc(int seq) {
        StringBuilder buf = new StringBuilder();
        dataBuilderVars.methodAroundSuperSet.forEach(method -> {
            List<DataMethodSrcInfo> list = dataBuilderVars.methodAroundSrcInfoMap.get(method);
            if ( seq < list.size() ) {
                buf.append(getSuperInvokerInitSrc(method, seq));
            }
        });
        return buf;
    }

    /**
     * Around拦截源码
     * 
     * @param method 方法
     * @param seq 序号
     * @return StringBuilder
     */
    private StringBuilder getAroundMethodSrc(Method method, int seq) {

        boolean hasReturn = !void.class.equals(method.getReturnType());
        // ---------------------------------- --------------------------------------------------
        //  @Override
        //  public .....
        //      return {returnType}{varAopObj}.{aopMethodName}(this, {varMethod}, {varSuperInvoker}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder sbAroundMethod = new StringBuilder();
        DataMethodSrcInfo info = dataBuilderVars.methodAroundSrcInfoMap.get(method).get(seq);

        sbAroundMethod.append(TAB1).append("@Override").append("\n");
        sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "")).append(" {\n");

        // 前5个参数判断类型自动入参
        StringBuilder sbAopMethodParams = aopMethodArgsMapping.mappingArgs(info.method, info.aopMethod, Around.class, info.varMethod,
                info.varSuperInvoker, "null");

        if ( hasReturn ) {
            // 有返回值
            String returnType = "";
            if ( !method.getReturnType().equals(info.aopMethodReturnType) ) {
                // 返回类型不同时需要强制转换
                returnType = "(" + method.getReturnType().getCanonicalName() + ")";
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

    /**
     * SuperInvoker变量初始化源码
     * 
     * @param method 方法
     * @param seq 序号
     * @return StringBuilder
     */
    private StringBuilder getSuperInvokerInitSrc(Method method, int seq) {

        StringBuilder sbSuperInvokerInit = new StringBuilder();
        boolean hasReturn = !void.class.equals(method.getReturnType());
        // ---------------------------------- --------------------------------------------------
        //  {varSuperInvoker} = (method, args) -> super.{methodName}({args})
        // ---------------------------------- --------------------------------------------------
        DataMethodSrcInfo info = dataBuilderVars.methodAroundSrcInfoMap.get(method).get(seq);

        // superInvoker$abc变量初始化
        if ( hasReturn ) {
            if ( method.getExceptionTypes().length > 0 ) {
                sbSuperInvokerInit.append(TAB2).append(info.varSuperInvoker).append(" = (args) -> {try{return super.").append(method.getName())
                        .append("(").append(AopUtil.getLambdaArgs(method)).append(");}catch(Exception e){throw new RuntimeException(e);}};")
                        .append("\n");
            } else {
                sbSuperInvokerInit.append(TAB2).append(info.varSuperInvoker).append(" = (args) -> super.").append(method.getName()).append("(")
                        .append(AopUtil.getLambdaArgs(method)).append(");").append("\n");
            }
        } else {
            if ( method.getExceptionTypes().length > 0 ) {
                sbSuperInvokerInit.append(TAB2).append(info.varSuperInvoker).append(" = (args) -> {try{super.").append(method.getName()).append("(")
                        .append(AopUtil.getLambdaArgs(method)).append(");return null;}catch(Exception e){throw new RuntimeException(e);}};")
                        .append("\n");
            } else {
                sbSuperInvokerInit.append(TAB2).append(info.varSuperInvoker).append(" = (args) -> {super.").append(method.getName()).append("(")
                        .append(AopUtil.getLambdaArgs(method)).append("); return null;};").append("\n");
            }
        }

        return sbSuperInvokerInit;
    }
}
