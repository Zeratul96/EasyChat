package com.bs.tool_package;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by 13273 on 2017/10/5.
 *
 */

public class FileHelper {

    public static void copyFile(@NonNull File originFile, @NonNull String targetPath){

        FileOutputStream out = null;
        FileInputStream in = null;

        try{
            int byteRead = 0;
            in = new FileInputStream(originFile);

/*            String[] piece = targetPath.split("/");
            String folderPath = "";
            for(int i=0,len=piece.length;i<len-1;i++)
                folderPath += piece[i];

            File file = new File(folderPath);
            if(!file.exists()){
                if(!file.mkdirs()){
                    Log.d("result", "mkdirsFailure");
                    return;
                }
            }*/

            out = new FileOutputStream(targetPath);

            byte[] buffer = new byte[1024];
            while ((byteRead = in.read(buffer)) != -1){
                out.write(buffer,0,byteRead);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(in != null){
                    in.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                if(out != null){
                    out.flush();
                    out.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getDiskCacheDir(Context context){
        String cachePath = null;
        //Environment.getExtemalStorageState() 获取SDcard的状态
        //Environment.MEDIA_MOUNTED 手机装有SDCard,并且可以进行读写
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }
}
