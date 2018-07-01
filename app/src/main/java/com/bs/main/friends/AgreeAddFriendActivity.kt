package com.bs.main.friends

/**
 * Intent:
 * record_id
 */
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bs.easy_chat.R
import com.bs.main.MainActivity
import com.bs.parameter.ChatServerConstant
import com.bs.parameter.Constant
import com.bs.util.BaseActivity
import com.bs.util.MainHandler
import com.bs.util.NetConnectionUtil
import com.bs.widget.CustomProgress
import net.sf.json.JSONObject

class AgreeAddFriendActivity : BaseActivity() {

    lateinit var finishBtn:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_add_friend_layout)
        (findViewById(R.id.title_name) as TextView).text = "通过验证"

        finishBtn = findViewById(R.id.send_btn) as TextView
        finishBtn.text = "确定"
        finishBtn.setOnClickListener {
            val progress = CustomProgress.show(this@AgreeAddFriendActivity, "请稍后…",false, null)
            val map = mapOf("msgType" to ChatServerConstant.AGREE_ADD_FRIEND_REQUEST, "remark_name" to (findViewById(R.id.remark_edit_text) as EditText).text.toString(), "instruction" to (findViewById(R.id.description_edit_text) as EditText).text.toString(), "record_id" to intent.extras.getString("record_id"))
            Thread{
                val result = NetConnectionUtil.uploadChatData(JSONObject.fromObject(map).toString())
                if(result == Constant.SERVER_CONNECTION_ERROR){
                    MainHandler.getInstance().post {
                        progress.dismiss()
                        Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    NetConnectionUtil.cat.setOnAgreeAddFriendListener {
                        msg ->
                        progress.dismiss()
                        if (msg["result"] == Constant.OPERATION_SUCCEED) {
                            if(MainActivity.mainActivity.messageFragment != null)
                                MainActivity.mainActivity.messageFragment.onReceiveSingleMessage(msg, false)

                            finish()
                        }

                        else Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }

        findViewById(R.id.cancel_btn).setOnClickListener { finish() }
        findViewById(R.id.title0).visibility = View.GONE
        findViewById(R.id.line0).visibility = View.GONE
        findViewById(R.id.line1).visibility = View.GONE
        findViewById(R.id.reason_edit_text).visibility = View.GONE
    }
}
