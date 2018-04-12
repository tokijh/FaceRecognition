package com.timejh.facerecognition.Systems;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timejh.facerecognition.R;

import java.util.ArrayList;

/**
 * Created by tokijh on 16. 2. 13..
 */
public class CatalogAdapter extends BaseAdapter {
    Context context;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> name;
    private ArrayList<String> info;
    LayoutInflater inflater;
    ImageView imageView;
    public CatalogAdapter(Context context, ArrayList<Bitmap> bitmaps, ArrayList<String> name, ArrayList<String> info) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.bitmaps = bitmaps;
        this.name = name;
        this.info = info;
    }

    public int getCount() {
        return bitmaps.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = inflater.inflate(R.layout.listview_imgs, parent, false);
        } else {
            view = convertView;
        }
        LinearLayout layout = (LinearLayout) view;

        imageView = (ImageView)layout.findViewById(R.id.iv_images);
        TextView tv_name = (TextView)layout.findViewById(R.id.tv_name);
        TextView tv_info = (TextView)layout.findViewById(R.id.tv_info);
        tv_name.setText(name.get(position));
        //tv_info.setText(name.get(position));
        imageView.setImageBitmap(bitmaps.get(position));

        Log.i(this.getClass().getName(), "View의 주소값:" + view);

        return layout;
    }
}
