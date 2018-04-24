# `gotoeasy-aop`
基于JavaCompiler的AOP实现，目的为增加一种折中的AOP实现方式

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

## 为什么还做AOP轮子
- JDK的代理必须要有接口，太鸡肋了，有谁会给自己的业务方法全部都加上接口。。。
- CGLIB的代理只要被代理类能继承就可以，非常强悍、优越、稳定、广泛使用，但是，很多时候会为了让hashCode/toString/equals等方法不被代理，而去做些本来没有的事。。。
- AspectJ的代理，编译的过程就被植入它的代理逻辑，这需要稍微调整编译环境，更需要有个能否接受被植入的态度
- 通过修改字节码实现代理，如利用javasisst或ASM修改字节码，比较熟悉的情况下还可以直接修改字节码，虽然费点劲就行代码也没多少，但就怕java版本升级后不兼容又要重来一次

#### `所以AOP轮子要达到的目标就是`
- 参考CGLIB使用继承的方式实现代理，避免接口要求
- 被代理的方法是能选择的，而不是把全部能继承的public方法都代理掉
- 有实用性，且性能要在可接受范围内
- 进一步简化AOP程序的编写实现

## `gotoeasy-aop初步成效列举`
- Before拦截，性能明显优于CGLIB，原因很简单，gotoeasy-aop运行时就是直接调用（测试机环境：win8.1，64位，8G内存，i5-4200U）
No.|调用次数|直接调用|gotoeasy-aop|CGLIB
-|-|-|-|-
1-1|100,000|4MS|7MS|14MS
1-2|100,000|5MS|6MS|32MS
1-3|100,000|5MS|6MS|13MS
2-1|1,000,000|11MS|13MS|78MS
2-2|1,000,000|16MS|9MS|55MS
2-3|1,000,000|10MS|10MS|58MS
3-1|10,000,000|29MS|33MS|346MS
3-2|10,000,000|10MS|36MS|361MS
3-3|10,000,000|11MS|13MS|343MS
4-1|100,000,000|15MS|21MS|2694MS
4-2|100,000,000|14MS|33MS|2438MS
4-3|100,000,000|13MS|17MS|2647MS

## GotoEasy系列
- `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/
- `gotoeasy-rmi` http://github.com/gotoeasy/gotoeasy-rmi/
- `gotoeasy-aop` http://github.com/gotoeasy/gotoeasy-aop/
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
