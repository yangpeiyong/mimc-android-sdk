package com.xiaomi.mimcdemo.common;

import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MIMCTokenFetcher;
import com.xiaomi.mimc.MimcLogger;
import com.xiaomi.mimc.MimcMessageHandler;
import com.xiaomi.mimc.MimcOnlineStatusListener;
import com.xiaomi.mimc.User;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
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
    private String token = "bJRLeg7AgtSh0T13YjL/IFDdK0JTjCJG4KdSfB9L7c0+fxzihaW2nqjG1PONAKI8oYJeHpzgG8crdV6Io0iEsdsXEK0ahSQmSLAMm2zInHcLaybr//o/Fq6eT3ET7RKjVYgi6wNBiMnJ7WfN26gCINUcJoML89/+OdcsrnHlV/g9pEi7rhcRYL2elnC9oUjIpGda6yEk1veedO/WPxD4T32Pa+kn+bw5gnkWFLuJJEm9irZAI+YHWVGRGJB30Ae1UnmcfeBusobSS8Co3jtt1VbHeuSlkvrUo0xlOwgQCASzChUeMDHUJizxUYjNl9NDct4VwyPy0jFYYTKE+yYvLg==";

    private UserManager() {
    }

    private OnSendMsgListener onSendMsgListener;

    public void setOnSendMsgListener(OnSendMsgListener onSendMsgListener) {
        this.onSendMsgListener = onSendMsgListener;
    }

    public interface OnSendMsgListener {
        void onHandleMessage(MIMCMessage message);
        void onHandleGroupMessage(MIMCGroupMessage message);
        void onStatusChanged(int status);
        void onServerAck(String packetId);
        void onCreateGroup(String json, boolean isSuccess);
        void onQueryGroupInfo(String json, boolean isSuccess);
        void onQueryGroupsOfAccount(String json, boolean isSuccess);
        void onJoinGroup(String json, boolean isSuccess);
        void onQuitGroup(String json, boolean isSuccess);
        void onKickGroup(String json, boolean isSuccess);
        void onUpdateGroup(String json, boolean isSuccess);
        void onDismissGroup(String json, boolean isSuccess);
        void onPullP2PHistory(String json, boolean isSuccess);
        void onPullP2THistory(String json, boolean isSuccess);
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
        onSendMsgListener.onHandleMessage(message);
    }

    public void addMsg(MIMCGroupMessage message) {
        onSendMsgListener.onHandleGroupMessage(message);
    }

    public void serverAck(String packetId){
        onSendMsgListener.onServerAck(packetId);
    }

    public User getUser() {
        return mUser;
    }

    public User newUser(String account){
        if (account == null) return null;
        if (mUser != null) {
            if (!account.equals(appAccount)){
                mUser.logout();
                mUser = null;
            }
        }
        if (mUser == null) {
            mUser = new User(appId, account);
            mUser.registerTokenFetcher(new TokenFetcher());
            mUser.registerMessageHandler(new MessageHandler());
            mUser.registerOnlineStatusListener(new OnlineStatusListener());
            appAccount = account;
        }

        return mUser;
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
                    MimcLogger.w("data failure");
                }
                data = object.getJSONObject("data");
                token = data.getString("token");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data.toString();
        }
    }

    public void createGroup(final String groupName, final String users) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId;
        String json = "{\"topicName\":\"" + groupName + "\", \"accounts\":\"" + users + "\"}";
        MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .post(RequestBody.create(JSON, json))
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onCreateGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryGroupInfo(final String groupId) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .get()
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onQueryGroupInfo(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryGroupsOfAccount() {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/account";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .get()
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onQueryGroupsOfAccount(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinGroup(final String groupId, final String users) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/accounts";
        String json = "{\"accounts\":\"" + users + "\"}";
        MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .post(RequestBody.create(JSON, json))
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onJoinGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void quitGroup(final String groupId) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/account";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .delete()
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onQuitGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void kickGroup(final String groupId, final String users) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId + "/accounts?accounts=" + users;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .delete()
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onKickGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGroup(final String groupId, final String newOwnerUuid,  final String newGroupName, final String newGroupBulletin) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
        final String json = "{\"topicId\":\"" + groupId + "\", \"ownerUuid\":\"" + newOwnerUuid + "\", \"topicName\":\""
                + newGroupName + "\", \"bulletin\":\"" + newGroupBulletin + "\"}";
        MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .put(RequestBody.create(JSON, json))
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onUpdateGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissGroup(final String groupId) {
        url = "https://mimc.chat.xiaomi.net/api/topic/" + appId + "/" + groupId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("token", token)
                .delete()
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onCreateGroup(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onDismissGroup(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pullP2PHistory(String toAccount, String fromAccount, String utcFromTime, String utcToTime) {
        url = "https://mimc.chat.xiaomi.net/api/msg/p2p/query/";
        String json = "{\"appId\":\"" + appId + "\", \"toAccount\":\"" + toAccount + "\", \"fromAccount\":\""
                + fromAccount + "\", \"utcFromTime\":\"" + utcFromTime + "\", \"utcToTime\":\"" +
                utcToTime + "\"}";
        MediaType JSON = MediaType.parse("application/json;charset=UTF-8");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("Accept", "application/json;charset=UTF-8")
                .addHeader("token", token)
                .post(RequestBody.create(JSON, json))
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onPullP2PHistory(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onPullP2PHistory(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pullP2THistory(String account, String topicId, String utcFromTime, String utcToTime) {
        url = "https://mimc.chat.xiaomi.net/api/msg/p2t/query/";
        String json = "{\"appId\":\"" + appId + "\", \"account\":\"" + account + "\", \"topicId\":\""
                + topicId + "\", \"utcFromTime\":\"" + utcFromTime + "\", \"utcToTime\":\"" + utcToTime + "\"}";
        MediaType JSON = MediaType.parse("application/json;charset=UTF-8");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(url)
                .addHeader("Accept", "application/json;charset=UTF-8")
                .addHeader("token", token)
                .post(RequestBody.create(JSON, json))
                .build();
        try {
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    onSendMsgListener.onPullP2THistory(e.getMessage(), false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        onSendMsgListener.onPullP2THistory(response.body().string(), true);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



