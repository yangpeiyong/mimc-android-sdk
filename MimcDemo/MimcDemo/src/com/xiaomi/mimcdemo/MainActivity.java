package com.xiaomi.mimcdemo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.mimcdemo.common.ChatAdapter;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.ParseJson;
import com.xiaomi.mimcdemo.common.SystemUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimcdemo.dialog.CreateGroupDialog;
import com.xiaomi.mimcdemo.dialog.DismissGroupDialog;
import com.xiaomi.mimcdemo.dialog.GroupInfoDialog;
import com.xiaomi.mimcdemo.dialog.JoinGroupDialog;
import com.xiaomi.mimcdemo.dialog.KickGroupDialog;
import com.xiaomi.mimcdemo.dialog.LoginDialog;
import com.xiaomi.mimcdemo.dialog.PullP2PHistoryMsgDialog;
import com.xiaomi.mimcdemo.dialog.PullP2THistoryMsgDialog;
import com.xiaomi.mimcdemo.dialog.QueryGroupInfoDialog;
import com.xiaomi.mimcdemo.dialog.QuitGroupDialog;
import com.xiaomi.mimcdemo.dialog.SendGroupMsgDialog;
import com.xiaomi.mimcdemo.dialog.SendMsgDialog;
import com.xiaomi.mimcdemo.dialog.UpdateGroupDialog;
import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MimcConstant;
import com.xiaomi.mimc.MimcException;
import com.xiaomi.mimc.User;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements UserManager.OnSendMsgListener {
    private ChatAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<MIMCGroupMessage> mdatas = new ArrayList<>();
    GroupInfoDialog groupInfoDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        groupInfoDialog = new GroupInfoDialog(this);
        UserManager.getInstance().setOnSendMsgListener(this);

        findViewById(R.id.mimc_login).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog LoginDialog = new LoginDialog(MainActivity.this);
                    LoginDialog.show();
                }
            });

        findViewById(R.id.mimc_logout).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = UserManager.getInstance().getUser();
                    if (user != null) {
                        user.logout();
                    }
                }
            });

        findViewById(R.id.mimc_sendMsg).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog SendMsgDialog = new SendMsgDialog(MainActivity.this);
                    SendMsgDialog.show();
                }
            });

        findViewById(R.id.btn_create_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgCreateGroup = new CreateGroupDialog(MainActivity.this);
                    dlgCreateGroup.show();
                }
            });

        findViewById(R.id.btn_query_group_info).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgQueryGroupInfo = new QueryGroupInfoDialog(MainActivity.this);
                    dlgQueryGroupInfo.show();
                }
            });

        findViewById(R.id.btn_query_all_group_info).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetWorkUtils.isNetwork(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                        Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UserManager.getInstance().queryGroupsOfAccount();
                }
            });

        findViewById(R.id.btn_join_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgJoinGroup = new JoinGroupDialog(MainActivity.this);
                    dlgJoinGroup.show();
                }
            });

        findViewById(R.id.btn_quit_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgQuitGroup = new QuitGroupDialog(MainActivity.this);
                    dlgQuitGroup.show();
                }
            });

        findViewById(R.id.btn_kick_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgKickGroup = new KickGroupDialog(MainActivity.this);
                    dlgKickGroup.show();
                }
            });

        findViewById(R.id.btn_update_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgUpdateGroup = new UpdateGroupDialog(MainActivity.this);
                    dlgUpdateGroup.show();
                }
            });

        findViewById(R.id.btn_dismiss_group).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgDismissGroup = new DismissGroupDialog(MainActivity.this);
                    dlgDismissGroup.show();
                }
            });

        findViewById(R.id.btn_send_group_msg).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgSendGroupMsg = new SendGroupMsgDialog(MainActivity.this);
                    dlgSendGroupMsg.show();
                }
            });

        findViewById(R.id.btn_p2p_history).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog dlgP2PHistory = new PullP2PHistoryMsgDialog(MainActivity.this);
                    dlgP2PHistory.show();
                }
            });

        findViewById(R.id.btn_p2t_history).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialog dlgP2THistory = new PullP2THistoryMsgDialog(MainActivity.this);
                        dlgP2THistory.show();
                    }
                });


        mRecyclerView = (RecyclerView) findViewById(R.id.rv_chat);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChatAdapter(this, mdatas);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        onChannelStatusChanged(UserManager.getInstance().getStatus());
    }

    public void onChannelStatusChanged(int status) {
        TextView textView = (TextView) findViewById(R.id.mimc_status);
        Drawable drawable;
        if (status == MimcConstant.STATUS_LOGIN_SUCCESS) {
            drawable = getResources().getDrawable(R.drawable.point_h);
        } else {
            drawable = getResources().getDrawable(R.drawable.point);
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null,
                null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(UserManager.getInstance().getAccount())) {
            try {
                User user = UserManager.getInstance().getUser();
                if (user != null) user.pull();
            } catch (MimcException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onHandleMessage(final MIMCMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MIMCGroupMessage groupMessage = new MIMCGroupMessage();
                groupMessage.setGroupId(-1);
                groupMessage.setPayload(message.getPayload());
                groupMessage.setFromAccount(message.getFromAccount());
                groupMessage.setFromResource(message.getFromResource());
                mdatas.add(groupMessage);
                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onHandleGroupMessage(final MIMCGroupMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mdatas.add(message);
                mAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onStatusChanged(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onChannelStatusChanged(status);
            }
        });
    }

    @Override
    public void onServerAck(final String packetId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SystemUtils.getContext(), "Server has received packetId："
                        + packetId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreateGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseCreateGroupJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onQueryGroupInfo(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseQueryGroupInfoJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onQueryGroupsOfAccount(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseQueryGroupsOfAccountJson(this, json);
        }
        final String info = json;
                runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onJoinGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseJoinGroupJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onQuitGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseQuitGroupJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onKickGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseKickGroupJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onUpdateGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseUpdateGroupJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onDismissGroup(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseDismissGroupJson(json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onPullP2PHistory(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseP2PHistoryJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }

    @Override
    public void onPullP2THistory(String json, boolean isSuccess) {
        if (isSuccess) {
            json = ParseJson.parseP2THistoryJson(this, json);
        }
        final String info = json;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupInfoDialog.show();
                groupInfoDialog.setContent(info);
            }
        });
    }
}