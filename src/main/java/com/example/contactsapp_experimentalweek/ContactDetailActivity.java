package com.example.contactsapp_experimentalweek;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

public class ContactDetailActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private static final int MAX_REQUEST_COUNT = 3;
    private static final String PREFS_NAME = "PermissionPrefs";
    private static final String PREF_REQUEST_COUNT = "RequestCount";

    private EditText edittextName;
    private EditText editTextPhone;
    private EditText editTextEmail;
    private ImageButton imageButton_detail;
    private Spinner spinnerDetail;
    private Button buttonCallContact;
    private ContactViewModel contactViewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ContactRoomDatabase db_contact;
    private String name;  // 确保name在类中声明

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
                    makePhoneCall();
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
            contactViewModel.searchContactByName(name).observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    contact.setName(edittextName.getText().toString());
                    contact.setPhoneNumber(editTextPhone.getText().toString());
                    contact.setEmail(editTextEmail.getText().toString());
                    contact.setAvatarUri(selectedImageUri.toString());
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
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int requestCount = prefs.getInt(PREF_REQUEST_COUNT, 0);

            if (requestCount >= MAX_REQUEST_COUNT) {
                Toast.makeText(this, "请在设置中手动授予电话权限", Toast.LENGTH_SHORT).show();
            } else if (ContextCompat.checkSelfPermission(ContactDetailActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ContactDetailActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
            } else {
                String dial = "tel:" + phoneNumber;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
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
            } else {
                requestCount++;
                editor.putInt(PREF_REQUEST_COUNT, requestCount);
                editor.apply();

                if (requestCount >= MAX_REQUEST_COUNT) {
                    Toast.makeText(this, "请在设置中手动授予电话权限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "需要电话权限才能拨打电话", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
