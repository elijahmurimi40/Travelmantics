package com.fortie40.travelmantics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ListActivity extends AppCompatActivity {

    public static RecyclerView mRvDeals;
    private FloatingActionButton mFab;

    public static SwipeRefreshLayout mSwipeRefreshLayout;
    public static Toolbar mToolbar;
    public static BottomSheetDialog mBottomSheetDialog;

    static ListActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        if(FirebaseUtil.mIsAdmin) {
            setContentView(R.layout.activity_list);
        } else {
            setContentView(R.layout.activity_user_list);
        }
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, DealActivity.class);
                startActivity(intent);
            }
        });

        // Protecting MainThread
        enableStrictMode();

        mRvDeals = findViewById(R.id.rvDeals);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        MenuItem insertMenu = menu.findItem(R.id.insert_menu);

        if(FirebaseUtil.mIsAdmin) {
            insertMenu.setVisible(true);
        } else {
            insertMenu.setVisible(false);
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
        if(id == R.id.insert_menu) {
            Intent intent = new Intent(ListActivity.this, DealActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.logout_menu); {
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(FirebaseUtil.mAuthListener != null ) {
            FirebaseUtil.detachListener();
        }
        CheckActivityState.listActivityPaused();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onResume() {
        super.onResume();

        CheckActivityState.listActivityResumed();

        // Display SnackBar if item was deleted or added
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if(message != null) {
            Snackbar.make(mRvDeals, message, Snackbar.LENGTH_LONG).show();
        }

        // swipe to refresh
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);
        //notes
        displayNotesWhenNetwok(this);

        // pull down to refresh
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                //displayTravelDeals(ListActivity.this);
                displayNotesWhenNetwok(ListActivity.this);
            }
        });

    }

    // get the notes
    public void displayNotesWhenNetwok(Context context) {
        if(CheckActivityState.isNetworkAvailable(context)) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);

                    // Display travel deals
                    displayTravelDeals();

                    FirebaseUtil.attachListener();
                }
            });
        } else {
            mToolbar.setTitle(context.getString(R.string.connecting));
            //Dialog
            getDialog();
        }
    }

    // display a list of travel deals
    public void displayTravelDeals() {
        FirebaseUtil.openFbReference("traveldeals", ListActivity.this);
        //mRvDeals = view.findViewById(R.id.rvDeals);
        final DealAdapter adapter = new DealAdapter();
        mRvDeals.setAdapter(adapter);
    }

    // get the dialog
    public void getDialog() {
        if(mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mBottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        mBottomSheetDialog.setContentView(dialogView);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setCancelable(false);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);

        Button close = dialogView.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        Button retry = dialogView.findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNotesWhenNetwok(ListActivity.this);
            }
        });
    }

    // instance
    public static ListActivity getInstance() {
        return instance;
    }

    // reload
    public void reload() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        recreate();
        overridePendingTransition(0, 0);
    }

    // protecting main thread
    // TODO: All Database Interaction To Be Removed From Main Thread
    // TODO: Interact With Database Directly Using ID'S
    // TODO: Remove The TravelDeal Class After Implementation Of Interacting With Database Directly
    private void enableStrictMode() {
        if(BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    // logout a user
    private boolean logOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUtil.attachListener();
                    }
                });
        FirebaseUtil.detachListener();
        return true;
    }

    // invalidating a menu for non admin
        public void showMenu() {
        invalidateOptionsMenu();
    }

    // truncate
    public static String truncate(String value, int length) {
        if(value.length() > length) {
            return  value.substring(0, length) + " .....";
        } else {
            return value;
        }
    }
}
