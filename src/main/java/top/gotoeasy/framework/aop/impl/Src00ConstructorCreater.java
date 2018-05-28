package top.gotoeasy.framework.aop.impl;

import top.gotoeasy.framework.aop.util.AopUtil;

/**
 * 构造方法代码生成类
 * 
 * @author 青松
 * @since 2018/04
 */
public class Src00ConstructorCreater {

    private static final String TAB1 = "    ";
    private static final String TAB2 = TAB1 + TAB1;

    private DataBuilderVars     dataBuilderVars;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public Src00ConstructorCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
    }

    /**
     * 构造方法代码生成
     * 
     * @return 构造方法代码
     */
    public StringBuilder getConstructorSrc(int max, int seq) {
        StringBuilder buf = new StringBuilder();
        String simpleClassName = AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, seq);

        if ( max == seq || dataBuilderVars.constructor != null ) {
            buf.append(TAB1).append("public ").append(simpleClassName).append("(").append(AopUtil.getParameterDefines(dataBuilderVars.constructor))
                    .append("){").append("\n");
            buf.append(TAB2).append("super(").append(AopUtil.getParameterNames(dataBuilderVars.constructor)).append(");").append("\n");

            if ( max == seq ) {
                buf.append(getInitMethodVarsSrc());
            }

            buf.append(TAB1).append("}").append("\n");
        }

        return buf;
    }

    private StringBuilder getInitMethodVarsSrc() {
        StringBuilder buf = new StringBuilder();
        dataBuilderVars.argMethodList.forEach(method -> {
            buf.append(TAB2).append(dataBuilderVars.methodFieldMap.get(method));
            buf.append(" = AopUtil.getMethod(this, \"").append(method.getName()).append("\"");
            if ( AopUtil.hasParameters(method) ) {
                buf.append(", ");
            }
            buf.append(AopUtil.getParameterTypes(method)).append(");").append("\n");
        });
        return buf;
    }

}
