package top.gotoeasy.framework.aop.impl;

import top.gotoeasy.framework.aop.util.AopUtil;

public class Src00ConstructorCreater {

    private String      TAB1 = "    ";
    private String      TAB2 = TAB1 + TAB1;

    private DataBuilderVars dataBuilderVars;

    public Src00ConstructorCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
    }

    public StringBuilder getConstructorSrc() {
        StringBuilder buf = new StringBuilder();
        if ( dataBuilderVars.constructor == null ) {
            return buf;
        }

        buf.append(TAB1).append("public ").append(AopUtil.getEnhanceSimpleName(dataBuilderVars.clas)).append("(")
                .append(AopUtil.getParameterDefines(dataBuilderVars.constructor)).append("){").append("\n");
        buf.append(TAB2).append("super(").append(AopUtil.getParameterNames(dataBuilderVars.constructor)).append(");").append("\n");
        buf.append(TAB1).append("}").append("\n");
        return buf;
    }

}
