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

public class KickGroupDialog extends Dialog {

    public KickGroupDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kick_group_dialog);
        setCancelable(true);
        setTitle(R.string.kick_group);
        final EditText etGroupId = (EditText)findViewById(R.id.et_group_id);
        final EditText etUsers = (EditText)findViewById(R.id.et_users);

        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String groupId = etGroupId.getText().toString();
                String users = etUsers.getText().toString();

                if (!NetWorkUtils.isNetwork(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                    Toast.makeText(getContext(), getContext().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    return;
                } else if (groupId.isEmpty()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.input_id_of_group), Toast.LENGTH_SHORT).show();
                    return;
                } else if (users.isEmpty()) {
                    Toast.makeText(getContext(), getContext().getString(R.string.input_members_of_group), Toast.LENGTH_SHORT).show();
                    return;
                }

                UserManager.getInstance().kickGroup(groupId, users);
                dismiss();
            }
        });
    }
}
