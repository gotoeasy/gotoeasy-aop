package top.gotoeasy.framework.aop

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.sample.aop.sample1.Sample1Add
import top.gotoeasy.sample.aop.sample1.Sample1Aop


class EnhanceBuilderTest   extends Specification {

    @Test
    public void test() {
        expect:
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
