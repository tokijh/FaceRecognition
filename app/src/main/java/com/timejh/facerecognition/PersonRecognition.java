package com.timejh.facerecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import com.googlecode.javacv.cpp.opencv_contrib;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

/**
 * Created by tokijh on 16. 2. 17..
 */
public class PersonRecognition {

    FaceRecognizer faceRecognizer;

    String mPath;

    MainActivity mcontext;

    static  final int WIDTH= 128;
    static  final int HEIGHT= 128;
    private int mProb[] = new int[10];

    public PersonRecognition(MainActivity mcontext,String mPath){
        faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2,8,8,8,200);
        this.mcontext = mcontext;
        this.mPath = mPath;
    }

    void changeRecognizer(int nRec)
    {
        switch(nRec) {
            case 0: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(1,8,8,8,100);
                break;
            case 1: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer();
                break;
            case 2: faceRecognizer = com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer();
                break;
        }
        train();
    }

    public boolean train(){
        ArrayList<File> filearr = new ArrayList<>();
        File root = new File(mPath);
        File[] childs = root.listFiles(new FileFilter() {
            public boolean accept(File pathname) { return pathname.isFile(); }
        });
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

            if (childchilds != null) {
                for (int j = 0; j < childchilds.length; j++) {
                    String childName = childchilds[j].toString();

                    if ((childName.toLowerCase().endsWith(".jpg"))) {
                        filearr.add(new File(childName));
                    }
                }
            }
        }
        int counter = 0;
        Log.d("FileArr:!!",filearr.size()+"");
        Log.d("TIme:!!", "Here1");
        File[] imageFiles = new File[filearr.size()];
        for(int i=0;i<filearr.size();i++){
            imageFiles[i]=filearr.get(i);
        }

        MatVector images = new MatVector(imageFiles.length);


        int[] labels = new int[imageFiles.length];

        int label;


        IplImage img=null;
        IplImage grayImg;

        for (File image : imageFiles) {
            String p = image.getAbsolutePath();
            img = cvLoadImage(p);

            if (img==null)
                Log.e("Error","Error cVLoadImage");
            //Log.i("image",p);

            String buf = image.getPath();
            String bufsp[] = buf.split("/");
            buf = bufsp[bufsp.length-2];
            //Log.d("<<>>Buf:",buf);
            int i1=(mPath+buf+"/").length();//이름을 내가 잠시 뻄 그래서 0윤중현/ 해서 5 꼭보기
            /*
            ArrayList 에서 string으로 위치 찾고 label에다가 넣어주기
             */
            int i2=p.lastIndexOf("-");

            String description=p.substring(i1, i2);
            //Log.d("<<>>Descreiption",description);
            label = mcontext.persons.indexOf(description);

            grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);

            cvCvtColor(img, grayImg, CV_BGR2GRAY);

            images.put(counter, grayImg);

            labels[counter] = label;

            counter++;
        }
        if (counter>0)
            if(mcontext.persons.size()>1) {
                faceRecognizer.train(images, labels);
            }
        Log.d("TIme:!!","Here2");
        return true;
    }
    opencv_core.IplImage MatToIplImage(Mat m,int width,int heigth)
    {


        Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(m, bmp);
        return BitmapToIplImage(bmp,width, heigth);

    }

    opencv_core.IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

        if ((width != -1) || (height != -1)) {
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp = bmp2;
        }

        opencv_core.IplImage image = opencv_core.IplImage.create(bmp.getWidth(), bmp.getHeight(),
                IPL_DEPTH_8U, 4);

        bmp.copyPixelsToBuffer(image.getByteBuffer());

        opencv_core.IplImage grayImg = opencv_core.IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);

        cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

        return grayImg;
    }
    public String predict(Mat[] m,int max) {
        int n[] = new int[max];
        double p[] = new double[max];
        String faces="";
        for(int i=0;i<max;i++) {
            IplImage ipl = MatToIplImage(m[i], WIDTH, HEIGHT);
//		IplImage ipl = MatToIplImage(m,-1, -1);

            faceRecognizer.predict(ipl, n, p);

            if (n[i]!=-1) {
                mProb[i] = (int) p[i];
                faces = faces+","+mcontext.persons.get(n[i]);
            }
            else {
                mProb[i] = -1;
                faces = faces + "," + " Unknow";
            }
        }
        //	if ((n[0] != -1)&&(p[0]<95))
        return faces;
    }
    public int[] getProb() {
        // TODO Auto-generated method stub
        return mProb;
    }
}
