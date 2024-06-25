package com.example.contactsapp_experimentalweek;
import android.app.Activity;
import android.widget.Toast;

public class ThemeUtil { public static boolean night = false;//定义静态全局变量，默认为false，即不是深色模式
    public static void changeTheme(Activity activity) {
//如果night为true，即当前为深色模式
        if (ThemeUtil.night) {
            activity.setTheme(R.style.AppTheme_Dark);//设置深色主题，默认为false，即不是深色模式
            //弹出一个短暂的Toast提示消息，通知用户成功切换到深色模式。
            //Toast.makeText(activity.getApplicationContext(),"Successfully switched to night mode",Toast.LENGTH_SHORT).show();
        } else {
        //如果night为false，即当前为浅色模式
            activity.setTheme(R.style.AppTheme);//设置浅色主题
            //Toast.makeText(activity.getApplicationContext(),"Successfully switched to light mode",Toast.LENGTH_SHORT).show();
        }
    }
}
