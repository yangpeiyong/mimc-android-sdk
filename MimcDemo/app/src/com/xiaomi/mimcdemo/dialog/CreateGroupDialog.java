package com.xiaomi.mimcdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.push.mimc.MimcConstant;

public class CreateGroupDialog extends Dialog {

    public CreateGroupDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group_dialog);
        setCancelable(true);
        setTitle(R.string.create_group);
        final EditText etGroupName = (EditText)findViewById(R.id.et_group_name);
        final EditText etUsers = (EditText)findViewById(R.id.et_users);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String groupName = etGroupName.getText().toString();
                String users = etUsers.getText().toString();

                if (!NetWorkUtils.isNetwork(getContext())) {
                    Toast.makeText(getContext(), "创建失败，无网络连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                    Toast.makeText(getContext(), "登录异常，请重新登陆", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (groupName.isEmpty()) {
                    Toast.makeText(getContext(), "请指定群名", Toast.LENGTH_SHORT).show();
                    return;
                } else if (users.isEmpty()) {
                    Toast.makeText(getContext(), "请指定群成员", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserManager.getInstance().createGroup(groupName, users);
                dismiss();
            }
        });
    }
}
