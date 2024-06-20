package com.example.contactsapp_experimentalweek;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

//在这个 ContactRepository 类中，我们封装了对 ContactDao 的所有调用。
public class ContactRepository {
    private ContactDao cContactDao;
    // 单例模式获取数据库实例
    private LiveData<List<Contact>> cAllContacts;
    ContactRepository(Application application) {
        ContactRoomDatabase db = ContactRoomDatabase.getDatabase(application);
        cContactDao = db.contactDao();
        cAllContacts = cContactDao.getAllContacts();
    }

    LiveData<List<Contact>>getAllContacts() {
        return cAllContacts;
    }

    public void insertContact(Contact contact) {
        new insertAsyncTask(cContactDao).execute(contact);
    }
    // 插入多个联系人

    // 更新联系人
    public void updateContact(Contact contact) {
//        cContactDao.updateContact(contact);
        new updateAsyncTask(cContactDao).execute(contact);
    }

    // 删除联系人
    public void deleteContact(Contact contact) {
//        cContactDao.deleteContact(contact);
        new deleteContactAsyncTask(cContactDao).execute(contact);

    }
    public LiveData<List<Contact>> searchContactsByKeyword(String keyword) {
        return cContactDao.searchContactsByKeyword(keyword);
    }
    public LiveData<Contact> searchContactByName(String keyword) {
        return cContactDao.searchContactByName(keyword);
    }
    public LiveData<List<Contact>>searchContactsByGroup(String group) {
        return cContactDao.searchContactsByGroup(group);
    }
    private static class insertAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao cAsyncTaskDao;
        insertAsyncTask(ContactDao dao) {
            cAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(final Contact... params) {
            cAsyncTaskDao.insertContact(params[0]);
            return null;
        }
    }

    private static class deleteContactAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao mAsyncTaskDao;
        deleteContactAsyncTask(ContactDao dao) {
            mAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(final Contact... params) {
            mAsyncTaskDao.deleteContact(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao cAsyncTaskDao;
        updateAsyncTask(ContactDao dao) {
            cAsyncTaskDao = dao;
        }
        @Override
        protected Void doInBackground(final Contact... params) {
            cAsyncTaskDao.updateContact(params[0]);
            return null;
        }
    }

}
