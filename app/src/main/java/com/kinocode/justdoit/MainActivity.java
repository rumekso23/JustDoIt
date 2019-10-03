package com.kinocode.justdoit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.kinocode.justdoit.adapter.CustomAdapter;
import com.kinocode.justdoit.model.Notes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextToSpeech toSpeech;

    @BindView(R.id.tv_no_note)
    TextView tv_noNote;
    @BindView(R.id.lv_swipeMenu)
    SwipeMenuListView lv_swipeMenu;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    public static ArrayList<Notes> notes = new ArrayList<>();
    CustomAdapter myCustomAdapter;
    private List<ApplicationInfo> mAppList;
    private FirebaseAuth auth;
    public static boolean first = true;
    public static String userId;
    public static FirebaseUser user;
    public static FirebaseDatabase firebaseDatabase;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        auth = FirebaseAuth.getInstance();
        userId = user.getUid();

        mAppList = getPackageManager().getInstalledApplications(0);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(MainActivity.this, CreateNoteActivity.class));
                CreateNoteActivity.ListSelected = false;
            }
        });

        if (!(notes.size() > 0)){
            lv_swipeMenu.setVisibility(View.GONE);
            tv_noNote.setVisibility(View.VISIBLE);
        }else {
            lv_swipeMenu.setVisibility(View.VISIBLE);
            tv_noNote.setVisibility(View.GONE);
        }

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                switch (menu.getViewType()){
                    case 0:
                        createMenu1(menu);
                        break;
                }
            }
        };

        lv_swipeMenu.setMenuCreator(creator);
        toSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR){
                    Locale locale = new Locale("en", "US");
                    toSpeech.setLanguage(locale);
                }
            }
        });

        myCustomAdapter = new CustomAdapter(this, R.layout.note_row, notes);
        lv_swipeMenu.setAdapter(myCustomAdapter);

        lv_swipeMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CreateNoteActivity.ListPosition = position;
                CreateNoteActivity.ListSelected = true;
                finish();
                startActivity(new Intent(MainActivity.this, CreateNoteActivity.class));
            }
        });

        lv_swipeMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toSpeech.speak(notes.get(position).getContent().toString(), TextToSpeech.QUEUE_FLUSH, null);
                return true;
            }
        });

        lv_swipeMenu.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                ApplicationInfo item = mAppList.get(position);
                switch (index){
                    //Edit
                    case 0:
                        CreateNoteActivity.ListPosition = position;
                        CreateNoteActivity.ListSelected = true;
                        finish();
                        startActivity(new Intent(MainActivity.this, CreateNoteActivity.class));
                        break;
                    //Share
                    case 1:
                        Intent i = new Intent(android.content.Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Notes");
                        i.putExtra(Intent.EXTRA_TEXT, "" + notes.get(position).getTitle() + "\n" + notes.get(position).getContent());
                        startActivity(Intent.createChooser(i, "Share"));
                        break;
                    //Delete
                    case 2:
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(MainActivity.this);
                        }
                        builder.setTitle("Deletion Process")
                                .setMessage("Are you sure you want to delete the selected note?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mAppList.remove(position);
                                        notes.remove(position);
                                        CreateNoteActivity object = new CreateNoteActivity();
                                        object.database_operations();
                                        myCustomAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        break;

                    //Add Calendar API
                    case 3:
                        String s = notes.get(position).getDeadline_date();
                        String[] split = s.split("/");
                        String day = split[0];
                        String month = split[1];
                        String year = split[2];
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day), 17, 50);
                        long startMillis = 0;
                        long endMillis = 0;
                        endMillis = endTime.getTimeInMillis();
                        Calendar beginTime = Calendar.getInstance();
                        beginTime.set(2017, 12, 20, 6, 30);
                        startMillis = beginTime.getTimeInMillis();

                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setType("vnd.android.cursor.item/event");
                        intent.putExtra("title", notes.get(position).getTitle());
                        intent.putExtra("description", notes.get(position).getContent());
                        intent.putExtra("beginTime", startMillis);
                        intent.putExtra("endTime", endMillis);
                        startActivity(intent);
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        notes.clear();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menu_sort_deadline){
            Collections.sort(notes, Sort_deadlinedate);
            myCustomAdapter.notifyDataSetChanged();
        }

        if (id == R.id.menu_sort_priority){
            Collections.sort(notes, Sort_priority);
            myCustomAdapter.notifyDataSetChanged();
        }

        if (id == R.id.menu_sign_out){
            auth.signOut();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
            notes.clear();
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createMenu1(SwipeMenu menu) {
        SwipeMenuItem itemMenu1 = new SwipeMenuItem(getApplicationContext());
        itemMenu1.setBackground(new ColorDrawable(Color.parseColor("#EFEBE9")));
        itemMenu1.setWidth(dp2px(50));
        itemMenu1.setIcon(R.drawable.ic_edit);
        menu.addMenuItem(itemMenu1);

        SwipeMenuItem itemMenu2 = new SwipeMenuItem(getApplicationContext());
        itemMenu2.setWidth(dp2px(50));
        itemMenu2.setIcon(R.drawable.ic_share);
        menu.addMenuItem(itemMenu2);

        SwipeMenuItem itemMenu3 = new SwipeMenuItem(getApplicationContext());
        itemMenu3.setBackground(new ColorDrawable(Color.parseColor("#EEEEEE")));
        itemMenu3.setWidth(dp2px(50));
        itemMenu3.setIcon(R.drawable.ic_delete);

        SwipeMenuItem itemMenu4 = new SwipeMenuItem(getApplicationContext());
        itemMenu4.setWidth(dp2px(50));
        itemMenu4.setIcon(R.drawable.ic_add);
        menu.addMenuItem(itemMenu4);

    }

    private int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public Comparator<Notes> Sort_deadlinedate = new Comparator<Notes>() {
        @Override
        public int compare(Notes o1, Notes o2) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            try {
                Date d = dateFormat.parse(o1.getDeadline_date());
                Date d1 = dateFormat.parse(o2.getDeadline_date());

                return d.compareTo(d1);
            }catch (Exception e){

                System.out.println("Exception " + e);
                return 0;
            }
        }
    };

    public Comparator<Notes> Sort_priority = new Comparator<Notes>() {
        @Override
        public int compare(Notes o1, Notes o2) {

            Integer priorityo1=0,priorityo2=0;
            if(o1.getPriority().equalsIgnoreCase("Low")){
                priorityo1 = 3;
            }
            if(o1.getPriority().equalsIgnoreCase("Medium")){
                priorityo1 = 2;
            }
            if(o1.getPriority().equalsIgnoreCase("High")){
                priorityo1 = 1;
            }
            if(o2.getPriority().equalsIgnoreCase("Low")){
                priorityo2 = 3;
            }
            if(o2.getPriority().equalsIgnoreCase("Medium")){
                priorityo2 = 2;
            }
            if(o2.getPriority().equalsIgnoreCase("High")){
                priorityo2 = 1;
            }
            return priorityo1.compareTo(priorityo2);
        }
    };

    @Override
    protected void onPause() {
        if (toSpeech != null){
            toSpeech.stop();
            toSpeech.shutdown();
        }
        super.onPause();
    }
}
