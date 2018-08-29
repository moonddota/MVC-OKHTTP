package com.skylin.uav.drawforterrain.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import android.widget.TextView;

import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.R;

import java.util.ArrayList;

/**
 * Created by wh on 2017/4/1.
 */

public class BoundaryAdapter extends RecyclerView.Adapter<BoundaryAdapter.ViewHolder> {

    private ArrayList<Point> list;
    private Activity mActivity;
    private int indext = -1;
    private int delete;
    private boolean deleat = false;
    private boolean clickAble = true;

    public BoundaryAdapter(ArrayList<Point> list, Activity mActivity) {
        this.list = list;
        this.mActivity = mActivity;
    }


    public boolean isClickAble() {
        return clickAble;
    }

    public void setClickAble(boolean clickAble) {
        this.clickAble = clickAble;
    }

    public boolean isDeleat() {
        return deleat;
    }

    public void setDeleat(boolean deleat) {
        this.deleat = deleat;
    }

    public int isDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }

    public int getIndext() {
        return indext;
    }

    public void setIndext(int indext) {
        this.indext = indext;
    }

    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onItemClicked(View view, int position);
    }

    //设置点击回调接口
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private LongClickListener longClickListener;

    public interface LongClickListener {
        void onLongClicked(View view, int position);
    }

    //设置长按点击回调接口
    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.boundary_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.boundary_item_number.setText((position+1) + "");
        if (clickAble) {
            holder.boundary_item_number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClicked(v, position);
                }
            });
            holder.boundary_item_number.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null) {
                        longClickListener.onLongClicked(v, position);
                    }
                    return true;
                }
            });
            holder.boundary_item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClicked(v, position);
                }
            });
        }
        if (deleat) {
            holder.boundary_item_delete.setVisibility(View.VISIBLE);
            if (position == delete) {
                holder.boundary_item_number.setBackgroundResource(R.drawable.editsharp6);
                holder.boundary_item_number.setTextColor(mActivity.getResources().getColor(R.color.white));
            } else {
                holder.boundary_item_number.setBackgroundResource(R.drawable.editsharp7);
                holder.boundary_item_number.setTextColor(mActivity.getResources().getColor(R.color.text_gray));
            }
        } else {
            holder.boundary_item_delete.setVisibility(View.INVISIBLE);
            if (position == indext) {
                holder.boundary_item_number.setBackgroundResource(R.drawable.editsharp6);
                holder.boundary_item_number.setTextColor(mActivity.getResources().getColor(R.color.white));
            } else {
                holder.boundary_item_number.setBackgroundResource(R.drawable.editsharp7);
                holder.boundary_item_number.setTextColor(mActivity.getResources().getColor(R.color.text_gray));
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size() == 0 ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView boundary_item_number;
        ImageButton boundary_item_delete;
        public ViewHolder(View itemView) {
            super(itemView);
            boundary_item_number = (TextView) itemView.findViewById(R.id.boundary_item_number);
            boundary_item_delete = (ImageButton) itemView.findViewById(R.id.boundary_item_delete);
        }
    }
}
