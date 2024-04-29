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

流程：
1. 服務啟動時就會注冊自己的服務信息（服務名、IP、端口）到注冊中
2. 調用者可以從注冊中心訂閱想要的服務，獲取服務對應的實例列表（1個服務可能多實例部署）
3. 調用者自己對實例列表負載均衡，挑選一個實例
4. 調用者向該實例發起遠程調用

當服務提供者的實例宕機或者啟動新實例時，調用者如何得知呢？
1. 服務提供者會定期向注冊中心發送請求，報告自己的健康狀態（心跳請求）
2. 當注冊中心長時間收不到提供者的心跳時，會認為該實例宕機，將其從服務的實例列表中剔除
3. 當服務有新實例啟動時，會發送注冊服務請求，其信息會被記錄在注冊中心的服務實例列表
4. 當注冊中心服務列表變更時，會主動通知微服務，更新本地服務列表


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
2. 新增配置文件bootstrap.yml(優先級大於application.yml)：確保在應用程式初始化階段就能夠正確加載外部配置，將application.yml 中關於spring cloud 內容一併移過去
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
以下操作基本在cloud-consumer-feign-order80
* 超時控制：默認60秒拋出超時錯誤，透過getById id=1 驗證(8001/8002 Controller 需改寫)；可進一步從YML 配置connectTimeout/readTimeout。
* 重試機制：默認關閉，開啟要新增FeignConfig，一樣透過getById id=1 驗證
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

Spring Cloud Circuit Breaker 是介面、用於實現在分布式系統中的服務熔斷功能，實現有Resilience4J(功能更完備) 及Spring Retry。

### 熔斷
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

#### 實作 COUNT_BASED(記數的滑動窗口)
1. 修改cloud-provide-paymenr8001：新增PayCircuitController
2. 修改cloud-api-commons PayFeignApi接口
3. 修改cloud-consumer-feign-order80：POM 引入`spring-cloud-starter-circuitbreaker-resilience4j`、`spring-boot-starter-aop`，YAM 配置`spring.cloud.openfeign.circuitbreaker`、`resilience4j`，新增OrderCircuitController
4. 驗證：啟用feign80/8001，circuit/get/1與circuit/get/-1

#### 實作 TIME_BASED(時間的滑動窗口)
1. 修改cloud-consumer-feign-order80：YAM 配置`resilience4j`，新增OrderCircuitController
2. 為避免影響驗證結果，關閉cloud-consumer-feign-order80 FeignConfig 重試機制
3. 驗證：circuit/get/1與circuit/get/9999

### 隔離
Bulkhead 用來限制對於下游服務的最大併發數量的限制，即依賴隔離和負載保護。Resilience4j提供了如下兩種隔離的實現方式：
* SemaphoreBulkhead 使用了信號量：當一個線程要訪問受保護的資源或服務時，首先要試圖獲得信號量。如果信號量已滿（表示已經達到了最大並發訪問數量），則這個線程會被阻塞，直到有其他線程釋放了信號量。當一個線程訪問完畢後，會釋放信號量，讓其他線程可以訪問該資源或服務。
* FixedThreadPoolBulkhead 使用了有界隊列和固定大小線程池

#### 實作 SemaphoreBulkhead
1. 修改cloud-provide-paymenr8001：修改PayCircuitController
2. 修改cloud-api-commons PayFeignApi接口
3. 修改cloud-consumer-feign-order80：POM 引入`resilience4j-bulkhead`，YAM 配置`resilience4j`，修改OrderCircuitController
4. 驗證：bulkhead/get/1與bulkhead/get/9999

#### 實作 FixedThreadPoolBulkhead
1. 修改cloud-consumer-feign-order80：YAM 配置`resilience4j`，修改OrderCircuitController
2. 驗證：bulkhead/get/1與bulkhead/get/2與bulkhead/get/3

### 限流
常見的限流算法有以下幾種：
1. 固定窗口算法（Fixed Window Algorithm）：將時間分為固定大小的窗口，例如1秒或1分鐘，每個窗口內的請求數不能超過設定的閾值。
2. 滑動窗口算法（Sliding Window Algorithm）：與固定窗口算法類似，但滑動窗口會保留過去一段時間內的所有請求記錄，並動態計算窗口內的請求數。
3. 令牌桶算法（Token Bucket Algorithm）：系統會按照固定的速率往令牌桶中放入令牌，每個請求需要消耗一個令牌，當令牌桶中沒有足夠的令牌時，請求被拒絕。
4. 漏桶算法（Leaky Bucket Algorithm）：類似於令牌桶算法，但漏桶算法是將請求添加到漏桶中，並以固定速率從漏桶中排出請求，當漏桶滿了時，多餘的請求被拒絕。

#### 實作
1. 修改cloud-provide-paymenr8001：修改PayCircuitController
2. 修改cloud-api-commons PayFeignApi接口
3. 修改cloud-consumer-feign-order80：POM 引入`resilience4j-ratelimiter`，YAM 配置`resilience4j`，修改OrderCircuitController
4. 驗證：bulkhead/get/1

## Micrometer
為什麼不再使用Sleuth：Sleuth 也停止更新。

Micrometer 提供了一套完整的分布式鏈路追蹤收集(Distributed Tracing)
的解決方案且兼容支持了Zipkin 的數據展現。
為請求生成一個 Trace ID，Trace ID 是一串唯一標識符，用於唯一標識這個請求，然後在請求的頭信息中添加 Trace ID 和 Span ID ，最後將請求發送到相應的服務端

### Zipkin
Zipkin 是一種分布式鏈路跟蹤系統Web 圖形化的工具。
1. 官網下載jar(3.3.0)，運行`java -jar zipkin-server-3.3.0-exec.jar`
2. 訪問 http://localhost:9411/

### 實作
1. 父工程POM：引入系列包

| 依賴 | 作用 |
| -------- | -------- |
| micrometer-tracing-bom | 導入鏈路追蹤版本中心 |
| micrometer-tracing | 指標追蹤 |
| micrometer-tracing-bridge-brave | 與分布式追蹤工具Brave 集成，已收集應用程式分布式追蹤數據 |
| micrometer-observation | 收集應用程式的度量數據 |
| feign-micrometer | 收集客戶端請求的度量數據 |
| zipkin-reporter-brave | 將Brave 追蹤數據報告到Ziplin 追蹤系統 |
2. cloud-provider-payment8001 POM：引入系列包(除了`micrometer-tracing-bom`)、YML配置：`management`、新增PayMicrometerController
3. cloud-api-commons 修改PayFeignApi
4. cloud-consumer-feign-order80 POM：引入系列包(除了`micrometer-tracing-bom`)、YML配置：`management`、新增OrderMicrometerController
5. 驗證：啟動80/8001、Zipkin、Consul，micrometer/get/1，訪問http://127.0.0.1:9411/

## Gateway
為什麼不再使用Zuul：Zuul 也停止更新。

Spring Cloud Gateway 是Spring 生態系統之上構建的API 網關服務，提供統一的API 路由管理方式。核心是一系列的過濾器，將客戶端發送的請求轉發(路由)到對應的微服務。
Spring Cloud Gateway是加在整個微服務最前沿的防火牆和代理器，隱藏微服務節點IP 端口信息，從而加強安全保護。本身也是一個微服務，需要注冊進服務注冊中心。
功能：
* 反向代理
* 鑒權
* 流量控制
* 熔斷
* 日志監控 

核心：
1. Route(路由)：路由是構建網關的基本模塊，它由ID、目標URI、一系列的斷言和過濾器組成，如果斷言為true 則匹配該路由。
2. Predicate(斷言)：參考的是Java8 的java.util.function.Predicate，開發人員可以匹配HTTP 請求中的所有內容（例如請求頭或請求參數），如果請求與斷言相匹配則進行路由。
3. Filter(過濾)：指的是Spring 框架中GatewayFilter 的實例，使用過濾器，可以在請求被路由前或者之後對請求進行修改。在"pre" 類型的過濾器可以做參數校驗、權限校驗、流量監控、日志輸出、協議轉換等；在"post" 類型的過濾器中可以做響應內容、響應頭的修改，日志的輸出，流量監控等。

客戶端向Spring Cloud Gateway 發出請求，然後在Gateway Handler Mapping 中找到與請求相匹配的路由，將其發送到Gateway Web Handler。Handler 再通過指定的過濾器鏈(可能會在發送代理請求前後(pre/post)執行業務邏輯)來將請求發送到實際的服務然後返回。

### 實作 引入
1. 建立cloud-gateway9527，POM 引入`spring-cloud-starter-consul-discovery`、`spring-cloud-starter-gateway`，YUM 配置`spring.cloud.consul`，新增啟動類
2. 驗證：啟動9527、Consul

### 實作 Route
1. cloud-provider-payment8001 新建PayGatewayController
2. cloud-gateway9527 YUM 配置`spring.cloud.gateway.routes`
3. 驗證：啟動8001、訪問9527/pay/gateway/info
4. cloud-api-commons 修改PayFeignApi：對應方法和`@FeignClient`
5. cloud-consumer-feign-order80 新建OrderGatewayController
6. 再驗證：啟動80、訪問80/feign/gateway/pay/info
7. cloud-gateway9527 YUM 修改`spring.cloud.gateway.routes.uri` 以服務名來動態獲取，如此即便cloud-payment-service 更換路徑也可以成功路由
8. 再驗證：cloud-provider-payment8001 修改port 號重啟、訪問80/feign/gateway/pay/info

### 實作 Predicate
兩種配置方式：shortcuts、fully expanded arguments
```yaml=
spring:
  cloud:
    gateway:
      routes:
      #shortcuts
      - id: after_route
        uri: https://example.org
        predicates:
        - Cookie=mycookie,mycookievalue
      #fully expanded arguments
      - id: after_route
        uri: https://example.org
        predicates:
        - name: Cookie
          args:
            name: mycookie
            regexp: mycookievalue
```
1. cloud-gateway9527 YUM 配置`spring.cloud.gateway.routes.predicate`
2. 驗證：9527/pay/gateway/pay/get/2

#### 實作 自定義斷言
繼承AbstractRoutePredicateFactory 抽象類或是實現RoutePredicateFactory 介面，類名以RoutePredicateFactory 結尾。
1. cloud-gateway9527 新增MyRoutePredicateFactory
2. YML 配置`spring.cloud.gateway.routes.predicate.My`
3. 驗證：9527/pay/gateway/pay/get/2?userType=gold

### 實作 Filter
* 全局默認過濾器Global Filters：Gateway 默認已有的直接使用即可，主要作用於所有的路由，不需要在配置文件中配置，作用在所有的路由上，實現GlobalFilter接口即可
* 單一內置過濾器GatewayFilter：也可以稱為網關過濾器，這種過濾器主要是作用於單一路由或者某個路由分組，總共有幾十個所以接下來只實作常見的

#### AddRequestHeader
1. cloud-provider-payment8001 修改PayGatewayController
2. cloud-gateway9527 YUM 配置`spring.cloud.gateway.routes. - id: pay_filter`
3. 驗證：9527/pay/gateway/filter?customerName=qq、9527/pay/gateway/filter?customerId=654321&customerName=qq


## Nacos
### 安裝
### 實作 引入
1. 建立cloudalibaba-provider-payment9001 模塊，POM 引入`spring-boot-starter-web`、`spring-cloud-starter-alibaba-nacos-discovery`、`cloud-api-commons`，YML 配置`spring.cloud.nacos`，啟動類`@EnableDiscoveryClient`，新增PayAlibabaController
2. 建立cloudalibaba-consumer-nacos-order83 模塊，YML 配置`spring.cloud.nacos`、`service-url.nacos-user-service`，新增RestTemplateConfig、OrderNacosController\
3. 建立cloudalibaba-config-nacos-client3377 模塊，POM 引入`spring-boot-starter-web`、`spring-cloud-starter-alibaba-nacos-discovery`、`spring-cloud-starter-alibaba-nacos-config`、`spring-cloud-starter-bootstrap`，YML 配置`spring.cloud.nacos`，bootstrap.yml 配置`spring.profiles.active`，新增NacosConfigClientController

## Sentinel
## 

