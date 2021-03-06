[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3919db3635b04ff399228c478bdf2343)](https://www.codacy.com/app/gotoeasy/gotoeasy-aop?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gotoeasy/gotoeasy-aop&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/3919db3635b04ff399228c478bdf2343)](https://www.codacy.com/app/gotoeasy/gotoeasy-aop?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=gotoeasy/gotoeasy-aop&amp;utm_campaign=Badge_Coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/top.gotoeasy/gotoeasy-aop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/top.gotoeasy/gotoeasy-aop)
[![Javadocs](https://www.javadoc.io/badge/top.gotoeasy/gotoeasy-aop.svg)](https://www.javadoc.io/doc/top.gotoeasy/gotoeasy-aop)
[![License](https://img.shields.io/badge/License-apache2.0-brightgreen.svg)](https://github.com/gotoeasy/gotoeasy-aop/blob/master/LICENSE)

# `gotoeasy-aop`
基于JavaCompiler的继承方式AOP实现，在性能优良的基础上，提供更多的简易性。


Maven使用
```xml
<dependency>
    <groupId>top.gotoeasy</groupId>
    <artifactId>gotoeasy-aop</artifactId>
    <version>x.y.z</version>
</dependency>
```

Gradle使用
```gradle
compile group: 'top.gotoeasy', name: 'gotoeasy-aop', version: 'x.y.z'
```

- depend on `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/

## 青松的姿势
- 被代理类
```java
package top.gotoeasy.sample.aop.sample1;
public class Sample1Add {
	private int total = 0;
	public int add(int intVal) {
		total += intVal;
		return total;
	}
	public int getTotal() {
		return total;
	}
}
```
- 拦截处理类
```java
package top.gotoeasy.sample.aop.sample1;
import java.lang.reflect.Method;
import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;

@Aop
public class Sample1Aop {
    private int count;

    @Before("*.Sample1Add.add(*)")
    public void before(Enhance enhance, Method method, int val) {
        System.err.println("[Sample1Aop]Before add " + val);
        count++;
    }

    public int getCount() {
        return count;
    }
}
```
- 运行测试
```java
package top.gotoeasy.sample.aop.sample1;

import top.gotoeasy.framework.aop.EnhanceBuilder;

public class Sample1Main {

    public static void main(String[] args) {
        Sample1Aop aop = new Sample1Aop();
        Sample1Add enhance = EnhanceBuilder.get()
                                .setSuperclass(Sample1Add.class)
				.matchAop(aop)
				.build();

        enhance.add(1);
        enhance.add(2);
        enhance.add(3);

        System.err.println("Total: " + enhance.getTotal() + ", Count=" + aop.getCount());
    }

}

// 输出结果：
[Sample1Aop]Before add 1
[Sample1Aop]Before add 2
[Sample1Aop]Before add 3
Total: 6, Count = 3
```

## `gotoeasy-aop特性`
- 通过动态生成子类继承的方式实现代理（不能继承的不支持），避免目标类的接口要求
- 提供丰富灵活的目标方法匹配方式，拦截你想要拦截的方法，不少拦也不多拦
- 目标方法可以被多个拦截处理多次拦截，提供拦截顺序设定功能，有序管理拦截处理程序的执行
- 简化AOP程序的编写实现，拦截程序本身也没有接口的要求
- 针对拦截处理的方法参数，提供高度的灵活性，按需编写，清爽，同时也有效降低参数转换的性能损失
- 当前实现是经过各种实验后的选定，性能已是越战越勇状态

## `gotoeasy-aop性能测试列举`
Around拦截（Sample2），（测试机环境：Java8，win8.1，64位，8G内存，i5-4200U）
随着调用次数的增多可以被优化，消耗时间并没有直线上升

|No.|调用次数|直接调用|gotoeasy-aop|cglib3.2.4|
|----------|----------|----------|----------|----------|
|1-1|100,000|5MS|16MS|11MS|
|1-2|100,000|4MS|20MS|18MS|
|1-3|100,000|5MS|17MS|19MS|
|2-1|1,000,000|7MS|26MS|107MS|
|2-2|1,000,000|7MS|26MS|63MS|
|2-3|1,000,000|9MS|24MS|61MS|
|3-1|10,000,000|10MS|100MS|322MS|
|3-2|10,000,000|9MS|95MS|387MS|
|3-3|10,000,000|10MS|94MS|369MS|
|4-1|100,000,000|13MS|684MS|2454MS|
|4-2|100,000,000|13MS|674MS|2293MS|
|4-3|100,000,000|14MS|666MS|2306MS|

简单常用的Before拦截（Sample3），性能已经完全逼近原始调用

|No.|调用次数|直接调用|gotoeasy-aop|cglib3.2.4|
|----------|----------|----------|----------|----------|
|1-1|100,000|4MS|7MS|14MS|
|1-2|100,000|5MS|6MS|32MS|
|1-3|100,000|5MS|6MS|13MS|
|2-1|1,000,000|11MS|13MS|78MS|
|2-2|1,000,000|16MS|9MS|55MS|
|2-3|1,000,000|10MS|10MS|58MS|
|3-1|10,000,000|29MS|33MS|346MS|
|3-2|10,000,000|10MS|36MS|361MS|
|3-3|10,000,000|11MS|13MS|343MS|
|4-1|100,000,000|15MS|21MS|2694MS|
|4-2|100,000,000|14MS|33MS|2438MS|
|4-3|100,000,000|13MS|17MS|2647MS|


## GotoEasy系列
- `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/
- `gotoeasy-aop` http://github.com/gotoeasy/gotoeasy-aop/
- `gotoeasy-rmi` http://github.com/gotoeasy/gotoeasy-rmi/
- `gotoeasy-rmi` http://github.com/gotoeasy/gotoeasy-orm/
- TODO
- TODO
