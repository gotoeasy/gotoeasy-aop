package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import top.gotoeasy.framework.aop.exception.AopException;
import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.compiler.MemoryClassLoader;
import top.gotoeasy.framework.core.compiler.MemoryJavaCompiler;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.CmnBean;

/**
 * 代理对象创建器
 * <p>
 * 通过继承的方式对指定类进行增强、创建代理对象
 * </p>
 * 
 * @author 青松
 * @since 2018/04
 */
public class ProxyObjectCreater {

    private static final Log      log = LoggerFactory.getLogger(ProxyObjectCreater.class);

    private DataBuilderVars           dataBuilderVars;
    private AopDataMatcher          aopDataMatcher;
    private Src91EnhanceClassCreater     src91EnhanceClassCreater;
    private Src92SuperClassCreater src92SuperClassCreater;

    public ProxyObjectCreater(DataBuilderVars dataBuilderVars) {
        this.dataBuilderVars = dataBuilderVars;
        this.aopDataMatcher = new AopDataMatcher(dataBuilderVars);
        this.src91EnhanceClassCreater = new Src91EnhanceClassCreater(dataBuilderVars);
        this.src92SuperClassCreater = new Src92SuperClassCreater(dataBuilderVars);
    }

    /**
     * 创建代理对象
     * 
     * @param <T> 被代理类
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T create() {

        // 设定拦截
        aopDataMatcher.matchAops();

        // final类或没有匹配的拦截时，不做增强处理
        try {
            if ( Modifier.isFinal(dataBuilderVars.clas.getModifiers()) || dataBuilderVars.aopObjSeq == 1 ) {
                log.warn("final类或没有匹配的拦截时，不做增强处理：{}", dataBuilderVars.clas);
                if ( dataBuilderVars.constructor == null ) {
                    return (T)dataBuilderVars.clas.newInstance();
                } else {
                    return (T)dataBuilderVars.constructor.newInstance(dataBuilderVars.initargs);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AopException(e);
        }

        // Around拦截排序
        for ( Method method : dataBuilderVars.methodAroundSuperList ) {
            dataBuilderVars.methodAroundSrcInfoMap.get(method).sort((srcInfo1, srcInfo2) -> srcInfo2.aopOrder - srcInfo1.aopOrder);
        }

        // 代理类的类名
        String className = AopUtil.getEnhanceName(dataBuilderVars.clas);
        Map<String, String> map = new HashMap<>();
        // 创建中间类源码
        int maxSize = dataBuilderVars.getMaxSizeOfMethodAroundList();
        for ( int i = 0; i < maxSize; i++ ) {
            map.put(AopUtil.getAroundMiddleClassName(dataBuilderVars.clas, maxSize, i), src92SuperClassCreater.createAroundMiddleClassCode(maxSize, i));
        }
        // 创建代理类源码
        map.put(className, src91EnhanceClassCreater.createEnhanceClassCode());

        // 编译
        MemoryJavaCompiler compiler = new MemoryJavaCompiler();
        compiler.compile(map);

        // 创建代理对象
        Object proxyObject;
        try ( MemoryClassLoader loader = new MemoryClassLoader() ) {
            if ( dataBuilderVars.constructor != null && dataBuilderVars.constructor.getParameterCount() == 1
                    && (dataBuilderVars.constructor.isVarArgs() || dataBuilderVars.constructor.getParameterTypes()[0].isArray()) ) {
                // 单个可变参数或数组参数时要特殊处理
                proxyObject = loader.loadClass(className).getDeclaredConstructors()[0].newInstance((Object)dataBuilderVars.initargs);
            } else {
                proxyObject = loader.loadClass(className).getDeclaredConstructors()[0].newInstance(dataBuilderVars.initargs);
            }
        } catch (Exception e) {
            throw new AopException(e);
        }

        // 设定拦截处理对象
        dataBuilderVars.aopObjFieldMap.keySet().forEach(aopObj -> CmnBean.setFieldValue(proxyObject, dataBuilderVars.aopObjFieldMap.get(aopObj), aopObj));

        // 返回代理对象
        return (T)proxyObject;
    }

}
