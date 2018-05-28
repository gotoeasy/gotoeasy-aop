package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.aop.util.AopUtil;

public class Src21NormalMethodCreater {

    private String               TAB1 = "    ";
    private String               TAB2 = TAB1 + TAB1;

    private DataBuilderVars      dataBuilderVars;
    private AopMethodArgsMapping aopMethodArgsMapping;

    public Src21NormalMethodCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.aopMethodArgsMapping = new AopMethodArgsMapping(dataBuilderVars);
    }

    public StringBuilder getNormalMethodSrc() {
        // ---------------------------------- --------------------------------------------------
        //  @Override
        //  public .....
        //      if  varMethod == null  
        //          {varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes})
        //      {varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        //      {returnType} rs = super.{methodName}({parameterNames})
        //      {varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        //      return rs
        // ---------------------------------- --------------------------------------------------
        StringBuilder sbNormalMethod = new StringBuilder();
        dataBuilderVars.methodNormalAopMap.keySet().forEach(method -> {

            StringBuilder sbBeforeSrc = new Src10BeforeCreater(dataBuilderVars).getBeforeSrc(method);
            StringBuilder sbAfterSrc = new Src12AfterCreater(dataBuilderVars).getAfterSrc(method);
            StringBuilder sbThrowingSrc = new Src13ThrowingCreater(dataBuilderVars).getThrowingSrc(method);
            StringBuilder sbLastSrc = new Src14LastCreater(dataBuilderVars).getLastSrc(method);
            boolean hasAfter = sbAfterSrc.length() > 0;
            boolean hasAopContext = dataBuilderVars.aopContextMap.containsKey(method);
            boolean hasAfterUseContext = hasAopContext && dataBuilderVars.aopContextMap.get(method).contains(After.class.getSimpleName());
            boolean hasThrowingUseContext = hasAopContext && dataBuilderVars.aopContextMap.get(method).contains(Throwing.class.getSimpleName());
            boolean hasLastUseContext = hasAopContext && dataBuilderVars.aopContextMap.get(method).contains(Last.class.getSimpleName());
            boolean hasContextResult = hasAfterUseContext || hasThrowingUseContext || hasLastUseContext;

            sbNormalMethod.append(TAB1).append("@Override").append("\n");
            sbNormalMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "final")).append(" {\n");

            // 使用方法参数时，初始化
            if ( dataBuilderVars.hasUseMethodArg(method) ) {
                sbNormalMethod.append(TAB2).append("if (").append(dataBuilderVars.methodFieldMap.get(method)).append(" == null ) ");
                sbNormalMethod.append(dataBuilderVars.methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
                        .append("\"");
                if ( AopUtil.hasParameters(method) ) {
                    sbNormalMethod.append(", ");
                }
                sbNormalMethod.append(AopUtil.getParameterTypes(method)).append(");\n").append("\n");
            }

            // 使用上下文时，初始化
            if ( hasAopContext ) {
                sbNormalMethod.append(TAB2).append("AopContext context = new AopContext(System.currentTimeMillis());").append("\n");
            }

            // Before
            sbNormalMethod.append(sbBeforeSrc);

            boolean hasTry = dataBuilderVars.methodThrowingSrcInfoMap.containsKey(method) || dataBuilderVars.methodLastSrcInfoMap.containsKey(method);
            if ( hasTry ) {
                sbNormalMethod.append("try {").append("\n");
            }

            // Before后的代码块
            setMethodBlockSrc(method, sbNormalMethod, sbAfterSrc, hasAfter, hasContextResult, hasAfterUseContext);

            // 异常块语句生成
            setThrowingLastSrc(method, sbNormalMethod, sbThrowingSrc, sbLastSrc);

            sbNormalMethod.append(TAB1).append("}\n\n");
        });

        return sbNormalMethod;
    }

    // 方法代码块
    private void setMethodBlockSrc(Method method, StringBuilder sbNormalMethod, StringBuilder sbAfterSrc, boolean hasAfter, boolean hasContextResult,
            boolean hasAfterUseContext) {

        if ( void.class.equals(method.getReturnType()) ) {
            // 无返回值
            sbNormalMethod.append(TAB2).append("super.").append(method.getName()).append("(").append(AopUtil.getParameterNames(method, null))
                    .append(");\n");
            sbNormalMethod.append(sbAfterSrc); // After
        } else {
            // 有返回值
            if ( hasAfter ) {
                setAfterBlockSrc(method, sbNormalMethod, sbAfterSrc, hasContextResult, hasAfterUseContext);
            } else {
                if ( hasContextResult ) {
                    // 没有After，但有Throwing或Last要用到context
                    sbNormalMethod.append(TAB2).append(method.getReturnType().getName()).append(" rs = super.").append(method.getName()).append("(")
                            .append(AopUtil.getParameterNames(method, null)).append(");\n");
                    sbNormalMethod.append(TAB2).append("context.setResult(rs);").append("\n");

                    sbNormalMethod.append(sbAfterSrc); // After
                    sbNormalMethod.append(TAB2).append("return rs;").append("\n");
                } else {
                    // 没有After，没有Throwing或Last要用到context，直接返回
                    sbNormalMethod.append(TAB2).append("return super.").append(method.getName()).append("(")
                            .append(AopUtil.getParameterNames(method, null)).append(");\n");
                }
            }
        }

    }

    // after块部分的代码
    private void setAfterBlockSrc(Method method, StringBuilder sbNormalMethod, StringBuilder sbAfterSrc, boolean hasContextResult,
            boolean hasAfterUseContext) {
        String returnType = "";
        if ( !method.getReturnType().equals(Object.class) ) {
            // 返回类型不同时需要强制转换
            returnType = "(" + method.getReturnType().getName() + ")";
        }

        sbNormalMethod.append(TAB2).append(method.getReturnType().getName()).append(" rs = super.").append(method.getName()).append("(")
                .append(AopUtil.getParameterNames(method, null)).append(");\n");
        if ( hasContextResult ) {
            sbNormalMethod.append(TAB2).append("context.setResult(rs);").append("\n");
        }
        sbNormalMethod.append(sbAfterSrc); // After

        if ( hasAfterUseContext && hasContextResult ) {
            sbNormalMethod.append(TAB2).append("return ").append(returnType).append("context.getResult();").append("\n");
        } else {
            sbNormalMethod.append(TAB2).append("return rs;").append("\n");
        }
    }

    // 异常块语句生成
    private void setThrowingLastSrc(Method method, StringBuilder sbNormalMethod, StringBuilder sbThrowingSrc, StringBuilder sbLastSrc) {
        if ( dataBuilderVars.methodThrowingSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} catch (Exception ex) {").append("\n");
            sbNormalMethod.append(sbThrowingSrc); // Throwing
            sbNormalMethod.append("throw new RuntimeException(ex);").append("\n");
        }
        if ( dataBuilderVars.methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} finally {").append("\n");
            sbNormalMethod.append(sbLastSrc); // Last
        }
        if ( dataBuilderVars.methodThrowingSrcInfoMap.containsKey(method) || dataBuilderVars.methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("}").append("\n");
        }
        sbNormalMethod.append("\n");
    }

}
