package com.xiaomi.mimcdemo.common;

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.push.mimc.MIMCGroupMessage;
import com.xiaomi.push.mimc.MIMCMessage;
import com.xiaomi.push.mimc.MIMCTokenFetcher;
import com.xiaomi.push.mimc.MimcMessageHandler;
import com.xiaomi.push.mimc.MimcOnlineStatusListener;
import com.xiaomi.push.mimc.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserManager {
    /**
     * @important!!! appId/appKey/appSec：
     * 小米开放平台(https://dev.mi.com/cosole/man/)申请
     * 信息敏感，不应存储于APP端，应存储在AppProxyService
     * appAccount:
     * APP帐号系统内唯一ID
     * 此处appId/appKey/appSec为小米MIMC Demo所有，会在一定时间后失效
     * 请替换为APP方自己的appId/appKey/appSec
     **/
    private long appId = 2882303761517669588L;
    private String appKey = "5111766983588";
    private String appSecret = "b0L3IOz/9Ob809v8H2FbVg==";
    private String appAccount;

    private String url;
    private User mUser;
    private int mStatus;


    private final static UserManager instance = new UserManager();

    private UserManager() {
    }

    private OnSendMsgListener onSendMsgListener;

    public void setOnSendMsgListener(OnSendMsgListener onSendMsgListener) {
        this.onSendMsgListener = onSendMsgListener;
    }

    public interface OnSendMsgListener {
        void onSent(MIMCMessage message);
        void onSent(MIMCGroupMessage message);
        void onStatusChanged(int status);
        void onServerAck(String packetId);
        void onGroupInfo(String info);
    }

    public static UserManager getInstance() {
        return instance;
    }

    public String getAccount() {
        return appAccount;
    }

    public int getStatus() {
        return mStatus;
    }

    public void addMsg(MIMCMessage message) {
        onSendMsgListener.onSent(message);
    }

    public void addMsg(MIMCGroupMessage message) {
        onSendMsgListener.onSent(message);
    }

    public void serverAck(String packetId){
        onSendMsgListener.onServerAck(packetId);
    }

    public User getUser(String account) {
        if (!account.equals(appAccount)){
            mUser = newUser(account);
        }
        if (mUser == null) {
            mUser = newUser(account);
        }
        appAccount = account;
        return mUser;
    }

    public User newUser(String appAccount){
        User user = new User(appId, appAccount);
        user.registerTokenFetcher(new TokenFetcher());
        user.registerMessageHandler(new MessageHandler());
        user.registerOnlineStatusListener(new OnlineStatusListener());
        return user;
    }

    class OnlineStatusListener implements MimcOnlineStatusListener {
        @Override
        public void onStatusChanged(int status, int code, String msg) {
            mStatus = status;
            onSendMsgListener.onStatusChanged(status);
        }
    }

    class MessageHandler implements MimcMessageHandler {

        @Override
        public void handleMessage(List<MIMCMessage> packets) {
            for (int i = 0; i < packets.size(); ++i) {
                MIMCMessage message = new MIMCMessage();
                message.setFromAccount(packets.get(i).getFromAccount());
                message.setFromResource(packets.get(i).getFromResource());
                message.setPayload(packets.get(i).getPayload());
                addMsg(message);
            }
        }

        @Override
        public void handleGroupMessage(List<MIMCGroupMessage> packets) {
            for (int i = 0; i < packets.size(); i++) {
                if (!getAccount().equals(packets.get(i).getFromAccount())) {
                    addMsg(packets.get(i));
                }
            }
        }

        @Override
        public void handleServerAck(String packetId) {
           serverAck(packetId);
        }
    }


    class TokenFetcher implements MIMCTokenFetcher {

        @Override
        public String fetchToken() {
            /**
             * @important!!!
             * appId/appKey/appSec：
             *     小米开放平台(https://dev.mi.com/cosole/man/)申请
             *     信息敏感，不应存储于APP端，应存储在AppProxyService
             * appAccount:
             *      APP帐号系统内唯一ID
             * AppProxyService：
             *     a) 验证appAccount合法性；
             *     b) 访问TokenService，获取Token并下发给APP；
             * !!此为Demo APP所以appId/appKey/appSec存放于APP本地!!
             **/
            url = "https://mimc.chat.xiaomi.net/api/account/token";
            String json = "{\"appId\":" + appId + ",\"appKey\":\"" + appKey + "\",\"appSecret\":\"" + appSecret + "\",\"appAccount\":\"" + appAccount + "\"}";
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request
                    .Builder()
                    .url(url)
                    .post(RequestBody.create(JSON, json))
                    .build();
            Call call = client.newCall(request);
            JSONObject data = null;
            try {
                Response response = call.execute();
                JSONObject object = new JSONObject(response.body().string());
                if (!object.getString("message").equals("success")) {
                    MyLog.w("data failure");
                }
                data = object.getJSONObject("data");
            } catch (Exception e) {
                MyLog.w("http request exception: " + e.getMessage());
            }
            MyLog.w("token:"+data);
            return data.toString();
        }
    }

    public void createGroup(final String groupName, final String users) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId;
                String json = "{\"topicName\":\"" + groupName + "\", \"accounts\":\"" + users + "\"}";
                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .post(RequestBody.create(JSON, json))
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void queryGroupInfo(final String groupId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .get()
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void queryGroupsOfAccount() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/account";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .get()
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        JSONArray members = object.getJSONArray("data");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "群ID：" + member.getString("topicId") + "\n";
                            info += "群名：" + member.getString("topicName") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void joinGroup(final String groupId, final String users) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/accounts";
                String json = "{\"accounts\":\"" + users + "\"}";
                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .post(RequestBody.create(JSON, json))
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void quitGroup(final String groupId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/account";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .delete()
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    String info = "";
                    if (response.isSuccessful()) {
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                    } else if (response.code() == 500){
                        JSONObject object = new JSONObject(response.body().string());
                        info += object.getString("message");
                    }
                    onSendMsgListener.onGroupInfo(info);
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void kickGroup(final String groupId, final String users) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/accounts?accounts=" + users;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .delete()
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    String info = "";
                    if (response.isSuccessful()) {
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void updateGroup(final String groupId, final String newOwnerUuid,  final String newGroupName, final String newGroupBulletin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
                String json = "{\"topicId\":\"" + groupId + "\", \"ownerUuid\":\"" + newOwnerUuid + "\", \"topicName\":\""
                        + newGroupName + "\", \"bulletin\":\"" + newGroupBulletin + "\"}";
                MediaType JSON = MediaType.parse("application/json");
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .put(RequestBody.create(JSON, json))
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        object = object.getJSONObject("data");
                        JSONObject topicInfo = object.getJSONObject("topicInfo");
                        info += "群ID：" + topicInfo.getString("topicId") + "\n";
                        info += "OwnerUuid：" + topicInfo.getString("ownerUuid") + "\n";
                        info += "群名：" + topicInfo.getString("topicName") + "\n";
                        info += "群名公告：" + topicInfo.getString("bulletin") + "\n";
                        JSONArray members = object.getJSONArray("members");
                        for (int i = 0; i < members.length(); i++) {
                            JSONObject member = members.getJSONObject(i);
                            info += "成员：" + member.getString("account") + "    uuid：" + member.getString("uuid") + "\n";
                        }
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }

    public void dissmissGroup(final String groupId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request
                        .Builder()
                        .url(url)
                        .addHeader("appKey", appKey)
                        .addHeader("appSecret", appSecret)
                        .addHeader("appAccount", appAccount)
                        .delete()
                        .build();
                Call call = client.newCall(request);
                try {
                    Response response = call.execute();
                    if (response.isSuccessful()) {
                        String info = "";
                        JSONObject object = new JSONObject(response.body().string());
                        info = object.getString("message");
                        onSendMsgListener.onGroupInfo(info);
                    }
                } catch (Exception e) {
                    onSendMsgListener.onGroupInfo(e.getMessage());
                }
            }
        }).start();
    }
}




