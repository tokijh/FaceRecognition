package com.timejh.facerecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.timejh.facerecognition.Systems.CatalogAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tokijh on 16. 2. 18..
 */
public class Catalog extends Activity {
    String mPath="";

    ListView listView;
    CatalogAdapter catalogAdapter;
    ArrayList<String> name;
    ArrayList<String> info;
    ArrayList<Bitmap> bitmaps;
    ArrayList<String> persons;
    ArrayList<Integer> persons_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_view);
        listView = (ListView)findViewById(R.id.listView);
        name = new ArrayList<>();
        info = new ArrayList<>();
        bitmaps = new ArrayList<>();
        persons = new ArrayList<>();
        persons_count = new ArrayList<>();

        mPath=getExternalCacheDir()+"/facesData/";

        getpersons();
        addbits();
        catalogAdapter = new CatalogAdapter(this,bitmaps,name,info);

        listView.setAdapter(catalogAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialogSelectOption(name.get(position));
            }
        });
    }
    private void DialogSelectOption(final String names) {
        AlertDialog.Builder ab = new AlertDialog.Builder(Catalog.this);
        ab.setTitle("Title");
        ab.setMessage("정말로 삭제하시겠습니까?");
        ab.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                        Log.d("NAME:",names);
                        deleteDir(mPath + names);
                        bitmaps.remove(name.indexOf(names));
                        name.remove(names);
                        for(int i=0;i<persons.size();i++){
                            if(persons.get(i).contains(names)){
                                persons.remove(i);
                                persons_count.remove(i);
                            }
                        }
                        putpersons();
                        catalogAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }
    public int deleteDir(String a_path){
        File file = new File(a_path);
        if(file.exists()){
            File[] childFileList = file.listFiles();
            for(File childFile : childFileList){
                if(childFile.isDirectory()){
                    deleteDir(childFile.getAbsolutePath());

                }
                else{
                    childFile.delete();
                }
            }
            file.delete();
            return 1;
        }else{
            return 0;
        }
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
            if((childName.endsWith("0-0.jpg"))) {
                System.out.println(childs[i]);
                String imgpath = childs[i].toString();
                Bitmap bm = BitmapFactory.decodeFile(imgpath);
                bitmaps.add(bm);
                String sp[] = childName.split("/");
                String m = sp[sp.length-1];
                Log.d("MM:", m);
                m = m.replace("0-0.jpg","");
                name.add(m);
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

                    if ((childName.toLowerCase().endsWith("0-0.jpg"))) {
                        System.out.println(childName);
                        String imgpath = childName;
                        Bitmap bm = BitmapFactory.decodeFile(imgpath);
                        bitmaps.add(bm);
                        String sp[] = childName.split("/");
                        String m = sp[sp.length-1];
                        Log.d("MM:",m);
                        m = m.replace("0-0.jpg","");
                        name.add(m);
                    }
                }
            }
        }
    }

    public void getpersons(){
        persons.clear();
        persons_count.clear();
        try {
            //if(isFile(mPath,"PeopleInfo.txt")) {
            FileInputStream file = new FileInputStream(mPath + "PeopleInfo.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(file));
            String a = "";
            while ((a = in.readLine()) != null) {
                String sp[] = a.split(",");
                persons.add(sp[0]);
                persons_count.add(Integer.parseInt(sp[1]));
            }
            //}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //fr.updateArray(persons,persons_count);
    }

    public void putpersons(){
        Log.d("PutPsersons:",persons.toString());
        File savefile = new File(mPath + "PeopleInfo.txt");
        if(!savefile.isFile()){
            return;
        }
        else {
            try {
                FileOutputStream fos = new FileOutputStream(savefile);
                for(int i=0;i<persons.size();i++) {
                    String testStr = persons.get(i)+","+persons_count.get(i)+"\n";
                    fos.write(testStr.getBytes());
                }
                fos.close();
                //Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
            }
        }
    }
}