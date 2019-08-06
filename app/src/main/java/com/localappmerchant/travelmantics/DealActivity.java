package com.localappmerchant.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.localappmerchant.travelmantics.models.TravelDeal;
import com.localappmerchant.travelmantics.utils.db.FirebaseUtil;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_RESULT = 42;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle, txtPrice, txtDescription;
    ImageView imageView;
    TravelDeal deal;
    private Button btnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        //mFirebaseDatabase = FirebaseDatabase.getInstance();
        //mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");
        //FirebaseUtil.openFBReference("traveldeals", this);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        imageView = findViewById(R.id.image);

        deal = (TravelDeal) getIntent().getSerializableExtra("deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
        showImage(deal.getImageUrl());

        btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);

        if (FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
        }
        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved!", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal deleted!", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                 return super.onOptionsItemSelected(item);
        }


    }

    private void clean() {
        txtTitle.setText(""); txtTitle.requestFocus();
        txtPrice.setText("");
        txtDescription.setText("");
    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());

        if (deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        }
        else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal(){
        if (deal == null){
            Toast.makeText(this, "Please save the deal before deleting.", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        Log.d("DELETE IMAGE", deal.getImageName());

        if (deal.getImageName() != null && deal.getImageName().isEmpty() == false){
            StorageReference ref = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("DELETE IMAGE","Image delete successful - ");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("DELETE IMAGE","Image delete failed - "+e.getMessage());
                }
            });
        }
    }

    private void backToList(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
        finish();
    }


    private void enableEditText(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        btnImage.setVisibility(isEnabled == true ? View.VISIBLE : View.INVISIBLE);
    }

    private void showImage(String url){
        if (url != null && url.isEmpty() == false) {
            int screenWidthInPixel = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url).resize(screenWidthInPixel, screenWidthInPixel*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("UPLOAD", "Upload completed");

                    // https://stackoverflow.com/a/52045211/380138
                    Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            String imageName = uri.getPath();

                            Log.d("UPLOAD", "Image name: " + imageName);

                            Log.d("UPLOAD", "Url gotten: " + imageUrl);
                            deal.setImageUrl(imageUrl);
                            showImage(imageUrl);
                        }
                    });
                }
            });
        }
    }
}
