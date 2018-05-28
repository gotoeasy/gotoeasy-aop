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
import top.gotoeasy.framework.aop.annotation.Afters;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.aop.annotation.Arounds;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.aop.annotation.Befores;
import top.gotoeasy.framework.aop.annotation.Last;
import top.gotoeasy.framework.aop.annotation.Lasts;
import top.gotoeasy.framework.aop.annotation.Throwing;
import top.gotoeasy.framework.aop.annotation.Throwings;
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
    // 方法DESC+SEQ：superInvoker变量名
    private Map<String, String>              superInvokerFieldMap     = new LinkedHashMap<>();

    // Around独占拦截， Map<拦截目标方法：拦截处理方法信息>
    private Map<Method, MethodSrcInfo>       methodAroundSrcInfoMap1  = new LinkedHashMap<>();

    // 普通非独占拦截， Map<拦截目标方法：List<拦截处理方法信息>>
    private Map<Method, List<MethodSrcInfo>> methodBeforeSrcInfoMap   = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodAfterSrcInfoMap    = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodThrowingSrcInfoMap = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodLastSrcInfoMap     = new LinkedHashMap<>();
    private Map<Method, List<MethodSrcInfo>> methodAroundSrcInfoMap   = new LinkedHashMap<>();

    // Map<拦截目标方法：拦截处理方法>
    private Map<Method, Method>              methodNormalAopMap       = new LinkedHashMap<>();
    private Map<Method, Method>              methodAroundAopMap       = new LinkedHashMap<>();

    // 拦截目标方法是否属于父类方法
    private Map<Method, Boolean>             methodSuperMap           = new LinkedHashMap<>();

    // 拦截目标方法是否有拦截上下文参数要求（值:Before/After/Throwing/Last的组合拼接）
    private Map<Method, String>              aopContextMap            = new HashMap<>();

    // 中间类所需实现的环绕拦截方法
    private List<Method>                     methodAroundSuperList    = new ArrayList<>();

    // method描述
    private Map<Method, String>              methodDesc               = new HashMap<>();

    private static final String              TAB1                     = "    ";
    private static final String              TAB2                     = TAB1 + TAB1;

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
            log.warn("无法通过继承来增强的final类：{}" + clas);
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

        if ( Modifier.isFinal(clas.getModifiers()) ) {
            // final类无法继承，不必匹配
            return;
        }

        // 取得待匹配的目标方法，并标识方法是否为自己声明
        Method[] methods = clas.getMethods();
        for ( Method method : methods ) {
            methodSuperMap.put(method, true);
        }
        methods = clas.getDeclaredMethods();
        for ( Method method : methods ) {
            methodSuperMap.put(method, false);
        }

        methodSuperMap.keySet().forEach(method -> methodDesc.put(method, AopUtil.getMethodDesc(clas, method)));

        int modifiers;
        for ( Method method : methodSuperMap.keySet() ) {
            modifiers = method.getModifiers();
            if ( Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) ) {
                // 不拦截指定标识的方法：final、static、private、protected
                continue;
            }

            for ( Object aopObj : aopList ) {
                if ( aopObj.getClass().isAnnotationPresent(Aop.class) ) {
                    // 检查@Aop
                    matchMethodWithAopObject(method, aopObj);
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

        // final类或没有匹配的拦截时，不做增强处理
        try {
            if ( Modifier.isFinal(clas.getModifiers()) || aopObjSeq == 1 ) {
                if ( constructor == null ) {
                    return (T)clas.newInstance();
                } else {
                    return (T)constructor.newInstance(initargs);
                }
            }
        } catch (Exception e) {
            throw new AopException(e);
        }

        // Around拦截排序
        for ( Method method : methodAroundSuperList ) {
            methodAroundSrcInfoMap.get(method).sort((srcInfo1, srcInfo2) -> srcInfo2.aopOrder - srcInfo1.aopOrder);
        }

        // 创建代理类源码
        String className = AopUtil.getEnhanceName(clas);
        Map<String, String> map = new HashMap<>();
        map.put(className, createEnhanceClassCode());
        // 创建中间类源码
        int maxSize = getMaxSizeOfMethodAroundList();
        for ( int i = 0; i < maxSize; i++ ) {
            map.put(AopUtil.getAroundMiddleClassName(clas, maxSize, i), createAroundMiddleClassCode(maxSize, i));
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

    // 中间类最大数
    private int getMaxSizeOfMethodAroundList() {
        int max = 0;
        for ( Method method : methodAroundSuperList ) {
            int size = methodAroundSrcInfoMap.get(method).size();
            if ( max < size ) {
                max = size;
            }
        }
        return max;
    }

    /**
     * 拦截检查
     * 
     * @param method 被拦截方法
     * @param aopMethod 拦截处理方法
     * @param isAround 是否环绕拦截
     */
    private void checkAop(Method method, Method aopMethod, boolean isAround) {
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
        if ( (methodNormalAopMap.containsKey(method) && methodAroundAopMap.containsKey(method) || isAround && methodAroundAopMap.containsKey(method))
                && !methodAroundSuperList.contains(method) ) {
            methodAroundSuperList.add(method);
        }
    }

    // 取得匹配的拦截信息：目标方法 & 拦截处理方法
    private List<AopData> getMatchAopDataList(Method method, Method aopMethod) {
        List<AopData> list = new ArrayList<>();
        // @Before
        if ( aopMethod.isAnnotationPresent(Before.class) || aopMethod.isAnnotationPresent(Befores.class) ) {
            list.add(getBeforeAopData(method, aopMethod));
        }
        // @After
        if ( aopMethod.isAnnotationPresent(After.class) || aopMethod.isAnnotationPresent(Afters.class) ) {
            list.add(getAfterAopData(method, aopMethod));
        }
        // @Around
        if ( aopMethod.isAnnotationPresent(Around.class) || aopMethod.isAnnotationPresent(Arounds.class) ) {
            list.add(getAroundAopData(method, aopMethod));
        }
        // @Throwing
        if ( aopMethod.isAnnotationPresent(Throwing.class) || aopMethod.isAnnotationPresent(Throwings.class) ) {
            list.add(getThrowingAopData(method, aopMethod));
        }
        // @Last
        if ( aopMethod.isAnnotationPresent(Last.class) || aopMethod.isAnnotationPresent(Lasts.class) ) {
            list.add(getLastAopData(method, aopMethod));
        }
        return list;
    }

    // 取得匹配的拦截信息：目标方法 & Before拦截处理方法
    private AopData getBeforeAopData(Method method, Method aopMethod) {
        Before[] aopAnnos = aopMethod.getAnnotationsByType(Before.class);
        for ( Before before : aopAnnos ) {
            AopData aopData = new AopData();
            aopData.annoValue = before.value();
            aopData.annoPackages = CmnString.isBlank(before.packages()) ? new String[0] : before.packages().split(",");
            aopData.annoTypeAnnotations = before.typeAnnotations();
            aopData.annoClasses = before.classes();
            aopData.annoMethodAnnotations = before.annotations();
            aopData.annoMatchSuperMethod = before.matchSuperMethod();
            aopData.annoMatchEquals = before.matchEquals();
            aopData.annoMatchToString = before.matchToString();
            aopData.annoMatchHashCode = before.matchHashCode();
            aopData.annoOrder = before.order();

            if ( matchMethodWithAnnoData(method, aopData) ) {
                // 目标方法匹配成功，拦截
                aopData.isAround = false;
                aopData.methodSrcInfoMap = methodBeforeSrcInfoMap;
                return aopData;
            }
        }

        return null;
    }

    // 取得匹配的拦截信息：目标方法 & After拦截处理方法
    private AopData getAfterAopData(Method method, Method aopMethod) {
        After[] aopAnnos = aopMethod.getAnnotationsByType(After.class);
        for ( After after : aopAnnos ) {
            AopData aopData = new AopData();
            aopData.annoValue = after.value();
            aopData.annoPackages = CmnString.isBlank(after.packages()) ? new String[0] : after.packages().split(",");
            aopData.annoTypeAnnotations = after.typeAnnotations();
            aopData.annoClasses = after.classes();
            aopData.annoMethodAnnotations = after.annotations();
            aopData.annoMatchSuperMethod = after.matchSuperMethod();
            aopData.annoMatchEquals = after.matchEquals();
            aopData.annoMatchToString = after.matchToString();
            aopData.annoMatchHashCode = after.matchHashCode();
            aopData.annoOrder = after.order();

            if ( matchMethodWithAnnoData(method, aopData) ) {
                // 目标方法匹配成功，拦截
                aopData.isAround = false;
                aopData.methodSrcInfoMap = methodAfterSrcInfoMap;
                return aopData;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Around拦截处理方法
    private AopData getAroundAopData(Method method, Method aopMethod) {
        Around[] aopAnnos = aopMethod.getAnnotationsByType(Around.class);
        for ( Around around : aopAnnos ) {
            AopData aopData = new AopData();
            aopData.annoValue = around.value();
            aopData.annoPackages = CmnString.isBlank(around.packages()) ? new String[0] : around.packages().split(",");
            aopData.annoTypeAnnotations = around.typeAnnotations();
            aopData.annoClasses = around.classes();
            aopData.annoMethodAnnotations = around.annotations();
            aopData.annoMatchSuperMethod = around.matchSuperMethod();
            aopData.annoMatchEquals = around.matchEquals();
            aopData.annoMatchToString = around.matchToString();
            aopData.annoMatchHashCode = around.matchHashCode();
            aopData.annoOrder = around.order();

            if ( matchMethodWithAnnoData(method, aopData) ) {
                // 目标方法匹配成功，拦截
                aopData.isAround = true;
                aopData.methodSrcInfoMap = methodAroundSrcInfoMap;
                return aopData;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Throwing拦截处理方法
    private AopData getThrowingAopData(Method method, Method aopMethod) {
        Throwing[] aopAnnos = aopMethod.getAnnotationsByType(Throwing.class);
        for ( Throwing throwing : aopAnnos ) {
            AopData aopData = new AopData();
            aopData.annoValue = throwing.value();
            aopData.annoPackages = CmnString.isBlank(throwing.packages()) ? new String[0] : throwing.packages().split(",");
            aopData.annoTypeAnnotations = throwing.typeAnnotations();
            aopData.annoClasses = throwing.classes();
            aopData.annoMethodAnnotations = throwing.annotations();
            aopData.annoMatchSuperMethod = throwing.matchSuperMethod();
            aopData.annoMatchEquals = throwing.matchEquals();
            aopData.annoMatchToString = throwing.matchToString();
            aopData.annoMatchHashCode = throwing.matchHashCode();
            aopData.annoOrder = throwing.order();

            if ( matchMethodWithAnnoData(method, aopData) ) {
                // 目标方法匹配成功，拦截
                aopData.isAround = false;
                aopData.methodSrcInfoMap = methodThrowingSrcInfoMap;
                return aopData;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Last拦截处理方法
    private AopData getLastAopData(Method method, Method aopMethod) {
        Last[] aopAnnos = aopMethod.getAnnotationsByType(Last.class);
        for ( Last last : aopAnnos ) {
            AopData aopData = new AopData();
            aopData.annoValue = last.value();
            aopData.annoPackages = CmnString.isBlank(last.packages()) ? new String[0] : last.packages().split(",");
            aopData.annoTypeAnnotations = last.typeAnnotations();
            aopData.annoClasses = last.classes();
            aopData.annoMethodAnnotations = last.annotations();
            aopData.annoMatchSuperMethod = last.matchSuperMethod();
            aopData.annoMatchEquals = last.matchEquals();
            aopData.annoMatchToString = last.matchToString();
            aopData.annoMatchHashCode = last.matchHashCode();
            aopData.annoOrder = last.order();

            if ( matchMethodWithAnnoData(method, aopData) ) {
                // 目标方法匹配成功，拦截
                aopData.isAround = false;
                aopData.methodSrcInfoMap = methodLastSrcInfoMap;
                return aopData;
            }
        }
        return null;
    }

    // 匹配：目标方法 & 拦截处理方法的一个拦截注解
    private boolean matchMethodWithAnnoData(Method method, AnnoData annoData) {
        String name = method.getName();

        // 方法描述必须匹配，最常用，只要不匹配就直接返回
        if ( !CmnString.wildcardsMatch(annoData.annoValue, methodDesc.get(method)) ) {
            return false; // 按通配符匹配方法描述失败：结果为不匹配(false)
        }

        // 目标方法注解检查
        boolean matchMethodAnno = Annotation.class.equals(annoData.annoMethodAnnotations[0]); // 默认true
        for ( Class<? extends Annotation> annotationClass : annoData.annoMethodAnnotations ) {
            if ( method.isAnnotationPresent(annotationClass) ) {
                matchMethodAnno = true;
                break;
            }
        }

        return !(!matchMethodAnno // 指定了目标方法需带的注解，但实际没有：结果为不匹配(false)
                // 指定目标类的包名范围，但本类不在包名范围内：结果为不匹配(false)
                || !matchPackage(annoData)
                // 指定目标类的类注解范围，但本类不带相关注解：结果为不匹配(false)
                || !matchTypeAnnotation(annoData)
                // 指定目标类的范围，但本类不在指定类范围内：结果为不匹配(false)
                || !matchClass(annoData)
                // 当前是父类方法，但声明的匹配范围不含父类方法：结果为不匹配(false)
                || !annoData.annoMatchSuperMethod && methodSuperMap.get(method)
                // 当前是equals方法，但声明的匹配范围不含equals方法：结果为不匹配(false)
                || "equals".equals(name) && method.getParameterCount() == 1 && !annoData.annoMatchEquals
                // 当前是toString方法，但声明的匹配范围不含toString方法：结果为不匹配(false)
                || "toString".equals(name) && method.getParameterCount() == 0 && !annoData.annoMatchToString
                // 当前是hashCode方法，但声明的匹配范围不含hashCode方法：结果为不匹配(false)
                || "hashCode".equals(name) && method.getParameterCount() == 0 && !annoData.annoMatchHashCode);
    }

    // 目标类范围检查
    private boolean matchClass(AnnoData annoData) {
        if ( void.class.equals(annoData.annoClasses[0]) ) {
            return true;
        }

        for ( Class<?> cls : annoData.annoClasses ) {
            if ( clas.equals(cls) ) {
                return true;
            }
        }
        return false;
    }

    // 目标类所带注解检查
    private boolean matchTypeAnnotation(AnnoData annoData) {
        if ( Annotation.class.equals(annoData.annoTypeAnnotations[0]) ) {
            return true;
        }

        for ( Class<? extends Annotation> annotationClass : annoData.annoTypeAnnotations ) {
            if ( clas.isAnnotationPresent(annotationClass) ) {
                return true;
            }
        }
        return false;
    }

    // 目标类包名检查
    private boolean matchPackage(AnnoData annoData) {
        if ( annoData.annoPackages.length == 0 ) {
            return true;
        }

        String classPackage = clas.getPackage().getName();
        for ( String pack : annoData.annoPackages ) {
            if ( classPackage.startsWith(pack.trim()) ) {
                return true;
            }
        }
        return false;
    }

    // 匹配：目标方法 & AOP对象
    private void matchMethodWithAopObject(Method method, Object aopObj) {
        List<Method> aopMethods = MethodScaner.getDeclaredPublicMethods(aopObj.getClass()); // AOP仅支持本类方法的拦截声明
        for ( Method aopMethod : aopMethods ) {

            // 取得匹配的拦截信息
            List<AopData> list = getMatchAopDataList(method, aopMethod);
            list.forEach(aopData -> {
                if ( aopData == null ) {
                    return;
                }

                // 有匹配时，进一步检查是否存在拦截冲突
                checkAop(method, aopMethod, aopData.isAround);

                // 终于匹配成功，保存匹配结果
                if ( aopData.isAround ) {
                    saveAroundResult(method, aopMethod, aopObj, aopData.methodSrcInfoMap, aopData.annoOrder);
                } else {
                    saveNormalResult(method, aopMethod, aopObj, aopData.methodSrcInfoMap, aopData.annoOrder);
                }
            });

        }

    }

    private String getAopVarName(Object aopObj) {
        return CmnString.uncapitalize(aopObj.getClass().getSimpleName()) + "$" + aopObjSeq++;
    }

    private String getMethodVarName(Method method) {
        return "method" + methodSeq++ + "$" + method.getName();
    }

    private String getSuperInvokerVarName(Method method, int aopOrder) {
        return "superInvoker" + superInvokerSeq++ + "$" + aopOrder + method.getName();
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

    private void saveAroundResult(Method method, Method aopMethod, Object aopObj, Map<Method, List<MethodSrcInfo>> methodInfoMap, int aopOrder) {
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

        // superInvoker变量
        String key = method.toGenericString() + methodInfoMap.get(method).size();
        String varSuperInvoker = superInvokerFieldMap.get(key);
        if ( varSuperInvoker == null ) {
            varSuperInvoker = getSuperInvokerVarName(method, methodInfoMap.get(method).size());
            methodSrcInfo.varSuperInvoker = varSuperInvoker;
            superInvokerFieldMap.put(key, varSuperInvoker);
        }

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

            StringBuilder sbBeforeSrc = getBeforeSrc(method);
            StringBuilder sbAfterSrc = getAfterSrc(method);
            StringBuilder sbThrowingSrc = getThrowingSrc(method);
            StringBuilder sbLastSrc = getLastSrc(method);
            boolean hasAfter = sbAfterSrc.length() > 0;
            boolean hasAopContext = aopContextMap.containsKey(method);
            boolean hasAfterUseContext = hasAopContext && aopContextMap.get(method).contains(After.class.getSimpleName());
            boolean hasThrowingUseContext = hasAopContext && aopContextMap.get(method).contains(Throwing.class.getSimpleName());
            boolean hasLastUseContext = hasAopContext && aopContextMap.get(method).contains(Last.class.getSimpleName());
            boolean hasContextResult = hasAfterUseContext || hasThrowingUseContext || hasLastUseContext;

            sbNormalMethod.append(TAB1).append("@Override").append("\n");
            sbNormalMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "final")).append(" {\n");
            sbNormalMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) ");
            sbNormalMethod.append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName()).append("\"");
            if ( AopUtil.hasParameters(method) ) {
                sbNormalMethod.append(", ");
            }
            sbNormalMethod.append(AopUtil.getParameterTypes(method)).append(");\n").append("\n");

            // AopContext context = new AopContext(System.currentTimeMillis())
            if ( hasAopContext ) {
                sbNormalMethod.append(TAB2).append("AopContext context = new AopContext(System.currentTimeMillis());").append("\n");
            }

            // Before
            sbNormalMethod.append(sbBeforeSrc);

            boolean hasTry = methodThrowingSrcInfoMap.containsKey(method) || methodLastSrcInfoMap.containsKey(method);
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
        if ( methodThrowingSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} catch (Exception ex) {").append("\n");
            sbNormalMethod.append(sbThrowingSrc); // Throwing
            sbNormalMethod.append("throw new RuntimeException(ex);").append("\n");
        }
        if ( methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("} finally {").append("\n");
            sbNormalMethod.append(sbLastSrc); // Last
        }
        if ( methodThrowingSrcInfoMap.containsKey(method) || methodLastSrcInfoMap.containsKey(method) ) {
            sbNormalMethod.append("}").append("\n");
        }
        sbNormalMethod.append("\n");
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
        methodAroundSrcInfoMap1.keySet().forEach(method -> {
            if ( isMiddleClass && !methodAroundSuperList.contains(method) || !isMiddleClass && methodAroundSuperList.contains(method) ) {
                return;
            }

            boolean hasReturn = !void.class.equals(method.getReturnType());
            MethodSrcInfo info = methodAroundSrcInfoMap1.get(method);
            sbAroundMethod.append(TAB1).append("@Override").append("\n");
            sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "")).append(" {\n");
            // method$abc变量初始化
            sbAroundMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) ");
            sbAroundMethod.append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName()).append("\"");
            String parameterTypes = AopUtil.getParameterTypes(method);
            if ( CmnString.isNotBlank(parameterTypes) ) {
                sbAroundMethod.append(", ").append(parameterTypes);
            }
            sbAroundMethod.append(");\n");
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
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, Around.class.getSimpleName(), info.varMethod,
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
     * 针对拦截处理方法的前5个参数，进行自动匹配并入参
     * 
     * @param method 被拦截的目标方法
     * @param aopMethod 拦截处理方法
     * @param aopType 拦截类型（Around/Before/After/Throwing/Last）
     * @param varMethodName 目标方法变量名
     * @param varSuperInvokerName 原方法调用器变量名
     * @param varExceptionName 异常对象变量名
     * @return 入参代码片段StringBuilder
     */
    private StringBuilder mappingAopMethodParameters(Method method, Method aopMethod, String aopType, String varMethodName,
            String varSuperInvokerName, String varExceptionName) {

        StringBuilder buf = new StringBuilder();
        Class<?>[] aopParamClass = aopMethod.getParameterTypes();
        Class<?> paramClass;
        for ( int i = 0; i < aopParamClass.length && i < 5; i++ ) {
            paramClass = aopParamClass[i];
            String var = null;
            if ( Enhance.class.isAssignableFrom(paramClass) ) {
                var = "this";
            } else if ( Method.class.isAssignableFrom(paramClass) ) {
                var = varMethodName;
            } else if ( SuperInvoker.class.isAssignableFrom(paramClass) ) {
                var = varSuperInvokerName;
            } else if ( AopContext.class.isAssignableFrom(paramClass) ) {
                var = "context";
                setMethodContextInfo(method, aopType);
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

    private void setMethodContextInfo(Method method, String aopType) {
        // aopType: 拦截类型（Around/Before/After/Throwing/Last）
        String codes = CmnString.nullToBlank(aopContextMap.get(method));
        if ( !Around.class.getSimpleName().equals(aopType) && !codes.contains(aopType) ) {
            aopContextMap.put(method, codes + aopType);
        }
    }

    private StringBuilder getAroundMethodSrc(int seq) {
        StringBuilder buf = new StringBuilder();
        methodAroundSuperList.forEach(method -> {
            List<MethodSrcInfo> list = methodAroundSrcInfoMap.get(method);
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
        MethodSrcInfo info = methodAroundSrcInfoMap.get(method).get(seq);

        sbAroundMethod.append(TAB1).append("@Override").append("\n");
        sbAroundMethod.append(TAB1).append(AopUtil.getMethodDefine(method, "")).append(" {\n");
        // superInvoker$abc变量初始化
        sbAroundMethod.append(TAB2).append("if (").append(methodFieldMap.get(method)).append(" == null ) ");
        sbAroundMethod.append(methodFieldMap.get(method)).append(" = AopUtil.getMethod(this, \"").append(method.getName()).append("\"");
        String parameterTypes = AopUtil.getParameterTypes(method);
        if ( CmnString.isNotBlank(parameterTypes) ) {
            sbAroundMethod.append(", ").append(parameterTypes);
        }
        sbAroundMethod.append(");\n");
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
        StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, Around.class.getSimpleName(), info.varMethod,
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

    /**
     * 创建环绕拦截中间类源码
     * 
     * @param max 中间类最大数
     * @param order 中间类序号
     * @return 环绕拦截中间类源码
     */
    private String createAroundMiddleClassCode(int max, int seq) {
        StringBuilder sbMethodField = new StringBuilder();
        StringBuilder sbSuperInvokerField = new StringBuilder();
        StringBuilder sbAopField = new StringBuilder();

        if ( seq == 0 ) {
            // protected Method varMethod
            methodFieldMap.keySet()
                    .forEach(method -> sbMethodField.append(TAB1).append("protected Method ").append(methodFieldMap.get(method)).append(";\n"));
            // protected SuperInvoker varSuperInvoker
            superInvokerFieldMap.keySet().forEach(method -> sbSuperInvokerField.append(TAB1).append("protected SuperInvoker ")
                    .append(superInvokerFieldMap.get(method)).append(";\n"));
            // public {aopClass} varAopObj
            aopObjFieldMap.keySet().forEach(aopObj -> sbAopField.append(TAB1).append("public").append(" ").append(aopObj.getClass().getName())
                    .append(" ").append(aopObjFieldMap.get(aopObj)).append(";\n"));
        }

        // AroundMethod
        StringBuilder sbAroundMethod = getAroundMethodSrc(seq);
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
        sbClass.append("package ").append(clas.getPackage().getName()).append(";\n");
        sbClass.append("\n");
        sbClass.append("import java.lang.reflect.Method;").append("\n");
        sbClass.append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.util.AopUtil;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.Enhance;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.SuperInvoker;").append("\n");
        sbClass.append("import top.gotoeasy.framework.aop.AopContext;").append("\n");
        sbClass.append("\n");
        if ( seq == 0 ) {
            sbClass.append("public class ").append(AopUtil.getAroundMiddleClassSimpleName(clas, max, seq)).append(" extends ")
                    .append(clas.getSimpleName()).append(" implements Enhance {").append("\n");
        } else {
            sbClass.append("public class ").append(AopUtil.getAroundMiddleClassSimpleName(clas, max, seq)).append(" extends ")
                    .append(AopUtil.getAroundMiddleClassSimpleName(clas, max, seq - 1)).append(" {").append("\n");
        }
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
        if ( list == null ) {
            return buf;
        }

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);

        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, Before.class.getSimpleName(), info.varMethod,
                    "null", "null");

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

    private StringBuilder getAfterSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodAfterSrcInfoMap.get(method);
        if ( list == null ) {
            return buf;
        }

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, After.class.getSimpleName(), info.varMethod,
                    "null", "null");

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

    private StringBuilder getThrowingSrc(Method method) {
        // ---------------------------------- --------------------------------------------------
        //		{varAopObj}.{aopMethodName}(this, {varMethod}, {parameterNames})
        // ---------------------------------- --------------------------------------------------
        StringBuilder buf = new StringBuilder();
        List<MethodSrcInfo> list = methodThrowingSrcInfoMap.get(method);
        if ( list == null ) {
            return buf;
        }

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, Throwing.class.getSimpleName(), info.varMethod,
                    "null", "ex");

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
        if ( list == null ) {
            return buf;
        }

        list.sort((info1, info2) -> info1.aopOrder - info2.aopOrder);
        for ( MethodSrcInfo info : list ) {
            // 前5个参数判断类型自动入参
            StringBuilder sbAopMethodParams = mappingAopMethodParameters(info.method, info.aopMethod, Last.class.getSimpleName(), info.varMethod,
                    "null", "null");

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
            superInvokerFieldMap.keySet().forEach(
                    key -> sbSuperInvokerField.append(TAB1).append("private SuperInvoker ").append(superInvokerFieldMap.get(key)).append(";\n"));
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
            sbClass.append(AopUtil.getAroundMiddleClassSimpleName(clas, getMaxSizeOfMethodAroundList(), getMaxSizeOfMethodAroundList() - 1))
                    .append(" {");
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

    // 仅存放AOP注解信息
    private static class AnnoData {

        protected String                        annoValue;
        protected String[]                      annoPackages;
        protected Class<? extends Annotation>[] annoTypeAnnotations;
        protected Class<?>[]                    annoClasses;
        protected Class<? extends Annotation>[] annoMethodAnnotations;
        protected boolean                       annoMatchSuperMethod;
        protected boolean                       annoMatchEquals;
        protected boolean                       annoMatchToString;
        protected boolean                       annoMatchHashCode;
        protected int                           annoOrder;
    }

    // 存放AOP注解信息+补充信息
    private static class AopData extends AnnoData {

        private boolean                          isAround;
        private Map<Method, List<MethodSrcInfo>> methodSrcInfoMap = null;
    }
}
