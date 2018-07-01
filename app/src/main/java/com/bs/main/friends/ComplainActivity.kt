package com.bs.main.friends

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.util.BaseActivity
import com.bs.util.NetConnectionUtil
import net.sf.json.JSONObject

/**
 * 需要数据friendID
 */
class ComplainActivity : BaseActivity() ,View.OnTouchListener,View.OnClickListener{

    lateinit var selections:Array<LinearLayout>
    lateinit var lines:Array<View>
    lateinit var commitBtn:Button
    var originY = 0
    val DEVIATION = 10
    var option = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.complain_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        selections = arrayOf(findViewById(R.id.selection_one) as LinearLayout,findViewById(R.id.selection_two) as LinearLayout,findViewById(R.id.selection_three) as LinearLayout,findViewById(R.id.selection_four) as LinearLayout)
        lines = arrayOf(findViewById(R.id.line0), findViewById(R.id.line1), findViewById(R.id.line2), findViewById(R.id.line3),findViewById(R.id.line4))
        commitBtn = findViewById(R.id.commit_btn) as Button

        for(view in selections){
            view.setOnTouchListener(this)
            view.setOnClickListener(this)
        }

        commitBtn.setOnClickListener{
            val myHandler = object : Handler(){
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                        if(msg?.what == -1){
                            Toast.makeText(this@ComplainActivity, getString(R.string.no_network), Toast.LENGTH_SHORT).show()
                        }
                        else{
                            (findViewById(R.id.detail_edit) as EditText).clearFocus()
                            Toast.makeText(this@ComplainActivity, "提交成功，一经查实我们会严肃处理。", Toast.LENGTH_SHORT).show()
                            commitBtn.setOnClickListener(null)
                            for(view in selections)
                                view.setOnClickListener(null)

                            postDelayed( { finish() },300)
                        }
                }
            }

            Thread{
                val map = mapOf("msgType" to Constant.INSERT_COMPLAIN, "userID" to Preference.userInfoMap["user_id"], "complainedUser" to intent.extras.get("friendID"),
                "content" to  (selections[option].getChildAt(0) as TextView).text.toString()+"。"+(findViewById(R.id.detail_edit) as EditText).text.toString())

                val result = NetConnectionUtil.uploadData(JSONObject.fromObject(map).toString(), 0)
                val message = Message()
                message.what = if(result == "[null]" || result == Constant.SERVER_CONNECTION_ERROR) -1 else 0
                myHandler.sendMessage(message)

            }.start()
        }
    }

    private fun resetLinesColor() {
        lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray))
        lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray))
        lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.line_gray))
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val TOP_Y = 0
        val BOTTOM_Y = view.bottom - view.top

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {

                originY = motionEvent.y.toInt()
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.pressed_background))

                if (view === selections[0])
                    lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                else if (view === selections[1]) {
                    lines[1].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                } else if (view === selections[2]) {
                    lines[2].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                } else if (view === selections[3])
                    lines[3].setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            }

            MotionEvent.ACTION_UP -> {
                resetLinesColor()
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))

                if (motionEvent.y < TOP_Y || motionEvent.y > BOTTOM_Y || Math.abs(originY - motionEvent.y) > DEVIATION) {
                    resetLinesColor()
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> if (motionEvent.y < TOP_Y || motionEvent.y > BOTTOM_Y || Math.abs(originY - motionEvent.y) > DEVIATION) {
                resetLinesColor()
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                return true
            }
        }
        return false
    }

    override fun onClick(view: View?) {
        if (option != -1)
            (selections[option].getChildAt(1) as ImageView).setImageDrawable(null)

        commitBtn.isEnabled = true

        when(view){
            selections[0] -> option = 0

            selections[1] -> option = 1

            selections[2] -> option = 2

            selections[3] -> option = 3
        }

        (selections[option].getChildAt(1) as ImageView).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.blue_circle))
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
