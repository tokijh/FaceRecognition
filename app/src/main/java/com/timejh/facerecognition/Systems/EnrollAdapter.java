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
public class EnrollAdapter extends BaseAdapter {
    Context context;
    private ArrayList<Bitmap> bitmaps;
    private ArrayList<String> bitmapsname;
    LayoutInflater inflater;
    ImageView imageView;
    public EnrollAdapter(Context context,ArrayList<Bitmap> bitmaps,ArrayList<String> bitmapsname) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.bitmaps = bitmaps;
        this.bitmapsname = bitmapsname;
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
            view = inflater.inflate(R.layout.listview_imgs_enroll, parent, false);
        } else {
            view = convertView;
        }
        LinearLayout layout = (LinearLayout) view;

        imageView = (ImageView)layout.findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmaps.get(position));

        Log.i(this.getClass().getName(), "View의 주소값:" + view);

        return layout;
    }
}
