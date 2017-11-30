快速开始
===

## 1) 预备步骤

 APP开发者访问小米开放平台（dev.mi.com）申请appId/appKey/appSec。
 
 步骤如下：登陆小米开放平台网页 -> ”管理控制台” -> ”小米应用商店” -> ”创建应用” ->  填入应用名和包名 -> ”创建” -> 记下看到的AppId/AppKey/AppSec 。

## 2) 在应用的AndroidManifest.xml里添加以下配置：

``` xml
    <permission
        android:name="com.xiaomi.mimcdemo.permission.MIPUSH_RECEIVE"
        android:protectionLevel="signature" />

    <uses-permission android:name="com.xiaomi.mimcdemo.permission.MIPUSH_RECEIVE" />
```

## 3) 获取Token

+ appId/appKey/appSec：

  小米开放平台(dev.mi.com/cosole/man/)申请
  
  信息敏感，不应存储于APP端，应存储在AppProxyService
  
+ appAccount:

  APP帐号系统内唯一ID
  
+ AppProxyService：

  a) 验证appAccount合法性；
  
  b) 访问TokenService，获取Token并下发给APP；
  
#### 访问TokenService获取Token方式如下：
```
    curl “https://mimc.chat.xiaomi.net/api/account/token”
    -XPOST -d '{"appId":$appId,"appKey":$appKey,"appSecret":$appSec,"appAccount":$appAccount}' 
    -H "Content-Type: application/json"
```

## 4) 初始化

``` java 
    MimcClient.initialize(this);
    User user = new User(appId, appAccount);
```

## 5) 请求到Token并返回

``` java 
    user.registerTokenFetcher(MIMCTokenFetcher  fetcher); 
    interface MIMCTokenFetcher {
        /**
         * @return: 小米TokenService服务下发的原始数据
         * @note: fetchToken()访问APP应用方自行实现的AppProxyService服务，该服务实现以下功能：
                    1. 存储appId/appKey/appSec（不应当存储在APP客户端）
                    2. 用户在APP系统内的合法鉴权
                    3. 调用小米TokenService服务，并将小米TokenService服务返回结果通过fetchToken()原样返回，参考3）获取Token
         **/
        public String fetchToken();
    }
```

## 6) 获得连接状态

``` java 
    user.registerOnlineStatusHandler(MIMCOnlineStatusHandler handler);
    interface MIMCOnlineStatusHandler {
        public void statusChange();
    }
```

## 7) 接收消息

``` java 
    //每条消息都有packetId，按发送的时间顺序递增
    user.registerMessageHandler(MIMCMessageHandler handler);
    interface MIMCMessageHandler {
        public void handleMessage(List<MIMCMessage> packets);        
        public void handleGroupMessage(List<MIMCGroupMessage> packets); 
        //这里返回的packetId为服务器端收到消息后返回此消息的packetId 
        public void handleServerAck(String packetId);
    }
```

## 8) 登陆

``` java 
    user.login();
```

## 9) 发送消息

``` java 
    //返回值为packetId，表示客户端此次发送的消息的packetId
    //用户每次发送消息后，会收到服务器端返回的packetId，保证发送的消息成功到达服务器端   
    user.sendMessage(String appAccount, byte[]); 
```

## 10) 注销

``` java 
    user.logout();
```

