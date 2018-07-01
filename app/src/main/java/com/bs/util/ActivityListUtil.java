package com.bs.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 13273 on 2017/9/16.
 *
 */

public class ActivityListUtil {

    private static List<Activity> activityList = new ArrayList<>();

    static void addActivityToList(Activity activity){
        activityList.add(activity);
    }

    static void removeActivityFromList(Activity activity){
        activityList.remove(activity);
    }

    public static void destroyAllActivity(){
        for(Activity activity : activityList){
            activity.finish();
        }
    }

    public static void destroyLastActivity(int number){
        for(int i = 0;i<number;i++)
            activityList.get(activityList.size()-1-i).finish();
    }

    public static boolean appIsRunningForeground(){
        for(Activity activity:activityList){
            if(isForeground(activity , activity.getClass().getName()))
                return true;
        }
        return false;
    }

    private static boolean isForeground(Context context, String className){
        if(context == null || TextUtils.isEmpty(className)) return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if(list != null && list.size() > 0){
            ComponentName cpn = list.get(0).topActivity;
            if(className.equals(cpn.getClassName()))
                return true;
        }
        return false;
    }

    public static void destroySpecialActivity(String className){
        for(Activity activity : activityList){
            if(className.equals(activity.getClass().getName()))
                activity.finish();
        }
    }
}
