package top.gotoeasy.framework.aop.util;

/**
 * 代理类的代码模板类
 * @since 2018/04
 * @author 青松
 */
public class SourceTemplate {

	/**
	 * 环绕拦截代码
	 * @return 代码
	 */
	public static String getSourceAround() {
		StringBuilder sb = new StringBuilder();
		sb.append("    @Override").append("\n");
		sb.append("    {methodDefine} {").append("\n");
		sb.append("        String desc = \"{desc}\";").append("\n");
		sb.append("        AroundPoint point = new AroundPoint(this, {superClass}.class, desc, {parameterNames});").append("\n");
		sb.append("        {return} ((AopAround){aopObj}).around(point);").append("\n");
		sb.append("    }").append("\n");
		return sb.toString();
	}

	/**
	 * 类代码
	 * @return 代码
	 */
	public static String getSourceClass() {
		StringBuilder sb = new StringBuilder();
		sb.append("package {pack};").append("\n");
		sb.append("\n");
		sb.append("import java.lang.reflect.*;").append("\n");
		sb.append("import java.util.*;").append("\n");
		sb.append("import top.gotoeasy.framework.aop.*;").append("\n");
		sb.append("\n");
		sb.append("public class {simpleName}$$gotoeasy$$ extends {superClass} {").append("\n");
		sb.append("\n");
		sb.append("    private Map<String, Method>	map	= new HashMap<>();").append("\n");
		sb.append("\n");
		sb.append("{field}").append("\n");
		sb.append("\n");
		sb.append("    private Method getMethodByDesc(String desc){").append("\n");
		sb.append("        Method rs = map.get(desc);").append("\n");
		sb.append("        if ( rs == null ) {").append("\n");
		sb.append("            for ( Method th : {superClass}.class.getDeclaredMethods() ) {").append("\n");
		sb.append("                if ( th.toGenericString().equals(desc) ) {").append("\n");
		sb.append("                    rs = th;").append("\n");
		sb.append("                    map.put(desc, th);").append("\n");
		sb.append("                    break;").append("\n");
		sb.append("                }").append("\n");
		sb.append("            }").append("\n");
		sb.append("        }").append("\n");
		sb.append("        return rs;").append("\n");
		sb.append("    }").append("\n");
		sb.append("\n");
		sb.append("{method}").append("\n");
		sb.append("{callSuper}").append("\n");
		sb.append("}").append("\n");
		return sb.toString();
	}

	/**
	 * 方法代码
	 * @return 代码
	 */
	public static String getSourceMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("    @Override").append("\n");
		sb.append("    {methodDefine} {").append("\n");
		sb.append("        Method method = getMethodByDesc(\"{desc}\");").append("\n");
		sb.append("\n");
		sb.append("        {returnType} rs;").append("\n");
		sb.append("{commentOut}        try {").append("\n");
		sb.append("{beforeCode}").append("\n");
		sb.append("            rs = super.{methodName}({parameterNames});").append("\n");
		sb.append("{afterCode}").append("\n");
		sb.append("            return rs;").append("\n");
		sb.append("{commentOut}        } catch (Throwable t) {").append("\n");
		sb.append("{commentOut}{throwingCode}").append("\n");
		sb.append("{commentOut}            throw new RuntimeException(t);").append("\n");
		sb.append("{commentOut}        } finally {").append("\n");
		sb.append("{commentOut}{lastCode}").append("\n");
		sb.append("{commentOut}        }").append("\n");
		sb.append("\n");
		sb.append("    }").append("\n");
		return sb.toString();
	}

	/**
	 * void方法代码
	 * @return 代码
	 */
	public static String getSourceVoidMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("    @Override").append("\n");
		sb.append("    {methodDefine} {").append("\n");
		sb.append("        Method method = getMethodByDesc(\"{desc}\");").append("\n");
		sb.append("\n");
		sb.append("{commentOut}        try {").append("\n");
		sb.append("{beforeCode}").append("\n");
		sb.append("            super.{methodName}({parameterNames});").append("\n");
		sb.append("{afterCode}").append("\n");
		sb.append("{commentOut}        } catch (Throwable t) {").append("\n");
		sb.append("{commentOut}{throwingCode}").append("\n");
		sb.append("{commentOut}            throw new RuntimeException(t);").append("\n");
		sb.append("{commentOut}        } finally {").append("\n");
		sb.append("{commentOut}{lastCode}").append("\n");
		sb.append("{commentOut}        }").append("\n");
		sb.append("\n");
		sb.append("    }").append("\n");
		return sb.toString();
	}

}
