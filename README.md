# `gotoeasy-aop`
基于JavaCompiler的AOP实现，目的为增加一种折中的AOP实现方式

- depend on `gotoeasy-core` http://github.com/gotoeasy/gotoeasy-core/

## 为什么还做AOP轮子
- JDK的代理必须要有接口，太鸡肋了，有谁会给自己的业务方法全部都加上接口。。。
- CGLIB的代理只要被代理类能继承就可以，非常强悍、优越、稳定、广泛使用，但是，很多时候会为了让hashCode/toString/equals等方法不被代理，而去做些本来没有的事。。。
- AspectJ的代理，编译的过程就被植入它的代理逻辑，这需要稍微调整编译环境，更需要有个能否接受被植入的态度
- 通过修改字节码实现代理，如利用javasisst或ASM修改字节码，比较熟悉的情况下还可以直接修改字节码，虽然费点劲就行代码也没多少，但就怕java版本升级后不兼容又要重来一次

#### 所以AOP轮子要达到的目标就是
- 参考CGLIB使用继承的方式实现代理，避免接口要求
- 被代理的方法是能选择的，而不是把全部能继承的public方法都代理掉
- 性能在尚可接受的范围内，不必比CGLIB优秀，但要有实用性
- 进一步简化AOP程序的编写实现

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
