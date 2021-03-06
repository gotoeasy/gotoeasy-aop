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
     * @param max 中间类最大序号
     * @return 构造方法代码
     */
    public StringBuilder getConstructorSrc(int max) {
        StringBuilder buf = new StringBuilder();
        String simpleClassName = AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, max);

        StringBuilder bufMethodVars = getInitMethodVarsSrc();
        if ( bufMethodVars.length() > 0 || dataBuilderVars.constructor != null ) {
            buf.append(TAB1).append("public ").append(simpleClassName).append("(").append(AopUtil.getParameterDefines(dataBuilderVars.constructor))
                    .append("){").append("\n");
            buf.append(TAB2).append("super(").append(AopUtil.getParameterNames(dataBuilderVars.constructor)).append(");").append("\n");

            buf.append(getInitMethodVarsSrc());

            buf.append(TAB1).append("}").append("\n");
        }

        return buf;
    }

    /**
     * 中间类构造方法代码生成
     * 
     * @param max 中间类最大序号
     * @param seq 序号
     * @param sbCode 代码
     * @return 构造方法代码
     */
    public StringBuilder getConstructorSrc(int max, int seq, StringBuilder sbCode) {
        StringBuilder buf = new StringBuilder();
        String simpleClassName = AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, seq);

        if ( sbCode.length() > 0 ) {
            buf.append(TAB1).append("public ").append(simpleClassName).append("(").append(AopUtil.getParameterDefines(dataBuilderVars.constructor))
                    .append("){").append("\n");
            buf.append(TAB2).append("super(").append(AopUtil.getParameterNames(dataBuilderVars.constructor)).append(");").append("\n");
            buf.append(sbCode);
            buf.append(TAB1).append("}").append("\n");
        }

        return buf;
    }

    private StringBuilder getInitMethodVarsSrc() {
        StringBuilder buf = new StringBuilder();
        dataBuilderVars.argMethodMap.keySet().forEach(method -> {
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
