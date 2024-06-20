package com.example.contactsapp_experimentalweek;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert
    void insertContact(Contact contact); // insertContact: 用于插入一个新的联系人到数据库中，并返回新插入行的ID。
    // 插入多个联系人
    @Update
    void  updateContact(Contact contact);//updateContact: 用于更新数据库中的现有联系人记录。
    @Delete
    void deleteContact(Contact contact);//deleteContact: 用于从数据库中删除一个联系人。
    @Query("SELECT * FROM contacts ORDER BY name COLLATE UNICODE ASC")
    LiveData<List<Contact>> getAllContacts();//getAllContacts: 用于获取所有联系人的列表。

    @Query("DELETE FROM contacts")
    void deleteAll();

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :keyword || '%' OR email LIKE '%' || :keyword || '%'  OR phoneNumber LIKE '%' || :keyword || '%' OR groupName LIKE '%' || :keyword || '%' ")
    LiveData<List<Contact>> searchContactsByKeyword(String keyword);

    @Query("SELECT * FROM contacts WHERE name =:keyword LIMIT 1")
    LiveData<Contact>searchContactByName(String keyword);
    @Query("SELECT * FROM contacts WHERE groupName =:keyword ")
    LiveData<List<Contact>>searchContactsByGroup(String keyword);
}
