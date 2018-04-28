package top.gotoeasy.sample.aop.sample5;

import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.CmnDate;

@Aop
public class Sample5AopEdit {

    private static final Log log = LoggerFactory.getLogger(Sample5AopEdit.class);

    @SuppressWarnings("unchecked")
    @After("*.Sample5Bean.getData()")
    public void edit11(AopContext context) {
        List<Map<String, Object>> list = (List<Map<String, Object>>)context.getResult();
        log.debug("修改前：{}", list);

        list.forEach(map -> {
            if ( map.get("No.").equals(1) ) {
                map.put("Date", CmnDate.format(map.get("Date"), "yy年MM月dd日"));
            }
        });

        log.info("修改后：{}", list);
    }

    @SuppressWarnings("unchecked")
    @After("*.Sample5Bean.getData()")
    public void edit12(AopContext context) {
        List<Map<String, Object>> list = (List<Map<String, Object>>)context.getResult();
        log.debug("修改前：{}", list);

        list.forEach(map -> {
            if ( map.get("No.").equals(2) ) {
                map.put("Date", CmnDate.format(map.get("Date"), "yyyy年MM月dd日"));
            }
        });

        log.info("修改后：{}", list);
    }

    @SuppressWarnings("unchecked")
    @After("*.Sample5Bean.getData()")
    public void edit13(AopContext context) {
        List<Map<String, Object>> list = (List<Map<String, Object>>)context.getResult();
        log.debug("修改前：{}", list);

        list.forEach(map -> {
            if ( map.get("No.").equals(3) ) {
                map.put("Name", "雷锋");
                map.put("Date", CmnDate.format(map.get("Date"), "M月dd日"));
            }
        });

        log.info("修改后：{}", list);
    }

    @Before(value = "*.Sample5Bean.*", order = 0)
    public void befoer1() {
        log.debug("要求被最先执行的权限检查befoer1");
    }

    @Before(value = "*.Sample5Bean.*")
    public void befoer2() {
        log.debug("无序要求的befoer2");
    }

    @Before(value = "*.Sample5Bean.*")
    public void befoer3() {
        log.debug("无序要求的befoer3");
    }

    @Before(value = "*.Sample5Bean.*")
    public void befoer4() {
        log.debug("无序要求的befoer4");
    }

    @Before(value = "*.Sample5Bean.*")
    public void befoer5() {
        log.debug("无序要求的befoer5");
    }

    @After(value = "*.Sample5Bean.*", order = 999)
    public void after(AopContext context) {
        log.debug("最后统计执行时间，共计花费{}毫秒！", System.currentTimeMillis() - context.getStartTime());
    }

}
