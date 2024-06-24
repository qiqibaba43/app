package com.example.contactsapp_experimentalweek;
import android.app.Activity;
import android.widget.Toast;

public class ThemeUtil {
    public static boolean night = false;//定义静态全局变量

    public static void changeTheme(Activity activity) {
        if (ThemeUtil.night) {
            activity.setTheme(R.style.AppTheme_Dark);//设置深色主题
            //Toast.makeText(activity.getApplicationContext(),"Successfully switched to night mode",Toast.LENGTH_SHORT).show();
        } else {
            activity.setTheme(R.style.AppTheme);//设置浅色主题
            //Toast.makeText(activity.getApplicationContext(),"Successfully switched to light mode",Toast.LENGTH_SHORT).show();
        }
    }
}
