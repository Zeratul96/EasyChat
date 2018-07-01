package com.bs.main.friends

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.bs.easy_chat.R
import com.bs.util.BaseActivity

/**
 * friendID
 * tel
 * trueName
 * email
 * handwriting
 * self_introduction
 */

class FriendMoreActivity : BaseActivity(){

    var originY = 0
    val DEVIATION = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friend_more_layout)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        (findViewById(R.id.handwriting_view) as TextView).text = intent.extras.getString("handwriting")
        (findViewById(R.id.true_name_view) as TextView).text = if(intent.extras.getString("trueName").isEmpty()) getString(R.string.user_not_fill) else intent.extras.getString("trueName")
        (findViewById(R.id.tel_view) as TextView).text = intent.extras.getString("tel")
        (findViewById(R.id.email_view) as TextView).text = intent.extras.getString("email")

        if(intent.extras.getString("self_introduction").isEmpty()) findViewById(R.id.self_content).visibility = View.GONE else (findViewById(R.id.introduction_view) as TextView).text = intent.extras.getString("self_introduction")
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
