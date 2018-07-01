package com.bs.main.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.parameter.SQLLiteConstant
import com.bs.util.BaseActivity
import com.bs.util.MainHandler
import com.bs.util.NetConnectionUtil
import net.sf.json.JSONObject

/**
 * Intent:
 * msg:备注名
 * friendID:用户ID
 * instruction:用户描述
 */
class RemarkNameActivity : BaseActivity(),TextWatcher {

    lateinit var finishBtn:TextView
    lateinit var remarkName:EditText
    lateinit var description:EditText

    var friendID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.remark_name_layout)
        friendID = intent.extras.getString("friendID")

        findViewById(R.id.cancel_btn).setOnClickListener{finish()}
        finishBtn = findViewById(R.id.finish_btn) as TextView
        finishBtn.setOnClickListener{
            val map = mapOf("msgType" to Constant.UPDATE_FRIEND, "mode" to "0", "remark_name" to remarkName.text.toString(), "instruction" to description.text.toString(),
                            "self_id" to Preference.userInfoMap["user_id"], "friend_id" to friendID)
            Thread{
                val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0)
                MainHandler.getInstance().post {
                    if(result == "[null]" || result == Constant.SERVER_CONNECTION_ERROR)
                        Toast.makeText(this@RemarkNameActivity, getString(R.string.no_network), Toast.LENGTH_SHORT).show()

                    else{
                        DataBaseUtil.update("update friend set remark_name = '"+remarkName.text.toString()+"' where friend_id = '"+friendID+"'", SQLLiteConstant.FRIEND_TABLE)
                        finish()
                    }
                }
            }.start()

        }

        remarkName = findViewById(R.id.remark_edit) as EditText
        remarkName.setText(intent.extras.getString("msg"))
        remarkName.setSelection(remarkName.text.toString().length)
        description = findViewById(R.id.description_edit) as EditText
        description.setText(intent.extras.getString("instruction"))
        remarkName.addTextChangedListener(this)
        description.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        finishBtn.isEnabled = remarkName.text.toString().isNotEmpty() || description.text.toString().isNotEmpty()
    }
}
