package com.fortie40.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    ArrayList<TravelDeal> mDeals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    public DealAdapter() {
        // FirebaseUtil.openFbReference("traveldeal);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDeals = FirebaseUtil.mDeals;

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                mDeals.add(td);
                notifyItemInserted(mDeals.size() - 1);
                ListActivity.mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.travel_deals_list, parent, false);

        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal deal = mDeals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return mDeals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTvTitle;
        TextView mTvDescription;
        TextView mTvPrice;
        ImageView mImageView;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tvTitle);
            mTvDescription = itemView.findViewById(R.id.tvDecscription);
            mTvPrice = itemView.findViewById(R.id.tvPrice);
            mImageView = itemView.findViewById(R.id.imageDeal);

            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal) {
            mTvTitle.setText(ListActivity.truncate(deal.getTitle(), 60));
            mTvDescription.setText(ListActivity.truncate(deal.getDescription(), 40));
            mTvPrice.setText("Ksh. " + deal.getPrice());

            DealActivity.showImage(deal.getImageUrl(), mImageView);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = mDeals.get(position);
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }
    }
}
