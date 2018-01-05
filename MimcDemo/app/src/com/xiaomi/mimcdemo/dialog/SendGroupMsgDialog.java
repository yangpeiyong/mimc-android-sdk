package com.xiaomi.mimcdemo.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaomi.channel.commonutils.logger.MyLog;
import com.xiaomi.mimcdemo.R;
import com.xiaomi.mimcdemo.common.NetWorkUtils;
import com.xiaomi.mimcdemo.common.UserManager;
import com.xiaomi.push.mimc.MIMCGroupMessage;
import com.xiaomi.push.mimc.MIMCMessage;
import com.xiaomi.push.mimc.MimcConstant;
import com.xiaomi.push.mimc.MimcException;
import com.xiaomi.push.mimc.User;

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
        findViewById(R.id.btn_group_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTo = etGroupId.getText().toString();
                byte mContent[] = ((EditText)findViewById(R.id.et_group_content))
                        .getText().toString().getBytes();

                if (!NetWorkUtils.isNetwork(getContext())) {
                    Toast.makeText(getContext(), "发送失败，无网络连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (UserManager.getInstance().getStatus() != MimcConstant.STATUS_LOGIN_SUCCESS) {
                    Toast.makeText(getContext(), "登录异常，请重新登陆", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(mTo)){
                    UserManager userManager = UserManager.getInstance();
                    User user = userManager.getUser(userManager.getAccount());
                    try {
                        user.sendGroupMessage(Long.parseLong(mTo), mContent);
                    } catch (MimcException e) {
                        MyLog.w("send msg exception:"+e.getMessage());
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
