package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;
import androidx.viewpager.widget.ViewPager;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.CalendarContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import notification.NotificationHelper;


public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabsAccessorAdaptor myTabsAccessorAdaptor;

   // private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private String currentUserID;

    private TabLayout myTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        (FirebaseDatabase.getInstance().getReference()).keepSynced(true);


        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannel();


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Departmental Notification");
        //Checking the connection




        mAuth = FirebaseAuth.getInstance();

        RootRef= FirebaseDatabase.getInstance().getReference();
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdaptor = new TabsAccessorAdaptor(getSupportFragmentManager());


        myTabLayout = findViewById(R.id.main_tabs);
        myViewPager.setAdapter(myTabsAccessorAdaptor);

        MultiDex.install(this);

        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();


        Intent intent = new Intent(this, ForegroundService.class);
        startService(intent);

        Intent intent1 = new Intent(this, LocationService.class);
        startService(intent1);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");

            VerifyUserExistence();
        }

    }

    @Override
    protected void onStop()
    {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        super.onStop();

        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        super.onDestroy();
        if(currentUser!=null)
        {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistence()
    {
        String currentUserID=mAuth.getCurrentUser().getUid();

        RootRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name")).exists())

                {
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }
                else
                {
                  // SendUserToSettingsActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_logout_option:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;

            case R.id.main_settings_option:
                SendUserToSettingsActivity();
                break;

            case R.id.main_create_group_option:
                RequestNewGroup();
                break;


            case R.id.main_find_friends_option:
                SendUserToFindFriendsActivity();
                break;

            case R.id.about_developer:
                SendUserToDeveloperActivity();
                break;

            default:


        }

        return true;
    }

    private void SendUserToDeveloperActivity()
    {
       /* ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("about Developer");
        progressDialog.setIcon(R.drawable.ic_developer_mode_black_24dp);
        progressDialog.setMessage("This is Developed under the license of KRISHNA INSTITUTE OF TECHNOLOGY\n");
   //     progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
*/
       startActivity(new Intent(MainActivity.this,IntroScreenActivity.class));


       // Toast.makeText(MainActivity.this,"Developed and created by himanshu shukla",Toast.LENGTH_LONG).show();
    }

    private void RequestNewGroup()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,
                R.style.AlertDialog);

        builder.setView(R.drawable.inputs);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField=new EditText(MainActivity.this);

        groupNameField.setHint(" Himanshu's App");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName= groupNameField.getText().toString();

                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this,
                            "Please write group name",
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    CreateNewGroup(groupName);

                }
            }

            private void CreateNewGroup(final String groupName)
            {
                RootRef.child("Groups")
                        .child(groupName)
                        .setValue("")

                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(MainActivity.this,
                                            groupName+" group is created Successfully",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void SendUserToSettingsActivity() {
        Intent loginIntent = new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(loginIntent);

    }

   /* @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }*/

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void SendUserToFindFriendsActivity() {
        Intent FindFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);

        startActivity(FindFriendIntent);
    }
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");

        saveCurrentTime=currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap =new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID=mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);


    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}
