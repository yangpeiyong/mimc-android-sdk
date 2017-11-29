package com.xiaomi.mimcdemo;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.xiaomi.mimcdemo.common.ChatAdapter;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.push.mimc.MIMCMessage;
import com.xiaomi.push.mimc.MimcConstant;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements UserManager.OnSendMsgListener{
    private ChatAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<MIMCMessage> mdatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        UserManager.getInstance().getUser(UserManager
                                .getInstance().getAccount()).logout();
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


        mRecyclerView = (RecyclerView) findViewById(R.id.rv_chat);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new ChatAdapter(this, mdatas);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSent(final MIMCMessage message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mdatas.add(message);
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onSentStatus(final int status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onChannelStatusChanged(status);
            }
        });
    }
}