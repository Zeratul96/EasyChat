package com.bs.function.words

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

class WriteWordsActivity : BaseActivity() , TextWatcher {

    lateinit var editText: EditText
    lateinit var finishBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.write_words_layout)

        findViewById(R.id.cancel_btn).setOnClickListener { finish() }
        finishBtn = findViewById(R.id.finish_btn) as TextView
        finishBtn.setOnClickListener {
            Thread{
                val map = mapOf("msgType" to Constant.INSERT_WORDS, "user_id" to Preference.userInfoMap["user_id"], "write_to_user" to intent.extras.get("write_to_user"), "content" to editText.text.toString())
                val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0)
                MainHandler.getInstance().post {
                    if(result== Constant.SERVER_CONNECTION_ERROR)
                        Toast.makeText(this@WriteWordsActivity, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    else{
                        WordsActivity.shouldRefreshContent = true
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
