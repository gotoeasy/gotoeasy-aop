package top.gotoeasy.sample.aop.sample6;

import top.gotoeasy.framework.aop.AopContext;
import top.gotoeasy.framework.aop.annotation.After;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;

@Aop
public class Sample6AopEdit {

    private static final Log log = LoggerFactory.getLogger(Sample6AopEdit.class);

    @After("*.Sample6Bean.*")
    public void edit11(AopContext context) {
        String rs = (String)context.getResult();
        context.setResult(rs.replace("Hello", "Hi"));
        log.info("耗时：{}MS", System.currentTimeMillis() - context.getStartTime());
    }

}
