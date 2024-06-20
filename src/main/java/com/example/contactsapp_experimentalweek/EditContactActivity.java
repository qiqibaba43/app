package com.example.contactsapp_experimentalweek;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;

public class EditContactActivity extends AppCompatActivity {
    private ImageView imageView_edit;
    private EditText edittextName;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private Spinner spinnerGroup;
    private ContactRoomDatabase db_contact;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String avatarPath="drawable/image_contact.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        }

        // 引用视图控件
        imageView_edit=findViewById(R.id.imageView_edit);
        imageView_edit.setImageResource(R.drawable.image_contact);//设置初始头像
        edittextName = findViewById(R.id.editText_add_contact_name);
        editTextPhone = findViewById(R.id.editTextPhone_add_contact);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_add_contact);
        spinnerGroup = findViewById(R.id.spinner_add_contact);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.groups, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(spinnerAdapter);

        // 创建数据库实例
        db_contact= Room.databaseBuilder(getApplicationContext(), ContactRoomDatabase.class, "contact_database").build();

        imageView_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });


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
            // 获取用户输入的数据
            String name = edittextName.getText().toString();
            String phone = editTextPhone.getText().toString();
            String email = editTextEmail.getText().toString();
            String group = spinnerGroup.getSelectedItem().toString();
            // 创建Contact对象
            Contact newContact = new Contact(name, phone, email, group,avatarPath);
            // 插入数据到数据库
            new Thread(() -> {
                db_contact.contactDao ().insertContact(newContact);
                runOnUiThread(() -> {
                    Toast.makeText(EditContactActivity.this, "联系人已保存", Toast.LENGTH_SHORT).show();
                });
            }).start();

            Intent intent = new Intent(EditContactActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            avatarPath=selectedImageUri.toString();
            try {
                Glide.with(EditContactActivity.this)
                        .load(selectedImageUri)
                        .into(imageView_edit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
