package com.example.contactsapp_experimentalweek;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FloatingActionButton fabAddContact;
    private ContactAdapter CAdapter;
    private ContactViewModel contactViewModel;
    private DrawerLayout drawer;
    private LiveData<List<Contact>> currentLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 隐藏标题

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fabAddContact = findViewById(R.id.floatingActionButton);
        fabAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditContactActivity.class);
                startActivity(intent);
            }
        });

        CAdapter = new ContactAdapter(this);
        RecyclerView recyclerView_contacts = findViewById(R.id.recyclerView_contacts);
        recyclerView_contacts.setAdapter(CAdapter);
        recyclerView_contacts.setLayoutManager(new LinearLayoutManager(this));

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        observeContacts(contactViewModel.getAllContacts());

        SearchView searchViewContact = findViewById(R.id.Search_contact);
        searchViewContact.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Contact myContact = CAdapter.getContactAtPosition(position);
                        Toast.makeText(MainActivity.this, getString(R.string.delete_contact_preamble) + " " + myContact.getName(), Toast.LENGTH_LONG).show();
                        contactViewModel.deleteContact(myContact);
                    }
                });
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
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_all) {
            observeContacts(contactViewModel.getAllContacts());
        } else {
            String group = "";
            if (itemId == R.id.nav_family) {
                group = "家人";
            } else if (itemId == R.id.nav_friend) {
                group = "朋友";
            } else if (itemId == R.id.nav_classmate) {
                group = "同学";
            } else if (itemId == R.id.nav_colleague) {
                group = "同事";
            }

            observeContacts(contactViewModel.searchContactsByGroup(group));
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 观察联系人列表的变化，并更新 RecyclerView 的显示
     *
     * @param liveData 包含联系人列表的 LiveData
     */
    private void observeContacts(LiveData<List<Contact>> liveData) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }
        currentLiveData = liveData;
        currentLiveData.observe(this, contacts -> {
            CAdapter.setContacts(contacts);
            CAdapter.notifyDataSetChanged();
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

    /**
     * 执行搜索操作，根据关键字查询联系人
     *
     * @param query 搜索关键字
     */
    private void performSearch(String query) {
        observeContacts(contactViewModel.searchContactsByKeyword(query));
    }
}
