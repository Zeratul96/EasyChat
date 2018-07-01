package com.bs.main.friends

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.bs.easy_chat.R
import com.bs.util.BaseActivity
import com.bs.util.ImageTransmissionUtil

class FriendSculptureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置为全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.friend_sculpture_layout)
        val image = findViewById(R.id.main_layout)
        image.setOnClickListener{ finish()}

        val bitmap = ImageTransmissionUtil.loadSculptureOnlyInLocal(this,intent.extras.getString("msg"),false)
        if(bitmap !== null){
            (findViewById(R.id.image) as ImageView).setImageBitmap(bitmap)
            System.gc()
        }
    }
}
