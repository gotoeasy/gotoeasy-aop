package top.gotoeasy.framework.aop.impl;

import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

/**
 * 增强类代码生成类
 * 
 * @author 青松
 * @since 2018/04
 */
public class Src91EnhanceClassCreater {

    private static final Log         log  = LoggerFactory.getLogger(Src91EnhanceClassCreater.class);

    private static final String      TAB1 = "    ";

    private DataBuilderVars          dataBuilderVars;
    private Src00ConstructorCreater  src00ConstructorCreater;
    private Src22NormalMethodCreater src22NormalMethodCreater;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public Src91EnhanceClassCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.src00ConstructorCreater = new Src00ConstructorCreater(dataBuilderVars);
        this.src22NormalMethodCreater = new Src22NormalMethodCreater(dataBuilderVars);
    }

    /**
     * 创建代理类源码
     * 
     * @return 代理类源码
     */
    public String createEnhanceClassCode() {
        StringBuilder sbMethodField = new StringBuilder();
        StringBuilder sbSuperInvokerField = new StringBuilder();
        StringBuilder sbAopField = new StringBuilder();

        // 没有中间类的时候添加全局变量，否则全局变量全部放在中间类中
        if ( dataBuilderVars.methodAroundSuperSet.isEmpty() ) {
            // private Method varMethod
            dataBuilderVars.methodFieldMap.keySet().forEach(
                    method -> sbMethodField.append(TAB1).append("private Method ").append(dataBuilderVars.methodFieldMap.get(method)).append(";\n"));
//            // private SuperInvoker varSuperInvoker
//            dataBuilderVars.superInvokerFieldMap.keySet().forEach(key -> sbSuperInvokerField.append(TAB1).append("private SuperInvoker ")
//                    .append(dataBuilderVars.superInvokerFieldMap.get(key)).append(";\n"))
            // public {aopClass} varAopObj
            dataBuilderVars.aopObjFieldMap.keySet().forEach(aopObj -> sbAopField.append(TAB1).append("public ").append(aopObj.getClass().getName())
                    .append(" ").append(dataBuilderVars.aopObjFieldMap.get(aopObj)).append(";\n"));
        }

        // NormalMethod
        StringBuilder sbNormalMethod = src22NormalMethodCreater.getNormalMethodSrc();
        // Class
        StringBuilder sbClass = new StringBuilder();
        // -------------------------------------------------------------------------
        //  package ....

        //  import ....
        //  import ....
        //
        //  public class superClass$$gotoeasy$$ extends superClass implements Enhance ...
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
        sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.Enhance;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.AopContext;").append("\n");
        sbClass.append("\n");
        sbClass.append("public class ").append(AopUtil.getEnhanceSimpleName(dataBuilderVars.clas)).append(" extends ");
        int max = dataBuilderVars.getMaxMethodAroundCount();
        if ( dataBuilderVars.methodAroundSuperSet.isEmpty() ) {
            sbClass.append(dataBuilderVars.clas.getSimpleName()).append(" implements Enhance {");
        } else {
            sbClass.append(AopUtil.getAroundMiddleClassSimpleName(dataBuilderVars.clas, max, max - 1)).append(" {");
        }
        sbClass.append("\n");
        sbClass.append(sbMethodField);
        sbClass.append(sbSuperInvokerField);
        sbClass.append(sbAopField);
        sbClass.append("\n");
        sbClass.append(src00ConstructorCreater.getConstructorSrc(max));
        sbClass.append("\n");
        sbClass.append(sbNormalMethod);
        sbClass.append("\n");
        sbClass.append("}").append("\n");

        String srcCode = sbClass.toString();
        log.trace("\n{}", srcCode);
        return srcCode;
    }

}
