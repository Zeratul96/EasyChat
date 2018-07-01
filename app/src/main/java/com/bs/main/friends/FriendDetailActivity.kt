package com.bs.main.friends

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.function.words.WordsActivity
import com.bs.main.MainActivity
import com.bs.main.chat.ChatActivity
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.parameter.SQLLiteConstant
import com.bs.person.album.PersonAlbumActivity
import com.bs.tool_package.FastJSON
import com.bs.tool_package.NetWorkUtils
import com.bs.tool_package.UserInfoHelper
import com.bs.util.*
import net.sf.json.JSONObject
import java.util.ArrayList

/**
 * Intent获取：friend_id
 * send_msg_finish
 */
class FriendDetailActivity : BaseActivity(),View.OnTouchListener,View.OnClickListener{

    lateinit var selections:Array<LinearLayout>
    lateinit var lines:Array<View>
    var originY = 0
    val DEVIATION = 10
    var firstInitData = true

    var shownName = ""
    lateinit var friendInfoMap:Map<String,String>

    lateinit var callBtn:Button
    lateinit var sendMessageBtn:Button

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.action_edit_info ->{
                val bundle = Bundle()
                bundle.putString("msg",friendInfoMap["remark_name"])
                bundle.putString("friendID", friendInfoMap["friend_id"])
                bundle.putString("shield_friend",friendInfoMap["shield_friend_moments"])
                bundle.putString("stealth_self", friendInfoMap["stealth_self_moments"])
                bundle.putString("instruction", friendInfoMap["instruction"])
                startActivity(Intent(this@FriendDetailActivity,FriendSettingsActivity::class.java).putExtras(bundle))
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_detail_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener)

        selections = arrayOf(findViewById(R.id.remark_layout) as LinearLayout,findViewById(R.id.area_layout) as LinearLayout,findViewById(R.id.album_layout) as LinearLayout,findViewById(R.id.words_layout) as LinearLayout,findViewById(R.id.more_info_layout) as LinearLayout)
        lines = arrayOf(findViewById(R.id.line5),findViewById(R.id.line6))

        registerBtnListener()
        findViewById(R.id.sculpture_image).setOnClickListener {
            val bundle = Bundle()
            bundle.putString("msg", friendInfoMap["sculpture"])
            startActivity(Intent(this@FriendDetailActivity, FriendSculptureActivity::class.java).putExtras(bundle))
        }

        for(view in selections) {
            view.setOnTouchListener(this)
            view.setOnClickListener(this)
        }

        //先从本地加载数据
        loadDataLocally(intent.extras.getString("friend_id"))
        if(NetWorkUtils.isNetworkAvailable(this))
            queryNecessaryInfoThread(intent.extras.getString("friend_id"))
    }

    override fun onStart() {
        super.onStart()
        //从其之后的Activity设置回传过来刷新界面数据
        if(!firstInitData){
            val friendInfoTempList = DataBaseUtil.queryFriendsExactly(intent.extras.getString("friend_id"))
            if(friendInfoTempList.isNotEmpty()){
                friendInfoMap = friendInfoTempList[0]
                loadTextData()
            }
            else finish()
        }
        else firstInitData = false
    }

    override fun onResume() {
        super.onResume()

        resetLinesColor()
        for(selection in selections)
            selection.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val TOP_Y = 0
        val BOTTOM_Y = v?.bottom!! - v.top

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                originY = event.y.toInt()

                if(v===selections[1]) return false
                v.setBackgroundColor(Color.parseColor("#d9d9d9"))
                //lines color change
                when(v){
                    selections[2] ->
                    {
                        lines[0].setBackgroundColor(Color.parseColor("#FFFFFF"))
                        lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"))
                    }

                    selections[3] -> lines[1].setBackgroundColor(Color.parseColor("#FFFFFF"))
                }
            }

            MotionEvent.ACTION_UP,

            MotionEvent.ACTION_MOVE -> if (event.y < TOP_Y || event.y > BOTTOM_Y || Math.abs(originY - event.y) > DEVIATION) {
                resetLinesColor()
                v.setBackgroundColor(Color.parseColor("#FFFFFF"))
                return true
            }
        }
        return false
    }

    override fun onClick(v: View?) {
        when(v){
            selections[0] -> {
                val bundle = Bundle()
                bundle.putString("msg", friendInfoMap["remark_name"])
                bundle.putString("friendID", friendInfoMap["friend_id"])
                bundle.putString("instruction", friendInfoMap["instruction"])
                startActivity(Intent(this, RemarkNameActivity::class.java).putExtras(bundle))
            }

            selections[2] ->{
                val bundle = Bundle()
                bundle.putString("user_id", friendInfoMap["friend_id"])
                bundle.putString("title_name", shownName)
                bundle.putString("moments_background", friendInfoMap["moments_background"])
                startActivity(Intent(this, PersonAlbumActivity::class.java).putExtras(bundle))
            }

            selections[3] ->{
                val bundle = Bundle()
                bundle.putString("user_id",friendInfoMap["friend_id"])
                startActivity(Intent(this, WordsActivity::class.java).putExtras(bundle))
            }

            selections[4] -> {
                val bundle = Bundle()
                bundle.putString("friendID",friendInfoMap["friend_id"])
                bundle.putString("tel", friendInfoMap["tel"])
                bundle.putString("trueName",friendInfoMap["name"])
                bundle.putString("email", friendInfoMap["email"])
                bundle.putString("handwriting", friendInfoMap["handwriting"])
                bundle.putString("self_introduction", friendInfoMap["self_introduction"])
                startActivity(Intent(this, FriendMoreActivity::class.java).putExtras(bundle))
            }
        }
    }

    fun registerBtnListener(){
        callBtn = findViewById(R.id.call_btn) as Button
        callBtn.setOnClickListener{requestPermissionAndCallPhone()}
        sendMessageBtn = findViewById(R.id.send_msg_btn) as Button
        sendMessageBtn.setOnClickListener{
            if(intent.extras.getBoolean("send_msg_finish")) finish()
            else{
                val bundle = Bundle()
                bundle.putString("name", shownName)
                bundle.putString("friend_id", friendInfoMap["friend_id"])
                bundle.putString("sculpture", friendInfoMap["sculpture"])
                val message = DataBaseUtil.queryMessageWithFriend(friendInfoMap["friend_id"]) as ArrayList<Map<String, String>>
                startActivity(Intent(this,ChatActivity::class.java).putExtras(bundle).putExtra("message_detail", message))
            }
        }
    }

    fun resetLinesColor(){
        for(view in lines)
            view.setBackgroundColor(Color.parseColor("#d9d9d9"))
    }

    /**
     * 收集必要信息并且更新数据库
     */
    fun queryNecessaryInfoThread(friendID:String){
        Thread{
            val map = mapOf("msgType" to Constant.QUERY_FRIEND_INFO, "userID" to Preference.userInfoMap["user_id"], "friendID" to friendID)
            val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0)
            //数据加载成功
            if(result != "[null]" && result != Constant.SERVER_CONNECTION_ERROR){
                val serverData = FastJSON.parseJSON2ListString(result)[0]
                if(LocalDataIOUtil.syncSingleData(SQLLiteConstant.FRIEND_TABLE, friendInfoMap, serverData)){
                    friendInfoMap = serverData
                    //加载头像
                    val sculptureBitmap = ImageTransmissionUtil.loadSculpture(this@FriendDetailActivity,friendInfoMap["sculpture"]!!)
                    MainHandler.getInstance().post {
                        loadTextData()
                        (findViewById(R.id.sculpture_image) as ImageView).setImageBitmap(sculptureBitmap)
                    }
                }
            }
        }.start()
    }

    /**
     * 从本地加载信息
     */
    fun loadDataLocally(friendID: String){
        friendInfoMap = DataBaseUtil.queryFriendsExactly(friendID)[0]
        loadTextData()
        val sculptureBitmap = ImageTransmissionUtil.loadSculptureOnlyInLocal(this@FriendDetailActivity, friendInfoMap["sculpture"]!!, true)
        (findViewById(R.id.sculpture_image) as ImageView).setImageBitmap(sculptureBitmap)
    }

    /**
     * 加载相关数据
     */
    fun loadTextData(){
        //加载性别
        val genderImage = findViewById(R.id.gender) as ImageView
        if(friendInfoMap["gender"] == "男")
            genderImage.setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity,R.drawable.male))
        else if(friendInfoMap["gender"] == "女")
            genderImage.setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity,R.drawable.female))

        //没有备注名 显示昵称 ID 如果可能显示真实姓名
        if(friendInfoMap["remark_name"]!!.isEmpty()){
            (findViewById(R.id.name) as TextView).text = friendInfoMap["nickname"]
            (findViewById(R.id.one_view) as TextView).text = "易聊号： "+friendInfoMap["friend_id"]
            if(!friendInfoMap["name"]!!.isEmpty())
                (findViewById(R.id.two_view) as TextView).text = "真实姓名： "+friendInfoMap["name"]
            shownName = friendInfoMap["nickname"]!!
        }
        else{//有备注名 显示备注名 ID 昵称
            (findViewById(R.id.name) as TextView).text = friendInfoMap["remark_name"]
            (findViewById(R.id.one_view) as TextView).text = "易聊号： "+friendInfoMap["friend_id"]
            (findViewById(R.id.two_view) as TextView).text = "昵称： "+friendInfoMap["nickname"]
            shownName = friendInfoMap["remark_name"]!!
        }

        //加载朋友圈情况
        (findViewById(R.id.first_image) as ImageView).setImageDrawable(null)
        (findViewById(R.id.second_image) as ImageView).setImageDrawable(null)
        var flag = 0
        if(friendInfoMap["shield_friend_moments"] == "1") flag += 1

        if(friendInfoMap["stealth_self_moments"] == "1") flag += 2

        when(flag){
            0 ->{}

            1 -> (findViewById(R.id.second_image) as ImageView).setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity,R.drawable.shield_friend))

            2 -> (findViewById(R.id.second_image) as ImageView).setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity, R.drawable.stealth_self))

            3 ->{
                (findViewById(R.id.first_image) as ImageView).setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity, R.drawable.shield_friend))

                (findViewById(R.id.second_image) as ImageView).setImageDrawable(ContextCompat.getDrawable(this@FriendDetailActivity, R.drawable.stealth_self))
            }
        }

        //加载地区信息
        (findViewById(R.id.area_text_view) as TextView).text = UserInfoHelper.findUserArea(friendInfoMap["areas"]!!)

        //设置打电话按钮是否可用
        callBtn.isEnabled = friendInfoMap["tel"]!!.isNotEmpty()
    }

    //动态申请权限打电话
    fun requestPermissionAndCallPhone(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        else
            callPhone()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1 ->
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) callPhone()
        }
    }

    //打电话
    fun callPhone(){
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:"+friendInfoMap["tel"])
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if(MainActivity.mainActivity.contactFragment != null)
                    MainActivity.mainActivity.contactFragment.cancelEditFocusAndResetUI()
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.friend_detail_menu, menu)
        return true
    }
}
