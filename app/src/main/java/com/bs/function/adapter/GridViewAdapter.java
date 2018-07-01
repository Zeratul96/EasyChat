package com.bs.function.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bs.easy_chat.R;
import com.bs.util.MyListViewAdapter;
import com.bs.widget.MyGridView;

import java.util.List;

/**
 * Created by 13273 on 2017/11/2.
 *
 */

public class GridViewAdapter extends MyListViewAdapter{

        private List<Bitmap> list;
        private Context context;

        public GridViewAdapter(Context context, List<Bitmap> list){
            super(list==null?0:list.size());
            this.list = list;
            this.context = context;
        }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageViewHolder holder;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.picture_item, viewGroup ,false);
            holder = new ImageViewHolder((ImageView) view.findViewById(R.id.picture));
            view.setTag(holder);
        }
        else
            holder = (ImageViewHolder) view.getTag();

        if(((MyGridView)viewGroup).isOnMeasure) return view;

        Drawable drawable = new BitmapDrawable(context.getResources(), list.get(i));
        holder.imageView.setImageDrawable(drawable);

        return view;
    }

    private class ImageViewHolder{
        ImageView imageView;

        ImageViewHolder(ImageView iv){imageView = iv;}
    }
}
