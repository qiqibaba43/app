package com.example.contactsapp_experimentalweek;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;
import com.bumptech.glide.Glide;

public class EditContactActivity extends AppCompatActivity {
    private ImageView imageView_edit; // 编辑联系人头像的ImageView
    private EditText edittextName; // 编辑联系人姓名的EditText
    private EditText editTextPhone; // 编辑联系人电话号码的EditText
    private EditText editTextEmail; // 编辑联系人邮箱的EditText
    private Spinner spinnerGroup; // 编辑联系人分组的Spinner
    private ContactRoomDatabase db_contact; // 联系人数据库
    private static final int PICK_IMAGE_REQUEST = 1; // 请求代码，用于选择图片
    private String avatarPath = "drawable/image_contact.png"; // 头像路径，默认为默认头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeUtil.changeTheme(this);

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
        imageView_edit = findViewById(R.id.imageView_edit);
        imageView_edit.setImageResource(R.drawable.image_contact); // 设置初始头像
        edittextName = findViewById(R.id.editText_add_contact_name);
        editTextPhone = findViewById(R.id.editTextPhone_add_contact);
        editTextEmail = findViewById(R.id.editTextTextEmailAddress_add_contact);
        spinnerGroup = findViewById(R.id.spinner_add_contact);

        //设置Spinner适配器
        CharSequence [] arr={"默认分组","家人","朋友","同学","同事"};
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.item_spinner,R.id.textView_spinner,arr);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinnerGroup.setAdapter(spinnerAdapter);

        // 创建数据库实例
        db_contact = Room.databaseBuilder(getApplicationContext(), ContactRoomDatabase.class, "contact_database").build();

        // 设置头像点击事件，选择图片
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
        inflater.inflate(R.menu.menu_save, menu); // 加载保存菜单
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

            // 创建 Contact 对象
            Contact newContact = new Contact(name, phone, email, group, avatarPath);

            // 插入数据到数据库
            new Thread(() -> {
                db_contact.contactDao().insertContact(newContact);
                runOnUiThread(() -> {
                    Toast.makeText(EditContactActivity.this, "联系人已保存", Toast.LENGTH_SHORT).show();
                });
            }).start();

            // 返回到主界面
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
        // 检查请求码和结果码以及数据是否有效
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // 获取选择的图片的URI
            Uri selectedImageUri = data.getData();
            // 将头像路径更新为选择的图片的URI字符串形式
            avatarPath = selectedImageUri.toString();
            try {
                // 使用 Glide 加载选择的图片到 ImageView
                Glide.with(EditContactActivity.this)
                        .load(selectedImageUri)
                        .into(imageView_edit);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
