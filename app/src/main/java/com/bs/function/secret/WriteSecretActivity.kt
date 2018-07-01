package com.bs.function.secret

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.util.BaseActivity
import com.bs.util.MainHandler
import com.bs.util.NetConnectionUtil
import net.sf.json.JSONObject

class WriteSecretActivity : BaseActivity() , TextWatcher {

    lateinit var editText: EditText
    lateinit var finishBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.write_words_layout)

        (findViewById(R.id.title_name) as TextView).text = "写悄悄话"
        findViewById(R.id.cancel_btn).setOnClickListener { finish() }
        finishBtn = findViewById(R.id.finish_btn) as TextView
        finishBtn.setOnClickListener {
            Thread{
                val map = mapOf("msgType" to Constant.INSERT_SECRET, "user_id" to Preference.userInfoMap["user_id"],"content" to editText.text.toString())
                val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0)
                MainHandler.getInstance().post {
                    if(result== Constant.SERVER_CONNECTION_ERROR)
                        Toast.makeText(this@WriteSecretActivity, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    else{
                        SecretActivity.shouldRefreshContent = true
                        finish()
                    }
                }
            }.start()
        }
        editText = findViewById(R.id.editText) as EditText
        editText.addTextChangedListener(this)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        finishBtn.isEnabled = editText.text.toString().isNotEmpty()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable?) {}

}
