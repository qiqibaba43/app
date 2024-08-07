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
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fabAddContact;
    private ContactAdapter CAdapter;
    private ContactViewModel contactViewModel;
    private DrawerLayout drawer;
    private LiveData<List<Contact>> currentLiveData;
    private RecyclerView recyclerView_contacts;
    private List<String> letters = Arrays.asList("A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z");
    private LettersAdapter LAdapter;
    private LinearSmoothScroller smoothScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtil.changeTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fabAddContact = findViewById(R.id.floatingActionButton);
        fabAddContact.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditContactActivity.class);
            startActivity(intent);
        });

        CAdapter = new ContactAdapter(this);
        recyclerView_contacts = findViewById(R.id.recyclerView_contacts);
        recyclerView_contacts.setAdapter(CAdapter);
        recyclerView_contacts.setLayoutManager(new LinearLayoutManager(this));

        LAdapter = new LettersAdapter(this);
        RecyclerView recyclerViewLetters = findViewById(R.id.recycleView_letters);
        recyclerViewLetters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewLetters.setAdapter(LAdapter);
        LAdapter.setLetters(letters);

        smoothScroller = new LinearSmoothScroller(recyclerView_contacts.getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

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

        CAdapter.setOnItemClickListener(contact -> {
            Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
            intent.putExtra("contact_name", contact.getName());
            startActivity(intent);
        });

        LAdapter.setOnItemClickListener(this::scrollToLetter);
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
        Map<Integer, String> groupMap = new HashMap<>();
        groupMap.put(R.id.nav_default, getString(R.string.Default));
        groupMap.put(R.id.nav_family, getString(R.string.Family));
        groupMap.put(R.id.nav_friend, getString(R.string.Friend));
        groupMap.put(R.id.nav_classmate, getString(R.string.Classmate));
        groupMap.put(R.id.nav_colleague, getString(R.string.Colleague));

        int itemId = item.getItemId();
        if (itemId == R.id.nav_default) {
            observeContacts(contactViewModel.getAllContacts());
        } else {
            String group = groupMap.get(itemId);
            if (group != null) {
                observeContacts(contactViewModel.searchContactsByGroup(group));
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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

    private void performSearch(String query) {
        observeContacts(contactViewModel.searchContactsByKeyword(query));
    }

    public void scrollToLetter(String letter) {
        int position = findPositionForLetter(letter);
        if (position != -1) {
            smoothScroller.setTargetPosition(position);
            RecyclerView.LayoutManager layoutManager = recyclerView_contacts.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.startSmoothScroll(smoothScroller);
            }
        }
    }

    private int findPositionForLetter(String letter) {
        for (int i = 0; i < CAdapter.getItemCount(); i++) {
            Contact contact = CAdapter.getContactAtPosition(i);
            String firstPinyin = CAdapter.getFirstPinyin(contact);
            if (firstPinyin.startsWith(letter, 0)) {
                return i;
            }
        }
        return -1;
    }
}
