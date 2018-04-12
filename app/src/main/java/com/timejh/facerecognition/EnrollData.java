package com.timejh.facerecognition;

/**
 * Created by tokijh on 16. 2. 17..
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class EnrollData  extends DialogFragment {

    ImageView iv_img;
    EditText ed_name;
    Button bt_col,bt_cen;
    String mPath;

    MainActivity mainActivity;
    ArrayList<String> persons;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public EnrollData(MainActivity mainActivity,String mPath,ArrayList<String> persons) {
        this.mainActivity = mainActivity;
        this.mPath = mPath;
        this.persons = persons;
    }

    /*public void putMainActivty(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        prefs = getActivity().getSharedPreferences("DATAS", getActivity().MODE_PRIVATE);
        editor = prefs.edit();

        final AlertDialog.Builder mbuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mbuilder.setTitle("");
        View view = inflater.inflate(R.layout.activity_enrolldata, null);

        iv_img = (ImageView)view.findViewById(R.id.iv_img);
        ed_name = (EditText)view.findViewById(R.id.ed_name);
        bt_cen = (Button)view.findViewById(R.id.bt_cen);
        bt_col = (Button)view.findViewById(R.id.bt_col);

        iv_img.setImageBitmap(mainActivity.mBitmap);
        mbuilder.setView(view);
                // Add action buttons
        bt_col.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_name.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = ed_name.getText().toString();
                name = name.replace(" ", "");

                int a = 0;
                for (int i = 0; i < persons.size(); i++) {
                    if (persons.get(i).contains(name)) {
                        a++;
                    }
                }
                if (a > 0) {
                    editor.putBoolean("EnrollList", true);
                    editor.putString("Enrollname", name);
                    Log.d("<<a:", a + "");
                    editor.putInt("Enrolla", a);
                    editor.commit();
                } else {
                    name = a + "" + name + a + "";
                    Log.d("NAMEL:",name);

                    editor.putBoolean("EnEnrollData", true);
                    editor.putBoolean("EnrollData", true);
                    editor.putString("EnrollData_NAME", name);
                    editor.commit();
                }
                mainActivity.dissmissEnrollData();
            }
        });
        bt_cen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("EnEnrollData", false);
                editor.putBoolean("EnrollData", true);
                editor.putString("EnrollData_NAME", null);
                editor.commit();
                mainActivity.dissmissEnrollData();
            }
        });

        return mbuilder.create();
    }
}