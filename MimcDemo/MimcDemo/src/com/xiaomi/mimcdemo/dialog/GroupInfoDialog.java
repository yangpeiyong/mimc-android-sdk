package com.xiaomi.mimcdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.SystemUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.push.mimc.MIMCMessage;
import com.xiaomi.push.mimc.MimcConstant;
import com.xiaomi.push.mimc.MimcException;
import com.xiaomi.push.mimc.User;

public class GroupInfoDialog extends Dialog {
    private TextView tvContent;

    public GroupInfoDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_info_dialog);
        setCancelable(true);
        setTitle(R.string.tips);
        tvContent = (TextView)findViewById(R.id.tv_content);
    }

    public void setContent(String info) {
        tvContent.setText(info);
    }
}
