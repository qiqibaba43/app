package com.example.contactsapp_experimentalweek;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

// 定义数据库类，包含一个表（Contact）
@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactRoomDatabase extends RoomDatabase {
    // 获取DAO实例的抽象方法
    public abstract ContactDao contactDao();

    // 单例模式实例
    private static ContactRoomDatabase INSTANCE;

    // 获取数据库实例的方法，使用单例模式
    static ContactRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ContactRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ContactRoomDatabase.class, "contact_database")
                            // 如果没有迁移对象，则擦除并重新生成数据库
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback) // 在数据库打开时添加回调
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // 异步任务类，用于初始化数据库
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final ContactDao mDao;

        // 构造函数，初始化DAO
        PopulateDbAsync(ContactRoomDatabase db) {
            mDao = db.contactDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // 每次启动应用时，清空数据库
            // 如果只在创建时填充数据，不需要这行代码
            return null;
        }
    }

    // 数据库回调，用于在数据库打开时执行一些操作
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new PopulateDbAsync(INSTANCE).execute(); // 异步初始化数据库
        }
    };
}
