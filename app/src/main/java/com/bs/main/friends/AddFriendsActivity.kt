package com.bs.main.friends

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.main.adapter.AddFriendRequestAdapter
import com.bs.main.adapter.NewFriendAdapter
import com.bs.main.decoration.DividerItemDecoration
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.parameter.SQLLiteConstant
import com.bs.tool_package.FastJSON
import com.bs.util.*
import com.bs.widget.ClearEditText
import com.bs.widget.SwipeListView
import net.sf.json.JSONObject

class AddFriendsActivity : BaseActivity() ,View.OnKeyListener{

    lateinit var searchBar:ClearEditText
    lateinit var recyclerView:RecyclerView
    lateinit var noResult:TextView
    lateinit var adapter:NewFriendAdapter
    lateinit var requestListView:SwipeListView
    lateinit var requestFriendData:MutableList<Map<String,String>>
    val DEVIATION = 10

    /**
     * 加载所有好友的头像 累计五张加载失败便直接退出
     */
    private fun loadAndSaveFriendSculptures(tempData: List<Map<String, String>>?) {
        var errorCount = 0
        var loopCount = 0
        if (tempData == null || tempData.isEmpty()) return
        for (map in tempData) {
            loopCount++
            val picPaths = map["sculpture"]
            if (picPaths!!.isNotEmpty()) {
                if (!ImageTransmissionUtil.loadSculptureToLocal(this, picPaths)) errorCount++
                if (errorCount >= 5) {
                    MainHandler.getInstance().post { adapter.notifyDataSetChanged() }
                    return
                }
            }
            if (loopCount % 10 == 0)
                MainHandler.getInstance().post { adapter.notifyDataSetChanged() }
        }
        if (loopCount % 10 != 0)
            MainHandler.getInstance().post { adapter.notifyDataSetChanged() }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friends_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        noResult = findViewById(R.id.no_result) as TextView

        searchBar = findViewById(R.id.search_bar) as ClearEditText
        searchBar.setOnKeyListener(this)
        searchBar.setOnClearBtnDownListener {
            noResult.visibility = View.GONE
            recyclerView.visibility = View.GONE
            adapter.setData(null)
        }
        //设置为线性布局
        recyclerView.layoutManager = LinearLayoutManager(this)
        //设置适配器
        adapter = NewFriendAdapter(this,null)
        recyclerView.adapter = adapter
        //绘制分割线
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST))

        requestListView = findViewById(R.id.request_list_view) as SwipeListView
        requestListView.setOnItemClickListener { _, _, position, _ ->
            if(requestFriendData[position]["state"] == "waiting" || requestFriendData[position]["state"] == "delete"){
                val protentialUserID = if(requestFriendData[position]["user_id"] == Preference.userInfoMap["user_id"]) requestFriendData[position]["request_user"] else requestFriendData[position]["user_id"]
                var protentialFriendInfo:HashMap<String,String>
                val protentialData = DataBaseUtil.queryProtentialFriend(protentialUserID)
                if(protentialData.isNotEmpty()){
                    protentialFriendInfo = protentialData[0] as HashMap<String, String>
                    val bundle = Bundle()
                    bundle.putBoolean("is_need_reload_data", true)
                    bundle.putBoolean("is_request", requestFriendData[position]["request_user"] == Preference.userInfoMap["user_id"] && requestFriendData[position]["state"] != "delete")
                    bundle.putString("record_id", requestFriendData[position]["record_id"])
                    startActivity(Intent(this@AddFriendsActivity, ProtentialFriendActivity::class.java).putExtra("msg", protentialFriendInfo).putExtras(bundle))
                }
                else{
                    Thread{
                        val map = mapOf("msgType" to Constant.QUERY_USER_EXACTLY, "user_id" to protentialUserID)
                        val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString() ,0)
                        if(result != "[null]" && result != Constant.SERVER_CONNECTION_ERROR){
                            MainHandler.getInstance().post {
                                protentialFriendInfo = FastJSON.parseJSON2ListString(result)[0] as HashMap<String, String>
                                val bundle = Bundle()
                                bundle.putBoolean("is_need_reload_data", false)
                                bundle.putBoolean("is_request", requestFriendData[position]["request_user"] == Preference.userInfoMap["user_id"])
                                bundle.putString("record_id", requestFriendData[position]["record_id"])
                                startActivity(Intent(this@AddFriendsActivity, ProtentialFriendActivity::class.java).putExtra("msg", protentialFriendInfo).putExtras(bundle))
                            }
                        }
                    }.start()
                }

            }
            else{
                val bundle = Bundle()
                bundle.putString("friend_id", if(requestFriendData[position]["request_user"] == Preference.userInfoMap["user_id"]) requestFriendData[position]["user_id"] else requestFriendData[position]["request_user"])
                bundle.putBoolean("send_msg_finish", false)
                startActivity(Intent(this@AddFriendsActivity, FriendDetailActivity::class.java).putExtras(bundle))
            }
        }
        NetConnectionUtil.cat.setOnNewAddFriendRequestListener {
            loadingAddFriendRequestData()
        }
    }

    override fun onStart() {
        super.onStart()
        searchBar.clearFocus()
        loadingAddFriendRequestData()
    }

    fun loadingAddFriendRequestData(){
        requestFriendData = DataBaseUtil.queryAddFriendRequest()
        if(requestFriendData.isNotEmpty()){
            val requestAdapter = AddFriendRequestAdapter(this, requestFriendData, requestListView.rightViewWidth)
            requestAdapter.setOnRightItemClickListener { _, position ->
                DataBaseUtil.delete("delete from friend_request where record_id = '" + requestFriendData[position]["record_id"] + "'", SQLLiteConstant.FRIEND_REQUEST)
                requestFriendData.removeAt(position)
                requestAdapter.notifyDataSetChanged()
                requestListView.shrinkByDeletion()
                if (requestFriendData.size == 0)
                    (findViewById(R.id.request_text_view) as TextView).visibility = View.GONE
            }
            (findViewById(R.id.request_text_view) as TextView).visibility = View.VISIBLE
            requestListView.visibility = View.VISIBLE
            requestListView.adapter = requestAdapter
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
            if(searchBar.text.toString().isEmpty()) {
                showNotice("请输入搜索关键词")
                return false
            }

            Thread{
                val map = mapOf("msgType" to Constant.QUERY_USER, "info" to searchBar.text.toString(), "userID" to Preference.userInfoMap["user_id"])
                val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString() ,0)
                var detailData:List<Map<String,String>>? = null
                if(result != "[null]" && result != Constant.SERVER_CONNECTION_ERROR) {
                    detailData = FastJSON.parseJSON2ListString(result)
                    loadAndSaveFriendSculptures(detailData)

                    DataBaseUtil.createOrOpenDataBase(SQLLiteConstant.PROTENTIAL_FRIEND)
                    for (mapInfo in detailData){
                        DataBaseUtil.deleteContinuously("delete from protential_friend where user_id = '"+mapInfo["user_id"]+"'")
                        DataBaseUtil.insertContinuously("insert into protential_friend values('"+mapInfo["user_id"]+"','"+mapInfo["gender"]+
                        "','"+mapInfo["sculpture"]+"','"+mapInfo["nickname"]+"','"+mapInfo["areas"]+"','"+mapInfo["self_introduction"]+"','"+mapInfo["handwriting"]+"')")
                    }
                    DataBaseUtil.closeDatabase()
                }
                MainHandler.getInstance().post {
                    when(result) {
                        "[null]" -> showNotice("未搜索到匹配的用户")

                        Constant.SERVER_CONNECTION_ERROR -> showNotice(getString(R.string.no_network))

                        else -> {
                            noResult.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.setData(detailData).notifyDataSetChanged()
                        }
                    }
                }
            }.start()
        }
        return false
    }

    fun showNotice(text:String){
        noResult.text = text
        noResult.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        adapter.setData(null)
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
