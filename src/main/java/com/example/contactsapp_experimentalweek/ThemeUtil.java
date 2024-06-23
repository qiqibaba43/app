package com.example.contactsapp_experimentalweek;
import android.app.Activity;

public class ThemeUtil {
    public static boolean night = false;//定义静态全局变量
    public static void changeTheme(Activity activity){
        if (ThemeUtil.night){
            activity.setTheme(R.style.AppTheme_Dark);//设置深色主题
        }else{
            activity.setTheme(R.style.AppTheme);//设置浅色主题
        }
    }
}
