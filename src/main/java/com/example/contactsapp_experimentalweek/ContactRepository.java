package com.example.contactsapp_experimentalweek;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import java.util.List;

// 在这个 ContactRepository 类中，我们封装了对 ContactDao 的所有调用。
public class ContactRepository {
    private ContactDao cContactDao; // ContactDao 实例
    private LiveData<List<Contact>> cAllContacts; // 用于观察所有联系人的 LiveData 对象

    // 构造函数，初始化 ContactDao 和获取所有联系人
    ContactRepository(Application application) {
        ContactRoomDatabase db = ContactRoomDatabase.getDatabase(application); // 获取数据库实例
        cContactDao = db.contactDao(); // 获取 ContactDao 实例
        cAllContacts = cContactDao.getAllContacts(); // 获取所有联系人
    }

    // 获取所有联系人的 LiveData 对象
    LiveData<List<Contact>> getAllContacts() {
        return cAllContacts;
    }

    // 插入联系人
    public void insertContact(Contact contact) {
        new insertAsyncTask(cContactDao).execute(contact); // 异步插入联系人
    }

    // 更新联系人
    public void updateContact(Contact contact) {
        new updateAsyncTask(cContactDao).execute(contact); // 异步更新联系人
    }

    // 删除联系人
    public void deleteContact(Contact contact) {
        new deleteContactAsyncTask(cContactDao).execute(contact); // 异步删除联系人
    }

    // 通过关键字搜索联系人
    public LiveData<List<Contact>> searchContactsByKeyword(String keyword) {
        return cContactDao.searchContactsByKeyword(keyword); // 返回搜索结果
    }

    // 通过名字搜索联系人
    public LiveData<Contact> searchContactByName(String keyword) {
        return cContactDao.searchContactByName(keyword); // 返回搜索结果
    }

    // 通过分组搜索联系人
    public LiveData<List<Contact>> searchContactsByGroup(String group) {
        return cContactDao.searchContactsByGroup(group); // 返回搜索结果
    }

    // 异步任务类：插入联系人
    private static class insertAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao cAsyncTaskDao;

        insertAsyncTask(ContactDao dao) {
            cAsyncTaskDao = dao; // 初始化 ContactDao
        }

        @Override
        protected Void doInBackground(final Contact... params) {
            cAsyncTaskDao.insertContact(params[0]); // 在后台插入联系人
            return null;
        }
    }

    // 异步任务类：删除联系人
    private static class deleteContactAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao mAsyncTaskDao;

        deleteContactAsyncTask(ContactDao dao) {
            mAsyncTaskDao = dao; // 初始化 ContactDao
        }

        @Override
        protected Void doInBackground(final Contact... params) {
            mAsyncTaskDao.deleteContact(params[0]); // 在后台删除联系人
            return null;
        }
    }

    // 异步任务类：更新联系人
    private static class updateAsyncTask extends AsyncTask<Contact, Void, Void> {
        private ContactDao cAsyncTaskDao;

        updateAsyncTask(ContactDao dao) {
            cAsyncTaskDao = dao; // 初始化 ContactDao
        }

        @Override
        protected Void doInBackground(final Contact... params) {
            cAsyncTaskDao.updateContact(params[0]); // 在后台更新联系人
            return null;
        }
    }
}
