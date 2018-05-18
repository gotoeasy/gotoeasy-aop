package top.gotoeasy.framework.aop.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import top.gotoeasy.framework.aop.util.AopUtil;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

public class MethodDesc {

    private static final Log log = LoggerFactory.getLogger(MethodDesc.class);

    public String hello(String name) {
        return "Hello " + name;
    }

    public int add(int val1, int val2) {
        return val1 + val2;
    }

    public int sum(Integer ... vals) {
        int rs = 0;
        for ( Integer integer : vals ) {
            if ( integer != null ) {
                rs += integer;
            }
        }
        return rs;
    }

    public String sum(String name, int ... vals) {
        int rs = 0;
        for ( int integer : vals ) {
            rs += integer;
        }
        return name + "=" + rs;
    }

    public String sum2(String name, Integer ... vals) {
        int rs = 0;
        for ( Integer integer : vals ) {
            if ( integer != null ) {
                rs += integer;
            }
        }
        return name + "=" + rs;
    }

    public String join(String[] strs) {
        String rs = "";
        for ( String str : strs ) {
            rs += str;
        }
        return rs;
    }

    public String join(String pre, String[] strs) {
        return pre + join(strs);
    }

    public int writeLog(String[][] strss) {
        log.debug("param:{}", (Object)strss);
        return strss.length;
    }

    public void writeLog(Class<?> clas, Object ... args) {
        log.debug("{},{}", clas, Arrays.asList(args));
    }

    public void writeLog(Map<String, Object> map, Class<? extends Annotation> clas) {
        log.debug(map.toString() + clas);
    }

    public static void main(String[] args) {

        Method[] methods = MethodDesc.class.getMethods();
        for ( Method method : methods ) {
            if ( Modifier.isFinal(method.getModifiers()) ) {
                System.err.println(AopUtil.getMethodDesc(MethodDesc.class, method));
            } else {
                System.out.println(AopUtil.getMethodDesc(MethodDesc.class, method));
            }
        }
    }
}
