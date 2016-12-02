package com.yxwzyyk.painting.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.yxwzyyk.painting.R;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yyk on 05/10/2016.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private Context mContext;
    private List<File> mList;

    private OnRecyclerViewItemClickListener mClickListener;

    public MainAdapter(Context context, List<File> list) {
        mContext = context;
        mList = list;
    }

    public void setClickListener(OnRecyclerViewItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_main, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mItemMainImageView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClick(v, position);
            }
        });

        holder.mItemMainImageView.setOnLongClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onLongItemTouchHelper(v, position);
            }
            return true;
        });
        Picasso.with(mContext)
                .invalidate(mList.get(position));
        Picasso.with(mContext)
                .load(mList.get(position))
                .into(holder.mItemMainImageView);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_main_imageView)
        ImageView mItemMainImageView;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
        void onLongItemTouchHelper(View v, int position);
    }
}
