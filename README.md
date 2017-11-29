快速开始
===

## 1) 预备步骤

 APP开发者访问小米开放平台（dev.mi.com）申请appId/appKey/appSec。
 
 步骤如下：登陆小米开放平台网页 -> ”管理控制台” -> ”小米应用商店” -> ”创建应用” ->  填入应用名和包名 -> ”创建” -> 记下看到的AppId/AppKey/AppSec 。

## 2) 在应用的AndroidManifest.xml里添加以下配置：
//此处MIPUSH_RECEIVE为之前设置，以后会更改

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
  
+ 获取方式如下：
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

## 5) 登陆

``` java 
    user.login();
```

## 6) 发送消息

``` java 
    user.sendMessage(String appAccount, byte[]);
```

## 7) 注销

``` java 
    user.logout();
```

