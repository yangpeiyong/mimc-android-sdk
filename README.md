# MIMC官方详细文档点击此链接：[详细文档](https://github.com/Xiaomi-mimc/operation-manual)


# 快速开始

## 1) 预备步骤

APP开发者访问小米开放平台（dev.mi.com）申请appId/appKey/appSec。
 
步骤如下：登录小米开放平台网页 -> ”管理控制台” -> ”小米应用商店” -> ”创建应用” ->  填入应用名和包名 -> ”创建” -> 记下看到的AppId/AppKey/AppSec 。
 
#### PS：建议MIMC与小米推送使用的APP信息一致

## 2) 在应用的AndroidManifest.xml里添加以下配置：

``` xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="com.xiaomi.xmsf.permission.LOG_PROVIDER" />

<!-- 这里的包名"com.xiaomi.mimcdemo"必须替换成App自己的包名 --> 
<permission
    android:name="com.xiaomi.mimcdemo.permission.MIMC_RECEIVE"
    android:protectionLevel="signature" />
<uses-permission android:name="com.xiaomi.mimcdemo.permission.MIMC_RECEIVE" />

<service
    android:name="com.xiaomi.mimc.MimcService"
    android:enabled="true"
    android:exported="false" />

<service
    android:name="com.xiaomi.mimc.MimcCoreService"
    android:enabled="true"
    android:exported="false"
    android:process=":mimc"/>

<service
    android:name="com.xiaomi.mimc.MimcJobService"
    android:enabled="true"
    android:exported="false"
    android:permission="android.permission.BIND_JOB_SERVICE"
    android:process=":mimc" />

<receiver android:name="com.xiaomi.mimc.receivers.PingReceiver">
    <intent-filter>
	<action android:name="com.xiaomi.push.PING_TIMER" />
    </intent-filter>
</receiver>

<receiver
    android:name="com.xiaomi.mimc.receivers.MimcReceiver"
    android:exported="true">
    <intent-filter>
	<action android:name="com.xiaomi.channel.PUSH_STARTED" />
	<action android:name="com.xiaomi.push.service_started" />
	<action android:name="com.xiaomi.push.channel_opened" />
	<action android:name="com.xiaomi.push.channel_closed" />
	<action android:name="com.xiaomi.push.new_msg" />
	<action android:name="com.xiaomi.push.kicked" />
    </intent-filter>
</receiver>
```
#### 注意：
我们将MimcCoreService和MimcJobService定义在了mimc进程中，您也可以配置其运行在任意进程。如果没有配置android:process这个属性，那么它们将运行在应用的主进程中。

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
User user = new User(appId, username);
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
user.registerMessageHandler(MIMCMessageHandler handler);
interface MIMCMessageHandler {
	public void handleMessage(List<MIMCMessage> packets);        
	public void handleGroupMessage(List<MIMCGroupMessage> packets); 
	//参数packetId与9)、10）对应
	public void handleServerAck(String packetId);
}
```

## 8) 登录

``` java 
// 建议App从后台切换到前台时，调用一次登录。
user.login();
```
		
## 9) 发送P2P消息

``` java 
String packetId = user.sendMessage(String toUserName, byte[] payload);
```

## 10) 发送P2T消息

``` java
String packetId = user.sendGroupMessage(long groupID, byte[] payload); 
```

## 12) 拉取消息

``` java
// 当切换到前台时，从服务端拉取消息
user.pull();
```

## 12) 注销

``` java 
user.logout();
```

[回到顶部](#readme)
