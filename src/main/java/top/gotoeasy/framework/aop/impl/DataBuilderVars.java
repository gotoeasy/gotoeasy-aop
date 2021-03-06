package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 公用变量类
 * 
 * @author 青松
 * @since 2018/04
 */
public class DataBuilderVars {

    // 拦截目标类
    protected Class<?>                             clas                     = null;
    // 构造方法
    protected Constructor<?>                       constructor              = null;
    // 构造方法参数
    protected Object[]                             initargs                 = new Object[0];

    // 拦截处理对象列表
    protected List<Object>                         aopList                  = new ArrayList<>();

    // aopObj变量编号
    protected int                                  aopObjSeq                = 1;
    protected Map<Object, String>                  aopObjFieldMap           = new LinkedHashMap<>();

    // method变量编号
    protected int                                  methodSeq                = 1;
    protected Map<Method, String>                  methodFieldMap           = new LinkedHashMap<>();

    // superInvoker变量编号
    protected int                                  superInvokerSeq          = 1;
    // 方法DESC+SEQ：superInvoker变量名
    protected Map<String, String>                  superInvokerFieldMap     = new LinkedHashMap<>();

    // Around独占拦截， Map<拦截目标方法：拦截处理方法信息>
    protected Map<Method, DataMethodSrcInfo>       methodAroundSrcInfoMap1  = new LinkedHashMap<>();

    // 普通非独占拦截， Map<拦截目标方法：List<拦截处理方法信息>>
    protected Map<Method, List<DataMethodSrcInfo>> methodBeforeSrcInfoMap   = new LinkedHashMap<>();
    protected Map<Method, List<DataMethodSrcInfo>> methodAfterSrcInfoMap    = new LinkedHashMap<>();
    protected Map<Method, List<DataMethodSrcInfo>> methodThrowingSrcInfoMap = new LinkedHashMap<>();
    protected Map<Method, List<DataMethodSrcInfo>> methodLastSrcInfoMap     = new LinkedHashMap<>();
    protected Map<Method, List<DataMethodSrcInfo>> methodAroundSrcInfoMap   = new LinkedHashMap<>();

    // Map<拦截目标方法：拦截处理方法>
    protected Map<Method, Method>                  methodNormalAopMap       = new LinkedHashMap<>();
    protected Map<Method, Method>                  methodAroundAopMap       = new LinkedHashMap<>();

    // 拦截目标方法是否属于父类方法
    protected Map<Method, Boolean>                 methodSuperMap           = new LinkedHashMap<>();

    // 拦截目标方法是否有拦截上下文参数要求（值:Before/After/Throwing/Last的组合拼接）
    protected Map<Method, String>                  aopContextMap            = new HashMap<>();

    // 中间类所需实现的环绕拦截方法
    protected Set<Method>                          methodAroundSuperSet     = new HashSet<>();

    // method描述
    protected Map<Method, String>                  methodDesc               = new HashMap<>();

    // 要被作为参数使用的方法列表(方法：方法变量名)
    protected Map<Method, String>                  argMethodMap             = new HashMap<>();

    /**
     * 方法单位的最大Around拦截数
     * <p>
     * 数量即所需要的中间类数量
     * </p>
     * 
     * @return 方法单位的最大Around拦截数
     */
    public int getMaxMethodAroundCount() {
        int max = 0;
        for ( Method method : methodAroundSuperSet ) {
            int size = methodAroundSrcInfoMap.get(method).size();
            if ( max < size ) {
                max = size;
            }
        }
        return max;
    }

    /**
     * 拦截目标类
     * 
     * @param clas 拦截目标类
     */
    public void setTargetClass(Class<?> clas) {
        this.clas = clas;
    }

    /**
     * 目标类构造方法
     * 
     * @param constructor 目标类构造方法
     */
    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * 目标类构造方法参数
     * 
     * @param initargs 目标类构造方法参数
     */
    public void setInitargs(Object[] initargs) {
        this.initargs = initargs;
    }

    /**
     * 拦截处理对象列表
     * 
     * @return 拦截处理对象列表
     */
    public List<Object> getAopList() {
        return aopList;
    }

}
