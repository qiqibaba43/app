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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private LayoutInflater CInflater;
    private static List<Contact> contactList;
    private Context context;
    private OnItemClickListener listener;
    String firstPinyin;
    private static final int REQUEST_CODE_PERMISSION_STORAGE=1;
    public ContactAdapter(Context context) {
        CInflater = LayoutInflater.from(context);
        this.context = context;
        contactList=new ArrayList<>();
    }
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=CInflater.inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactViewHolder holder, int position) {
        Contact current=contactList.get(position);
        holder.contactNameView.setText(current.getName());
        if(current.getAvatarUri().equals("drawable/image_contact.png")){
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.image_contact);
            holder.imageButton.setImageDrawable(drawable);
        }else{
            // 使用Glide加载图片
            Glide.with(context)
                    .load(Uri.parse(current.getAvatarUri()))
                    .into(holder.imageButton);
        }
        firstPinyin = getFirstPinyin(current);
    }
    @Override
    public int getItemCount() {
        if (contactList != null)
            return contactList.size();
        else return 0;
    }
    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView contactNameView;
        public ImageButton imageButton;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameView=itemView.findViewById(R.id.textView_item_name);
            imageButton=itemView.findViewById(R.id.imageButton_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Contact contact = contactList.get(position);
                        listener.onItemClick(contact);
                    }
                }
            });
        }
    }
    public void setContacts(List<Contact> contacts) {
        this.contactList.clear();
        contactList= contacts;
        notifyDataSetChanged();
    }
    public static Contact getContactAtPosition(int position) {
        return contactList.get(position);
    }
    public String getFirstPinyin(Contact contact) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        try {
            String firstLetter = contact.getName().substring(0, 1).toUpperCase();
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstLetter.charAt(0), format);
            if (pinyinArray != null && pinyinArray.length > 0) {
                return pinyinArray[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
