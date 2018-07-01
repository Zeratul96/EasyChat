package com.bs.main.friends

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.bs.easy_chat.R
import com.bs.parameter.ChatServerConstant
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.util.BaseActivity
import com.bs.util.MainHandler
import com.bs.util.NetConnectionUtil
import com.bs.widget.CustomProgress
import net.sf.json.JSONObject

/**
 * Intent:request_user
 */
class RequestAddFriendActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_add_friend_layout)

        findViewById(R.id.cancel_btn).setOnClickListener { finish() }
        val requestReason = findViewById(R.id.reason_edit_text) as EditText
        requestReason.setText("你好，我是"+if(Preference.userInfoMap["name"].isNullOrEmpty()) Preference.userInfoMap["nickname"] else Preference.userInfoMap["name"])
        requestReason.setSelection(requestReason.text.length)

        findViewById(R.id.send_btn).setOnClickListener {
            val progress = CustomProgress.show(this@RequestAddFriendActivity, "请稍后…",false, null)
            val map = mapOf("msgType" to ChatServerConstant.REQUEST_ADD_FRIEND,"reason" to requestReason.text.toString(), "user_id" to Preference.userInfoMap["user_id"], "remark_name" to (findViewById(R.id.remark_edit_text) as EditText).text.toString(), "instruction" to (findViewById(R.id.description_edit_text) as EditText).text.toString(), "request_user" to intent.extras.get("request_user"))
            Thread{
                val result = NetConnectionUtil.uploadChatData(JSONObject.fromObject(map).toString())
                if(result.isNotEmpty()){//如果不是空的说明连接服务器失败
                    MainHandler.getInstance().post {
                        progress.dismiss()
                        Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    NetConnectionUtil.cat.setOnRequestAddFriendListener{
                        msg ->
                        progress.dismiss()
                        if(msg["result"] == Constant.OPERATION_SUCCEED) finish() else Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }
}
