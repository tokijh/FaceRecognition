package com.timejh.facerecognition;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends Activity  implements CvCameraViewListener2, OnTouchListener {


    private static final Scalar FACE_RECT_COLOR_GREEN     = new Scalar(0, 255, 0, 255);
    private static final Scalar FACE_RECT_COLOR_RED     = new Scalar(255, 0, 0, 255);
    private static final Scalar FACE_RECT_COLOR_BLUE     = new Scalar(0, 0, 255, 255);
    private static final Scalar FACE_RECT_COLOR_YELLOW     = new Scalar(255, 255, 0, 255);
    private static final Scalar FACE_RECT_COLOR_WHITE     = new Scalar(255, 255, 255, 255);

    public static final boolean TRAINING= true;
    public static final boolean IDLE= false;

    private static final int frontCam =1;
    private static final int backCam =2;

    private boolean faceState=IDLE;
    private boolean scanable=true;

    private MenuItem               mBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mCatalog;
    private MenuItem               mChangeType;
    int type=0;

    private Mat                     mRgba;
    private Mat                     mGray;
    private File                    mCascadeFile;
    private CascadeClassifier       mJavaDetector;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely[] = new int[10];

    String mPath="";

    private CameraView cameraView;
    private PersonRecognition fr;

    static final long MAXIMG = 10;
    int countImages=0;

    FrameLayout layout;
    public ProgressBar progressBar;
    TextView tvarr[] = new TextView[10];
    public Bitmap mBitmap;
    Bitmap[] t_bpm = new Bitmap[(int)MAXIMG];
    Mat[] t_mmat = new Mat[(int)MAXIMG];

    boolean firsttime = true;

    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    public ArrayList<String> persons;
    public ArrayList<Integer> persons_count;
    ArrayList<Rect> findedface;
    Rect enrollface;
    EnrollList enrollList;
    EnrollData enrollData;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    fr=new PersonRecognition(MainActivity.this,mPath);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                    fr.train();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    try {
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    cameraView.enableView();
                    cameraView.setOnTouchListener(MainActivity.this);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("DATAS", MODE_PRIVATE);
        editor = prefs.edit();

        layout = (FrameLayout)findViewById(R.id.layout);
        progressBar = (ProgressBar)findViewById(R.id.progressBarExample);

        cameraView = (CameraView) findViewById(R.id.tutorial3_activity_java_surface_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);
        cameraView.setFocusable(true);

        mPath=getExternalCacheDir()+"/facesData/";

        for(int i=0;i<10;i++) {
            mLikely[i] = -1;
            tvarr[i] = new TextView(this);
            tvarr[i].setText("");
            tvarr[i].setTextSize(30);
            layout.addView(tvarr[i]);
        }

        findedface = new ArrayList<>();
        persons = new ArrayList<>();
        persons_count = new ArrayList<>();
        getpersons();

        editor.putBoolean("EnrollData",true);
        editor.commit();

        make(mPath, "PeopleInfo.txt");

        boolean success=(new File(mPath)).mkdirs();
        if (!success)
            Log.e("Error","Error creating directory");
    }
    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getpersons();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        cameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @SuppressLint("NewApi") public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


        Rect[] facesArray = faces.toArray();
        String face_name = "Unknow";//얼굴이름 가져온다.

        if (faceState) {//TRAIN
            getpersons();
            editor.putBoolean("EnEnrollData", false);
            editor.commit();
            Mat m = new Mat();

            m=mRgba.submat(enrollface);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(m, mBitmap);

            if(countImages<MAXIMG){
                t_mmat[countImages]=m;
                t_bpm[countImages]=mBitmap;
                countImages++;
            }
            if(prefs.getBoolean("EnrollData",true)) {
                editor.putBoolean("EnrollData", false);
                editor.commit();
            }
            if(countImages==MAXIMG){
                faceState = IDLE;
                scanable = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<10;i++) {
                        tvarr[i].setText("");
                    }
                }
            });
        }
        else{//IDLE
            if(scanable) {
                if(firsttime){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                    fr.train();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    firsttime = false;
                }
                else if(prefs.getBoolean("EnEnrollData",false)){
                    //SaveIMG[] thread = new SaveIMG[10];  // 작업스레드 생성
                    persons.add(prefs.getString("EnrollData_NAME", "NULL"));
                    persons_count.add(0);
                    Log.d("PERSONS:", persons.toString());
                    Log.d("PERSONSCOUNT:", persons_count.toString());
                    putpersons();
                    for(int i=0;i<MAXIMG;i++){
                        saveImgs(prefs.getString("EnrollData_NAME", "NULL"),t_bpm[i]);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                    fr.train();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    editor.putBoolean("EnEnrollData",false);
                    editor.commit();
                }
                else if(!prefs.getBoolean("EnrollData",true)){
                    enrollData = new EnrollData(MainActivity.this, mPath, persons);
                    enrollData.show(getFragmentManager(), "enrolldata");
                    editor.putBoolean("EnrollData", true);
                    editor.commit();
                }
                else if(prefs.getBoolean("EnrollList",false)){
                    enrollList = new EnrollList(this, mPath, persons, prefs.getString("Enrollname","NULL"),prefs.getInt("Enrolla", 0));
                    enrollList.show(getFragmentManager(), "enrolldata");
                    editor.putBoolean("EnrollList", false);
                    editor.commit();
                }
                else if(facesArray.length!=0) {
                    if(persons.size()>1) {
                        Mat[] m = new Mat[facesArray.length];
                        for(int i=0;i<facesArray.length;i++){
                            m[i] = mGray.submat(facesArray[0]);
                        }
                        face_name = (fr.predict(m,facesArray.length)).substring(1);
                        mLikely = fr.getProb();
                        Log.d("<<>>FaceName:", face_name);
                        final String sp[] = face_name.split(",");
                        final Rect f[] = facesArray;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for(int i=0;i<10;i++) {
                                    if(i<sp.length) {
                                        String endname = sp[i].substring(1,sp[i].length()-1);
                                        while(true){
                                            char a[] = endname.toCharArray();
                                            switch (a[a.length-1]){
                                                case '0':
                                                case '1':
                                                case '2':
                                                case '3':
                                                case '4':
                                                case '5':
                                                case '6':
                                                case '7':
                                                case '8':
                                                case '9': endname = new String(a,0,a.length-1);
                                                    continue;
                                            }
                                            break;
                                        }
                                        String nn = "";
                                        char a[] = endname.toCharArray();
                                        for(int j=0;j<endname.length();j++){
                                            Log.d("NAME<>",a[j]+"");
                                            if(a[j]!='0'&&a[j]!='1'&&a[j]!='2'&&a[j]!='3'&&a[j]!='4'&&a[j]!='5'&&a[j]!='6'&&a[j]!='7'&&a[j]!='8'&&a[j]!='9'){
                                                nn = nn + a[j];
                                            }
                                        }
                                        tvarr[i].setText(nn);
                                        tvarr[i].setX((float) f[i].getX());
                                        tvarr[i].setY((float) f[i].getY() - 80);
                                        if (mLikely[i]<50)
                                            tvarr[i].setTextColor(Color.GREEN);
                                        else if (mLikely[i]<80)
                                            tvarr[i].setTextColor(Color.YELLOW);
                                        else
                                            tvarr[i].setTextColor(Color.RED);
                                    }
                                    else {
                                        tvarr[i].setText("");
                                    }
                                }
                            }
                        });
                    }
                    findedface.clear();
                    for (int i = 0; i < facesArray.length; i++) {
                        findedface.add(facesArray[i]);
                        Log.d("<<>>FaceNAMES:",tvarr[i].getText().toString()+" "+mLikely[i]);
                        if(mLikely[i]==-1)
                            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR_WHITE, 3);
                        else if (mLikely[i]<50)
                            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR_GREEN, 3);
                        else if (mLikely[i]<80)
                            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR_YELLOW, 3);
                        else
                            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR_RED, 3);
                    }
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i=0;i<10;i++) {
                                tvarr[i].setText("");
                            }
                        }
                    });
                }
            }
        }

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (cameraView.numberCameras()>1) {
            mBackCam = menu.add("셀카모드");
            mFrontCam = menu.add("카메라모드");
            mCatalog = menu.add("사진보기");
            mChangeType = menu.add("타입 바꾸기");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        mCatalog.setChecked(false);
        mChangeType.setChecked(false);
        //  mEigen.setChecked(false);
        if (item == mBackCam) {
            cameraView.setCamFront();
        }
        //fr.changeRecognizer(0);
        else if (item==mFrontCam) {
            cameraView.setCamBack();
        }
        else if (item==mCatalog) {
            Intent i = new Intent(this,Catalog.class);
            startActivity(i);
        }
        else if(item==mChangeType){
            type++;
            if(type%3==0)
                type=0;
            fr.changeRecognizer(type);
            Toast.makeText(MainActivity.this,""+type+"모드",Toast.LENGTH_SHORT).show();
        }
        item.setChecked(true);
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scanable = false;

        double cols = mRgba.cols();
        double rows = mRgba.rows();

        double xOffset = (cameraView.getWidth() - cols) / 2;
        double yOffset = (cameraView.getHeight() - rows) / 2;

        double x = (double)(event).getX() - xOffset;
        double y = (double)(event).getY() - yOffset;
        Log.d("Touch:", x + " " + y);
        for (int i = 0; i < findedface.size(); i++) {
            if ((findedface.get(i).getY() < y) && (y < findedface.get(i).getendY())) {
                if ((findedface.get(i).getX() < x) && (x < findedface.get(i).getendX())) {
                    enrollface = findedface.get(i);
                    countImages = 0;
                    faceState = TRAINING;
                    Log.d("Enroll:", enrollface.getX() + "X" + enrollface.getendX() + " " + enrollface.getY() + "Y" + enrollface.getendY());
                    return false;
                }
            }
        }
        cameraView.focusOnTouch(event);
        scanable = true;
        return false;
    }

    void make(String Path,String Filename){
        String dirPath = Path;
        File file = new File(dirPath);

        // 일치하는 폴더가 없으면 생성
        if( !file.exists() ) {
            file.mkdirs();
            //Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        // txt 파일 생성
        String testStr = "";
        File savefile = new File(dirPath+Filename);
        if(savefile.isFile()){
            return;
        }
        else {
            try {
                FileOutputStream fos = new FileOutputStream(savefile);
                //fos.write(testStr.getBytes());
                fos.close();
                //Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
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

    static  final int WIDTH= 128;
    static  final int HEIGHT= 128;

    public void saveImgs( String description,Bitmap bmp){

        bmp= Bitmap.createScaledBitmap(bmp, WIDTH, HEIGHT, false);

        FileOutputStream f;
        boolean success=(new File(mPath+description.substring(0,description.length()-1)+"/")).mkdirs();
        try {
            f = new FileOutputStream(mPath+description.substring(0,description.length()-1)+"/"+description+"-"+persons_count.get(persons.indexOf(description))+".jpg",true);
            persons_count.set(persons.indexOf(description),persons_count.get(persons.indexOf(description))+1);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, f);
            f.close();
            putpersons();
        } catch (Exception e) {
            Log.e("error", e.getCause() + " " + e.getMessage());
            e.printStackTrace();

        }
    }

    public void dissmissEnrollList(){
        if(enrollList!=null)
            enrollList.dismiss();
    }
    public void dissmissEnrollData(){
        if(enrollData!=null)
            enrollData.dismiss();
    }
}
