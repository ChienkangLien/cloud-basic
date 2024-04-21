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
服務調用和負載均衡：LoadBalancer、OpenFeign、~~Ribbon~~
分布式事務：Alibaba Seata
服務熔斷和降級：Resilience4J(Circuit Breaker的實現)、Alibaba Sentinel、~~Hystrix~~
服務鏈路追蹤：Micrometer Tracing、~~Sleuth + Zipkin~~
服務網關：Gateway、~~Zuul~~
分布式配置管理：Consul、Alibaba Nacos、~~Config + Bus~~

## 架構
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

### sping cloud 服務註冊
1. POM：引用`spring-cloud-starter-consul-discovery`
2. YML：配置`spring.cloud.consul`
3. 啟動類：註解`@EnableDiscoveryClient`
4. Controller (cloud-consumer-order80)：URL 使用服務註冊中心上的微服務名稱
5. 配置修改RestTemplateConfig (cloud-consumer-order80)：

### sping cloud 分布式配置
在cloud-consumer-order80 驗證有效即可
1. POM：引用`spring-cloud-starter-consul-config`、`spring-cloud-starter-bootstrap`
2. 新增配置文件bootstrap.yml(優先級大於application.yml)：將application.yml 中關於spring cloud 內容一併移過去
3. consul 服務器key/value 配置：Key / Values 中建立三個文件 config/cloud-payment-service/data、config/cloud-payment-service-prod/data、config/cloud-payment-service-dev/data
4. 啟動類：註解`@RefreshScope`
5. Controller 驗證