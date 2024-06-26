package com.example.contactsapp_experimentalweek;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.bumptech.glide.Glide;

public class ContactDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1; // 请求电话权限的请求码
    private EditText edittextName; // 编辑联系人姓名的EditText
    private EditText editTextPhone; // 编辑联系人电话的EditText
    private EditText editTextEmail; // 编辑联系人电子邮件的EditText
    private ImageButton imageButton_detail; // 显示联系人头像的ImageButton
    private Spinner spinnerDetail; // 显示联系人分组的Spinner
    private Button buttonCallContact; // 拨打电话按钮
    String name; // 联系人姓名
    private ContactViewModel contactViewModel; // 联系人视图模型
    private static final int PICK_IMAGE_REQUEST = 1; // 选择图片请求码
    Uri selectedImageUri; // 选中的图片URI
    private String avatarPath;
    private ContactRoomDatabase db_contact; // 联系人数据库
//    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int MAX_REQUEST_COUNT = 3;
    private static final int REQUEST_CODE_APP_SETTINGS = 2;
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String PREF_REQUEST_COUNT = "RequestCount";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.changeTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        edittextName = findViewById(R.id.editText_name_contact_detail);
        editTextPhone = findViewById(R.id.editTextPhone_contact_detail);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_contact_detail);
        imageButton_detail = findViewById(R.id.image_contact_detail);
        spinnerDetail = findViewById(R.id.spinner_group_contact_detail);
        buttonCallContact = findViewById(R.id.button_call);

        CharSequence[] arr = {"默认分组", "家人", "朋友", "同学", "同事"};
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.item_spinner, R.id.textView_spinner, arr);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerDetail.setAdapter(spinnerAdapter);

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        Intent intent = getIntent();
        name = intent.getStringExtra("contact_name");

        if (intent != null && intent.hasExtra("contact_name")) {
            contactViewModel.searchContactByName(name).observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    if (contact != null) {
                        edittextName.setText(contact.getName());
                        editTextPhone.setText(contact.getPhoneNumber());
                        editTextEmail.setText(contact.getEmail());
                        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                            CharSequence item = spinnerAdapter.getItem(i);
                            if (item.equals(contact.getGroupName())) {
                                spinnerDetail.setSelection(i);
                                break;
                            }
                        }
                        if ("drawable/image_contact.png".equals(contact.getAvatarUri())) {
                            Drawable drawable = ContextCompat.getDrawable(ContactDetailActivity.this, R.drawable.image_contact);
                            imageButton_detail.setImageDrawable(drawable);
                            avatarPath="drawable/image_contact.png";
                            selectedImageUri = Uri.parse("drawable/image_contact.png");
                        } else {
                            Glide.with(ContactDetailActivity.this)
                                    .load(Uri.parse(contact.getAvatarUri()))
                                    .into(imageButton_detail);
                        }
                    }
                }
            });

            imageButton_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });

            buttonCallContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkAndRequestPermission();
                }
            });
        }

        db_contact = Room.databaseBuilder(getApplicationContext(), ContactRoomDatabase.class, "contact_database").build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                Glide.with(ContactDetailActivity.this)
                        .load(selectedImageUri)
                        .into(imageButton_detail); // 使用Glide加载图片到ImageButton
                avatarPath=selectedImageUri.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_CODE_APP_SETTINGS) {
            checkAndRequestPermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            contactViewModel.searchContactByName(name).observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    contact.setName(edittextName.getText().toString());
                    contact.setPhoneNumber(editTextPhone.getText().toString());
                    contact.setEmail(editTextEmail.getText().toString());
                    contact.setAvatarUri(avatarPath);
                    // 异步更新数据到数据库
                    new Thread(() -> {
                        db_contact.contactDao().updateContact(contact);
                        runOnUiThread(() -> {
                            Toast.makeText(ContactDetailActivity.this, "联系人已更新", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                    Intent intent = new Intent(ContactDetailActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makePhoneCall() {
        String phoneNumber = editTextPhone.getText().toString();
        if (phoneNumber == null || phoneNumber.trim().length() == 0) {
            Toast.makeText(ContactDetailActivity.this, "电话号码无效", Toast.LENGTH_SHORT).show();
        } else {
            String dial = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    private void requestCallPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE},
                REQUEST_CALL_PERMISSION);
    }

    private void showAppSettings() {
        new AlertDialog.Builder(this)
                .setTitle("权限请求")
                .setMessage("请在设置中手动授予电话权限")
                .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_CODE_APP_SETTINGS);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void checkAndRequestPermission() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int requestCount = prefs.getInt(PREF_REQUEST_COUNT, 0);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (requestCount >= MAX_REQUEST_COUNT) {
                showAppSettings();
            } else {
                requestCallPermission();
            }
        } else {
            makePhoneCall();
            // 清空权限请求次数
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PREF_REQUEST_COUNT, 0);
            editor.apply();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            int requestCount = prefs.getInt(PREF_REQUEST_COUNT, 0);

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
                // 清空权限请求次数
                editor.putInt(PREF_REQUEST_COUNT, 0);
                editor.apply();
            } else {
                requestCount++;
                editor.putInt(PREF_REQUEST_COUNT, requestCount);
                editor.apply();

                if (requestCount >= MAX_REQUEST_COUNT) {
                    showAppSettings();
                } else {
                    requestCallPermission(); // 请求电话权限
                }
            }
        }
    }

}

