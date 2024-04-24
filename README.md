# cloud-basic
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
為什麼不再使用Ribbon：Ribbon 也停止更新。

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
* 天然支持Spring Cloud LoadBalancer 的負載均衡(不需要像cloud-consumer-order80 加註`@LoadBalanced`)
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
* 超時控制：默認60秒拋出超時錯誤，透過getById id=1 驗證(8001/8002 Controller 需改寫)；可進一步從YML 配置connectTimeout/readTimeout。YML(cloud-consumer-feign-order80)
* 重試機制：默認關閉，開啟要新增FeignConfig(cloud-consumer-feign-order80)，一樣透過getById id=1 驗證
* 默認HttpClient 可以修改：默認使用JDK 自帶的HttpURLConnection 發送HTTP 請求，官網建議替換性能佳的Apache HttpClient5。POM 引入`httpclient5`、`feign-hc5`，YML 配置`spring.cloud.openfeign.httpclient.hc5.enabled`
* 請求/響應壓縮(GZIP)：YML 配置`spring.cloud.openfeign.compression`
* 日誌紀錄：級別NONE(默認)/BASIC/HEADERS/FULL，修改FeignConfig，YML 配置`logging.level`

## Resilience4J
為什麼不再使用Hystrix：Hystrix 也停止更新。

斷路器本身是一種開關裝置，當某個服務單元發生故障，通過斷路器的故障監控，向調用方返回一個符合預期的、可處理的備選響應(FallBack)，而不是長時間的等待或是拋出調用方無法處理的異常。

* 服務熔斷：好比保險絲，當服務出現故障或超時時，熔斷器會中斷對該服務的請求，防止故障向下游系統擴散，同時還可以提供一個快速失敗的回應
* 服務降級：當系統負載過高或發生故障時，降級策略可以將一些非關鍵功能關閉或切換到低資源消耗的實現，以保證核心功能的穩定運行
* 服務限流：限制對服務的訪問，防止過多的請求壓垮服務
* 服務限時：設置請求的最大處理時間，防止長時間的等待，並使得服務可以及時釋放資源
* 服務預熱：在系統啟動時，預先加載一些必要的資源或數據，以提高系統的性能和響應速度

Spring Cloud Circuit Breaker 是介面、用於實現在分布式系統中的服務熔斷功能，實現有Resilience4J(功能更完備) 及Spring Retry。
Circuit Breaker包含三個主要狀態和兩個特殊狀態：
1. Closed（關閉）：初始狀態，此時調用會正常進行，Circuit Breaker會監控調用的成功率(基於調用數量或是時間)。
2. Open（開啟）：當失敗率達到一定閾值時，Circuit Breaker會切換到開啟狀態，此時調用會立即失敗，不會執行實際的調用操作，而是直接返回錯誤。
3. Half-Open（半開啟）：在一段時間後，Circuit Breaker會進入半開啟狀態，此時會允許部分調用進行，如果這些調用成功，則Circuit Breaker會重新切換到關閉狀態；如果仍然有調用失敗，則會繼續保持開啟狀態。
4. Disable(禁用)：始終允許訪問。
5. Force-Open(強制開啟)：始終拒絕訪問。

主要的CircuitBreaker 配置屬性
| 配置屬性                                          | 默認值                                                       | 描述                                                         |
| ------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| failure-rate-threshold                              | 50                                                           | 以百分比配置失敗率閾值。當失敗率等於或大於閾值時，斷路器狀態並關閉變為開啟，並進行服務降級。 |
| slow-call-rate-threshold                             | 100                                                          | 以百分比的方式配置，斷路器把調用時間大於`slowCallDurationThreshold`的調用視為滿調用，當慢調用比例大於等於閾值時，斷路器開啟，並進行服務降級。 |
| slow-call-duration-threshold                         | 60000 [ms]                                                   | 配置調用時間的閾值，高於該閾值的呼叫視為慢調用。 |
| permitted-number-of-calls-in-half-open-state             | 10                                                           | 斷路器在半開狀態下允許通過的調用次數。                       |
| sliding-window-type                                 | COUNT_BASED                                                  | 配置滑動窗口的類型，可以是count-based或time-based。如果滑動窗口類型是COUNT_BASED，將會統計記錄最近`slidingWindowSize`次調用的結果。如果是TIME_BASED，將會統計記錄最近`slidingWindowSize`秒的調用結果。 |
| sliding-window-size                                 | 100                                                          | 配置滑動窗口的大小。                                         |
| minimum-number-of-calls                              | 100                                                          | 斷路器計算失敗率或慢調用率之前所需的最小調用數（每個滑動窗口周期）。例如，如果minimumNumberOfCalls為10，則必須至少記錄10個調用，然後才能計算失敗率。 |
| wait-duration-in-open-state                           | 60000 [ms]                                                   | 斷路器從OPEN到HALF_OPEN應等待的時間。                         |
