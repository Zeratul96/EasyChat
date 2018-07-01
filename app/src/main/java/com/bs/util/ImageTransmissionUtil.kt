package com.bs.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bs.easy_chat.R
import com.bs.parameter.Constant
import com.bs.parameter.Preference
import com.bs.tool_package.FileHelper
import com.bs.tool_package.ImageTools
import com.bs.tool_package.TimeTools
import net.sf.json.JSONObject
import java.io.File

/**
 * Created by 13273 on 2017/10/15.
 * 加载图片类 负责图片的加载与上传
 */
object ImageTransmissionUtil {

    //加载图片、保存到SD卡中并且返回图片
    @JvmStatic fun loadPictureAndReturnBitmap(serverPath: String, localPath:String):Bitmap?{
        //如果本地有图片则直接返回
        if(File(localPath).exists()) return ImageTools.getLocalBitmap(localPath)

        val map = mapOf("msgType" to Constant.DOWNLOAD_PICTURE,"picPath" to serverPath)
        val mapStr = JSONObject.fromObject(map)
        val picData = NetConnectionUtil.downLoadPicture(mapStr.toString())
        //图片加载成功
        if(picData !== null){
            val pic = ImageTools.getBitmapFromByteArray(picData)
            ImageTools.saveBitmapToSDCard(pic, localPath)
            return pic
        }
        //图片加载失败
        else return null
    }

    //加载图片、保存到SD卡中并且返回加载结果
    @JvmStatic fun loadPictureAndReturnResult(serverPath: String, localPath:String):Boolean{
        if(File(localPath).exists()) return true

        val map = mapOf("msgType" to Constant.DOWNLOAD_PICTURE,"picPath" to serverPath)
        val mapStr = JSONObject.fromObject(map)
        val picData = NetConnectionUtil.downLoadPicture(mapStr.toString())
        //图片加载成功
        if(picData !== null){
            val pic = ImageTools.getBitmapFromByteArray(picData)
            ImageTools.saveBitmapToSDCard(pic, localPath)
            return true
        }
        //图片加载失败
        else return false
    }



    /**
     * 关于好友及个人头像
     */
    //上传头像至服务器
    @JvmStatic fun uploadSculpture(pictureOriginPath:String):String?{
        val serverPath = "sculpture/"+Preference.userInfoMap["user_id"]+"_"+TimeTools.generateNumberByTime()+".jpg"
        val map = mapOf("msgType" to Constant.UPLOAD_PICTURE,"picPath" to serverPath)
        val mapStr = JSONObject.fromObject(map)

        val result = NetConnectionUtil.uploadPicture(mapStr.toString(), ImageTools.BitmapToBytes(ImageTools.getLocalBitmap(pictureOriginPath)))
        if(result.isNullOrEmpty() || result==Constant.SERVER_CONNECTION_ERROR) return null

        //向主服务器更新信息
        val userMap = mapOf("msgType" to Constant.UPDATE_USER,"mode" to "8", "userID" to Preference.userInfoMap["user_id"],"sculpture" to serverPath)
        val result2 = NetConnectionUtil.uploadData(JSONObject.fromObject(userMap).toString(),0)
        return if(result2 == "[null]"|| result2 == Constant.SERVER_CONNECTION_ERROR) null else serverPath
    }

    //仅从本地加载头像
    @JvmStatic fun loadSculptureOnlyInLocal(context:Context, serverPath: String, isNeedReturn:Boolean):Bitmap?{
        //如果没有头像
        if(serverPath.isNullOrEmpty()) return BitmapFactory.decodeResource(context.resources, R.drawable.no_sculpture)
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/sculptures_"+ array[1]
        //如果本地有头像
        if(File(localPath).exists())
            return ImageTools.getLocalBitmap(localPath)
        else {
            if(isNeedReturn) return BitmapFactory.decodeResource(context.resources, R.drawable.no_picture)
            else return null
        }
    }

    //加载头像
    @JvmStatic fun loadSculpture(context:Context,serverPath:String):Bitmap?{
        //如果没有头像
        if(serverPath.isNullOrEmpty()) return BitmapFactory.decodeResource(context.resources, R.drawable.no_sculpture)
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/sculptures_"+ array[1]
        return loadPictureAndReturnBitmap(serverPath, localPath)
    }

    //加载头像至本地 返回加载是否成功
    @JvmStatic fun loadSculptureToLocal(context:Context,serverPath:String):Boolean{
        if(serverPath.isNullOrEmpty()) return true
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/sculptures_"+ array[1]
        return loadPictureAndReturnResult(serverPath, localPath)
    }


    /**
    * 关于朋友圈图片及背景
     */
    //加载朋友圈图片至本地
    @JvmStatic fun loadMomentsSculptureToLocal(context:Context,serverPath:String):Boolean{
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/moments_"+ array[1]
        return loadPictureAndReturnResult(serverPath, localPath)
    }

    //仅从本地加载朋友圈头像
    @JvmStatic fun loadMomentsSculptureOnlyLocal(context:Context,serverPath:String):Bitmap?{
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/moments_"+ array[1]
        //如果本地有图
        if(File(localPath).exists())
            return ImageTools.getLocalBitmap(localPath)
        else return BitmapFactory.decodeResource(context.resources, R.drawable.no_picture)
    }

    //加载朋友圈背景
    @JvmStatic fun loadMomentBackground(context:Context,serverPath:String):Bitmap?{
        if(serverPath.isNullOrEmpty()) return null
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/background_"+ array[1]
        return loadPictureAndReturnBitmap(serverPath, localPath)
    }

    /**
     * 加载聊天时的图片
     */
    //加载聊天时发送的图片
    @JvmStatic fun loadChatImage(context:Context,serverPath:String):Bitmap?{
        val array = serverPath.split("/")
        val localPath = FileHelper.getDiskCacheDir(context)+"/chat_"+ array[1]
        return loadPictureAndReturnBitmap(serverPath, localPath)
    }
}