package com.bs.main.friends

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.SQLLiteConstant
import com.bs.tool_package.FastJSON
import com.bs.tool_package.UserInfoHelper
import com.bs.util.*
import net.sf.json.JSONObject

/**
 * Intent:msg
 * is_need_reload_data
 * is_request 若为true则还有record_id
 */
class ProtentialFriendActivity : BaseActivity(){

    var originY = 0
    val DEVIATION = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.protential_friend_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val info = intent.getSerializableExtra("msg") as HashMap<String, String>
        loadInfoData(info)
        if(intent.extras.getBoolean("is_need_reload_data"))
            reloadUserInfo(info["user_id"]!!)
    }

    fun loadInfoData(map:HashMap<String,String>){
        findViewById(R.id.sculpture_image).setOnClickListener{
            val bundle = Bundle()
            bundle.putString("msg",  map["sculpture"])
            startActivity(Intent(this, FriendSculptureActivity::class.java).putExtras(bundle))
        }

        val button = findViewById(R.id.add_to_contact) as Button
        if(intent.extras.getBoolean("is_request")){
            button.text = "通过验证"
            button.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("record_id", intent.extras.getString("record_id"))
                startActivity(Intent(this@ProtentialFriendActivity, AgreeAddFriendActivity::class.java).putExtras(bundle))
            }
        }
        else{
            button.setOnClickListener{
                val bundle = Bundle()
                bundle.putString("request_user", map["user_id"])
                startActivity(Intent(this, RequestAddFriendActivity::class.java).putExtras(bundle))
            }
        }

        (findViewById(R.id.name) as TextView).text = map["nickname"]
        (findViewById(R.id.one_view) as TextView).text = "易聊号： "+map["user_id"]
        (findViewById(R.id.area_text_view) as TextView).text = UserInfoHelper.findUserArea(map["areas"]!!)
        (findViewById(R.id.handwriting_text_view) as TextView).text = map["handwriting"]
        (findViewById(R.id.introduction_view) as TextView).text = map["self_introduction"]
        val sculpture = findViewById(R.id.gender) as ImageView
        if(map["gender"] == "男")
            sculpture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.male))
        else if(map["gender"] == "女")
            sculpture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.female))

        Thread{
            val bitmap = ImageTransmissionUtil.loadSculpture(this@ProtentialFriendActivity,map["sculpture"]!!)
            MainHandler.getInstance().post {
                if(bitmap !== null)
                    (findViewById(R.id.sculpture_image) as ImageView).setImageBitmap(bitmap)
            }
        }.start()
    }

    fun reloadUserInfo(protentialFriendID:String){
        Thread{
            val map = mapOf("msgType" to Constant.QUERY_USER_EXACTLY, "user_id" to protentialFriendID)
            val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), Constant.USER_SERVER)
            if(result != Constant.SERVER_CONNECTION_ERROR && result != "[null]"){
                val mapInfo = FastJSON.parseJSON2ListString(result)[0]
                MainHandler.getInstance().post {
                    DataBaseUtil.delete("delete from protential_friend where user_id = '$protentialFriendID'", SQLLiteConstant.PROTENTIAL_FRIEND)
                    DataBaseUtil.insert("insert into protential_friend values('"+mapInfo["user_id"]+"','"+mapInfo["gender"]+
                    "','"+mapInfo["sculpture"]+"','"+mapInfo["nickname"]+"','"+mapInfo["areas"]+"','"+mapInfo["self_introduction"]+"','"+mapInfo["handwriting"]+"')", SQLLiteConstant.PROTENTIAL_FRIEND)
                    loadInfoData(mapInfo as HashMap<String, String>)
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
}
