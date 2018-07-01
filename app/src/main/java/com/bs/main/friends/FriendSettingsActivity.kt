package com.bs.main.friends

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.parameter.ChatServerConstant
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.parameter.SQLLiteConstant
import com.bs.util.ActivityListUtil
import com.bs.util.BaseActivity
import com.bs.util.MainHandler
import com.bs.util.NetConnectionUtil
import com.bs.widget.MyToggleButton
import net.sf.json.JSONObject

/**
 * 获取数据：shield_friend
 * stealth_self
 * friendID
 * instruction
 */
class FriendSettingsActivity : BaseActivity(),View.OnClickListener,View.OnTouchListener {

    lateinit var selections:Array<LinearLayout>
    var originY = 0
    val DEVIATION:Int = 10

    lateinit var shieldFriendBtn:MyToggleButton
    lateinit var stealthSelfBtn:MyToggleButton
    var shieldFriendState = "0"
    var stealthSelfState = "0"
    lateinit var alertDialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_settings_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selections = arrayOf(findViewById(R.id.remark_layout) as LinearLayout,findViewById(R.id.complain_layout) as LinearLayout)

        stealthSelfBtn = findViewById(R.id.stealth_self_btn) as MyToggleButton
        shieldFriendBtn = findViewById(R.id.shield_moments_btn) as MyToggleButton

        shieldFriendState = intent.extras.getString("shield_friend")
        stealthSelfState = intent.extras.getString("stealth_self")

        stealthSelfBtn.setToggleState( stealthSelfState == "1")
        shieldFriendBtn.setToggleState( shieldFriendState == "1")

        stealthSelfBtn.setOnToggleStateListener{
            val check = if(stealthSelfBtn.currentState)  "1" else "0"
            if(check == stealthSelfState) return@setOnToggleStateListener
            stealthSelfState = if(stealthSelfBtn.currentState)  "1" else "0"
            momentsAccessUpdate("stealth")
        }

        shieldFriendBtn.setOnToggleStateListener {
            val check = if(shieldFriendBtn.currentState)  "1" else "0"
            if(check == shieldFriendState) return@setOnToggleStateListener
            shieldFriendState = if(shieldFriendBtn.currentState) "1" else "0"
            momentsAccessUpdate("shield")
        }

        for(selection in selections){
            selection.setOnClickListener(this)
            selection.setOnTouchListener(this)
        }

        findViewById(R.id.del_btn).setOnClickListener{
            showAlertDialog()
        }


    }

    override fun onResume() {
        super.onResume()

        for(selection in selections)
            selection.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val TOP_Y = 0
        val BOTTOM_Y = v?.bottom!! - v.top

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                originY = event.y.toInt()

                v.setBackgroundColor(Color.parseColor("#d9d9d9"))
            }

            MotionEvent.ACTION_UP,

            MotionEvent.ACTION_MOVE -> if (event.y < TOP_Y || event.y > BOTTOM_Y || Math.abs(originY - event.y) > DEVIATION) {
                v.setBackgroundColor(Color.parseColor("#FFFFFF"))
                return true
            }
        }
        return false
    }


    override fun onClick(v: View?) {
        when(v){
            selections[0] ->{startActivity(Intent(this,RemarkNameActivity::class.java).putExtras(intent.extras))}

            selections[1] ->{
                val bundle = Bundle()
                bundle.putString("friendID",intent.extras.getString("friendID"))
                startActivity(Intent(this,ComplainActivity::class.java).putExtras(bundle))
            }
        }
    }

    fun momentsAccessUpdate(type:String){
        val info = mapOf("msgType" to Constant.UPDATE_FRIEND, "mode" to "1", "shield_friend_moments" to shieldFriendState, "stealth_self_moments" to stealthSelfState,
                "self_id" to Preference.userInfoMap["user_id"], "friend_id" to intent.extras.getString("friendID"))
        Thread{
            val result = NetConnectionUtil.uploadData(JSONObject.fromObject(info).toString(), 0)
            MainHandler.getInstance().post {
                if(result == "[null]" || result == Constant.SERVER_CONNECTION_ERROR){
                    Toast.makeText(this@FriendSettingsActivity, getString(R.string.no_network),Toast.LENGTH_SHORT).show()
                    if(type == "stealth") {
                        stealthSelfBtn.setToggleState(!stealthSelfBtn.currentState)
                        stealthSelfState = if(stealthSelfState == "1") "0" else "1"
                    } else {
                        shieldFriendBtn.setToggleState(!shieldFriendBtn.currentState)
                        shieldFriendState = if(shieldFriendState == "1") "0" else "1"
                    }
                }
                else{
                    val sql = "update friend set shield_friend_moments = '$shieldFriendState',stealth_self_moments = '$stealthSelfState'" +
                            " where friend_id = '"+intent.extras.get("friendID")+"'"
                    DataBaseUtil.update(sql, SQLLiteConstant.FRIEND_TABLE)
                }
            }
        }.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showAlertDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("提示")
        //对话框内容
        builder.setMessage("将联系人删除，同时删除所有与该联系人的聊天记录")
        //确定按钮
        builder.setPositiveButton("删除") { _, _ ->
            Thread{
                val map = mapOf("msgType" to ChatServerConstant.DELETE_FRIEND, "self_id" to Preference.userInfoMap["user_id"], "friend_id" to intent.extras.getString("friendID"))
                val result = NetConnectionUtil.uploadChatData(JSONObject.fromObject(map).toString())
                if(result.isNotEmpty()){//如果不是空的说明连接服务器失败
                    MainHandler.getInstance().post {
                        Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    NetConnectionUtil.cat.setOnDeleteFriendOKListener {
                        msg ->
                        if(msg["result"] == Constant.OPERATION_SUCCEED){
                            ActivityListUtil.destroyLastActivity(2)
                        }
                        else MainHandler.getInstance().post {
                            Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }.start()
        }
        builder.setNegativeButton("取消") { _, _ -> alertDialog.dismiss() }

        //固定对话框使其不可被取消
        builder.setCancelable(false)
        //创建对话框
        alertDialog = builder.create()
        alertDialog.show()
    }
}
