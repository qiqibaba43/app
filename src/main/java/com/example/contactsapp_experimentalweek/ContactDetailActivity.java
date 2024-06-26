package com.example.contactsapp_experimentalweek;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import com.bumptech.glide.Glide;

// 联系人详细信息活动类
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
    private ContactRoomDatabase db_contact; // 联系人数据库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.changeTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail); // 设置布局

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题
        }

        // 初始化视图
        edittextName = findViewById(R.id.editText_name_contact_detail);
        editTextPhone = findViewById(R.id.editTextPhone_contact_detail);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_contact_detail);
        imageButton_detail = findViewById(R.id.image_contact_detail);
        spinnerDetail = findViewById(R.id.spinner_group_contact_detail);
        buttonCallContact = findViewById(R.id.button_call); // 初始化拨打电话按钮

         //设置Spinner适配器
        CharSequence [] arr={"默认分组","家人","朋友","同学","同事"};
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.item_spinner,R.id.textView_spinner,arr);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerDetail.setAdapter(spinnerAdapter);

        // 初始化ViewModel
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        Intent intent = getIntent();
        name = intent.getStringExtra("contact_name");

        // 如果意图中包含联系人姓名，则获取联系人信息并显示
        if (intent != null && intent.hasExtra("contact_name")) {
            contactViewModel.searchContactByName(name).observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    if (contact != null) {
                        edittextName.setText(contact.getName());
                        editTextPhone.setText(contact.getPhoneNumber());
                        editTextEmail.setText(contact.getEmail());
                        // 遍历spinnerAdapter中的数据，找到匹配的选项
                        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                            CharSequence item = spinnerAdapter.getItem(i);
                            if ((item).equals(contact.getGroupName())) {
                                // 找到匹配的选项，设置给Spinner
                                spinnerDetail.setSelection(i); // 设置选中项
                                break;
                            }
                        }
                        // 加载头像图片
                        if (contact.getAvatarUri().equals("drawable/image_contact.png")) {
                            Drawable drawable = ContextCompat.getDrawable(ContactDetailActivity.this, R.drawable.image_contact);
                            imageButton_detail.setImageDrawable(drawable);
                            selectedImageUri=Uri.parse("drawable/image_contact.png");
                        } else {
                            // 使用Glide加载图片
                            Glide.with(ContactDetailActivity.this)
                                    .load(Uri.parse(contact.getAvatarUri()))
                                    .into(imageButton_detail);
                        }
                    }
                }
            });

            // 设置点击监听器，点击头像选择图片
            imageButton_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });

            // 设置拨打电话按钮点击监听器
            buttonCallContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makePhoneCall();
                }
            });
        }

        // 初始化联系人数据库
        db_contact = Room.databaseBuilder(getApplicationContext(), ContactRoomDatabase.class, "contact_database").build();
    }

    // 处理选择图片结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData(); // 获取选中的图片URI
                Glide.with(ContactDetailActivity.this)
                        .load(selectedImageUri)
                        .into(imageButton_detail); // 使用Glide加载图片到ImageButton
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu); // 加载保存菜单
        return true;
    }

    // 处理菜单项选择
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            contactViewModel.searchContactByName(name).observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    // 更新联系人信息
                    contact.setName((edittextName.getText().toString()));
                    contact.setPhoneNumber(editTextPhone.getText().toString());
                    contact.setEmail(editTextEmail.getText().toString());
                    contact.setAvatarUri(selectedImageUri.toString());
                    // 异步更新数据到数据库
                    new Thread(() -> {
                        db_contact.contactDao().updateContact(contact);
                        runOnUiThread(() -> {
                            Toast.makeText(ContactDetailActivity.this, "联系人已更新", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                    // 返回主活动
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

    // 拨打电话的方法
    private void makePhoneCall() {
        String phoneNumber = editTextPhone.getText().toString();
        if (phoneNumber.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(ContactDetailActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContactDetailActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            } else {
                String dial = "tel:" + phoneNumber;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        } else {
            Toast.makeText(ContactDetailActivity.this, "电话号码无效", Toast.LENGTH_SHORT).show();
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "需要电话权限才能拨打电话", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
