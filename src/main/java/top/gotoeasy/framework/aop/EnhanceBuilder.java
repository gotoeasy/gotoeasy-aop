package top.gotoeasy.framework.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.aop.exception.AopException;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.reflect.MethodScaner;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * 
 * @author 青松
 * @since 2018/04
 */
public class EnhanceBuilder {

    private static final Log                 log                      = LoggerFactory.getLogger(EnhanceBuilder.class);

    // 拦截目标类
    private Class<?>                         clas                     = null;
    // 构造方法
    private Constructor<?>                   constructor              = null;
    // 构造方法参数
    private Object[]                         initargs                 = new Object[0];

    // 拦截处理对象列表
    private List<Object>                     aopList                  = new ArrayList<>();

    // aopObj变量编号
    private int                              aopObjSeq                = 1;
    private Map<Object, String>              aopObjFieldMap           = new LinkedHashMap<>();

    // method变量编号
    private int                              methodSeq                = 1;
    private Map<Method, String>              methodFieldMap           = new LinkedHashMap<>();

    // superInvoker变量编号
    private int                              superInvokerSeq          = 1;
    private Map<Method, String>              superInvokerFieldMap     = new LinkedHashMap<>();

    // Around独占拦截， Map<拦截目标方法：拦截处理方法信息>
    private Map<Method, MethodSrcInfo>       methodAroundSrcInfoMap   = new LinkedHashMap<>();

    // 普通非独占拦截， Map<拦截目标方法：List<拦截处理方法信息>>
    private Map<Method, List<MethodSrcInfo>> methodBeforeSrcInfoMap   = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodAfterSrcInfoMap    = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodThrowingSrcInfoMap = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodLastSrcInfoMap     = new LinkedHashMap<>();

    // Map<拦截目标方法：拦截处理方法>
    private Map<Method, Method>              methodNormalAopMap       = new LinkedHashMap<>();
    private Map<Method, Method>              methodAroundAopMap       = new LinkedHashMap<>();

    // 拦截目标方法是否属于父类方法
    private Map<Method, Boolean>             methodSuperMap           = new LinkedHashMap<>();

    // 拦截目标方法是否有拦截上下文参数要求
    private Map<Method, Boolean>             aopContextMap            = new HashMap<>();

    // 中间类所需实现的环绕拦截方法
    private List<Method>                     methodAroundSuperList    = new ArrayList<>();

    private static final String              TAB1                     = "    ";
    private static final String              TAB2                     = TAB1 + TAB1;
    private static final String              TAB3                     = TAB2 + TAB1;

    /**
     * 生成创建器
     * 
     * @return 创建器
     */
    public static EnhanceBuilder get() {
        return new EnhanceBuilder();
    }

    /**
     * 设定被代理类
     * 
     * @param <T> 被代理类
     * @param clas 被代理类
     * @return 创建器
     */
    public <T> EnhanceBuilder setSuperclass(Class<T> clas) {
        if ( Modifier.isFinal(clas.getModifiers()) ) {
            throw new UnsupportedOperationException("无法通过继承来增强的final类：" + clas.getName());
        }

        this.clas = clas;
        return this;
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     * 
     * @param list 拦截处理对象列表
     * @return 创建器
     */
    public EnhanceBuilder matchAopList(List<Object> list) {
        aopList.addAll(list);
        return this;
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     * 
     * @param aops 拦截处理对象
     * @return 创建器
     */
    public EnhanceBuilder matchAop(Object ... aops) {
        for ( Object aopObj : aops ) {
            aopList.add(aopObj);
        }
        return this;
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     */
    private void matchAops() {
        // 标识方法是否为自己声明
        Method[] methods = clas.getMethods();
        for ( Method method : methods ) {
            methodSuperMap.put(method, true);
        }
        methods = clas.getDeclaredMethods();
        for ( Method method : methods ) {
            methodSuperMap.put(method, false);
        }

        int modifiers;
        for ( Method method : methodSuperMap.keySet() ) {
            modifiers = method.getModifiers();
            if ( Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) ) {
                continue;
            }

            for ( Object aopObj : aopList ) {
                if ( aopObj.getClass().isAnnotationPresent(Aop.class) ) {
                    // 检查@Aop
                    matchMethodWithAop(method, aopObj);
                }
            }
        }

    }

    /**
     * 指定构造方法
     * 
     * @param constructor 构造方法
     * @param initargs 构造方法参数
     * @return 创建器
     */
    public EnhanceBuilder setConstructorArgs(Constructor<?> constructor, Object ... initargs) {
        if ( constructor != null ) {
            this.constructor = constructor;
            this.initargs = initargs;
        }
        return this;
    }

    /**
     * 创建代理对象
     * 
     * @param <T> 被代理类
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T build() {

        // 设定拦截
        matchAops();

        // 没有匹配的拦截时，不做增强处理
        if ( aopObjSeq == 1 ) {
            if ( constructor == null ) {
                try {
                    return (T)clas.newInstance();
                } catch (Exception e) {
                    throw new AopException(e);
                }
            } else {
                try {
                    return (T)constructor.newInstance(initargs);
                } catch (Exception e) {
                    throw new AopException(e);
                }
            }

        }

        // 创建代理类源码
        String className = AopUtil.getEnhanceName(clas);
        Map<String, String> map = new HashMap<>();
        map.put(className, createEnhanceClassCode());
        // 创建中间类源码
        if ( !methodAroundSuperList.isEmpty() ) {
            map.put(AopUtil.getAroundMiddleClassName(clas), createAroundMiddleClassCode());
        }

        // 动态编译、创建代理对象
        MemoryJavaCompiler compiler = new MemoryJavaCompiler();
        compiler.compile(map);

        Object proxyObject;
        try ( MemoryClassLoader loader = new MemoryClassLoader() ) {
            if ( constructor != null && constructor.getParameterCount() == 1
                    && (constructor.isVarArgs() || constructor.getParameterTypes()[0].isArray()) ) {
                // 单个可变参数或数组参数时要特殊处理
                proxyObject = loader.loadClass(className).getDeclaredConstructors()[0].newInstance((Object)initargs);
            } else {
                proxyObject = loader.loadClass(className).getDeclaredConstructors()[0].newInstance(initargs);
            }
        } catch (Exception e) {
            throw new AopException(e);
        }

        // 设定拦截处理对象
        aopObjFieldMap.keySet().forEach(aopObj -> CmnBean.setFieldValue(proxyObject, aopObjFieldMap.get(aopObj), aopObj));

        return (T)proxyObject;
    }

    /**
     * 拦截检查
     * 
     * @param method 被拦截方法
     * @param aopMethod 拦截处理方法
     * @param isAround 是否环绕拦截
     */
    private void checkAop(Method method, Method aopMethod, boolean isAround) {

        // 拦截冲突检查
        if ( isAround && methodAroundAopMap.containsKey(method) ) {
            log.error("Around拦截冲突，目标方法：{}", method);
            log.error("   Around拦截处理1：{}", methodAroundAopMap.get(method));
            log.error("   Around拦截处理2：{}", aopMethod);
            throw new AopException("Around拦截必须独占，不能和其他Around拦截共同拦截同一方法 (" + aopMethod + ")");
        }

        // 方法返回类型检查
        if ( isAround && !AopUtil.isVoid(method) && AopUtil.isVoid(aopMethod) ) {
            log.error("拦截处理漏返回类型缺失，应和目标方法一致");
            log.error("   目标方法：{}", method);
            log.error("   拦截处理：{}", aopMethod);
            throw new AopException("拦截错误，目标方法有返回值，拦截处理漏返回 (" + aopMethod + ")");
        }

        // 保存
        if ( isAround ) {
            methodAroundAopMap.put(method, aopMethod);
        } else {
            methodNormalAopMap.put(method, aopMethod);
        }

        // Around拦截和普通拦截冲突时，创建中间类实现Around拦截处理，普通拦截继承中间类实现
        if ( methodNormalAopMap.containsKey(method) && methodAroundAopMap.containsKey(method) ) {
            methodAroundSuperList.add(method);
        }

    }

    private AopData getAopData(Method method, Method aopMethod) {
        AopData aopData = null;
        if ( aopMethod.isAnnotationPresent(Around.class) ) {
            // @Around
            Around aopAnno = aopMethod.getAnnotation(Around.class);
            if ( isMatchDescAndAnnotation(method, aopAnno.value(), aopAnno.annotation()) ) {
                aopData = new AopData();
                aopData.isAround = true;
                aopData.aopAnnoMatchSuperMethod = aopAnno.matchSuperMethod();
                aopData.aopAnnoMatchEquals = aopAnno.matchEquals();
                aopData.aopAnnoMatchToString = aopAnno.matchToString();
                aopData.aopAnnoMatchHashCode = aopAnno.matchHashCode();
            }
        } else if ( aopMethod.isAnnotationPresent(Before.class) ) {
            // @Before
            Before aopAnno = aopMethod.getAnnotation(Before.class);
            if ( isMatchDescAndAnnotation(method, aopAnno.value(), aopAnno.annotation()) ) {
                aopData = new AopData();
                aopData.isAround = false;
                aopData.aopAnnoMatchSuperMethod = aopAnno.matchSuperMethod();
                aopData.aopAnnoMatchEquals = aopAnno.matchEquals();
                aopData.aopAnnoMatchToString = aopAnno.matchToString();
                aopData.aopAnnoMatchHashCode = aopAnno.matchHashCode();
                aopData.aopOrder = aopAnno.order();
                aopData.methodNormalSrcInfoMap = methodBeforeSrcInfoMap;
            }
        } else if ( aopMethod.isAnnotationPresent(After.class) ) {
            // @After
            After aopAnno = aopMethod.getAnnotation(After.class);
            if ( isMatchDescAndAnnotation(method, aopAnno.value(), aopAnno.annotation()) ) {
                aopData = new AopData();
                aopData.isAround = false;
                aopData.aopAnnoMatchSuperMethod = aopAnno.matchSuperMethod();
                aopData.aopAnnoMatchEquals = aopAnno.matchEquals();
                aopData.aopAnnoMatchToString = aopAnno.matchToString();
                aopData.aopAnnoMatchHashCode = aopAnno.matchHashCode();
                aopData.aopOrder = aopAnno.order();
                aopData.methodNormalSrcInfoMap = methodAfterSrcInfoMap;
            }
        } else if ( aopMethod.isAnnotationPresent(Throwing.class) ) {
            // @Throwing
            Throwing aopAnno = aopMethod.getAnnotation(Throwing.class);
            if ( isMatchDescAndAnnotation(method, aopAnno.value(), aopAnno.annotation()) ) {
                aopData = new AopData();
                aopData.isAround = false;
                aopData.aopAnnoMatchSuperMethod = aopAnno.matchSuperMethod();
                aopData.aopAnnoMatchEquals = aopAnno.matchEquals();
                aopData.aopAnnoMatchToString = aopAnno.matchToString();
                aopData.aopAnnoMatchHashCode = aopAnno.matchHashCode();
                aopData.aopOrder = aopAnno.order();
                aopData.methodNormalSrcInfoMap = methodThrowingSrcInfoMap;
            }
        } else if ( aopMethod.isAnnotationPresent(Last.class) ) {
            // @Last
            Last aopAnno = aopMethod.getAnnotation(Last.class);
            if ( isMatchDescAndAnnotation(method, aopAnno.value(), aopAnno.annotation()) ) {
                aopData = new AopData();
                aopData.isAround = false;
                aopData.aopAnnoMatchSuperMethod = aopAnno.matchSuperMethod();
                aopData.aopAnnoMatchEquals = aopAnno.matchEquals();
                aopData.aopAnnoMatchToString = aopAnno.matchToString();
                aopData.aopAnnoMatchHashCode = aopAnno.matchHashCode();
                aopData.aopOrder = aopAnno.order();
                aopData.methodNormalSrcInfoMap = methodLastSrcInfoMap;
            }
        }
        return aopData;
    }

    private boolean isMatchDescAndAnnotation(Method method, String aopAnnoValue, Class<? extends Annotation> aopAnnoAnnotation) {
        // 按通配符匹配方法描述，指定注解匹配时还要同时满足注解的匹配
        return CmnString.wildcardsMatch(aopAnnoValue, method.toGenericString())
                && (aopAnnoAnnotation.equals(Aop.class) || method.isAnnotationPresent(aopAnnoAnnotation));
    }

    private boolean isMatchWithAopData(Method method, AopData aopData) {
        String name = method.getName();
        if ( aopData.aopAnnoMatchSuperMethod ) {
            // 匹配父类方法时，检查匹配equals、toString、hashCode
            if ( "equals".equals(name) && method.getParameterCount() == 1 ) {
                return aopData.aopAnnoMatchEquals; // 自定义是否拦截equals(java.lang.Object)方法
            } else if ( "toString".equals(name) && method.getParameterCount() == 0 ) {
                return aopData.aopAnnoMatchToString; // 自定义是否拦截toString()方法
            } else if ( "hashCode".equals(name) && method.getParameterCount() == 0 ) {
                return aopData.aopAnnoMatchHashCode; // 自定义是否拦截hashCode()方法
            }
        } else {
            // 不匹配父类方法
            if ( methodSuperMap.get(method) ) {
                return false; // 父类方法
            }
        }

        return true;
    }

    private void matchMethodWithAop(Method method, Object aopObj) {
        List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass());
        AopData aopData;
        for ( Method aopMethod : aopMethods ) {
            aopData = getAopData(method, aopMethod);
            if ( aopData != null && isMatchWithAopData(method, aopData) ) {
                // 拦截检查
                checkAop(method, aopMethod, aopData.isAround);
                // 匹配成功，保存匹配结果
                if ( aopData.isAround ) {
                    saveAroundResult(method, aopMethod, aopObj);
                } else {
                    saveNormalResult(method, aopMethod, aopObj, aopData.methodNormalSrcInfoMap, aopData.aopOrder);
                }
            }

        }
    }

    private String getAopVarName(Object aopObj) {
        return CmnString.uncapitalize(aopObj.getClass().getSimpleName()) + "$" + aopObjSeq++;
    }

    private String getMethodVarName(Method method) {
        return "method" + methodSeq++ + "$" + method.getName();
    }

    private String getSuperInvokerVarName(Method method) {
        return "superInvoker" + superInvokerSeq++ + "$" + method.getName();
    }

    private void saveNormalResult(Method method, Method aopMethod, Object aopObj, Map<Method, List<MethodSrcInfo>> methodInfoMap, int aopOrder) {
        // 匹配成功，保存匹配结果
        log.debug("匹配【{}拦截{}】", aopMethod, method);

        // aop变量
        String varAopObj = aopObjFieldMap.get(aopObj);
        if ( varAopObj == null ) {
            varAopObj = getAopVarName(aopObj);
            aopObjFieldMap.put(aopObj, varAopObj);
        }

        // method变量
        String varMethod = methodFieldMap.get(method);
        if ( varMethod == null ) {
            varMethod = getMethodVarName(method);
            methodFieldMap.put(method, varMethod);
        }

        // 方法信息变量
        MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
        methodSrcInfo.method = method;
        methodSrcInfo.varMethod = varMethod;
        methodSrcInfo.varAopObj = varAopObj;
        methodSrcInfo.aopMethodReturnType = aopMethod.getReturnType();
        methodSrcInfo.aopMethodName = aopMethod.getName();
        methodSrcInfo.aopMethod = aopMethod;
        methodSrcInfo.aopOrder = aopOrder;
        List<MethodSrcInfo> list = methodInfoMap.computeIfAbsent(method, val -> new ArrayList<>());
        list.add(methodSrcInfo);
    }

    private void saveAroundResult(Method method, Method aopMethod, Object aopObj) {
        // 匹配成功，保存匹配结果
        log.debug("匹配【{}拦截{}】", aopMethod, method);

        // aop变量
        String varAopObj = aopObjFieldMap.get(aopObj);
        if ( varAopObj == null ) {
            varAopObj = getAopVarName(aopObj);
            aopObjFieldMap.put(aopObj, varAopObj);
        }

        // method变量
        String varMethod = methodFieldMap.get(method);
        if ( varMethod == null ) {
            varMethod = getMethodVarName(method);
            methodFieldMap.put(method, varMethod);
        }

        // superInvoker变量
        String varSuperInvoker = superInvokerFieldMap.get(method);
        if ( varSuperInvoker == null ) {
            varSuperInvoker = getSuperInvokerVarName(method);
            superInvokerFieldMap.put(method, varSuperInvoker);
        }

        // 方法信息变量
        MethodSrcInfo methodSrcInfo = new MethodSrcInfo();
        methodSrcInfo.method = method;
        methodSrcInfo.varMethod = varMethod;
        methodSrcInfo.varSuperInvoker = varSuperInvoker;
        methodSrcInfo.varAopObj = varAopObj;
        methodSrcInfo.aopMethodReturnType = aopMethod.getReturnType();
        methodSrcInfo.aopMethodName = aopMethod.getName();
        methodSrcInfo.aopMethod = aopMethod;
        methodAroundSrcInfoMap.put(method, methodSrcInfo);
    }

    private StringBuilder getNormalMethodSrc() {
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
        methodNormalAopMap.keySet().forEach(method -> {

            sbNormalMethod.append(TAB1).append("@Override").append("\n");
            sbNormalMethod.append(TAB1).append(AopUtil.getMethodDefine(method)).append(" {\n");
            sbNormalMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) {").append("\n");
            sbNormalMethod.append(TAB3).append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
                    .append("\"");
            if ( AopUtil.hasParameters(method) ) {
                sbNormalMethod.append(", ");
            }
            sbNormalMethod.append(AopUtil.getParameterTypes(method)).append(");\n");

            sbNormalMethod.append(TAB2).append("}").append("\n");

            sbNormalMethod.append(getBeforeSrc(method)); // Before

            boolean hasTry = methodThrowingSrcInfoMap.containsKey(method) || methodLastSrcInfoMap.containsKey(method);
            if ( hasTry ) {
                sbNormalMethod.append("try {").append("\n");
            }

            StringBuilder sbAfter = getAfterSrc(method);
            StringBuilder sbNewAopContext = new StringBuilder();
            StringBuilder sbAopContextSetRestult = new StringBuilder();
            if ( aopContextMap.containsKey(method) ) {
                sbNewAopContext.append(TAB2).append("AopContext context = new AopContext(System.currentTimeMillis());").append("\n");
                if ( !void.class.equals(method.getReturnType()) ) {
                    sbAopContextSetRestult.append(TAB2).append("context.setResult(rs);").append("\n");
                }
            }

            sbNormalMethod.append(sbNewAopContext); // AopContext context = new AopContext(System.currentTimeMillis())
            if ( void.class.equals(method.getReturnType()) ) {
                // 无返回值
                sbNormalMethod.append(TAB2).append("super.").append(method.getName()).append("(").append(AopUtil.getParameterNames(method, null))
                        .append(");\n");
            } else {
                // 有返回值
                sbNormalMethod.append(TAB2).append(method.getReturnType().getName()).append(" rs = super.").append(method.getName()).append("(")
                        .append(AopUtil.getParameterNames(method, null)).append(");\n");
                sbNormalMethod.append(sbAopContextSetRestult); // context.setResult(rs)
            }

            sbNormalMethod.append(sbAfter); // After

            // 返回相关语句生成
            setReturnSrc(method, sbNormalMethod, sbAopContextSetRestult);

            // 异常块语句生成
            setThrowingLastSrc(method, sbNormalMethod);

            sbNormalMethod.append(TAB1).append("}\n\n");
        });

        return sbNormalMethod;
    }

    // 异常块语句生成
    private void setThrowingLastSrc(Method method, StringBuilder sbNormalMethod) {
        if ( methodThrowingSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} catch (Exception ex) {").append("\n");
            sbNormalMethod.append(getThrowingSrc(method)); // Throwing
            sbNormalMethod.append("throw new RuntimeException(ex);").append("\n");
        }
        if ( methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} finally {").append("\n");
            sbNormalMethod.append(getLastSrc(method)); // Last
        }
        if ( methodThrowingSrcInfoMap.containsKey(method) || methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("}").append("\n");
        }
        sbNormalMethod.append("\n");
    }

    // 返回语句生成
    private void setReturnSrc(Method method, StringBuilder sbNormalMethod, StringBuilder sbAopContextSetRestult) {
        if ( !void.class.equals(method.getReturnType()) ) {
            sbNormalMethod.append(TAB2);
            if ( sbAopContextSetRestult.length() > 0 ) {
                String returnType = "";
                if ( !method.getReturnType().equals(Object.class) ) {
                    // 返回类型不同时需要强制转换
                    returnType = "(" + method.getReturnType().getName() + ")";
                }
                sbNormalMethod.append("return ").append(returnType).append("context.getResult();");
            } else {
                sbNormalMethod.append("return rs;");
            }
            sbNormalMethod.append("\n");
        }
    }

    /**
     * Around拦截源码
     * 
     * @param isMiddleClass 是否中间类
     * @return StringBuilder
     */
    private StringBuilder getAroundMethodSrc(boolean isMiddleClass) {
        // ---------------------------------- --------------------------------------------------
        //  @Override
        //  public .....
        //      if varMethod == null
        //          {varMethod} = AopUtil.getMethod(this, "{methodName}", {parameterTypes})
        //          {varSuperInvoker} = (method, args) -> super.{methodName}({args})
        //      return {returnType}{varAopObj}.{aopMethodName}(this, {varMethod}, {varSuperInvoker}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder sbAroundMethod = new StringBuilder();
        methodAroundSrcInfoMap.keySet().forEach(method -> {
            if ( isMiddleClass && !methodAroundSuperList.contains(method) || !isMiddleClass && methodAroundSuperList.contains(method) ) {
                return;
            }

            MethodSrcInfo info = methodAroundSrcInfoMap.get(method);
            sbAroundMethod.append(TAB1).append("@Override").append("\n");
            sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method)).append(" {\n");
            sbAroundMethod.append(TAB2).append("if (").append(superInvokerFieldMap.get(method)).append(" == null ) {").append("\n");
            sbAroundMethod.append(TAB3).append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName())
                    .append("\"");

            String parameterTypes = AopUtil.getParameterTypes(method);
            if ( CmnString.isNotBlank(parameterTypes) ) {
                sbAroundMethod.append(", ").append(parameterTypes);
            }
            sbAroundMethod.append(");\n");

            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, "null", info.varMethod, info.varSuperInvoker,
                    "null");

            if ( void.class.equals(method.getReturnType()) ) {
                // 无返回值
                sbAroundMethod.append(TAB3).append(superInvokerFieldMap.get(method)).append(" = (args) -> {super.").append(method.getName())
                        .append("(").append(AopUtil.getLambdaArgs(method)).append("); return null;};").append("\n");
                sbAroundMethod.append(TAB2).append("}").append("\n");
                sbAroundMethod.append(TAB2).append(info.varAopObj).append(".").append(info.aopMethodName).append("(");
                sbAroundMethod.append(sbAopMethodParams);
            } else {
                // 有返回值
                String returnType = "";
                if ( !method.getReturnType().equals(info.aopMethodReturnType) ) {
                    // 返回类型不同时需要强制转换
                    returnType = "(" + method.getReturnType().getName() + ")";
                }
                sbAroundMethod.append(TAB3).append(superInvokerFieldMap.get(method)).append(" = (args) -> super.").append(method.getName())
                        .append("(").append(AopUtil.getLambdaArgs(method)).append(");\n");
                sbAroundMethod.append(TAB2).append("}").append("\n");
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
     * 针对拦截处理方法的前5个参数，进行自动匹配并入参
     * 
     * @param method 被拦截的目标方法
     * @param aopMethod 拦截处理方法
     * @param varAopContextName AOP上下文变量名
     * @param varMethodName 目标方法变量名
     * @param varSuperInvokerName 原方法调用器变量名
     * @param varExceptionName 异常对象变量名
     * @return 入参代码片段StringBuilder
     */
    private StringBuilder mappingAopMethodParameters(Method method, Method aopMethod, String varAopContextName, String varMethodName,
            String varSuperInvokerName, String varExceptionName) {

        StringBuilder buf = new StringBuilder();
        Class<?>[] aopParamClass = aopMethod.getParameterTypes();
        Class<?> paramClass;
        for ( int i = 0; i < aopParamClass.length && i < 5; i++ ) {
            paramClass = aopParamClass[i];
            String var = null;
            if ( Enhance.class.isAssignableFrom(paramClass) ) {
                var = "this";
            } else if ( paramClass.equals(Method.class) ) {
                var = varMethodName;
            } else if ( paramClass.equals(SuperInvoker.class) ) {
                var = varSuperInvokerName;
            } else if ( paramClass.equals(AopContext.class) ) {
                var = varAopContextName;
                aopContextMap.put(method, true); // 有AOP上下文要求
            } else if ( Exception.class.isAssignableFrom(paramClass) ) {
                var = varExceptionName;
            }

            if ( var != null ) {
                if ( buf.length() > 0 ) {
                    buf.append(", ");
                }
                buf.append(var);
            }
        }

        return buf;
    }

    /**
     * 创建环绕拦截中间类源码
     * 
     * @return 环绕拦截中间类源码
     */
    private String createAroundMiddleClassCode() {
        StringBuilder sbMethodField = new StringBuilder();
        StringBuilder sbSuperInvokerField = new StringBuilder();
        StringBuilder sbAopField = new StringBuilder();

        // private Method varMethod
        methodFieldMap.keySet()
                .forEach(method -> sbMethodField.append(TAB1).append("protected Method ").append(methodFieldMap.get(method)).append(";\n"));

        // private SuperInvoker varSuperInvoker
        superInvokerFieldMap.keySet().forEach(
                method -> sbSuperInvokerField.append(TAB1).append("protected SuperInvoker ").append(superInvokerFieldMap.get(method)).append(";\n"));

        // public {aopClass} varAopObj
        aopObjFieldMap.keySet().forEach(aopObj -> sbAopField.append(TAB1).append("public ").append(aopObj.getClass().getName()).append(" ")
                .append(aopObjFieldMap.get(aopObj)).append(";\n"));

        // AroundMethod
        StringBuilder sbAroundMethod = getAroundMethodSrc(true);

        // Class
        StringBuilder sbClass = new StringBuilder();
        // -------------------------------------------------------------------------
        //  package ....

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
        //  
        // -------------------------------------------------------------------------
        sbClass.append("package ").append(clas.getPackage().getName()).append(";\n");
        sbClass.append("\n");
        sbClass.append("import java.lang.reflect.Method;").append("\n");
        sbClass.append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.Enhance;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.AopContext;").append("\n");
        sbClass.append("\n");
        sbClass.append("public class ").append(AopUtil.getAroundMiddleClassSimpleName(clas)).append(" extends ").append(clas.getSimpleName())
                .append(" implements Enhance {").append("\n");
        sbClass.append("\n");
        sbClass.append(sbMethodField);
        sbClass.append(sbSuperInvokerField);
        sbClass.append(sbAopField);
        sbClass.append("\n");
        sbClass.append(getConstructorSrc());
        sbClass.append("\n");
        sbClass.append(sbAroundMethod);
        sbClass.append("}").append("\n");

        String srcCode = sbClass.toString();

        log.trace("\n{}", srcCode);
        return srcCode;
    }

    /**
     * 创建代理类源码
     * 
     * @return 代理类源码
     */
    private String createEnhanceClassCode() {
        StringBuilder sbMethodField = new StringBuilder();
        StringBuilder sbSuperInvokerField = new StringBuilder();
        StringBuilder sbAopField = new StringBuilder();

        // 没有中间类的时候添加全局变量，否则全局变量全部放在中间类中
        if ( methodAroundSuperList.isEmpty() ) {
            // private Method varMethod
            methodFieldMap.keySet()
                    .forEach(method -> sbMethodField.append(TAB1).append("private Method ").append(methodFieldMap.get(method)).append(";\n"));

            // private SuperInvoker varSuperInvoker
            superInvokerFieldMap.keySet().forEach(method -> sbSuperInvokerField.append(TAB1).append("private SuperInvoker ")
                    .append(superInvokerFieldMap.get(method)).append(";\n"));

            // public {aopClass} varAopObj
            aopObjFieldMap.keySet().forEach(aopObj -> sbAopField.append(TAB1).append("public ").append(aopObj.getClass().getName()).append(" ")
                    .append(aopObjFieldMap.get(aopObj)).append(";\n"));
        }

        // AroundMethod
        StringBuilder sbAroundMethod = getAroundMethodSrc(false);

        // NormalMethod
        StringBuilder sbNormalMethod = getNormalMethodSrc();

        // Class
        StringBuilder sbClass = new StringBuilder();
        // -------------------------------------------------------------------------
        //	package ....

        //	import ....
        //	import ....
        //
        //	public class superClass$$gotoeasy$$ extends superClass implements Enhance ...
        //
        //		methodField...
        //		superInvokerField...
        //		aopObjField...
        //
        //		method...
        //	
        // -------------------------------------------------------------------------
        sbClass.append("package ").append(clas.getPackage().getName()).append(";\n");
        sbClass.append("\n");
        sbClass.append("import java.lang.reflect.Method;").append("\n");
        sbClass.append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.Enhance;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.AopContext;").append("\n");
        sbClass.append("\n");
        sbClass.append("public class ").append(AopUtil.getEnhanceSimpleName(clas)).append(" extends ");
        if ( methodAroundSuperList.isEmpty() ) {
            sbClass.append(clas.getSimpleName()).append(" implements Enhance {");
        } else {
            sbClass.append(AopUtil.getAroundMiddleClassSimpleName(clas)).append(" {");
        }
        sbClass.append("\n");
        sbClass.append(sbMethodField);
        sbClass.append(sbSuperInvokerField);
        sbClass.append(sbAopField);
        sbClass.append("\n");
        sbClass.append(getConstructorSrc());
        sbClass.append("\n");
        sbClass.append(sbNormalMethod);
        sbClass.append("\n");
        sbClass.append(sbAroundMethod);
        sbClass.append("}").append("\n");

        String srcCode = sbClass.toString();

        log.trace("\n{}", srcCode);
        return srcCode;
    }

    private StringBuilder getConstructorSrc() {
        StringBuilder buf = new StringBuilder();
        if ( constructor == null ) {
            return buf;
        }

        buf.append(TAB1).append("public ").append(AopUtil.getEnhanceSimpleName(clas)).append("(").append(AopUtil.getParameterDefines(constructor))
                .append("){").append("\n");
        buf.append(TAB2).append("super(").append(AopUtil.getParameterNames(constructor)).append(");").append("\n");
        buf.append(TAB1).append("}").append("\n");

        return buf;
    }

    private StringBuilder getBeforeSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------

        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodBeforeSrcInfoMap.get(method);
        if ( list != null ) {
            list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);

            for ( MethodSrcInfo info : list ) {
                // 前5个参数判断类型自动入参
                StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, "null", info.varMethod, "null", "null");

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
        }
        return buf;
    }

    private StringBuilder getAfterSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodAfterSrcInfoMap.get(method);
        if ( list != null ) {

            list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
            for ( MethodSrcInfo info : list ) {
                // 前5个参数判断类型自动入参
                StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, "context", info.varMethod, "null", "null");

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
        }
        return buf;
    }

    private StringBuilder getThrowingSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodThrowingSrcInfoMap.get(method);

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, "null", info.varMethod, "null", "ex");

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

    private StringBuilder getLastSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodLastSrcInfoMap.get(method);

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, "null", info.varMethod, "null", "null");

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

    // 存放目标方法和AOP方法的变量关联信息
    private static class MethodSrcInfo {

        private Method   method;
        private String   varMethod;
        private String   varSuperInvoker;
        private String   varAopObj;
        private String   aopMethodName;
        private Method   aopMethod;
        private Class<?> aopMethodReturnType;
        private int      aopOrder;

    }

    // 存放AOP注解等信息
    private static class AopData {

        private boolean                          isAround;
        private boolean                          aopAnnoMatchSuperMethod;
        private boolean                          aopAnnoMatchEquals;
        private boolean                          aopAnnoMatchToString;
        private boolean                          aopAnnoMatchHashCode;
        private Map<Method, List<MethodSrcInfo>> methodNormalSrcInfoMap = null;
        private int                              aopOrder;
    }
}
