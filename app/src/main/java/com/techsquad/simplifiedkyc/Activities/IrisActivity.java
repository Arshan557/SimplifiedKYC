package com.techsquad.simplifiedkyc.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.techsquad.simplifiedkyc.CountDownTimerWithPause;
import com.techsquad.simplifiedkyc.OpenCameraView;
import com.techsquad.simplifiedkyc.R;
import com.techsquad.simplifiedkyc.Utils.Constants;
import com.techsquad.simplifiedkyc.Utils.FolderUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class IrisActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "IrisActivity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
    public static final int JAVA_DETECTOR = 0;
    private static final int TM_SQDIFF = 0;
    private static final int TM_SQDIFF_NORMED = 1;
    private static final int TM_CCOEFF = 2;
    private static final int TM_CCOEFF_NORMED = 3;
    private static final int TM_CCORR = 4;
    private static final int TM_CCORR_NORMED = 5;


    private int learn_frames = 0;
    private Mat teplateR;
    private Mat teplateL;
    int method = 0;

    // matrix for zooming
    private Mat mZoomWindow;
    private Mat mZoomWindow2;

    private MenuItem mItemFace50;
    private MenuItem mItemFace40;
    private MenuItem mItemFace30;
    private MenuItem mItemFace20;

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private File mCascadeFileEye;
    private CascadeClassifier mJavaDetector;
    private CascadeClassifier mJavaDetectorEye;


    private int mDetectorType;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private CameraBridgeViewBase mOpenCvCameraView;
    private OpenCameraView cameraBridgeViewBase;
    private SeekBar mMethodSeekbar;
    private TextView mValue;

    private CountDownTimerWithPause countDownTimer;
    private TextView showTime;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis;

    MediaPlayer toneMP;
    private boolean isFinished;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // load cascade file from application resources
                        InputStream ise = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEye = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileEye = new File(cascadeDirEye, "haarcascade_lefteye_2splits.xml");
                        FileOutputStream ose = new FileOutputStream(mCascadeFileEye);

                        while ((bytesRead = ise.read(buffer)) != -1) {
                            ose.write(buffer, 0, bytesRead);
                        }
                        ise.close();
                        ose.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            mJavaDetector = null;
                        }

                        mJavaDetectorEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                        if (mJavaDetectorEye.empty()) {
                            mJavaDetectorEye = null;
                        }

                        cascadeDir.delete();
                        cascadeDirEye.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        countDownTimer.cancel();
        mTimeLeftInMillis = 0;
        isFinished = false;
    }

    private void updateTimerUI(Long milliSeconds) {
        int minutes = (int) (milliSeconds / 1000) / 60;
        int seconds = (int) (milliSeconds / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        showTime.setText(timeLeftFormatted);
    }

    private void startTimer() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (countDownTimer == null) {
                    countDownTimer = new CountDownTimerWithPause(mTimeLeftInMillis, 1000, true) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            updateTimerUI(millisUntilFinished);
                        }

                        @Override
                        public void onFinish() {
                            updateTimerUI(0L);
                           /* AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
                            alertDialog.setTitle("Light Me");
                            alertDialog.setIcon(R.drawable.iris);
                            alertDialog.setMessage("Your session has ended, you will be redirected to main page.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            System.exit(1);
                                        }
                                    });
                            alertDialog.show();*/
                            isFinished = true;
                        }
                    }.create();
                }
            }
        });
    }

    public IrisActivity() {
        mDetectorType = JAVA_DETECTOR;
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_iris);

        toneMP = MediaPlayer.create(getApplicationContext(), R.raw.tone);

        isFinished = false;

        showTime = findViewById(R.id.text_view_countdown);
        Bundle b = getIntent().getExtras();
        String secs = b.getString("minutes");

        long secsToTerminate = Long.parseLong(secs);

        secsToTerminate *= 1000;
        mTimeLeftInMillis = secsToTerminate;
        updateTimerUI(mTimeLeftInMillis);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mMethodSeekbar = findViewById(R.id.methodSeekBar);
        mValue = findViewById(R.id.method);

        mMethodSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                method = progress;
                switch (method) {
                    case 0:
                        mValue.setText("TM_SQDIFF");
                        break;
                    case 1:
                        mValue.setText("TM_SQDIFF_NORMED");
                        break;
                    case 2:
                        mValue.setText("TM_CCOEFF");
                        break;
                    case 3:
                        mValue.setText("TM_CCOEFF_NORMED");
                        break;
                    case 4:
                        mValue.setText("TM_CCORR");
                        break;
                    case 5:
                        mValue.setText("TM_CCORR_NORMED");
                        break;
                }


            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        if (mGray != null)
            mGray.release();
        if (mRgba != null)
            mRgba.release();
        if (mZoomWindow != null)
            mZoomWindow.release();
        if (mZoomWindow2 != null)
            mZoomWindow2.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

        if (mZoomWindow == null || mZoomWindow2 == null)
            CreateAuxiliaryMats();

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }

        final Rect[] facesArray = faces.toArray();


        if (facesArray.length == 0) {
            if(!isFinished) {
                if (toneMP.isPlaying()) {
                    toneMP.stop();
                    toneMP.reset();
                    toneMP.release();
                }
                toneMP = MediaPlayer.create(getApplicationContext(), R.raw.tone);
                toneMP.start();

                if (countDownTimer != null)
                    countDownTimer.pause();
            }
        } else {
            if(!isFinished) {
                if (toneMP.isPlaying())
                    toneMP.stop();

                if (countDownTimer == null)
                    startTimer();
                else if (countDownTimer != null)
                    countDownTimer.resume();
            }
        }

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 1);
            Rect r = facesArray[i];

            // compute the eye area
            Rect eyearea = new Rect(r.x + r.width / 8,
                    (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
                    (int) (r.height / 3.0));

            // split it
            Rect eyearea_right = new Rect(r.x + r.width / 16,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

            Rect eyearea_left = new Rect(r.x + r.width / 16
                    + (r.width - 2 * r.width / 16) / 2,
                    (int) (r.y + (r.height / 4.5)),
                    (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));

            if (learn_frames < 10) {
                teplateR = get_template(mJavaDetectorEye, eyearea_right, 24);
                teplateL = get_template(mJavaDetectorEye, eyearea_left, 24);
                learn_frames++;
            } else if (learn_frames == 10) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //Toast.makeText(getApplicationContext(), "I am ready.", Toast.LENGTH_SHORT).show();

                        cameraBridgeViewBase = (OpenCameraView) findViewById(R.id.fd_activity_surface_view);
                        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
                        cameraBridgeViewBase.setCvCameraViewListener(IrisActivity.this);
                        cameraBridgeViewBase.disableFpsMeter();
                        String userName = "";
                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                        if (null != sharedPreferences) {
                            userName = sharedPreferences.getString("fname", "");
                        }
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                        String fileName = userName + sdf.format(new Date()) + ".jpg";
                        String outPicture = Constants.SCAN_IMAGE_LOCATION + File.separator + fileName;
                        FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("fileName", fileName);
                        editor.commit();

                        cameraBridgeViewBase.takePicture(outPicture);
                        //Toast.makeText(IrisActivity.this, "Picture has been taken ", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Path " + outPicture);

                        /*Toast.makeText(getApplicationContext(), "Your IRIS has been captured successfully", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(IrisActivity.this, HomeActivity.class);
                        intent.putExtra("irisFinish", "yes");
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();*/
                    }
                });
                learn_frames++;
            } else {
                match_eye(eyearea_right, teplateR, method);
                match_eye(eyearea_left, teplateL, method);
            }
        }
        Mat mRgbaT = mRgba.t();
        Core.flip(mRgba.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void CreateAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null) {
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
                    + cols / 10, cols);
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
                    + cols / 10, cols);
        }

    }

    private void match_eye(Rect area, Mat mTemplate, int type) {
        try {
            Point matchLoc;
            Mat mROI = mGray.submat(area);
            int result_cols = mROI.cols() - mTemplate.cols() + 1;
            int result_rows = mROI.rows() - mTemplate.rows() + 1;

            // Check for bad template size
            if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
                return;
            }
            Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);
            Log.d("fdActivity_mResult: ",""+mResult.toString());

            switch (type) {
                case TM_SQDIFF:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
                    break;
                case TM_SQDIFF_NORMED:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult,
                            Imgproc.TM_SQDIFF_NORMED);
                    break;
                case TM_CCOEFF:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
                    break;
                case TM_CCOEFF_NORMED:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult,
                            Imgproc.TM_CCOEFF_NORMED);
                    break;
                case TM_CCORR:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
                    break;
                case TM_CCORR_NORMED:
                    Imgproc.matchTemplate(mROI, mTemplate, mResult,
                            Imgproc.TM_CCORR_NORMED);
                    break;
            }

            Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
            // there is difference in matching methods - best match is max/min value
            if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
                matchLoc = mmres.minLoc;
            } else {
                matchLoc = mmres.maxLoc;
            }

            Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
            Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x, matchLoc.y + mTemplate.rows() + area.y);
            Imgproc.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 255, 0, 255));
            Rect rec = new Rect(matchLoc_tx, matchLoc_ty);

        } catch (Exception exc) {

        }
    }


    private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                        | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30),
                new Size());

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length; ) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int) e.tl().x,
                    (int) (e.tl().y + e.height * 0.4), (int) e.width,
                    (int) (e.height * 0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);


            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 1);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int) iris.x - size / 2, (int) iris.y
                    - size / 2, size, size);
            Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(),
                    new Scalar(255, 0, 0, 255), 2);
            template = (mGray.submat(eye_template)).clone();
            return template;
        }
        return template;
    }

    public void onRecreateClick(View v) {
        learn_frames = 0;
    }

}
