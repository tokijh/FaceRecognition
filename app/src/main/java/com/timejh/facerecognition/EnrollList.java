package com.timejh.facerecognition;

/**
 * Created by tokijh on 16. 2. 17..
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.timejh.facerecognition.Systems.EnrollAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

public class EnrollList extends DialogFragment {

    ListView listView;
    Button btcancel;
    String mPath;

    MainActivity mainActivity;
    ArrayList<String> persons;
    ArrayList<Bitmap> bitmaps;
    ArrayList<String> bitmapsname;
    String name;
    int a;

    EnrollAdapter enrollAdapter;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public EnrollList(MainActivity mainActivity,String mPath,ArrayList<String> persons,String name, int a){
        this.mainActivity = mainActivity;
        this.mPath = mPath;
        this.persons = persons;
        this.name = name;
        this.a = a;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        prefs = getActivity().getSharedPreferences("DATAS", getActivity().MODE_PRIVATE);
        editor = prefs.edit();

        final AlertDialog.Builder mbuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mbuilder.setTitle("");
        View view = inflater.inflate(R.layout.activity_enrolllist, null);

        listView = (ListView)view.findViewById(R.id.listView);
        btcancel = (Button)view.findViewById(R.id.btcancel);

        bitmaps = new ArrayList<>();
        bitmapsname = new ArrayList<>();
        addbits();

        enrollAdapter = new EnrollAdapter(mainActivity,bitmaps,bitmapsname);

        listView.setAdapter(enrollAdapter);

        Log.d("bitmapname",bitmapsname.toString());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = bitmapsname.get(position);
                editor.putBoolean("EnEnrollData", true);
                editor.putBoolean("EnrollData", true);
                Log.d("<<>>NAME:", name);
                name = name.substring(0,name.length()-1);
                int b=0;
                for(int i=0;i<mainActivity.persons.size();i++){
                    if(mainActivity.persons.get(i).contains(name)){
                        b++;
                    }
                }
                name = name +""+b;
                Log.d("<<>>NAMEA:",name);
                editor.putString("EnrollData_NAME", name);
                editor.commit();
                mainActivity.dissmissEnrollList();
            }
        });
        btcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*mainActivity.persons_count.remove(mainActivity.persons_count.size()-1);
                mainActivity.persons_count.add(a);*/
                Log.d("NAME<<>>:",name);
                name = a + "" + name + "0";
                Log.d("NAME<<>>:",name);

                editor.putBoolean("EnEnrollData", true);
                editor.putBoolean("EnrollData", true);
                editor.putString("EnrollData_NAME", name);
                editor.commit();
                mainActivity.dissmissEnrollList();
            }
        });
        mbuilder.setView(view);

        return mbuilder.create();
    }
    void addbits(){
        File root = new File(mPath);
        //fdsafds
        /** 지정한 디렉토리 하위 파일의 갯수 **/
        File[] childs = root.listFiles(new FileFilter() {
            public boolean accept(File pathname) { return pathname.isFile(); }
        });

        //childs.length가 해당 폴더 안의 파일+하위폴더 갯수를 뜻한다.       .
        for(int i=0 ; i<childs.length ; i++) {
            String childName = childs[i].toString().toLowerCase();

            // 하위폴더와 필요없는 파일들을 제외하고 필요한 음악파일들만 출력한다.
            if((childName.endsWith(name+i+"-0.jpg"))) {
                System.out.println(childs[i]);
                String imgpath = childs[i].toString();
                Bitmap bm = BitmapFactory.decodeFile(imgpath);
                bitmaps.add(bm);
                String sp[] = childName.split("/");
                String m = sp[sp.length-1];
                Log.d("MM:",m);
                m = m.replace("-0.jpg","");
                bitmapsname.add(m);
            }
        }
        /** 지정한 디렉토리 하위 디렉토리 **/
        childs = root.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for(int i=0; i<childs.length; i++) {
            File[] childchilds = childs[i].listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isFile();
                }
            });

            // 하위폴더 안에 파일이 있다면~~
            if (childchilds != null) {
                // System.out.println(childs[i] + " : (" + childchilds.length +")");
                for (int j = 0; j < childchilds.length; j++) {
                    String childName = childchilds[j].toString();

                    if ((childName.toLowerCase().endsWith(name+j+"-0.jpg"))) {
                        System.out.println(childName);
                        String imgpath = childName;
                        Bitmap bm = BitmapFactory.decodeFile(imgpath);
                        bitmaps.add(bm);
                        String sp[] = childName.split("/");
                        String m = sp[sp.length-1];
                        Log.d("MM:",m);
                        m = m.replace("-0.jpg","");
                        bitmapsname.add(m);
                    }
                }
            }
        }
    }
}