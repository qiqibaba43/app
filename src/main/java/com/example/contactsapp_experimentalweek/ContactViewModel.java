package com.example.contactsapp_experimentalweek;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private ContactRepository cRepository;
    private LiveData<List<Contact>> cAllContact;
    public ContactViewModel (Application application) {
        super(application);
        cRepository = new ContactRepository(application);
        cAllContact = cRepository.getAllContacts();
    }
    //新建联系人
    public void insertContact(Contact contact) {
        cRepository.insertContact(contact);
    }
    // 更新联系人
    public void  updateContact(Contact contact) {
        cRepository.updateContact(contact);
        // 更新 LiveData
        cAllContact = cRepository.getAllContacts();
    }
    // 删除联系人
    public void deleteContact(Contact contact) {
        cRepository.deleteContact(contact);
    }

    LiveData<List<Contact>>getAllContacts() { return cAllContact; }
    public LiveData<List<Contact>> searchContactsByKeyword(String keyword) {
        return cRepository.searchContactsByKeyword(keyword);
    }

    public LiveData<Contact> searchContactByName(String keyword){
        return  cRepository.searchContactByName(keyword);
    }
    public LiveData<List<Contact>> searchContactsByGroup(String group){
        return  cRepository.searchContactsByGroup(group);
    }
}