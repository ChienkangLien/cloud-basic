# Spring Cloud
在此範例選用版本

|  | Version |
| -------- | -------- |
| Spring Cloud | 2023.0.0 |
| Spring Boot | 3.2.0 |
| Spring Cloud Alibaba | 2022.0.0.0-RC2 |
| Java | 17+ |
| Maven | 3.9+ |
| Mysql | 8.0+ |

服務註冊與發現：Consul、ALibaba Nacos、~~Eureka~~
負載均衡和服務調用：LoadBalancer、OpenFeign、~~Ribbon~~
分布式事務：Alibaba Seata
服務熔斷和降級：Resilience4J(Circuit Breaker的實現)、Alibaba Sentinel、~~Hystrix~~
服務鏈路追蹤：Micrometer Tracing、~~Sleuth + Zipkin~~
服務網關：Gateway、~~Zuul~~
分布式配置管理：Consul、Alibaba Nacos、~~Config + Bus~~

## 初始架構
父工程：cloud2024
微服務提供者，支付模塊：cloud-provider-payment8001
微服物消費者，訂單模塊：cloud-consumer-order80
自製工具包(使用前要先`maven install`成jar)：cloud-api-commons

## Consul
為什麼要引入服務註冊中心：實現微服務之間的動態註冊與發現。


| 組件 | 語言 | CAP | 服務健康檢查 | 對外暴露接口 | spring cloud 集成 |
| -------- | -------- | -------- | -------- | -------- | -------- |
| Eureka | Java | AP | 可配支持 | HTTP | YES |
| Consul | Go | CP | 支持 | HTTP/DNS | YES |
| Zookeeper | Java | CP | 支持 | 客戶端 | YES |

為什麼不再使用Eureka：Eureka 停止更新、註冊中心需獨立且和微服務解耦。

Consul  是一個開源的分布式服務發現和配置管理系統，由HashiCorp 公司用Go 語言開發。
* 服務發現：提供HTTP 和DNS 兩種發現方式
* 健康監測：支持多種方式，HTTP、TCP、Docker、Shell腳本定制
* KV存儲：Key、Value 的存儲方式
* 多數據中心：支持多數據中心
* 可視化Web 介面

### 安裝
在此使用Windows 版(1.17.1) https://developer.hashicorp.com/consul/install
1. 解壓後cmd 執行`consul --version` 得到版本號即成功
2. 開發模式啟動`consul agent -dev`
3. 訪問 http://localhost:8500

### 實作 服務註冊
1. POM：引用`spring-cloud-starter-consul-discovery`
2. YML：配置`spring.cloud.consul`
3. 啟動類：註解`@EnableDiscoveryClient`
4. Controller (cloud-consumer-order80)：URL 使用服務註冊中心上的微服務名稱
5. 配置修改RestTemplateConfig (cloud-consumer-order80)：註解`@LoadBalanced`，這個註解屬於Spring Cloud LoadBalancer、但是在這裡要先加上才能起作用

### 實作 分布式配置
在cloud-provider-payment8001 驗證有效即可
1. POM：引用`spring-cloud-starter-consul-config`、`spring-cloud-starter-bootstrap`
2. 新增配置文件bootstrap.yml(優先級大於application.yml)：將application.yml 中關於spring cloud 內容一併移過去
3. consul 服務器key/value 配置：Key / Values 中建立三個文件 config/cloud-payment-service/data、config/cloud-payment-service-prod/data、config/cloud-payment-service-dev/data，對應YML 中spring.profiles.active 的值
4. 啟動類：註解`@RefreshScope`
5. Controller 驗證

### 實作 數據持久化並註冊為Windows 服務
1. consul.exe 安裝目錄下新建mydata 文件夾以及consul_start.bat 文件
```shell=
@echo.服務啟動...
@echo off
@sc create Consul binpath= "D:\cloudDev\consul\consul.exe agent -server -ui -bind=127.0.0.1 -client=0.0.0.0 -bootstrap-expect 1 -data-dir D:\cloudDev\consul\mydata	"
@net start Consul
@sc config Consul start= AUTO
@echo.Consul start is OK...success
@pause
```
2. 右鍵管理員權限運行即可將以上bat 運行，工具管理員也可看見此服務，重啟服務KV 也不會遺失


## LoadBalancer
為什麼不再使用Ribbon：Ribbon 也停止更新

Spring Cloud LoadBalancer 是Spring Cloud 官方提供的一個開源的客戶端負載均衡器，包含在Spring Cloud Commons 中。相較於Ribbon，Spring Cloud LoadBalancer 不僅支持RestTemplate、還支持WebClient(Spring Web Flux 中提供的功能，實現響應式異步請求)。

客戶端負載均衡器（Client-side Load Balancer）v.s. 服務器端負載均衡器（Server-side Load Balancer）
* Nginx 是服務器負載均衡，客戶端所有請求都會交給Nginx，由Nginx 實現轉發請求。
* Spring Cloud LoadBalancer 是客戶端負載均衡，在調用微服務時，會在註冊中心上獲取註冊訊息服務列表並緩存到JVM 本地，從而在本地實現RPC 遠程服務調用技術。

負載算法默認兩種
1. RoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer
2. RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer

### 實作
1. 複製出cloud-provider-payment8002
2. 啟動Consul(`consul agent -dev`)即可看見兩個instance 註冊進cloud-payment-service
3. POM(cloud-consumer-order80)：引用`spring-cloud-starter-loadbalancer`
4. Controller 驗證(cloud-consumer-order80)：LoadBalancer + 動態獲取所有上線服務列表
5. (可選)更改負載策略(cloud-consumer-order80)：RestTemplateConfig

## OpenFeign
OpenFeign 是一個聲明式web 服務客戶端，只需創建一個Rest 接口並在該接口上添加註解`@FeignClient` 即可，OpenFeign 基本上就是當前微服務之間調用的事實標準。 

* 可插拔的註解支持，包括Feign 註解和JAX-RS 註解
* 支持可插拔的HTTP 編碼器和解碼器
* 支持Sentinel 和它的Fallback
* 支持Spring Cloud LoadBalancer 的負載均衡
* 支持HTTP 請求和響應的壓縮

### 實作
與原先的RestTemplate 調用方式區隔，另外建立cloud-consumer-feign-order80 模塊
1. POM(cloud-consumer-feign-order80)：複製原80的，引用`spring-cloud-starter-openfeign`，移除`spring-cloud-starter-loadbalancer`
2. 啟動類(cloud-consumer-feign-order80)：複製原80的，註解`@EnableFeignClients`
3. POM(cloud-api-commons)：引用`spring-cloud-starter-openfeign`
4. 新建服務接口PayFeignApi(cloud-api-commons)
5. 新建Controller(cloud-consumer-feign-order80)
6. 驗證：啟動Consul、微服務8001、微服務8002、feign-order80

### 高級特性
* 超時控制：默認60秒拋出超時錯誤，驗證 getById id=1；可進一步從YML 配置connectTimeout/readTimeout。YML(cloud-consumer-feign-order80)、指定
* 重試機制：