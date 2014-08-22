android-SpillOver
=================

#SpillOver是一个封装的网络通信框架

>集合Cache-control、Expires、ttl等多种缓存模式的http请求框架,保证response的全体系聚合

>并且SpillOver将提供更灵活性的接口来定制用户自己的request,保证request的全体系聚合;

>集成一些常用的android解决方案,比如range请求,保证快速开发实战;

>..


#现阶段:

>集合Cache-control、Expires、ttl等多种缓存模式的http请求框架,并设置是否缓存;

>post、get请求添加参数,设置请求头,并且可以设置请求优先级

>回调接口保证在main线程,就没必要使用线程同步

>内部并发自处理


#需要封装
  
>各种response的预处理,返回处理好的数据

>图片两种方式的缓存

>cookie、session的封装

>range的封装(还没想好怎么做)

>.. 
