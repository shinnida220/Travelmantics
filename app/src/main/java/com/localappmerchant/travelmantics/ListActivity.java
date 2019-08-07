package com.localappmerchant.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.localappmerchant.travelmantics.adapters.TravelDealAdapter;
//import com.localappmerchant.travelmantics.models.TravelDeal;
import com.localappmerchant.travelmantics.utils.db.FirebaseUtil;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    // private FirebaseDatabase mFirebaseDatabase;
    //private DatabaseReference mDatabaseReference;
    //private ChildEventListener mChildEventListener;
    // ArrayList<TravelDeal> deals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
    }

    // As oncreateOption is not being not called again, its better to set this up...
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupMenu(menu);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);

        // Show insert menu based on role.
        setupMenu(menu);
        return true;
    }

    private void setupMenu(Menu menu){
        // Show insert menu based on role.
        MenuItem insertMenu = menu.findItem(R.id.insert_menu);
        if( FirebaseUtil.isAdmin ){
            insertMenu.setVisible(true);
        }
        else{
            insertMenu.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.insert_menu:
                startActivity(new Intent(this, DealActivity.class));
                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                Log.d("Logout", "User logged out");
                                FirebaseUtil.attachFirebaseAuthListener();
                            }
                        });
                FirebaseUtil.detachFirebaseAuthListener();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachFirebaseAuthListener();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reason this was moved here is because at onCreate, user is not yet logged in ..
        // i believe a more better approach will be to place the login check in a global app area..
        // Somewhere not dependent on the=is activity..
        RecyclerView rvDeals = findViewById(R.id.rvDeals);
        final TravelDealAdapter tdAdapter = new TravelDealAdapter(this);
        rvDeals.setAdapter(tdAdapter);
        LinearLayoutManager dealsLayoutMgr = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvDeals.setLayoutManager(dealsLayoutMgr);

        FirebaseUtil.attachFirebaseAuthListener();
    }

    public void showMenu(){
        invalidateOptionsMenu();
    }
}
