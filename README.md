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

* AP 模式：優先保證系統的可用性（Availability），即使在面臨網路分區（Partition）的情況下，系統仍然可以繼續運作。這種設計通常會犧牲一定程度的一致性（Consistency），即不同節點上的數據可能存在一段時間的不一致，但這種不一致是暫時的。
* CP 模式：優先保證系統的一致性（Consistency），即確保所有節點上的資料在任何時候都是一致的。這種設計通常會犧牲一定程度的可用性（Availability），即在面臨網絡分區的情況下，系統可能無法繼續運作。
## 初始架構
父工程：cloud2024
微服務提供者，支付模塊：cloud-provider-payment8001
微服物消費者，訂單模塊：cloud-consumer-order80
自製工具包(使用前要先`maven install`成jar)：cloud-api-commons
資料庫：db2024.sql

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
5. 驗證：啟動80/8001、Zipkin、Consul，micrometer/get/1，訪問 http://127.0.0.1:9411/

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
3. 驗證：9527/pay/gateway/get/2?userType=gold

### 實作 Filter
* 全局默認過濾器Global Filters：Gateway 默認已有的直接使用即可，主要作用於所有的路由，不需要在配置文件中配置，作用在所有的路由上，實現GlobalFilter接口即可
* 單一內置過濾器GatewayFilter：也可以稱為網關過濾器，這種過濾器主要是作用於單一路由或者某個路由分組，總共有幾十個所以範例中只實作常見的

1. cloud-provider-payment8001 修改PayGatewayController
2. cloud-gateway9527 YUM 配置`spring.cloud.gateway.routes. - id: pay_filter`
3. 驗證：9527/pay/gateway/filter(測試請求頭、回應頭)、9527/pay/gateway/filter?customerId=654321&customerName=xxyy(測試參數)、9527/gateway/filter(路徑)、9527/XYZ/abc/filter(路徑)
5. cloud-gateway9527 YUM 配置`spring.cloud.gateway.default-filters`，全局通用
6. 再驗證：9527/pay/gateway/filter

#### 實作 自定義Global Filter
1. cloud-gateway9527 新增MyGlobalFilter
2. 驗證：9527/pay/gateway/filter、9527/pay/gateway/info、/pay/gateway/get/2

#### 實作 自定義條件Filter
繼承AbstractGatewayFilterFactory 抽象類或是實現GatewayFilterFactory 介面，類名以GatewayFilterFactory 結尾。
1. cloud-gateway9527 新增MyGatewayFilterFactory
2. YML 配置`spring.cloud.gateway.routes.predicate.My`
3. 驗證：9527/pay/gateway/filter?pass

## Nacos
Nacos = Spring Cloud Consul = Eureka + Config + Bus
相較於Spring Cloud Consul，Nacos 默認是AP 模式

### 安裝
使用版本2.2.3
1. 解壓後在bin 底下執行`startup.cmd -m standalone`
2. 訪問 http://localhost:8848/nacos/
3. 關閉`shutdown.cmd` 或是Ctrl + C
### 實作 服務註冊
1. 建立cloudalibaba-provider-payment9001 模塊，POM 引入`spring-boot-starter-web`、`spring-cloud-starter-alibaba-nacos-discovery`，YML 配置`spring.cloud.nacos`，啟動類`@EnableDiscoveryClient`，新增PayAlibabaController
2. 複製出cloudalibaba-provider-payment9002 模塊
3. 建立cloudalibaba-consumer-order83 模塊，POM 引入`spring-boot-starter-web`、`spring-cloud-starter-alibaba-nacos-discovery`、`spring-cloud-starter-loadbalancer`，YML 配置`spring.cloud.nacos`、`service-url.nacos-user-service`，啟動類`@EnableDiscoveryClient`，新增RestTemplateConfig、OrderNacosController
4. 驗證：啟動Nacos、83/9001/9002，訪問83/consumer/pay/nacos/1

### 實作 分布式配置
1. cloudalibaba-provider-payment9001/cloudalibaba-provider-payment9002：POM 引入`spring-cloud-starter-alibaba-nacos-config`、`spring-cloud-starter-bootstrap`，YML 配置`spring.profiles.active`，bootstrap.yml 將application.yml 中關於spring cloud 內容一併移過去，修改PayAlibabaController
2. cloudalibaba-consumer-order83 修改OrderNacosController
3. 在Nacos 新建配置(Group DEFAULT_GROUP)：nacos-config-client-dev.yaml、nacos-config-client.yaml、nacos-config-client-prod.yaml
4. 驗證：訪83/consumer/config/info

Nacos 數據模型Key 由三元組唯一確定，Namespace(默認為public)、Group(默認為Group DEFAULT_GROUP)、Data ID
5. 在Nacos 新建配置(Group PROD_GROUP)：nacos-config-client-dev.yaml、nacos-config-client.yaml、nacos-config-client-prod.yaml
6. bootstrap.yml配置`spring.cloud.nacos.config.group`
7. 再驗證：訪83/consumer/config/info
8. 在Nacos 新建命名空間Prod_Namespace 並新建配置
9. bootstrap.yml配置`spring.cloud.nacos.config.namespace`
10. 再驗證：訪83/consumer/config/info

## Sentinel
### 安裝
使用版本1.8.7
1. 執行`java -jar sentinel-dashboard-1.8.7.jar`
2. 訪問UI http://localhost:8080/ ，默認帳密sentinel

### 實作 引入
1. 建立cloudalibaba-sentinel-service8401 模塊，POM 引入`spring-boot-starter-web`、`spring-cloud-starter-alibaba-nacos-discovery`、`spring-cloud-starter-alibaba-sentinel`，YML 配置`spring.cloud.nacos`、`spring.cloud.sentinel`，啟動類`@EnableDiscoveryClient`，新增FlowLimitController
2. 驗證：啟動Nacos、Sentinel、8401，訪問8401/testA 和8401/testB，再查看控制台

### 限流
|  | 描述 |
| -------- | -------- |
| 資源名 | 資源的唯一名稱，默認是請求的接口路徑 |
| 針對來源 | 具體針對某個微服務進行限流，莫認為default，表示不區分來源 |
| 閥值類型 | 通過QPS 或是併發線程數 |
| 單機閥值 | 為閥值類型的設定單位 |
| 是否集群 | 選中則表示集群環境 |

以下操作cloudalibaba-sentinel-service(http://localhost:8401/)
流控規則
1. 直接：默認的流控模式，當接口達到限流條件時，直接開啟限流功能。ex: 資源名/testA、QPS、3，限定每秒三次訪問/testA
2. 關聯：當關聯的資源達到閾值時，就限流自己；當與A 關聯的資源B 達到閥值後，就限流A 自己。ex: 資源名/testA、QPS、3、關聯資源/testB，每秒三次訪問/testB 而限流/testA
3. 鏈路：來自不同鏈路的請求對同一個目標訪問時，實施針對性的不同限流措施，比如C 請求來訪問就限流，D 請求來訪問就是OK。ex: cloudalibaba-sentinel-service8401 模塊新增FlowLimitService，修改FlowLimitController，YUM 配置`spring.cloud.sentinel.web-context-unify`；資源名common、QPS、3、入口資源/testC，每秒三次訪問/testC 限流/testC 而/testD 不管

流控效果
1. 快速失敗：默認的流控處理，直接拋出異常
2. Warm Up：預熱模式，對超出閾值的請求同樣是拒絕並拋出異常。但這種模式閾值會動態變化，從一個較小值逐漸增加到最大閾值。請求閾值初始值是 threshold / coldFactor(默認值為3)，持續指定時長後，逐漸提高到threshold 值。ex: 資源名/testB、QPS 的threshold 為10，預熱時間為5秒，那麽初始閾值就是 10 / 3 ，也就是3，然後在5秒後逐漸增長到10。
3. 排隊等待：讓所有請求進入一個隊列中，然後按照閾值允許的時間間隔依次執行。後來的請求必須等待前面執行完成，如果請求預期的等待時間超出最大時長，則會被拒絕。ex: 修改FlowLimitController、資源名/testE、QPS、1、超時時間10000ms

限流返回，新增RateLimitController
1. 默認返回。資源名/rateLimit/byUrl、閥值1，得到Blocked by Sentinel (flow limiting)
2. 自定義返回。資源名byResourceWithSentinelResource、閥值1，得到自定返回
3. 自定義返回 + 服務降級處理。資源名doActionWithSentinelResource、閥值1，自訂返回與降級處理共存

### 熔斷
* closed：關閉狀態，斷路器放行所有請求，並開始統計異常比例、慢請求比例。超過閾值則切換到open 狀態
* open：開啟狀態，服務呼叫被熔斷，存取被熔斷服務的請求會被拒絕，快速失敗，直接走降級邏輯。open 狀態持續一段時間後會進入half-open 狀態
* half-open：半開狀態，放行一次請求，根據執行結果來判斷接下來的操作。請求成功則切換到closed 狀態、 請求失敗則切換到open 狀態

熔斷策略，修改FlowLimitController
1. 慢調用比例：在統計時長內，實際請求數目＞設定的最小請求數且響應時間大於RT 且實際慢呼叫比例＞比例門檻。ex: 資源名/testF、最大RT 200ms、熔斷時長5s、統計時長5000ms、閥值0.1、最小請求數5，超過200ms 響應則為慢調用，一秒內超過比例0.1則進入5秒的熔斷狀態。
2. 異常比例：異常比例＞比例門檻。ex: 資源名/testG、熔斷時長5s、統計時長5000ms、閥值0.1、最小請求數5，還須暫時關閉cloud-api-commons GlobalExceptionHandler `@RestControllerAdvice`
3. 異常數：異常數＞閥值。ex: 資源名/testG、熔斷時長5s、統計時長5000ms、最小請求數5、異常數1，還須暫時關閉cloud-api-commons GlobalExceptionHandler `@RestControllerAdvice`

### 熱點規則
1. 修改RateLimitController
2. 資源名testHotKey、參數索引0(從0開始)、閥值1、統計時長1s。驗證：8401/testHotKey?p1=a&p2=b
3. 參數例外項：參數類型(基本類型或者String)String、參數值c、閥值3。驗證：8401/testHotKey?p1=c&p2=b

### 授權規則
在Sentinel的授權規則中，提供了白名單與黑名單兩種授權類型。
1. 新增EmpowerController、MyRequestOriginParser
2. 資源名/empower、流控應用test1、黑(白)名單。驗證：8401/empower?serverName=test1

### 實作 持久化
配置規則來自Nacos
1. cloudalibaba-sentinel-service8401 模塊，POM 引入`sentinel-datasource-nacos`，修改YML `spring.cloud.sentinel.datasource`
2. Nacos 新建配置cloudalibaba-sentinel-service
```json=
[
    {
        "resource": "/rateLimit/byUrl", #資源名稱
        "limitApp": "default", #來源應用
        "grade": 1, #閥值類型 0線程數 1QPS
        "count": 1, #閥值
        "strategy": 0, #流控模式 0直接 1關聯 2鏈路
        "controlBehavior": 0, #流控效果 0快速失敗 1Warm up 2排隊等待
        "clusterMode": false #是否集群
    }
]
```
3. 重啟8401，訪問8401/rateLimit/byUrl

### 與OpenFeign 集成
cloudalibaba-consumer-order83 透過OpenFeign 調用cloudalibaba-provider-payment9001 實現統一的fallback 服務降級
1. cloudalibaba-provider-payment9001 模塊，POM 引入`spring-cloud-starter-openfeign`、`spring-cloud-starter-alibaba-sentinel`，bootstrap.yml 配置`spring.cloud.sentinel`，修改PayAlibabaController
2. cloud-api-commons 模塊，POM 引入`spring-cloud-starter-alibaba-sentinel`，新增PayFeignSentinelApiFallback，新增PayFeignSentinelApi
3. cloudalibaba-consumer-order83 模塊，POM 引入`spring-cloud-starter-openfeign`、`spring-cloud-starter-alibaba-sentinel`，YML 配置`feign.sentinel.enable`，啟動類`@EnableFeignClients`，修改OrderNacosController
4. 調整版本：springboot + springcloud 版本太高，與Sentinel 不相容，父工程POM修改3.0.9/2022.0.2
5. 驗證：啟動Nacos、Sentinel、9001/83，訪問83/consumer/pay/nacos/get/2
6. Sentinel 新增流控規則(資源名getPayByOrderNo)
7. 驗證流控保護，訪問83/consumer/pay/nacos/get/2
8. 關閉9001
9. 驗證熔斷，訪問83/consumer/pay/nacos/get/2

驗證完恢復版本

### 與Gateway 集成
Sentinel 1.6.0 開始才提供Spring Cloud Gateway 的適配模塊。
cloudalibaba-sentinel-gateway9528 保護cloudalibaba-provider-payment9001 實現服務限流(提前在gateway 就先擋下)
1. 新增cloudalibaba-sentinel-gateway9528 模塊，POM 引入`spring-cloud-starter-gateway`、`sentinel-spring-cloud-gateway-adapter`、`sentinel-transport-simple-http`、`javax-anntation-api`，YML 配置`spring.cloud.nacos`、`spring.cloud.gateway`，啟動類`@EnableDiscoveryClient`，新增GatewayConfiguration
3. 驗證：啟動Nacos、9001/9528，訪問：9528/pay/nacos/234

## Seata
* 本地事務：更多的是透過關聯式資料庫來控制事務，利用資料庫本身的事務特性來實現，因此叫資料庫事務；而資料庫通常和應用在同一個伺服器，所以又被稱為本地事務。
* 分散式事務：分散式系統環境下由不同的服務之間透過網路遠端協作完成事務稱為分散式事務。

分布式事務產生的場景
1. 微服務架構，例如：訂單微服務和庫存微服務，下單的同時訂單微服務請求庫存微服務減庫存。簡言之：跨JVM 進程產生分布式事務。
2. 單體系統存取多個資料庫執行個體，例如：使用者資訊和訂單資訊分別在兩個MySQL 實例存儲，使用者管理系統刪除使用者訊息，需要分別刪除使用者資訊及使用者的訂單訊息。簡言之：跨資料庫執行個體產生分布式事務。
3. 多服務存取同一個資料庫實例，例如：訂單微服務和庫存微服務即使存取同一個資料庫也會產生分散式事務，原因也是跨JVM進程。

在Seata 的事務管理中有三個重要的角色
1. TC (Transaction Coordinator) - 事務協調者：就是Seata，維護全局和分支事務的狀態，驅動全局事務提交或回滾。
2. TM (Transaction Manager) - 事務管理器：標註`@GlobalTransactional`的微服務模組，它是事務的發起者，定義全局事務的範圍、並根據TC 維護的全局事務和分支事務狀態，做出開始、提交、回滾事務的決議。
3. RM (Resource Manager) - 資源管理器：就是資料庫本身(可以有多個)，管理分支事務，與TC 交談以註冊分支事務和報告分支事務的狀態，並驅動分支事務提交或回滾。

TC 以Seata 服務器(Server)型式獨立部屬，TM 和RM 則以Seata 客戶端的型式集成在微服務中運行。流程：
1. TM 向TC 申請開啟一個全局事務，全局事務創建成功並生成一個全局唯一的 XID
2. XID 在微服務調用鏈路的上下文中傳播
3. RM 向TC 註冊分之事務，將其納入XID 對應全局事務的管轄
4. TM 向TC 發起針對XID 的全局提交或回滾決議
5. TC 調度XID 下管轄的全部分之事務完成提交或回滾請求

Seata 有四種事務模式，在此只實作主流的AT 模式

### 安裝
使用版本2.0.0
1. 執行seata.sql
1. 解壓後更改配置，修改seata\conf\application.yml
```yaml=
server:
  port: 7091

spring:
  application:
    name: seata-server

logging:
  config: classpath:logback-spring.xml
  file:
    path: ${log.home:${user.home}/logs/seata}
  extend:
    logstash-appender:
      destination: 127.0.0.1:4560
    kafka-appender:
      bootstrap-servers: 127.0.0.1:9092
      topic: logback_to_logstash

console:
  user:
    username: seata
    password: seata
    
seata:
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace:
      group: SEATA_GROUP #後續自己在nacos裡面新建，不想新建SEATA_GROUP就寫DEFAULT_GROUP
      username: nacos
      password: nacos
  registry:
    type: nacos
    nacos:
      application: seata-server
      server-addr: 127.0.0.1:8848
      group: SEATA_GROUP #後續自己在nacos裡面新建，不想新建SEATA_GROUP就寫DEFAULT_GROUP
      namespace:
      cluster: default
      username: nacos
      password: nacos
  store:
    mode: db
    db:
      datasource: druid
      db-type: mysql
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/seata?characterEncoding-utf8&useSSL=false&rewriteBatchedStatements=true&allowPublicRetrieval=true&serverTimezone=Asia/Taipei
      user: root
      password: password
      min-conn: 10
      max-conn: 100
      global-table: global_table
      branch-table: branch_table
      lock-table: lock_table
      distributed-lock-table: distributed_lock
      query-limit: 1000
      max-wait: 5000
  security:
    secretKey: SeataSecretKey0c382ef121d778043159209298fd40bf3850a017
    tokenValidityInMilliseconds: 1800000
    ignore:
      urls: /,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.jpeg,/**/*.ico,/api/v1/auth/login,/metadata/v1/**
```
2. 啟動Nacos、再啟動Seata (bin下執行`seata-server.bat`)
3. 驗證：seata-server 入住Nacos，訪問 http://localhost:7091 (默認帳密seata)

### 實作
模擬一個購物場景：總共涉及訂單、庫存、帳戶服務。用戶下單後，產生訂單，並遠端呼叫扣減對應的庫存和帳戶餘額。
