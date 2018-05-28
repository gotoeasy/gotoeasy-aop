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
