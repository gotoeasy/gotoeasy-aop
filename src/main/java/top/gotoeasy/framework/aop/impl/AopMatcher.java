package top.gotoeasy.framework.aop.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Afters;
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
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.reflect.MethodScaner;
import top.gotoeasy.framework.core.util.CmnString;

/**
 * AOP拦截匹配器
 * 
 * @author 青松
 * @since 2018/04
 */
public class AopMatcher {

    private static final Log log = LoggerFactory.getLogger(AopMatcher.class);

    private DataBuilderVars  dataBuilderVars;

    /**
     * 构造方法
     * 
     * @param dataBuilderVars 公用变量
     */
    public AopMatcher(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
    }

    private String getAopVarName(Object aopObj) {
        return CmnString.uncapitalize(aopObj.getClass().getSimpleName()) + "$" + dataBuilderVars.aopObjSeq++;
    }

    private String getMethodVarName(Method method) {
        return "method" + dataBuilderVars.methodSeq++ + "$" + method.getName();
    }

    private String getSuperInvokerVarName(Method method, int aopOrder) {
        return "superInvoker" + dataBuilderVars.superInvokerSeq++ + "$" + aopOrder + method.getName();
    }

    /**
     * 设定拦截
     * <p>
     * 传入带拦截注解的拦截器对象，自动拦截匹配的public方法
     * </p>
     */
    public void matchAops() {

        if ( Modifier.isFinal(dataBuilderVars.clas.getModifiers()) ) {
            // final类无法继承，不必匹配
            return;
        }

        // 取得待匹配的目标方法，并标识方法是否为自己声明
        Method[] methods = dataBuilderVars.clas.getMethods();
        for ( Method method : methods ) {
            dataBuilderVars.methodSuperMap.put(method, true);
        }
        methods = dataBuilderVars.clas.getDeclaredMethods();
        for ( Method method : methods ) {
            dataBuilderVars.methodSuperMap.put(method, false);
        }

        // 初始化待匹配的方法描述字符串
        dataBuilderVars.methodSuperMap.keySet()
                .forEach(method -> dataBuilderVars.methodDesc.put(method, AopUtil.getMethodDesc(dataBuilderVars.clas, method)));

        int modifiers;
        for ( Method method : dataBuilderVars.methodSuperMap.keySet() ) {
            modifiers = method.getModifiers();
            if ( Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) ) {
                // 不拦截指定标识的方法：final、static、private、protected
                continue;
            }

            for ( Object aopObj : dataBuilderVars.aopList ) {
                matchMethodWithAopObject(method, aopObj);
            }
        }

    }

    // 取得匹配的拦截信息：目标方法 & 拦截处理方法
    private List<DataAopInfo> getMatchAopDataList(Method method, Method aopMethod) {
        List<DataAopInfo> list = new ArrayList<>();
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
    private DataAopInfo getBeforeAopData(Method method, Method aopMethod) {
        Before[] aopAnnos = aopMethod.getAnnotationsByType(Before.class);
        for ( Before before : aopAnnos ) {
            DataAopInfo dataAopInfo = new DataAopInfo();
            dataAopInfo.annoValue = before.value();
            dataAopInfo.annoPackages = CmnString.isBlank(before.packages()) ? new String[0] : before.packages().split(",");
            dataAopInfo.annoTypeAnnotations = before.typeAnnotations();
            dataAopInfo.annoClasses = before.classes();
            dataAopInfo.annoMethodAnnotations = before.annotations();
            dataAopInfo.annoMatchSuperMethod = before.matchSuperMethod();
            dataAopInfo.annoMatchEquals = before.matchEquals();
            dataAopInfo.annoMatchToString = before.matchToString();
            dataAopInfo.annoMatchHashCode = before.matchHashCode();
            dataAopInfo.annoOrder = before.order();

            if ( matchMethodWithAnnoData(method, dataAopInfo) ) {
                // 目标方法匹配成功，拦截
                dataAopInfo.isAround = false;
                dataAopInfo.methodSrcInfoMap = dataBuilderVars.methodBeforeSrcInfoMap;
                return dataAopInfo;
            }
        }

        return null;
    }

    // 取得匹配的拦截信息：目标方法 & After拦截处理方法
    private DataAopInfo getAfterAopData(Method method, Method aopMethod) {
        After[] aopAnnos = aopMethod.getAnnotationsByType(After.class);
        for ( After after : aopAnnos ) {
            DataAopInfo dataAopInfo = new DataAopInfo();
            dataAopInfo.annoValue = after.value();
            dataAopInfo.annoPackages = CmnString.isBlank(after.packages()) ? new String[0] : after.packages().split(",");
            dataAopInfo.annoTypeAnnotations = after.typeAnnotations();
            dataAopInfo.annoClasses = after.classes();
            dataAopInfo.annoMethodAnnotations = after.annotations();
            dataAopInfo.annoMatchSuperMethod = after.matchSuperMethod();
            dataAopInfo.annoMatchEquals = after.matchEquals();
            dataAopInfo.annoMatchToString = after.matchToString();
            dataAopInfo.annoMatchHashCode = after.matchHashCode();
            dataAopInfo.annoOrder = after.order();

            if ( matchMethodWithAnnoData(method, dataAopInfo) ) {
                // 目标方法匹配成功，拦截
                dataAopInfo.isAround = false;
                dataAopInfo.methodSrcInfoMap = dataBuilderVars.methodAfterSrcInfoMap;
                return dataAopInfo;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Around拦截处理方法
    private DataAopInfo getAroundAopData(Method method, Method aopMethod) {
        Around[] aopAnnos = aopMethod.getAnnotationsByType(Around.class);
        for ( Around around : aopAnnos ) {
            DataAopInfo dataAopInfo = new DataAopInfo();
            dataAopInfo.annoValue = around.value();
            dataAopInfo.annoPackages = CmnString.isBlank(around.packages()) ? new String[0] : around.packages().split(",");
            dataAopInfo.annoTypeAnnotations = around.typeAnnotations();
            dataAopInfo.annoClasses = around.classes();
            dataAopInfo.annoMethodAnnotations = around.annotations();
            dataAopInfo.annoMatchSuperMethod = around.matchSuperMethod();
            dataAopInfo.annoMatchEquals = around.matchEquals();
            dataAopInfo.annoMatchToString = around.matchToString();
            dataAopInfo.annoMatchHashCode = around.matchHashCode();
            dataAopInfo.annoOrder = around.order();

            if ( matchMethodWithAnnoData(method, dataAopInfo) ) {
                // 目标方法匹配成功，拦截
                dataAopInfo.isAround = true;
                dataAopInfo.methodSrcInfoMap = dataBuilderVars.methodAroundSrcInfoMap;
                return dataAopInfo;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Throwing拦截处理方法
    private DataAopInfo getThrowingAopData(Method method, Method aopMethod) {
        Throwing[] aopAnnos = aopMethod.getAnnotationsByType(Throwing.class);
        for ( Throwing throwing : aopAnnos ) {
            DataAopInfo dataAopInfo = new DataAopInfo();
            dataAopInfo.annoValue = throwing.value();
            dataAopInfo.annoPackages = CmnString.isBlank(throwing.packages()) ? new String[0] : throwing.packages().split(",");
            dataAopInfo.annoTypeAnnotations = throwing.typeAnnotations();
            dataAopInfo.annoClasses = throwing.classes();
            dataAopInfo.annoMethodAnnotations = throwing.annotations();
            dataAopInfo.annoMatchSuperMethod = throwing.matchSuperMethod();
            dataAopInfo.annoMatchEquals = throwing.matchEquals();
            dataAopInfo.annoMatchToString = throwing.matchToString();
            dataAopInfo.annoMatchHashCode = throwing.matchHashCode();
            dataAopInfo.annoOrder = throwing.order();

            if ( matchMethodWithAnnoData(method, dataAopInfo) ) {
                // 目标方法匹配成功，拦截
                dataAopInfo.isAround = false;
                dataAopInfo.methodSrcInfoMap = dataBuilderVars.methodThrowingSrcInfoMap;
                return dataAopInfo;
            }
        }
        return null;
    }

    // 取得匹配的拦截信息：目标方法 & Last拦截处理方法
    private DataAopInfo getLastAopData(Method method, Method aopMethod) {
        Last[] aopAnnos = aopMethod.getAnnotationsByType(Last.class);
        for ( Last last : aopAnnos ) {
            DataAopInfo dataAopInfo = new DataAopInfo();
            dataAopInfo.annoValue = last.value();
            dataAopInfo.annoPackages = CmnString.isBlank(last.packages()) ? new String[0] : last.packages().split(",");
            dataAopInfo.annoTypeAnnotations = last.typeAnnotations();
            dataAopInfo.annoClasses = last.classes();
            dataAopInfo.annoMethodAnnotations = last.annotations();
            dataAopInfo.annoMatchSuperMethod = last.matchSuperMethod();
            dataAopInfo.annoMatchEquals = last.matchEquals();
            dataAopInfo.annoMatchToString = last.matchToString();
            dataAopInfo.annoMatchHashCode = last.matchHashCode();
            dataAopInfo.annoOrder = last.order();

            if ( matchMethodWithAnnoData(method, dataAopInfo) ) {
                // 目标方法匹配成功，拦截
                dataAopInfo.isAround = false;
                dataAopInfo.methodSrcInfoMap = dataBuilderVars.methodLastSrcInfoMap;
                return dataAopInfo;
            }
        }
        return null;
    }

    // 匹配：目标方法 & 拦截处理方法的一个拦截注解
    private boolean matchMethodWithAnnoData(Method method, DataAnnoInfo dataAnnoInfo) {
        String name = method.getName();

        // 方法描述必须匹配，最常用，只要不匹配就直接返回
        if ( !CmnString.wildcardsMatch(dataAnnoInfo.annoValue, dataBuilderVars.methodDesc.get(method)) ) {
            return false; // 按通配符匹配方法描述失败：结果为不匹配(false)
        }

        // 目标方法注解检查
        boolean matchMethodAnno = Annotation.class.equals(dataAnnoInfo.annoMethodAnnotations[0]); // 默认true
        for ( Class<? extends Annotation> annotationClass : dataAnnoInfo.annoMethodAnnotations ) {
            if ( method.isAnnotationPresent(annotationClass) ) {
                matchMethodAnno = true;
                break;
            }
        }

        return !(!matchMethodAnno // 指定了目标方法需带的注解，但实际没有：结果为不匹配(false)
                // 指定目标类的包名范围，但本类不在包名范围内：结果为不匹配(false)
                || !matchPackage(dataAnnoInfo)
                // 指定目标类的类注解范围，但本类不带相关注解：结果为不匹配(false)
                || !matchTypeAnnotation(dataAnnoInfo)
                // 指定目标类的范围，但本类不在指定类范围内：结果为不匹配(false)
                || !matchClass(dataAnnoInfo)
                // 当前是父类方法，但声明的匹配范围不含父类方法：结果为不匹配(false)
                || !dataAnnoInfo.annoMatchSuperMethod && dataBuilderVars.methodSuperMap.get(method)
                // 当前是equals方法，但声明的匹配范围不含equals方法：结果为不匹配(false)
                || "equals".equals(name) && method.getParameterCount() == 1 && !dataAnnoInfo.annoMatchEquals
                // 当前是toString方法，但声明的匹配范围不含toString方法：结果为不匹配(false)
                || "toString".equals(name) && method.getParameterCount() == 0 && !dataAnnoInfo.annoMatchToString
                // 当前是hashCode方法，但声明的匹配范围不含hashCode方法：结果为不匹配(false)
                || "hashCode".equals(name) && method.getParameterCount() == 0 && !dataAnnoInfo.annoMatchHashCode);
    }

    // 目标类所带注解检查
    private boolean matchTypeAnnotation(DataAnnoInfo dataAnnoInfo) {
        if ( Annotation.class.equals(dataAnnoInfo.annoTypeAnnotations[0]) ) {
            return true;
        }

        for ( Class<? extends Annotation> annotationClass : dataAnnoInfo.annoTypeAnnotations ) {
            if ( dataBuilderVars.clas.isAnnotationPresent(annotationClass) ) {
                return true;
            }
        }
        return false;
    }

    // 目标类范围检查
    private boolean matchClass(DataAnnoInfo dataAnnoInfo) {
        if ( void.class.equals(dataAnnoInfo.annoClasses[0]) ) {
            return true;
        }

        for ( Class<?> cls : dataAnnoInfo.annoClasses ) {
            if ( dataBuilderVars.clas.equals(cls) ) {
                return true;
            }
        }
        return false;
    }

    // 目标类包名检查
    private boolean matchPackage(DataAnnoInfo dataAnnoInfo) {
        if ( dataAnnoInfo.annoPackages.length == 0 ) {
            return true;
        }

        String classPackage = dataBuilderVars.clas.getPackage().getName();
        for ( String pack : dataAnnoInfo.annoPackages ) {
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
            List<DataAopInfo> list = getMatchAopDataList(method, aopMethod);
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

    private void saveNormalResult(Method method, Method aopMethod, Object aopObj, Map<Method, List<DataMethodSrcInfo>> methodInfoMap, int aopOrder) {
        // 匹配成功，保存匹配结果
        log.debug("匹配【{}拦截{}】", aopMethod, method);

        // aop变量
        String varAopObj = dataBuilderVars.aopObjFieldMap.get(aopObj);
        if ( varAopObj == null ) {
            varAopObj = getAopVarName(aopObj);
            dataBuilderVars.aopObjFieldMap.put(aopObj, varAopObj);
        }

        // method变量
        String varMethod = dataBuilderVars.methodFieldMap.get(method);
        if ( varMethod == null ) {
            varMethod = getMethodVarName(method);
            dataBuilderVars.methodFieldMap.put(method, varMethod);
        }

        // 方法信息变量
        DataMethodSrcInfo dataMethodSrcInfo = new DataMethodSrcInfo();
        dataMethodSrcInfo.method = method;
        dataMethodSrcInfo.varMethod = varMethod;
        dataMethodSrcInfo.varAopObj = varAopObj;
        dataMethodSrcInfo.aopMethodReturnType = aopMethod.getReturnType();
        dataMethodSrcInfo.aopMethodName = aopMethod.getName();
        dataMethodSrcInfo.aopMethod = aopMethod;
        dataMethodSrcInfo.aopOrder = aopOrder;
        List<DataMethodSrcInfo> list = methodInfoMap.computeIfAbsent(method, val -> new ArrayList<>());
        list.add(dataMethodSrcInfo);
    }

    private void saveAroundResult(Method method, Method aopMethod, Object aopObj, Map<Method, List<DataMethodSrcInfo>> methodInfoMap, int aopOrder) {
        // 匹配成功，保存匹配结果
        log.debug("匹配【{}拦截{}】", aopMethod, method);

        // aop变量
        String varAopObj = dataBuilderVars.aopObjFieldMap.get(aopObj);
        if ( varAopObj == null ) {
            varAopObj = getAopVarName(aopObj);
            dataBuilderVars.aopObjFieldMap.put(aopObj, varAopObj);
        }

        // method变量
        String varMethod = dataBuilderVars.methodFieldMap.get(method);
        if ( varMethod == null ) {
            varMethod = getMethodVarName(method);
            dataBuilderVars.methodFieldMap.put(method, varMethod);
        }

        // 方法信息变量
        DataMethodSrcInfo dataMethodSrcInfo = new DataMethodSrcInfo();
        dataMethodSrcInfo.method = method;
        dataMethodSrcInfo.varMethod = varMethod;
        dataMethodSrcInfo.varAopObj = varAopObj;
        dataMethodSrcInfo.aopMethodReturnType = aopMethod.getReturnType();
        dataMethodSrcInfo.aopMethodName = aopMethod.getName();
        dataMethodSrcInfo.aopMethod = aopMethod;
        dataMethodSrcInfo.aopOrder = aopOrder;
        List<DataMethodSrcInfo> list = methodInfoMap.computeIfAbsent(method, val -> new ArrayList<>());
        list.add(dataMethodSrcInfo);

        // superInvoker变量
        String key = method.toGenericString() + methodInfoMap.get(method).size();
        String varSuperInvoker = dataBuilderVars.superInvokerFieldMap.get(key);
        if ( varSuperInvoker == null ) {
            varSuperInvoker = getSuperInvokerVarName(method, methodInfoMap.get(method).size());
            dataMethodSrcInfo.varSuperInvoker = varSuperInvoker;
            dataBuilderVars.superInvokerFieldMap.put(key, varSuperInvoker);
        }

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
        if ( isAround && AopUtil.hasReturnType(method) && !AopUtil.hasReturnType(aopMethod) ) {
            log.error("拦截处理漏返回类型缺失，应和目标方法一致");
            log.error("   目标方法：{}", method);
            log.error("   拦截处理：{}", aopMethod);
            throw new AopException("拦截错误，目标方法有返回值，拦截处理漏返回 (" + aopMethod + ")");
        }

        // 保存
        if ( isAround ) {
            dataBuilderVars.methodAroundAopMap.put(method, aopMethod);
        } else {
            dataBuilderVars.methodNormalAopMap.put(method, aopMethod);
        }

        // 中间类实现Around拦截处理，普通拦截继承中间类实现
        if ( dataBuilderVars.methodAroundAopMap.containsKey(method) ) {
            dataBuilderVars.methodAroundSuperSet.add(method);
        }

    }
}
