package com.example.contactsapp_experimentalweek;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fabAddContact;
    static ContactAdapter CAdapter;
    static ContactViewModel contactViewModel;
    static List contactsList=new ArrayList<>();
    RecyclerView recyclerView_contacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        // 初始化FloatingActionButton
        fabAddContact = findViewById(R.id.floatingActionButton);
        // 为FloatingActionButton设置点击事件监听器
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建新建联系人
                Intent intent = new Intent(MainActivity.this, EditContactActivity.class);
                startActivity(intent);
            }
        });
        // 创建适配器
        CAdapter = new ContactAdapter(this);
        // 设置ContactRecyclerView为布局
        recyclerView_contacts = findViewById(R.id.recyclerView_contacts);
        recyclerView_contacts .setAdapter(CAdapter);
        LinearLayoutManager layoutManager_contact = new LinearLayoutManager(this);
        recyclerView_contacts.setLayoutManager(layoutManager_contact);

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contactViewModel.getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                contactsList.addAll(contacts);
                CAdapter.setContacts(contacts);
                CAdapter.notifyDataSetChanged();
            }
        });

        SearchView searchViewContact=findViewById(R.id.Search_contact);
        searchViewContact.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 用户提交搜索时调用
                performSearch(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // 用户输入时调用
                performSearch(newText);
                return false;
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    // We are not implementing onMove() in this app
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    // When the use swipes a word,
                    // delete that word from the database.
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
                    {
                        int position = viewHolder.getAdapterPosition();
                        Contact myContact = CAdapter.getContactAtPosition(position);
                        Toast.makeText(MainActivity.this,
                                getString(R.string.delete_contact_preamble) + " " +
                                        myContact.getName(), Toast.LENGTH_LONG).show();
                        // Delete the word
                        contactViewModel.deleteContact(myContact);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView_contacts);

        CAdapter.setOnItemClickListener(new ContactAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Contact contact) {
                Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
                intent.putExtra("contact_name", contact.getName());
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performSearch(String query) {
        // 使用 Room 数据库进行搜索
        contactViewModel.searchContactsByKeyword(query).observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                // 更新 RecyclerView
                CAdapter.setContacts(contacts);
            }
        });
    }
}