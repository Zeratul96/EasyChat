package com.bs.tool_package;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

/**
 * Created by 13273 on 2017/9/29.
 *
 */

public class ImageTools {

    public static Bitmap createCircleImage(Bitmap source){
        int length = source.getWidth()<source.getHeight()?source.getWidth():source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        Bitmap target = Bitmap.createBitmap(length,length,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.drawCircle(length/2, length/2, length/2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0 ,0, paint);
        return target;
    }

    /**
     * 得到本地的图片
     * @param picPath:图片名
     * @return bitmap格式文件
     */
    public static Bitmap getLocalBitmap(String picPath){

        BitmapFactory.Options options = new BitmapFactory.Options();
        /* 不进行图片抖动处理 */
        options.inDither = false;
        /* 设置让解码器以最佳方式解码 */
        options.inPreferredConfig = null;
         /* 图片长宽方向缩小倍数 */
        options.inSampleSize = 1;
        return BitmapFactory.decodeFile(picPath, options);
    }

    /**
     * 从数组解码到BitMap
     */
    public static Bitmap getBitmapFromByteArray(byte[] array) {
        return BitmapFactory.decodeByteArray(array, 0, array.length);
    }

    /**
     *
     * @param bitmap:bitmap对象
     * @param path:路径
     */
    public static void saveBitmapToSDCard(Bitmap bitmap, String path){
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {if(fos != null) fos.close();}catch (Exception e){e.printStackTrace();}
        }
    }


    /**
     * 把Bitmap转化为Byte
     */
    public static byte[] BitmapToBytes(Bitmap bitmap){
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        return fos.toByteArray();
    }


}
