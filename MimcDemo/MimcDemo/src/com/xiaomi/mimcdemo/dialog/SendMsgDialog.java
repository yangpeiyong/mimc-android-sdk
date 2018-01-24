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
import com.xiaomi.mimc.MIMCMessage;
import com.xiaomi.mimc.MimcConstant;
import com.xiaomi.mimc.MimcException;
import com.xiaomi.mimc.User;


public class SendMsgDialog extends Dialog {

    public SendMsgDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_msg_dialog);
        setCancelable(true);
        setTitle(R.string.button_send);
        final EditText toEditText = (EditText)findViewById(R.id.chat_to);
        final SharedPreferences sp = SystemUtils.getContext()
                .getSharedPreferences("user", Context.MODE_PRIVATE);
        toEditText.setText(sp.getString("toAccount", null));

        findViewById(R.id.chat_send).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String mTo = toEditText.getText().toString();
                byte mContent[] = ((EditText) findViewById(R.id.chat_content))
                        .getText().toString().getBytes();

                sp.edit().putString("toAccount", mTo).commit();

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
                            user.sendMessage(mTo, mContent);
                    } catch (MimcException e) {
                        e.printStackTrace();
                    }

                    MIMCMessage message = new MIMCMessage();
                    message.setPayload(mContent);
                    message.setFromAccount(userManager.getAccount());
                    UserManager.getInstance().addMsg(message);
                    dismiss();
                }
            }
        });
    }
}
