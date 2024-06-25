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

    // 常量定义：请求码、SharedPreferences名称、拒绝权限计数键名和最大拒绝次数
    private static final int REQUEST_CODE = 1024;
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String KEY_PERMISSION_DENIED_COUNT = "PermissionDeniedCount";
    private static final int MAX_DENIED_COUNT = 2;

    private ContactViewModel contactViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.changeTheme(this); // 更改主题
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar_configuration);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 启用返回按钮
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        // 初始化 ContactViewModel
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        // 导出按钮点击事件
        Button exportButton = findViewById(R.id.button2);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission(); // 请求权限
            }
        });

        // 导入按钮点击事件
        Button importButton = findViewById(R.id.button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readContactsFromFileAndInsertToDatabase(); // 读取联系人文件并插入数据库
            }
        });

        // 主题选择
        RadioGroup radioGroup_theme = findViewById(R.id.radioGroup_theme_set);
        SharedPreferences sharedPreferences_theme = getSharedPreferences("MyPrefsTheme", MODE_PRIVATE);
        int savedThemeRadioButtonId = sharedPreferences_theme.getInt("selectedThemeRadioButtonId", -1);
        if (savedThemeRadioButtonId != -1) {
            RadioButton savedRadioButton = findViewById(savedThemeRadioButtonId);
            savedRadioButton.setChecked(true);
        } else {
            RadioButton radioButton_theme_light = findViewById(R.id.radioButton_theme_light);
            radioButton_theme_light.setChecked(true);
        }
        radioGroup_theme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selectedThemeRadioButtonId = radioGroup.getCheckedRadioButtonId();
                SharedPreferences sharedPreferences_theme = getSharedPreferences("MyPrefsTheme", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences_theme.edit();
                editor.putInt("selectedThemeRadioButtonId", selectedThemeRadioButtonId);
                editor.apply();
                if (selectedThemeRadioButtonId == R.id.radioButton_theme_dark) {
                    ThemeUtil.night = true;
                }
                if (selectedThemeRadioButtonId == R.id.radioButton_theme_light) {
                    ThemeUtil.night = false;
                }
                ThemeUtil.changeTheme(SettingActivity.this);
                recreate(); // 重新创建活动以应用主题更改
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ThemeUtil.changeTheme(this); // 确保主题在活动恢复时正确应用
    }

    // 从文本文件导入联系人
    private void importContactsFromTextFile() {
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
            String[] contacts = contactData.toString().split("\n");
            for (String contactStr : contacts) {
                String[] parts = contactStr.split(",");
                if (parts.length >= 1) {
                    String name = parts[0].split(":")[1].trim();
                    String phone = parts.length > 1 ? parts[1].split(":")[1].trim() : null;
                    String email = parts.length > 2 ? parts[2].split(":")[1].trim() : null;
                    String groupName = parts.length > 3 ? parts[3].split(":")[1].trim() : "默认分组";
                    String avatarUri = parts.length > 4 ? parts[4].split(":")[1].trim() : "drawable/image_contact.png";
                    Contact contact = new Contact(name, phone, email, groupName, avatarUri);
                    contactViewModel.insertContact(contact);
                }
            }
            Toast.makeText(this, "联系人导入成功", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
        }
    }

    // 读取联系人文件并插入数据库
    private void readContactsFromFileAndInsertToDatabase() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int deniedCount = preferences.getInt(KEY_PERMISSION_DENIED_COUNT, 0);

        if (deniedCount >= MAX_DENIED_COUNT) {
            Toast.makeText(this, "读取联系人权限请求已被拒绝，请在设置中手动开启权限。", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                importContactsFromTextFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            importContactsFromTextFile();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // 返回上一级
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        super.onBackPressed();
    }

    // 请求权限
    private void requestPermission() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int deniedCount = preferences.getInt(KEY_PERMISSION_DENIED_COUNT, 0);

        if (deniedCount >= MAX_DENIED_COUNT) {
            Toast.makeText(this, "存储联系人权限请求已被拒绝，请在设置中手动开启权限。", Toast.LENGTH_LONG).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            readContactsFromDatabaseAndWriteToFile();
        }
    }

    // 从数据库读取联系人并写入文件
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
                                .append(", AvatarUri: ").append(contact.getAvatarUri())
                                .append("\n");
                    }
                    writeToFile(contactData.toString());
                } else {
                    Toast.makeText(SettingActivity.this, "没有联系人数据", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // 将数据写入文件
    private void writeToFile(String data) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        File exportFile = new File(externalStorageDir, "contacts.txt");
        try (FileOutputStream fos = new FileOutputStream(exportFile)) {
            fos.write(data.getBytes());
            Toast.makeText(this, "联系人导出成功", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "文件写入失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int deniedCount = preferences.getInt(KEY_PERMISSION_DENIED_COUNT, 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(KEY_PERMISSION_DENIED_COUNT, deniedCount + 1);
                editor.apply();

                Toast.makeText(this, "存储或读取联系人权限获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                readContactsFromDatabaseAndWriteToFile();
            } else {
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int deniedCount = preferences.getInt(KEY_PERMISSION_DENIED_COUNT, 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(KEY_PERMISSION_DENIED_COUNT, deniedCount + 1);
                editor.apply();

                Toast.makeText(this, "存储权限获取失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
