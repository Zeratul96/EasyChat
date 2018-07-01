package com.bs.function.moments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bs.database.DataBaseUtil
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.parameter.SQLLiteConstant
import com.bs.tool_package.FileHelper
import com.bs.tool_package.ImageTools
import com.bs.tool_package.TimeTools
import com.bs.util.*
import com.bs.widget.CustomProgress
import com.bs.widget.MyGridView
import com.bs.widget.SelectPicturePopupWindow
import com.bumptech.glide.Glide
import com.yongchun.library.view.ImageSelectorActivity
import net.sf.json.JSONObject
import java.io.File
import java.util.ArrayList

class CreateMomentsActivity : BaseActivity(),TextWatcher{

    lateinit var finishBtn:TextView
    lateinit var alertDialog:AlertDialog
    lateinit var contentEdit:EditText

    var takenPhotoPath:String? = null
    lateinit var selectPicturePopupWindow:SelectPicturePopupWindow
    lateinit var adapter:GridViewAdapter
    lateinit var gridView:GridView

    private val TAKE_PHOTO = 10
    val REMOVE_PICTURE = 12
    var picturePath = ArrayList<String>()
    var momentsPicturePath = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_moments_layout)
        avoidFileExposure()

        finishBtn = findViewById(R.id.finish_btn) as TextView
        finishBtn.setOnClickListener {
            val progress = CustomProgress.show(this, "上传中…",false, null)
            Thread{
                momentsPicturePath.clear()
                var uploadFailure = false
                var picturePathStr = ""

                if(picturePath.isNotEmpty())
                {
                    for(string in picturePath){
                        val picPer = "moments/"+ Preference.userInfoMap["user_id"] +string.split("/diary_")[1]
                        picturePathStr += picPer
                        momentsPicturePath.add(picPer)
                        val map = mapOf("msgType" to Constant.UPLOAD_PICTURE, "picPath" to picPer)
                        val pictureUploadResult = NetConnectionUtil.uploadPicture(JSONObject.fromObject(map).toString(), ImageTools.BitmapToBytes(ImageTools.getLocalBitmap(string)))
                        if(pictureUploadResult.isNullOrEmpty() || pictureUploadResult == Constant.SERVER_CONNECTION_ERROR){
                            uploadFailure = true
                            break
                        }
                        picturePathStr += "<#>"
                    }
                }

                //继续上传文字信息
                if(!uploadFailure){
                    val mapToUploadText = mapOf("msgType" to Constant.INSERT_MOMENTS, "user_id" to Preference.userInfoMap["user_id"], "content" to contentEdit.text.toString(), "pictures_id" to picturePathStr)
                    val result = NetConnectionUtil.uploadData(JSONObject.fromObject(mapToUploadText).toString(), 0)
                    uploadFailure = (result == Constant.SERVER_CONNECTION_ERROR || result == "[null]")
                }

                //上传成功并且有图片则先复制这次上传的图片到本地
                var count = 0
                if(!uploadFailure && momentsPicturePath.isNotEmpty()){
                    for (s in picturePath){
                        val localPath = FileHelper.getDiskCacheDir(this)+"/moments_"+ momentsPicturePath[count++].substring(8)
                        FileHelper.copyFile(File(s),localPath)
                    }
                }
                //上传成功并且开启同步保存到日记 上传失败并且开启草稿保存至日记
                if(!uploadFailure && Preference.isSync)
                    savedInToDiary("自动同步")

                else if((uploadFailure && Preference.isDraft))
                    savedInToDiary("草稿")

                //上传成功不需要保存成日记就把日记照片删除
                else if(!uploadFailure && !Preference.isSync){
                    for (s in picturePath)
                        File(s).delete()
                }

                MainHandler.getInstance().post {
                    if(uploadFailure)
                        Toast.makeText(this@CreateMomentsActivity, if(Preference.isDraft) "上传失败，内容已保存至日记。" else "上传失败" , Toast.LENGTH_SHORT).show()
                    else MomentsActivity.shouldRefreshContent = true

                    progress.dismiss()
                    if(!uploadFailure || Preference.isDraft) finish()
                }

            }.start()
        }

        findViewById(R.id.cancel_btn).setOnClickListener {
            if(finishBtn.isEnabled) showAlertDialog() else finish()
        }

        contentEdit = findViewById(R.id.editText) as EditText
        contentEdit.addTextChangedListener(this)
        selectPicturePopupWindow = SelectPicturePopupWindow(this)
        selectPicturePopupWindow.setOnSelectedListener { _, position ->
            when (position) {
            0 -> {
                takePhotos()
                selectPicturePopupWindow.dismissPopupWindow()
            }

            1 -> {
                requestPermissionAndPickPhoto()
                selectPicturePopupWindow.dismissPopupWindow()
            }

            2 -> selectPicturePopupWindow.dismissPopupWindow()
            }
        }

        gridView = findViewById(R.id.grid_view) as GridView
        adapter = GridViewAdapter()
        gridView.adapter = adapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val pictureNum = picturePath.size
            if (pictureNum < 9 && position == pictureNum) {
                contentEdit.clearFocus()
                selectPicturePopupWindow.showPopupWindow(this@CreateMomentsActivity)
            }
            else{
                val bundle = Bundle()
                bundle.putString("msg", picturePath[position])
                startActivityForResult(Intent(this,ImageActivity::class.java).putExtras(bundle), REMOVE_PICTURE)
            }
        }
    }

    private fun avoidFileExposure() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle("提示")
        //对话框内容
        builder.setMessage("确定要放弃此次编辑并退出吗？")
        //确定按钮
        builder.setPositiveButton("确定") { _, _ ->
            if(picturePath.isNotEmpty()){
                for (s in picturePath)
                    File(s).delete()
            }
            finish()
        }
        builder.setNegativeButton("取消") { _, _ -> alertDialog.dismiss() }

        //固定对话框使其不可被取消
        builder.setCancelable(false)

        //创建对话框
        alertDialog = builder.create()
        alertDialog.show()

    }

    //动态申请权限并且调用选照片
    private fun requestPermissionAndPickPhoto() =
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            else
                ImageSelectorActivity.start(this, 9 - picturePath.size, ImageSelectorActivity.MODE_MULTIPLE, false, true, false)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                ImageSelectorActivity.start(this, 9 - picturePath.size, ImageSelectorActivity.MODE_MULTIPLE, false, true, false)
            }
        }
    }

    private fun takePhotos() {
        val takeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //下面这句指定调用相机拍照后的照片存储的路径
        takenPhotoPath = FileHelper.getDiskCacheDir(this) + "/diary_" + TimeTools.generateNumberByTime() + ".jpg"

        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(takenPhotoPath)))
        startActivityForResult(takeIntent, TAKE_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            ImageSelectorActivity.REQUEST_IMAGE -> {
                val result = data?.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT) as ArrayList<String>
                for (s in result) {
                    val piece = s.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val path = FileHelper.getDiskCacheDir(this) + "/diary_" + TimeTools.generateNumberByTime() + "." + piece[piece.size - 1]
                    FileHelper.copyFile(File(s), path)
                    picturePath.add(path)
                }
                finishBtn.isEnabled = true
                adapter.notifyDataSetChanged()
            }

            TAKE_PHOTO -> {
                picturePath.add(takenPhotoPath!!)
                finishBtn.isEnabled = true
                adapter.notifyDataSetChanged()
            }

            REMOVE_PICTURE ->{
                picturePath.remove(data?.getStringExtra("msg"))
                if(picturePath.isEmpty() && contentEdit.text.toString().isEmpty())
                    finishBtn.isEnabled = false
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 存入日记本
     */
    fun savedInToDiary(string:String){
        var picStr = ""
        for (s in picturePath)
            picStr += s + "<#>"

        val sql = "insert into diary values('" + TimeTools.generateNumberByTime() + "','" + TimeTools.generateContentFormatTime() + "','" + TimeTools.generateDetailTime() + "','" + string + "','" + contentEdit.text.toString() + "','" + Preference.userInfoMap["user_id"] + "','" + picStr + "')"
        DataBaseUtil.insert(sql, SQLLiteConstant.DIARY_TABLE)

    }



    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        finishBtn.isEnabled = (contentEdit.text.toString().isNotEmpty()||picturePath.isNotEmpty())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun afterTextChanged(s: Editable?) = Unit


    inner class GridViewAdapter : MyListViewAdapter(picturePath.size) {
        private var itemNum: Int = 0
        var hasAddBtn: Boolean = false

        override fun getCount(): Int {
            itemNum = if (picturePath.size < 9) picturePath.size + 1 else 9
            hasAddBtn = picturePath.size < 9
            return itemNum
        }

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var view = view
            val holder: ViewHolder
            if (view === null) {
                view = LayoutInflater.from(this@CreateMomentsActivity).inflate(R.layout.picture_item, viewGroup ,false)
                holder = ViewHolder(view!!.findViewById<View>(R.id.picture) as ImageView)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            if ((viewGroup as MyGridView).isOnMeasure)
                return view

            //如果是最后一项
            if (i == itemNum - 1) {
                if (hasAddBtn) {
                    holder.imageView.setImageDrawable(ContextCompat.getDrawable(this@CreateMomentsActivity, R.drawable.add_picture))
                } else {
                    Glide.with(this@CreateMomentsActivity).load(File(picturePath[i])).centerCrop().into(holder.imageView)
                }
            } else {
                Glide.with(this@CreateMomentsActivity).load(File(picturePath[i])).centerCrop().into(holder.imageView)
            }
            return view
        }
    }

    internal inner class ViewHolder(var imageView: ImageView)
}
