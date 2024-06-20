package com.example.contactsapp_experimentalweek;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import com.bumptech.glide.Glide;

public class ContactDetailActivity extends AppCompatActivity {

    private EditText edittextName;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private ImageButton imageButton_detail;
    private Spinner spinnerDetail;
    String name;
    private ContactViewModel contactViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri selectedImageUri;
    private ContactRoomDatabase db_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        }

        edittextName = findViewById(R.id.editText_name_contact_detail);
        editTextPhone = findViewById(R.id.editTextPhone_contact_detail);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_contact_detail);
        imageButton_detail= findViewById(R.id.image_contact_detail);
        spinnerDetail=findViewById(R.id.spinner_group_contact_detail);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.groups, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDetail.setAdapter(spinnerAdapter);

        contactViewModel= new ViewModelProvider(this).get(ContactViewModel.class);
        Intent intent = getIntent();
        name = intent.getStringExtra("contact_name");

        if (intent != null && intent.hasExtra("contact_name")) {
            contactViewModel.searchContactByName(name).observe(this,new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    if(contact!=null) {
                        edittextName.setText(contact.getName() + "");
                        editTextPhone.setText(contact.getPhoneNumber() + "");
                        editTextEmail.setText(contact.getEmail() + "");
                        // 遍历spinnerAdapter中的数据，找到匹配的选项
                        for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                            CharSequence item = spinnerAdapter.getItem(i);
                            if ((item).equals(contact.getGroupName())) {
                                // 找到匹配的选项，设置给Spinner
                                spinnerDetail.setSelection(i);//spinner数量少
                                break;
                            }
                        }
                        if(contact.getAvatarUri().equals("drawable/image_contact.png")){
                            Drawable drawable = ContextCompat.getDrawable(ContactDetailActivity.this, R.drawable.image_contact);
                            imageButton_detail.setImageDrawable(drawable);
                        }else{
                            // 使用Glide加载图片
                            Glide.with(ContactDetailActivity.this)
                                    .load(Uri.parse(contact.getAvatarUri()))
                                    .into(imageButton_detail);
                        }
                    }
                }
            });


            // 设置点击监听器
            imageButton_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });

        }

        db_contact= Room.databaseBuilder(getApplicationContext(), ContactRoomDatabase.class, "contact_database").build();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                selectedImageUri = data.getData();
                Glide.with(ContactDetailActivity.this)
                        .load(selectedImageUri)
                        .into(imageButton_detail);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            contactViewModel.searchContactByName(name).observe(this,new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    contact.setPhoneNumber(editTextPhone.getText().toString());
                    contact.setEmail(editTextEmail.getText().toString());
                    contact.setAvatarUri(selectedImageUri.toString());
                    //   更新数据到数据库(异步，不会卡顿）
                    new Thread(() -> {
                        db_contact.contactDao ().updateContact(contact);
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
}
