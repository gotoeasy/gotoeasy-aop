package top.gotoeasy.framework.aop

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.sample.aop.sample1.Sample1Add
import top.gotoeasy.sample.aop.sample1.Sample1Aop
import top.gotoeasy.sample.aop.sample1.Sample1Main
import top.gotoeasy.sample.aop.sample2.Sample2Main
import top.gotoeasy.sample.aop.sample3.Sample3Main
import top.gotoeasy.sample.aop.sample4.Sample4Main
import top.gotoeasy.sample.aop.sample5.Sample5Main


class EnhanceBuilderTest   extends Specification {


    @Test
    public void test() {

        expect:

        // 把例子都跑一遍
        Sample1Main.main(null)
        Sample2Main.main(null)
        Sample3Main.main(null)
        Sample4Main.main(null)
        Sample5Main.main(null)

        Sample1Aop aop = new Sample1Aop();
        Sample1Add enhance = (Sample1Add)EnhanceBuilder.get().setSuperclass(clas).matchAop(aop).build();

        enhance.add(1);
        enhance.add(2);
        enhance.add(3);

        enhance.getTotal() == result

        where:
        clas | result
        Sample1Add.class  | 6
    }
}
