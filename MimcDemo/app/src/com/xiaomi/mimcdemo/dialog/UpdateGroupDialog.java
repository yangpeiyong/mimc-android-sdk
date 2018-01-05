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

public class UpdateGroupDialog extends Dialog {

    public UpdateGroupDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_group_dialog);
        setCancelable(true);
        setTitle(R.string.update_group);
        final EditText etGroupId = (EditText)findViewById(R.id.et_group_id);
        final EditText etNewOwnerUuid = (EditText)findViewById(R.id.et_new_owner_uuid);
        final EditText etGroupName =(EditText)findViewById(R.id.et_group_name);
        final EditText etGroupBulletin = (EditText)findViewById(R.id.et_group_bulletin);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String groupId = etGroupId.getText().toString();
                String newOwnerUuid = etNewOwnerUuid.getText().toString();
                String groupName = etGroupName.getText().toString();
                String groupBulletin = etGroupBulletin.getText().toString();

                if (!NetWorkUtils.isNetwork(getContext())) {
                    Toast.makeText(getContext(), "创建失败，无网络连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                    Toast.makeText(getContext(), "登录异常，请重新登陆", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (groupId.isEmpty()) {
                    Toast.makeText(getContext(), "请指定群ID", Toast.LENGTH_SHORT).show();
                    return;
                } else if(newOwnerUuid.isEmpty()) {
                    Toast.makeText(getContext(), "请指定新群主UUID", Toast.LENGTH_SHORT).show();
                    return;
                } else if (groupName.isEmpty()) {
                    Toast.makeText(getContext(), "请指定群名", Toast.LENGTH_SHORT).show();
                    return;
                } else if (groupBulletin.isEmpty()) {
                    Toast.makeText(getContext(), "请指定群公告", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserManager.getInstance().updateGroup(groupId, newOwnerUuid, groupName, groupBulletin);
                dismiss();
            }
        });
    }
}
