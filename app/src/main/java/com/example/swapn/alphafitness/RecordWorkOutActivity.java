package com.example.swapn.alphafitness;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swapn.alphafitness.common.CircleTransform;
import com.example.swapn.alphafitness.common.FirebaseDb;
import com.example.swapn.alphafitness.common.Util;
import com.example.swapn.alphafitness.fragments.EditProfileDetailsFragment;
import com.example.swapn.alphafitness.fragments.ProfileDetailsFragment;
import com.example.swapn.alphafitness.fragments.RecordWorkFragment;
import com.example.swapn.alphafitness.fragments.WorkOutDetailFragment;
import com.example.swapn.alphafitness.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class RecordWorkOutActivity extends AppCompatActivity implements RecordWorkFragment.OnFragmentInteractionListener,
WorkOutDetailFragment.OnFragmentInteractionListener, ProfileDetailsFragment.OnFragmentInteractionListener, EditProfileDetailsFragment.OnFragmentInteractionListener{
    private static final String TAG = "RecordActivity";
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference databaseUser;
    private UserModel userData;
    private Util u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        u = new Util();
        Util.setContext(getApplicationContext());
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference(FirebaseDb.TABLE_USER);
        userData = u.getUserFromSHaredPreference(getApplicationContext());
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // TODO Setup user Account exists check
                    if(userData != null) {
                        initialiseApp();
                    } else {
                        loginActivity();
                    }
                } else {
                    loginActivity();
                }
                // ...
            }
        };
    }

    private void loginActivity () {
        FragmentManager manager = getSupportFragmentManager();
        try {
            RecordWorkFragment fragment = (RecordWorkFragment) manager.findFragmentById(R.id.frame);
           // fragment.stopService();
        } catch (Exception e) {
            Log.e("Fragment","Fragment not found");
        }
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    public void loadAppropriateFragment() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
            openWorkOutDetailsFragment();
            Toast.makeText(this,"Landscape",Toast.LENGTH_LONG);
        }
        else {
            openRecordWorkOutFragment();
            // Portrait
        }
    }

    public void openWorkOutDetailsFragment() {
        drawerLayout.closeDrawers();
        WorkOutDetailFragment fragment = new WorkOutDetailFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }

    public void openProfileDetailFragment () {
        drawerLayout.closeDrawers();
        ProfileDetailsFragment fragment = new ProfileDetailsFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }

    public void openRecordWorkOutFragment () {
        drawerLayout.closeDrawers();
        RecordWorkFragment fragment = new RecordWorkFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }

    public void openEditProfileFragment () {
        drawerLayout.closeDrawers();
        EditProfileDetailsFragment fragment = new EditProfileDetailsFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();
    }

    private void initialiseApp() {
        if(userData != null) {
        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
            navigationView.setItemIconTintList(null);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.inbox:
                        //Toast.makeText(getApplicationContext(),"Inbox Selected",Toast.LENGTH_SHORT).show();
                        openRecordWorkOutFragment();
                        return true;

                    case R.id.log_off :
                        mAuth.signOut();

                        // For rest of the options we just show a toast on click
                    default:
                        //Toast.makeText(getApplicationContext(),"Inbox Selected",Toast.LENGTH_SHORT).show();
                        openRecordWorkOutFragment();
                        return true;

                }
            }
        });

        View header = navigationView.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileDetailFragment();
            }
        });
        ImageView profpic = (ImageView) header.findViewById(R.id.profile_image);
        TextView username = (TextView) header.findViewById(R.id.username);
        TextView useremail = (TextView) header.findViewById(R.id.email);
        username.setText(userData.getName());
        useremail.setText(userData.getEmail());
        Picasso.with(RecordWorkOutActivity.this).load(Uri.parse(userData.getProf_pic())).transform(new CircleTransform()).into(profpic);
      //  Picasso.with(RecordWorkOutActivity.this).load(Uri.parse(userData.getProf_pic())).fit().centerCrop().into(profpic);


        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
            loadAppropriateFragment();
        } else {
            loginActivity();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    public UserModel getUserData() {
        return userData;
    }
}
