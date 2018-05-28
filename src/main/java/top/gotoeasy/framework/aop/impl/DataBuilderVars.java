package top.gotoeasy.framework.aop.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataBuilderVars {

    // 拦截目标类
    public Class<?>                                clas                     = null;
    // 构造方法
    public Constructor<?>                          constructor              = null;
    // 构造方法参数
    public Object[]                                initargs                 = new Object[0];

    // 拦截处理对象列表
    public List<Object>                            aopList                  = new ArrayList<>();

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
    protected List<Method>                         methodAroundSuperList    = new ArrayList<>();

    // method描述
    protected Map<Method, String>                  methodDesc               = new HashMap<>();

    // 中间类最大数
    public int getMaxSizeOfMethodAroundList() {
        int max = 0;
        for ( Method method : methodAroundSuperList ) {
            int size = methodAroundSrcInfoMap.get(method).size();
            if ( max < size ) {
                max = size;
            }
        }
        return max;
    }
}
