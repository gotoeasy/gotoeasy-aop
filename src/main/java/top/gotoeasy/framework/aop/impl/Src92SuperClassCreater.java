package top.gotoeasy.framework.aop.impl;

import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class Src92SuperClassCreater {

    private static final Log       log  = LoggerFactory.getLogger(Src92SuperClassCreater.class);

    private String                 TAB1 = "    ";
    private String                 TAB2 = TAB1 + TAB1;

    private DataBuilderVars            dataBuilderVars;
    private AopMethodArgsMapping   aopMethodArgsMapping;
    private Src00ConstructorCreater  src00ConstructorCreater;
    private Src11AroundMethodCreater src11AroundMethodCreater;
    private Src21NormalMethodCreater src21NormalMethodCreater;

    public Src92SuperClassCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.aopMethodArgsMapping = new AopMethodArgsMapping(dataBuilderVars);
        this.src00ConstructorCreater = new Src00ConstructorCreater(dataBuilderVars);
        this.src11AroundMethodCreater = new Src11AroundMethodCreater(dataBuilderVars);
        this.src21NormalMethodCreater = new Src21NormalMethodCreater(dataBuilderVars);
    }

    /**
     * 创建环绕拦截中间类源码
     * 
     * @param max 中间类最大数
     * @param order 中间类序号
     * @return 环绕拦截中间类源码
     */
    public String createAroundMiddleClassCode(int max, int seq) {
        StringBuilder sbMethodField = new StringBuilder();
        StringBuilder sbSuperInvokerField = new StringBuilder();
        StringBuilder sbAopField = new StringBuilder();

        if ( seq == 0 ) {
            // protected Method varMethod
            dataBuilderVars.methodFieldMap.keySet().forEach(
                    method -> sbMethodField.append(TAB1).append("protected Method ").append(dataBuilderVars.methodFieldMap.get(method)).append(";\n"));
            // protected SuperInvoker varSuperInvoker
            dataBuilderVars.superInvokerFieldMap.keySet().forEach(method -> sbSuperInvokerField.append(TAB1).append("protected SuperInvoker ")
                    .append(dataBuilderVars.superInvokerFieldMap.get(method)).append(";\n"));
            // public {aopClass} varAopObj
            dataBuilderVars.aopObjFieldMap.keySet().forEach(aopObj -> sbAopField.append(TAB1).append("public").append(" ")
                    .append(aopObj.getClass().getName()).append(" ").append(dataBuilderVars.aopObjFieldMap.get(aopObj)).append(";\n"));
        }

        // AroundMethod
        StringBuilder sbAroundMethod = src11AroundMethodCreater.getAroundMethodSrc(seq);
        // Class
        StringBuilder sbClass = new StringBuilder();
        // -------------------------------------------------------------------------
        //  package ....
        //
        //  import ....
        //  import ....
        //
        //  public class superClass$$gotoeasy$$AroundBase extends superClass implements Enhance ...
        //
        //      methodField...
        //      superInvokerField...
        //      aopObjField...
        //
        //      method...
        // -------------------------------------------------------------------------
        sbClass.append("package ").append(dataBuilderVars.clas.getPackage().getName()).append(";\n");
        sbClass.append("\n");
        sbClass.append("import java.lang.reflect.Method;").append("\n");
        sbClass.append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.Enhance;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.AopContext;").append("\n");
        sbClass.append("\n");
        if ( seq == 0 ) {
            sbClass.append("public class ").append(AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, seq)).append(" extends ")
                    .append(dataBuilderVars.clas.getSimpleName()).append(" implements Enhance {").append("\n");
        } else {
            sbClass.append("public class ").append(AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, seq)).append(" extends ")
                    .append(AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, seq - 1)).append(" {").append("\n");
        }
        sbClass.append("\n");
        sbClass.append(sbMethodField);
        sbClass.append(sbSuperInvokerField);
        sbClass.append(sbAopField);
        sbClass.append("\n");
        sbClass.append(src00ConstructorCreater.getConstructorSrc());
        sbClass.append("\n");
        sbClass.append(sbAroundMethod);
        sbClass.append("}").append("\n");

        String srcCode = sbClass.toString();
        log.trace("\n{}", srcCode);
        return srcCode;
    }
}
