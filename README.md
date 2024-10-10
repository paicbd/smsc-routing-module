The `smsc-routing-module` is a service responsible for routing messages in the SMSC (Short Message Service Center) environment. This service interacts with Redis clusters, processes different types of messages (SMPP, HTTP, SS7), and manages delivery statuses (DLR). It is highly configurable with support for multi-threaded processing, WebSocket communication, and integration with a backend for tracking service provider credits.

**Key Configurable Variables:**
- **JVM Settings:**
  - `JVM_XMS`: Initial heap size for JVM (default: 512MB).
  - `JVM_XMX`: Maximum heap size for JVM (default: 1024MB).
  
- **Server Configuration:**
  - `SERVER_PORT`: Port the application runs on (default: 8989).
  - `APPLICATION_NAME`: Name of the application (default: "routing").
  - `APPLICATION_ADDRESS`: Server IP address (default: 127.0.0.1).
  
- **Redis Cluster Configuration:**
  - `CLUSTER_NODES`: Redis cluster nodes formatted as `host:port` (default: localhost cluster from ports 7000 to 7009).
  
- **Thread Pool Configuration:**
  - `THREAD_POOL_MAX_TOTAL`: Maximum number of threads (default: 60).
  - `THREAD_POOL_MAX_IDLE`: Maximum number of idle threads (default: 50).
  - `THREAD_POOL_MIN_IDLE`: Minimum number of idle threads (default: 10).
  
- **Message Processing Configuration:**
  - `REDIS_PRE_MESSAGE_LIST`: Redis list name for pre-message processing.
  - `REDIS_PRE_MESSAGE_ITEMS_TO_PROCESS`: Number of items to process per batch (default: 1000).
  - `REDIS_PRE_MESSAGE_WORKERS`: Number of workers handling pre-message processing (default: 10).
  - `REDIS_PRE_DELIVERY_LIST`: Redis list name for pre-delivery processing.
  - `REDIS_PRE_DELIVERY_ITEMS_TO_PROCESS`: Number of items to process per batch (default: 1000).
  - `REDIS_PRE_DELIVERY_WORKERS`: Number of workers handling pre-delivery processing (default: 10).
  
- **Routing and DLR Configuration:**
  - `APP_ROUTING_RULE_HASH`: Redis hash key for routing rules.
  - `APP_GENERAL_SETTINGS_HASH`: Redis hash key for general settings.
  - `SMPP_REDIS_DELIVER_SM_RETRY_LIST`: List for SMPP DLR retry handling.
  - `HTTP_REDIS_DELIVER_SM_RETRY_LIST`: List for HTTP DLR retry handling.
  
- **WebSocket Configuration:**
  - `WEBSOCKET_SERVER_ENABLED`: Enable/disable WebSocket server (default: true).
  - `WEBSOCKET_SERVER_HOST`: WebSocket server host (default: 127.0.0.1).
  - `WEBSOCKET_SERVER_PORT`: WebSocket server port (default: 9087).
  - `WEBSOCKET_SERVER_PATH`: Path for WebSocket communication (default: "/ws").
  
- **Backend Integration:**
  - `BACKEND_URL`: URL for backend API to notify service provider credit use (default: "http://localhost:9000/balance-credit/credit-used/").
  - `BACKEND_API_KEY`: API key for backend authentication.
  - `BACKEND_REQUEST_FREQUENCY`: Frequency in milliseconds to send credit updates to the backend (default: 1000ms).
  
- **JMX Monitoring Configuration:**
  - `ENABLE_JMX`: Enable/disable JMX monitoring (default: true).
  - `JMX_PORT`: Port for JMX (default: 9010).

This service is highly optimized for handling large amounts of messaging traffic with the ability to scale using Redis clusters, manage multiple threads, and integrate with external services for credit management and real-time monitoring.

---

### Docker Compose Example

```yaml
services:
  smsc-routing-module:
    image: paic/smsc-routing-module:latest
    ulimits:
      nofile:
        soft: 1000000
        hard: 1000000
    environment:
      JVM_XMS: "-Xms512m"
      JVM_XMX: "-Xmx1024m"
      SERVER_PORT: 8989
      APPLICATION_NAME: "routing"
      APPLICATION_ADDRESS: 127.0.0.1
      #RedisCluster
      CLUSTER_NODES: "localhost:7000,localhost:7001,localhost:7002,localhost:7003,localhost:7004,localhost:7005,localhost:7006,localhost:7007,localhost:7008,localhost:7009"
      THREAD_POOL_MAX_TOTAL: 60
      THREAD_POOL_MAX_IDLE: 50
      THREAD_POOL_MIN_IDLE: 10
      THREAD_POOL_BLOCK_WHEN_EXHAUSTED: true
      # Process
      REDIS_PRE_MESSAGE_LIST: "preMessage"
      REDIS_PRE_MESSAGE_ITEMS_TO_PROCESS: 1000
      REDIS_PRE_MESSAGE_WORKERS: 10
      REDIS_PRE_DELIVERY_LIST: "preDeliver"
      REDIS_PRE_DELIVERY_ITEMS_TO_PROCESS: 1000
      REDIS_PRE_DELIVERY_WORKERS: 10
      # Lists
      REDIS_SMPP_MESSAGE_LIST: "smpp_message"
      REDIS_HTTP_MESSAGE_LIST: "http_message"
      REDIS_SS7_MESSAGE_LIST: "ss7_message"
      REDIS_SMPP_RESULT: "submit_sm_result"
      REDIS_HTTP_RESULT: "http_submit_sm_result"
      REDIS_SMPP_DLR_LIST: "smpp_dlr"
      REDIS_HTTP_DLR_LIST: "http_dlr"
      # Routing
      APP_ROUTING_RULE_HASH: "routing_rules"
      APP_GENERAL_SETTINGS_HASH: "general_settings"
      APP_SS7_SETTINGS_HASH: "ss7_settings"
      APP_SMPP_HTTP_GSKEY: "smpp_http"
      APP_GATEWAYS: "gateways"
     
