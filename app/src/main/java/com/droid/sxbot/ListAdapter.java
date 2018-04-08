package com.droid.sxbot;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droid.sxbot.entity.Indicator;

import java.util.List;

/**
 * Created by lisongting on 2018/4/7.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<Indicator> fileList;
    private LayoutInflater inflater;

    ListAdapter.OnClickListener onClickListener;
    public interface OnClickListener{
        void onClick(View view,int pos);
    }

    public ListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.indicator_list_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        String file = fileList.get(position).getFile();
        if (file== null) {
            holder.tvFile.setText("点击这里选择音频文件");
        } else {
            holder.tvFile.setText(file);
        }
        holder.tvFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(holder.tvFile,position);
                }
            }
        });
        holder.tvNumber.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return fileList==null?0:fileList.size();
    }

    public void setData(List<Indicator> list) {
        this.fileList = list;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvNumber,tvFile;
        private ImageView ivDrag;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_item_number);
            tvFile = itemView.findViewById(R.id.tv_item_file);
            ivDrag = itemView.findViewById(R.id.iv_item_drag_icon);
        }
    }

    public void setOnClickListener(ListAdapter.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
