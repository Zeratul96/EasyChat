package com.bs.person.person_center

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.bs.easy_chat.R
import com.bs.parameter.Preference
import com.bs.tool_package.FileHelper
import com.bs.util.BaseActivity
import com.bs.util.ImageTransmissionUtil
import com.bs.util.MainHandler
import com.bs.widget.CustomProgress
import com.bumptech.glide.Glide
import com.yongchun.library.view.ImageCropActivity
import com.yongchun.library.view.ImageCropActivity.REQUEST_CROP
import com.yongchun.library.view.ImageSelectorActivity
import com.yongchun.library.view.ImageSelectorActivity.REQUEST_IMAGE
import net.sf.json.JSONObject
import java.io.File

/**
 * Created by 13273 on 2017/10/15.
 *
 */
class SculptureActivity : BaseActivity(){

    lateinit var sculptureImageView:ImageView
    val TAKE_PHOTO = 2

    lateinit var takenPhotoPath:String

    fun avoidFileExposure(){
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sculpture_layout)
        avoidFileExposure()

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setOnMenuItemClickListener {
            item: MenuItem ->
            when(item.itemId){
                R.id.action_take_photo -> takePhotos()

                R.id.action_select_from_gallery -> requestPermissionAndPickPhoto()
            }
            true
        }

        sculptureImageView = findViewById(R.id.image) as ImageView
        val bitmap = ImageTransmissionUtil.loadSculptureOnlyInLocal(this, Preference.userInfoMap["sculpture"]!!, false)
        if(bitmap !== null){
            sculptureImageView.setImageBitmap(bitmap)
        }
    }

    fun takePhotos(){
        val takeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        takenPhotoPath = FileHelper.getDiskCacheDir(this)+ "/self_sculpture_temp.jpg"
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(takenPhotoPath)))
        startActivityForResult(takeIntent, TAKE_PHOTO)
    }


    //动态申请权限并且调用照片
    fun requestPermissionAndPickPhoto(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        else
            //参数依次为：Context   最大图片数量   模式   允许拍照   允许预览   允许剪裁
            ImageSelectorActivity.start(this, 1, ImageSelectorActivity.MODE_SINGLE, false, true, true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            1 ->
                if(grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    ImageSelectorActivity.start(this, 1, ImageSelectorActivity.MODE_SINGLE, false, true, true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != RESULT_OK) return

        when(requestCode){

            TAKE_PHOTO ->{
                ImageCropActivity.startCrop(this, takenPhotoPath)
            }

            REQUEST_CROP ->{
                val picPath = data?.getStringExtra(ImageCropActivity.OUTPUT_PATH)
                Glide.with(this).load(picPath).into(sculptureImageView)
                uploadSculpture(picPath!!)
            }

            REQUEST_IMAGE -> {
                val picPath = data?.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT) as List<String>
                Glide.with(this).load(File(picPath[0])).into(sculptureImageView)
                uploadSculpture(picPath[0])
            }
        }
    }

    fun uploadSculpture(path:String){
        val progress = CustomProgress.show(this, "上传中…",false, null)
        Thread{
            val string = ImageTransmissionUtil.uploadSculpture(path)
            if(string !== null){
                val array = string.split("/")
                val localPath = FileHelper.getDiskCacheDir(this@SculptureActivity)+"/self_sculpture_"+ array[1]
                FileHelper.copyFile(File(path),localPath)
                Preference.userInfoMap["sculpture"] = string
                val edit = getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit()
                edit.putString("userInfo", JSONObject.fromObject(Preference.userInfoMap).toString())
                edit.apply()
                MainHandler.getInstance().post {
                    progress.dismiss()
                    Toast.makeText(this@SculptureActivity,"头像上传成功",Toast.LENGTH_SHORT).show()
                }
            }
            else
                MainHandler.getInstance().post {
                    progress.dismiss()
                    Toast.makeText(this@SculptureActivity,"头像上传失败",Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.personal_sculpture__menu, menu)
        return true
    }
}