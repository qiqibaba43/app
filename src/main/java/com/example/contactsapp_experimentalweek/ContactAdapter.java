package com.example.contactsapp_experimentalweek;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// 联系人适配器，用于管理RecyclerView中的联系人项
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private LayoutInflater CInflater; // 用于填充item视图的LayoutInflater
    private static List<Contact> contactList; // 保存联系人的列表
    private Context context; // 用于访问资源的上下文
    private OnItemClickListener listener; // 项目点击的监听器
    String firstPinyin; // 保存第一个拼音字符
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 1; // 权限请求代码

    // 构造函数，初始化上下文和LayoutInflater
    public ContactAdapter(Context context) {
        CInflater = LayoutInflater.from(context);
        this.context = context;
        contactList = new ArrayList<>(); // 初始化联系人列表
    }

    // 创建并返回一个ContactViewHolder
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = CInflater.inflate(R.layout.item_contact, parent, false); // 加载联系人项的布局
        return new ContactViewHolder(itemView); // 创建并返回一个ContactViewHolder
    }

    // 绑定数据到ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        Contact current = contactList.get(position); // 获取当前位置的联系人
        holder.contactNameView.setText(current.getName()); // 设置联系人的名字
        // 加载头像图片
        if (current.getAvatarUri().equals("drawable/image_contact.png")) { // 如果头像是默认图片
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.image_contact); // 获取默认图片
            holder.imageButton.setImageDrawable(drawable); // 设置默认图片
        } else {
            // 使用Glide从URI加载图片
            Glide.with(context)
                    .load(Uri.parse(current.getAvatarUri()))
                    .into(holder.imageButton); // 加载联系人头像
        }
        firstPinyin = getFirstPinyin(current); // 获取第一个拼音字符
    }

    // 返回联系人列表中的项数
    @Override
    public int getItemCount() {
        if (contactList != null)
            return contactList.size();
        else return 0;
    }

    // 项目点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(Contact contact); // 定义点击事件的方法
    }

    // 设置项目点击监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener; // 设置点击监听器
    }

    // 联系人项的ViewHolder类
    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView contactNameView; // 显示联系人的名字
        public ImageButton imageButton; // 显示联系人的头像

        // 构造函数，初始化视图并设置点击监听器
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameView = itemView.findViewById(R.id.textView_item_name); // 初始化名字TextView
            imageButton = itemView.findViewById(R.id.imageButton_item); // 初始化头像ImageButton
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition(); // 获取当前点击的位置
                    if (position != RecyclerView.NO_POSITION) { // 确保位置有效
                        Contact contact = contactList.get(position); // 获取点击的联系人
                        listener.onItemClick(contact); // 触发点击事件
                    }
                }
            });
        }
    }
    // 设置联系人列表并通知适配器数据已更改
    public void setContacts(List<Contact> contacts) {
        contactList = contacts; // 更新联系人列表
        notifyDataSetChanged(); // 通知数据集已更改
    }

    // 获取指定位置的联系人
    public static Contact getContactAtPosition(int position) {
        return contactList.get(position); // 返回指定位置的联系人
    }

    // 获取联系人的名字的第一个拼音字符
    public String getFirstPinyin(Contact contact) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat(); // 创建拼音输出格式
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE); // 设置拼音为大写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 设置拼音无音调
        format.setVCharType(HanyuPinyinVCharType.WITH_V); // 设置拼音中的'ü'用'v'表示
        try {
            String firstLetter = contact.getName().substring(0, 1).toUpperCase(); // 获取名字的第一个字母并转换为大写
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstLetter.charAt(0), format); // 将字母转换为拼音
            if (pinyinArray != null && pinyinArray.length > 0) {
                return pinyinArray[0]; // 返回第一个拼音字符
            }
        } catch (Exception e) {
            e.printStackTrace(); // 捕捉异常并打印堆栈跟踪
        }
        return ""; // 如果转换失败，返回空字符串
    }
}
