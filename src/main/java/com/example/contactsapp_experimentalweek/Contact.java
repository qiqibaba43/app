package com.example.contactsapp_experimentalweek;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {//主要数据设计
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "phoneNumber")
    private String phoneNumber;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "groupName")
    private String groupName;
    @ColumnInfo(name = "avatarUri")
    private String avatarUri;

    // 构造函数
    public Contact(String name,  String phoneNumber,String email, String groupName, String avatarUri) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.groupName= groupName;
        this.avatarUri = avatarUri;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName= groupName;
    }
    public String getAvatarUri() {
        return avatarUri;
    }
    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
    }
}
