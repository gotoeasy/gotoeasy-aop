# `gotoeasy-aop`
基于JavaCompiler的AOP实现，提供另一种折中的AOP实现方式

- depend on `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/

## 青松的姿势
- 被代理类
```java
package top.gotoeasy.sample.aop.sample1;
public class Sample1Add {
	private int total = 0;
	public int add(int intVal) { // 拦截目标方法
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
import top.gotoeasy.framework.aop.Enhancer;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;

@Aop
public class Sample1Aop {

	@Before("*.Sample1Add.add(*)")
	public void before(Enhancer enhancer, Method method, int val) {
		System.err.println("Before add " + val);
	}
}
```
- 运行测试
```java
package top.gotoeasy.sample.aop.sample1;
import top.gotoeasy.framework.aop.EnhancerBuilder;

public class Sample1Main {

	public static void main(String[] args) {
		Sample1Aop aop = new Sample1Aop();
		Sample1Add enhancer = (Sample1Add)EnhancerBuilder.get()
					.setSuperclass(Sample1Add.class)
					.matchAop(aop).build();

		enhancer.add(1);
		enhancer.add(2);
		enhancer.add(3);

		System.err.println("Total: " + enhancer.getTotal());
	}
}

// 输出结果：
Before add 1
After add 1
Before add 2
After add 2
Before add 3
After add 3
Total: 6
```

## `gotoeasy-aop特性`
- 参考CGLIB，通过动态生成子类继承的方式实现代理，避免接口要求
- 仅匹配的拦截方法才会被重写实现，而不是把全部能继承的public方法都重写代理掉，再也不必担心hashCode/toString/equals等方法被代理的副作用了
- 进一步简化AOP程序的编写实现，拦截程序本身也没有接口的要求
- 有实用性，经过各种尝试后的一调再调，性能已是越战越勇状态

## `gotoeasy-aop初步成效列举`
Before拦截（Sample2），性能完全逼近原始调用（测试机环境：win8.1，64位，8G内存，i5-4200U）

|No.|调用次数|直接调用|gotoeasy-aop|CGLIB|
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


Around拦截（Sample3），随着调用次数的增多而会被优化，越战越勇的状态

|No.|调用次数|直接调用|gotoeasy-aop|CGLIB|
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

## GotoEasy系列
- `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/
- `gotoeasy-aop` http://github.com/gotoeasy/gotoeasy-aop/
- `gotoeasy-rmi` http://github.com/gotoeasy/gotoeasy-rmi/
- TODO
- TODO
- TODO

## LICENSE

    Copyright (c) 2018 ZhangMing (www.gotoeasy.top)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
