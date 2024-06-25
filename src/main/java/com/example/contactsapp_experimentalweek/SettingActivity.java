package com.example.contactsapp_experimentalweek;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1024;
    private ContactViewModel contactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ThemeUtil.changeTheme(this);//应用到配置界面上
        super.onCreate(savedInstanceState);//super.onCreate(savedInstanceState)是调用父类的onCreate构造函数,savedInstanceState是保存当前Activity的状态信息
        ThemeUtil.changeTheme(this);
        setContentView(R.layout.activity_setting);//加载并设置当前布局文件作为当前界面的显示

        Toolbar toolbar = findViewById(R.id.toolbar_configuration);
        setSupportActionBar(toolbar);//Toolbar 通过 setSupportActionBar(toolbar) 被修饰成了actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//给左上角图标的左边加上一个返回的图标
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

        // 导入按钮点击事件
        Button importButton = findViewById(R.id.button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readContactsFromFileAndInsertToDatabase();
            }
        });
// 主题选择
        RadioGroup radioGroup_theme = findViewById(R.id.radioGroup_theme_set);
        // 获取名为"MyPrefsTheme"的SharedPreferences实例，用于存储和读取主题选择状态
        SharedPreferences sharedPreferences_theme = getSharedPreferences("MyPrefsTheme", MODE_PRIVATE);
        // 从SharedPreferences中读取已保存选中的RadioButton的ID，默认值为-1
        int savedThemeRadioButtonId = sharedPreferences_theme.getInt("selectedThemeRadioButtonId", -1);
        // 如果有已保存的RadioButton选中状态，则将该RadioButton设为选中状态
        if (savedThemeRadioButtonId != -1) {
            RadioButton savedRadioButton = findViewById(savedThemeRadioButtonId);
            savedRadioButton.setChecked(true);
        } else {
            // 如果没有保存的选中状态，默认选中浅色主题的RadioButton
            RadioButton radioButton_theme_light = findViewById(R.id.radioButton_theme_light);
            radioButton_theme_light.setChecked(true);
        }
// 设置RadioGroup的选中监听器
        radioGroup_theme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override//当RadioButton的选中状态发生变化时调用该方法。
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // 获取被选中的RadioButton的ID
                int selectedThemeRadioButtonId = radioGroup.getCheckedRadioButtonId();
                // 获取名为"MyPrefsTheme"的SharedPreferences实例，准备写入新的选中状态
                SharedPreferences sharedPreferences_theme = getSharedPreferences("MyPrefsTheme", MODE_PRIVATE);
                // 获取SharedPreferences的编辑器，准备进行修改
                SharedPreferences.Editor editor = sharedPreferences_theme.edit();
                // 将当前选中的RadioButton的ID存入SharedPreferences中
                editor.putInt("selectedThemeRadioButtonId", selectedThemeRadioButtonId);
                // 提交修改
                editor.apply();
                // 根据选中的主题RadioButton来设置全局变量ThemeUtil.night
                if (selectedThemeRadioButtonId == R.id.radioButton_theme_dark) {
                    ThemeUtil.night = true;
                }
                if (selectedThemeRadioButtonId == R.id.radioButton_theme_light) {
                    ThemeUtil.night = false;
                }
                // 应用主题变化
                ThemeUtil.changeTheme(SettingActivity.this);
                // 更新当前界面
                recreate(); // 重新创建当前活动以应用主题变化
            }
        });
        //提供主题选择功能，并通过SharedPreferences来保存用户的选择，应用在下次启动时记住用户的偏好。
    }
    @Override
    protected void onResume() {
        super.onResume();
        ThemeUtil.changeTheme(this); // 应用当前的主题设置
    }
    private void importContactsFromTextFile() {
        // 获取外部存储目录
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File importFile = new File(externalStorageDir, "contacts.txt");
        if (importFile.exists()) {
            StringBuilder contactData = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    contactData.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "读取文件失败", Toast.LENGTH_LONG).show();
                return;
            }
            // 解析文件内容并插入数据库
            String[] contacts = contactData.toString().split("\n");
            for (String contactStr : contacts) {
                String[] parts = contactStr.split(",");
                if (parts.length >= 1) {
                    String name = parts[0].split(":")[1].trim();
                    String phone = parts.length > 1 ? parts[1].split(":")[1].trim() : null;
                    String email = parts.length > 2 ? parts[2].split(":")[1].trim() : null;
                    String groupName = parts.length > 3 ? parts[3].split(":")[1].trim() : "默认分组";
                    String avatarUri = parts.length > 4 ? parts[4].split(":")[1].trim() : "drawable/image_contact.png";
                    // 创建 Contact 对象并插入数据库
                    Contact contact = new Contact(name, phone, email, groupName, avatarUri);
                    contactViewModel.insertContact(contact);
                }
            }
            Toast.makeText(this, "联系人导入成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
        }
    }

    // 读取外部文件中的联系人并导入数据库
    private void readContactsFromFileAndInsertToDatabase() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本，使用新的存储权限管理方式
            if (Environment.isExternalStorageManager()) {
                importContactsFromTextFile();
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
                importContactsFromTextFile();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS
                }, REQUEST_CODE);
            }
        } else {
            // Android 6.0以下版本，直接写入文件
            importContactsFromTextFile();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 处理返回按钮的点击事件
            case android.R.id.home:
                // 返回主页面
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 重写 onBackPressed() 方法
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        // 创建一个新的任务栈，并且清除之前任务栈中的所有活动
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    // 请求权限方法
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

    // 读取数据库中的联系人并写入文件
    @SuppressLint("Range")
    private void readContactsFromDatabaseAndWriteToFile() {
        contactViewModel.getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                if (contacts != null && contacts.size() > 0) {
                    StringBuilder contactData = new StringBuilder();
                    for (Contact contact : contacts) {
                        contactData.append("Name: ").append(contact.getName())
                                .append(", Phone: ").append(contact.getPhoneNumber())
                                .append(", Email: ").append(contact.getEmail())
                                .append(", Group: ").append(contact.getGroupName())
                                .append(", Avatar: ").append(contact.getAvatarUri())
                                .append("\n");
                    }
                    exportContactsToFile(contactData.toString());
                } else {
                    Toast.makeText(SettingActivity.this, "数据库中没有联系人数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 将联系人数据导出到文件
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

    // 处理权限请求结果
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

    // 处理权限设置页面返回结果
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
