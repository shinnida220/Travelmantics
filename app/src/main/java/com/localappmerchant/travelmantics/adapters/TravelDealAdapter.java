package com.localappmerchant.travelmantics.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.localappmerchant.travelmantics.DealActivity;
import com.localappmerchant.travelmantics.ListActivity;
import com.localappmerchant.travelmantics.R;
import com.localappmerchant.travelmantics.models.TravelDeal;
import com.localappmerchant.travelmantics.utils.db.FirebaseUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TravelDealAdapter extends RecyclerView.Adapter<TravelDealAdapter.TravelDealViewHolder> {

    private final FirebaseDatabase mFirebaseDatabase;
    private final DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ArrayList<TravelDeal> deals;

    public TravelDealAdapter(ListActivity activity){
        FirebaseUtil.openFBReference("traveldeals", activity);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);
                Log.i("TravelDealAdapter", deal.getTitle());

                // When a new child is added, we listen and then get the id and use as the deal's ID
                deal.setId(dataSnapshot.getKey());
                deals.add(deal);

                notifyItemInserted(deals.size()-1);
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

        deals = FirebaseUtil.tDeals;
    }


    @NonNull
    @Override
    public TravelDealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.deal_single, parent, false);
        return new TravelDealViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull TravelDealViewHolder holder, int position) {
        TravelDeal deal = deals.get(position);
        holder.bind(deal);
    }


    @Override
    public int getItemCount() {
        return deals.size();
    }


    public class TravelDealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvTitle, tvDesc, tvPrice;
        ImageView imageDeal;

        public TravelDealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.deal_tv_title);
            tvDesc = itemView.findViewById(R.id.deal_tv_description);
            tvPrice = itemView.findViewById(R.id.deal_tv_price);
            imageDeal = itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvDesc.setText(deal.getDescription());
            tvPrice.setText("$" + deal.getPrice());
            showImage(deal.getImageUrl(), imageDeal);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.i("TravelDealAdapter", "Clicked position: "+ position);

            TravelDeal d = deals.get(position);
            Intent i = new Intent(view.getContext(), DealActivity.class);
            i.putExtra("deal", d);
            view.getContext().startActivity(i);
        }

        private void showImage(String url, ImageView imageView){
            if (url != null && url.isEmpty() == false) {
                Picasso.get()
                        .load(url).
                        resize(480, 180)
                        .centerCrop()
                        .into(imageView);
            }
        }
    }
}
