# `gotoeasy-aop`
基于JavaCompiler的AOP实现，目的为增加一种折中的AOP实现方式

- depend on `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/

## 为什么还做AOP轮子
- JDK的代理必须要有接口，太鸡肋了，有谁会给自己的业务方法全部都加上接口。。。
- CGLIB的代理只要被代理类能继承就可以，非常强悍、优越、稳定、广泛使用，但是，很多时候会为了让hashCode/toString/equals等方法不被代理，而去做些本来没有的事。。。
- AspectJ的代理，编译的过程就被植入它的代理逻辑，这需要稍微调整编译环境，更需要有个能否接受被植入的态度
- 通过修改字节码实现代理，如利用javasisst或ASM修改字节码，比较熟悉的情况下还可以直接修改字节码，虽然费点劲就行代码也没多少，但就怕java版本升级后不兼容又要重来一次

#### `所以AOP轮子要达到的目标就是`
- 参考CGLIB使用继承的方式实现代理，避免接口要求
- 被代理的方法是能选择的，而不是把全部能继承的public方法都代理掉
- 性能在尚可接受的范围内，不必比CGLIB优秀，但要有实用性
- 进一步简化AOP程序的编写实现

## `gotoeasy-aop初步成效`
- Java8环境，1千万次调用，仅常用的前置拦截或后置拦截的话，性能有时逼近原始调用，通常会优于CGLIB
```java
  // 直接在继承的方法中写入以下类似代码实现代理功能，所以性能不会差哪去
  java.lang.String rs;
  ((AopBefore)aopObj1).before(this, method, p0);
  rs = super.hello(p0);
  ((AopAfter)aopObj2).after(this, method, p0);
  return rs;
```
```java
  // catch和finally也要拦截的话，类似代码如下，由于try-catch-finally的缘故性能会降低，总体上也接近CGLIB
  java.lang.String rs;
  try {
      ((AopBefore)aopObj1).before(this, method, p0);
      rs = super.hello(p0);
      ((AopAfter)aopObj2).after(this, method, p0);
      return rs;
  } catch (Throwable t) {
      ((AopThrowing)aopObj3).throwing(this, method, t, p0);
      throw new RuntimeException(t);
  } finally {
      ((AopLast)aopObj4).last(this, method, p0);
  }
```
- Java8环境，1千万次调用，Around拦截，性能比CGLIB差一半，尚能使用
```java
    // Around拦截类似代码如下，性能损失主要在于super方法的反射调用，而CGLIB做了fastClass优化表现优秀
    // 不想修改字节码，按MethodHandle方式的super方法调用的话，性能逼近原始调用
    // 可惜MethodHandle方式太不稳定，时对时错时快时慢，希望它在高版本能表现出色
    // Java高版本的性能越来越好，方法反射调用应用普遍，很可能以后会被出色优化，这也是目前简单采用反射调用的原因
    @Override
    public final java.lang.String hello(java.lang.String p0) {
        String desc = "public java.lang.String top.gotoeasy.sample.aop.MyTatrget.hello(java.lang.String)";
        AroundPoint point = new AroundPoint(this, top.gotoeasy.sample.aop.MyTatrget.class, desc, p0);
        return (java.lang.String) ((AopAround)aopObj1).around(point);
    }

    public java.lang.String gotoeasy$di9c5modhv5tbt586tih0s4gl(java.lang.String p0){
        return super.hello(p0);
    }
```


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
