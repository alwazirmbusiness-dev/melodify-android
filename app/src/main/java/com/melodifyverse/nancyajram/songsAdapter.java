package com.melodifyverse.nancyajram;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class songsAdapter extends RecyclerView.Adapter<songsAdapter.songsViewHolder> {

    public void updateList(List<songsItem> newList) {
        this.mData.clear();
        this.mData.addAll(newList);
        notifyDataSetChanged();
    }



    public interface OnItemClickListener {
        void onItemClick(songsItem item, int pos);
        void OnImageClick(songsItem item, ImageView v, int pos);
    }
    List<songsItem> tempmdata = new ArrayList<>();
    Context mContext;
    List<songsItem> mData ;
    List<songsItem> mDataFiltered ;
    private OnItemClickListener listener;

    private int playingPosition = -1;

    public songsAdapter(Context mContext, List<songsItem> mData, OnItemClickListener listener) {
        this.mContext = mContext;
        this.mData = mData;
        this.listener = listener;
        this.mDataFiltered = mData;
        tempmdata.addAll(mData);
    }

    @NonNull
    @Override
    public songsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_songs, parent, false);
        return new songsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull songsViewHolder holder, int position) {

        songsItem song = mData.get(position);

        // TITLE
        holder.tv_title.setText(song.getTitle());

        // USER / SONG ICON
        holder.img_user.setImageResource(song.getUserPhoto());

        // =========================
        // FAVORITE STATE
        // =========================
        if (song.getFavourite() == 2) { // Favorites Screen
            holder.img_fav.setImageResource(R.drawable.ic_baseline_close_24);
            holder.itemView.setBackgroundResource(R.drawable.item_glass_glow);
        } else if (song.getFavourite() == 1) { // Main screen - Favorited
            holder.img_fav.setImageResource(R.drawable.favorite_active);
            holder.itemView.setBackgroundResource(R.drawable.item_glass_glow);
        } else { // Main screen - Not favorited
            holder.img_fav.setImageResource(R.drawable.favorite);
            holder.itemView.setBackgroundResource(R.drawable.item_glass_bg);
        }

        // =========================
        // PLAYING EQUALIZER
        // =========================
        Drawable drawable = holder.img_equalizer.getDrawable();

        if (position == playingPosition) {
            holder.img_equalizer.setVisibility(View.VISIBLE);

            holder.img_equalizer.post(() -> {
                Drawable d = holder.img_equalizer.getDrawable();
                if (d instanceof Animatable) {
                    ((Animatable) d).start();
                }
            });

        } else {
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).stop();
            }
            holder.img_equalizer.setVisibility(View.GONE);
        }

        // =========================
        // CLICK LISTENERS (SAFE POSITION)
        // =========================
        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(song, adapterPos);
            }
        });

        holder.img_fav.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION && listener != null) {
                listener.OnImageClick(song, holder.img_fav, adapterPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataFiltered.size();
    }

    // =========================
    // CALLED FROM MAIN ACTIVITY
    // =========================
    public void setPlayingPosition(int position) {
        int oldPos = playingPosition;
        playingPosition = position;

        if (oldPos != -1) notifyItemChanged(oldPos);
        notifyItemChanged(playingPosition);
    }

    // =========================
    // VIEW HOLDER
    // =========================
    public static class songsViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title;
        ImageView img_user;
        ImageView img_fav;
        ImageView img_equalizer;

        public songsViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
            img_user = itemView.findViewById(R.id.img_user);
            img_fav = itemView.findViewById(R.id.img_fav);
            img_equalizer = itemView.findViewById(R.id.img_equalizer);
        }
    }
}

