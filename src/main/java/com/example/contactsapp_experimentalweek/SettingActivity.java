package com.example.contactsapp_experimentalweek;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1024;
    private ContactViewModel contactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar_configuration);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        // 初始化 ContactViewModel
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        // 导出按钮点击事件
        Button exportButton = findViewById(R.id.button2);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 处理返回按钮的点击事件
            case android.R.id.home:
                //刷新主页面
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 重写onBackPressed()方法
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        // 创建一个新的任务栈，并且之前任务栈中的所有活动都会被清除
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本，使用新的存储权限管理方式
            if (Environment.isExternalStorageManager()) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                // 请求用户授权管理所有文件访问权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0及以上版本，需要动态请求存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS
                }, REQUEST_CODE);
            }
        } else {
            // Android 6.0以下版本，直接写入文件
            readContactsFromDatabaseAndWriteToFile();
        }
    }

    @SuppressLint("Range")
    private void readContactsFromDatabaseAndWriteToFile() {
        contactViewModel.getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                if (contacts != null && contacts.size() > 0) {
                    StringBuilder contactData = new StringBuilder();
                    for (Contact contact : contacts) {
                        contactData.append("Name: ").append(contact.getName()).append(", Phone: ").append(contact.getPhoneNumber());
                        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                            contactData.append(", Email: ").append(contact.getEmail());
                        }
                        contactData.append("\n");
                    }
                    exportContactsToFile(contactData.toString());
                } else {
                    Toast.makeText(SettingActivity.this, "数据库中没有联系人数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void exportContactsToFile(String contactData) {
        // 获取外部存储目录
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File exportFile = new File(externalStorageDir, "contacts.txt");

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            fos.write(contactData.getBytes());
            fos.flush();
            String message = "联系人导出成功：" + exportFile.getAbsolutePath() + "\n\n" + contactData;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                Toast.makeText(this, "存储或读取联系人权限获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 在Android 11及以上版本，处理权限请求结果
            if (Environment.isExternalStorageManager()) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                Toast.makeText(this, "存储权限获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
