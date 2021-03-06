package com.xiaomi.mimcdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.SystemUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.mimc.MIMCGroupMessage;
import com.xiaomi.mimc.MimcConstant;
import com.xiaomi.mimc.MimcException;
import com.xiaomi.mimc.User;

public class SendGroupMsgDialog extends Dialog {

    public SendGroupMsgDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_group_msg_dialog);
        setCancelable(true);
        setTitle(R.string.send_group_msg);
        final EditText etGroupId = (EditText)findViewById(R.id.et_group_id);
        final SharedPreferences sp = SystemUtils.getContext()
                .getSharedPreferences("user", Context.MODE_PRIVATE);
        etGroupId.setText(sp.getString("toGroupId", null));

        findViewById(R.id.btn_group_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTo = etGroupId.getText().toString();

                sp.edit().putString("toGroupId", mTo).commit();

                byte mContent[] = ((EditText)findViewById(R.id.et_group_content))
                        .getText().toString().getBytes();

                if (!NetWorkUtils.isNetwork(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.network_unavailable), Toast.LENGTH_SHORT).show();
                    return;
                } else if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                    Toast.makeText(getContext(), getContext().getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!TextUtils.isEmpty(mTo)){
                    UserManager userManager = UserManager.getInstance();
                    User user = userManager.getUser();
                    try {
                        if (user != null)
                            user.sendGroupMessage(Long.parseLong(mTo), mContent);
                    } catch (MimcException e) {
                        e.printStackTrace();
                    }

                    MIMCGroupMessage message = new MIMCGroupMessage();
                    message.setPayload(mContent);
                    message.setFromAccount(userManager.getAccount());
                    UserManager.getInstance().addMsg(message);
                    dismiss();
                }
            }
        });
    }
}
