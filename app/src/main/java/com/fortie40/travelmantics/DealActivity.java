package com.fortie40.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class DealActivity extends AppCompatActivity {

    public static final int PICTURE_CODE = 42;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText mTxtTitle;
    private EditText mTxtDescription;
    private EditText mTxtPrice;
    private TextView mTxtTitlee;
    private TextView mTxtDescriptionn;
    private TextView mTxtPricee;
    private TravelDeal deal;
    ImageView mImageView;
    ImageView mImageVieww;
    private Button mBtnImage;
    public static Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(FirebaseUtil.mIsAdmin) {
            setContentView(R.layout.activity_deal);
        } else {
            setContentView(R.layout.activity_user);
        }
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Database Reference
        getDatabaseReference();

        // display note from intent
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(deal == null) {
            deal = new TravelDeal();
        }

        this.deal = deal;

        if(FirebaseUtil.mIsAdmin) {
            mTxtTitle.setText(deal.getTitle());
            mTxtDescription.setText(deal.getDescription());
            mTxtPrice.setText(deal.getPrice());
            mImageView = findViewById(R.id.image);
        } else {
            mTxtTitlee.setText(deal.getTitle());
            mTxtDescriptionn.setText(deal.getDescription());
            mTxtPricee.setText("Ksh. " + deal.getPrice());
            mImageVieww = findViewById(R.id.image);
        }

        // upload pic
        uploadPic();

        // show image
        if(FirebaseUtil.mIsAdmin) {
            showImage(deal.getImageUrl(), mImageView);
        } else {
            showImage(deal.getImageUrl(), mImageVieww);
        }

        // check network availability
        CheckActivityState.isNetworkAvailable(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckActivityState.dealActivityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CheckActivityState.dealActivityPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deal, menu);

        if(FirebaseUtil.mIsAdmin) {
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete_menu).setVisible(true);
            //enableEditText(true);
            mBtnImage.setVisibility(View.VISIBLE);
        } else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete_menu).setVisible(false);
            //enableEditText(false);
            mBtnImage.setVisibility(View.GONE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
         if(id == R.id.save_menu) {
            // saving deals to database
             InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(findViewById(R.id.dealView).getWindowToken(), 0);

            saveDeal();
            clean();
            backToList("Deal Saved Successfully");
            return true;
        } else if (id == R.id.delete_menu) {
             deleteDeal();
             backToList("Deal Deleted Successfully");
         }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> results = taskSnapshot.getStorage().getDownloadUrl();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageName(pictureName);
                    results.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            deal.setImageUrl(imageUrl);
                            if(FirebaseUtil.mIsAdmin) {
                                showImage(imageUrl, mImageView);
                            } else {
                                showImage(imageUrl, mImageVieww);
                            }
                        }
                    });
                }
            });
        }
    }

    // uploading a picture to firebase
    private void uploadPic() {
        mBtnImage = findViewById(R.id.btnImage);
        mBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_CODE);
            }
        });
    }

    // firebase reference
    private void getDatabaseReference() {
        Log.d("getDatabaseReference", "Thread: " + Thread.currentThread().getId());

        // FirebaseUtil.openFbReference("traveldeals");
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        if(FirebaseUtil.mIsAdmin) {
            mTxtTitle = findViewById(R.id.txtTitle);
            mTxtDescription = findViewById(R.id.txtDescription);
            mTxtPrice = findViewById(R.id.txtPrice);
        } else {
            mTxtTitlee = findViewById(R.id.txtTitle);
            mTxtDescriptionn = findViewById(R.id.txtDescription);
            mTxtPricee = findViewById(R.id.txtPrice);
        }
    }

    // show snackbar
    private void handleMessage(String message) {
        View view = findViewById(R.id.dealView);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    // saving a deal to firebase
    private void saveDeal() {
        deal.setTitle(mTxtTitle.getText().toString());
        deal.setDescription(mTxtDescription.getText().toString());
        deal.setPrice(mTxtPrice.getText().toString());

        if(deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        } else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    //deleting a deal from firebase
    private void deleteDeal() {
        if( deal == null) {
            handleMessage("Invalid Options");
            return;
        }
         mDatabaseReference.child(deal.getId()).removeValue();

        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //
                }
            });
        }
    }

    // going back to ListActivity
    private void backToList(String message) {
        Intent intent = new Intent(this, ListActivity.class);
        intent.putExtra("message", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // clean the inputs after saving a deal
    private void clean() {
        mTxtTitle.setText("");
        mTxtDescription.setText("");
        mTxtPrice.setText("");

        mTxtTitle.requestFocus();
    }

    // enable edit text if its an admin
    private void enableEditText (boolean isEnabled) {
        mTxtTitle.setEnabled(isEnabled);
        mTxtDescription.setEnabled(isEnabled);
        mTxtPrice.setEnabled(isEnabled);
    }

    // show image
    public static void showImage(String url, ImageView view) {
        if(url != null) {
            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .error(R.drawable.ic_error_black_24dp)
                    .into(view);
        }
    }
}
