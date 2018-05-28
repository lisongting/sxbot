package com.droid.sxbot.mvp.user.register;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.droid.sxbot.AnimateDialog;
import com.droid.sxbot.R;
import com.droid.sxbot.util.ImageUtils;
import com.droid.sxbot.util.Util;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static android.view.View.GONE;

/**
 * Created by lisongting on 2017/12/14.
 */

public class CameraActivity extends AppCompatActivity implements RegisterContract.View,View.OnTouchListener{

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private ImageView ivShow,btCapture,btBack,btSwitchCam,btRotateLeft,btRotateRight;
    private CameraManager mCameraManager;//摄像头管理器
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Button btOk,btRecapture,btHome;
    private LinearLayout llBtHolder,llRotateLayout;

    public static final String TAG = "CameraActivity";
    private Bitmap faceBitmap;
//    private SoftReference<Bitmap> bitmapSoftReference ;
    private Size mPreviewSize;
    private CaptureRequest.Builder requestBuilder;
    private RegisterContract.Presenter presenter;
    private boolean isPreviewing = true;
    private int cameraFacingMode;
    private DisplayMetrics metrics;
    private AnimateDialog dialog;
    private FragmentManager fragmentManager;
    private int ivShowRotateAngle = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_head_photo);

        llBtHolder = (LinearLayout) findViewById(R.id.bottom_linear_layout);
        llRotateLayout = (LinearLayout) findViewById(R.id.bottom_rotate_layout);
        btOk = (Button) findViewById(R.id.id_bt_ok);
        btRecapture = (Button) findViewById(R.id.id_bt_again);
        btHome = (Button) findViewById(R.id.id_bt_home);
        ivShow = (ImageView) findViewById(R.id.id_iv_show_picture);
        btBack = findViewById(R.id.bt_back);
        mTextureView = (TextureView) findViewById(R.id.id_texture_view);
        btCapture = (ImageView)findViewById(R.id.capture);
        btSwitchCam = (ImageView)findViewById(R.id.bt_switch);
        btRotateLeft = (ImageView)findViewById(R.id.id_bt_rotate_left);
        btRotateRight = (ImageView)findViewById(R.id.id_bt_rotate_right);

        //LENS_FACING_FRONT 后置摄像头， LENS_FACING_BACK  前置摄像头
        cameraFacingMode = CameraCharacteristics.LENS_FACING_BACK;

        initView();

        initCamera();

        initOnClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentManager = getSupportFragmentManager();
        presenter = new RegisterPresenter(this);
    }

    public void initView() {
        View status_bar = findViewById(R.id.status_bar_view);
        ViewGroup.LayoutParams params = status_bar.getLayoutParams();
        params.height = Util.getStatusBarHeight(this);
        status_bar.setLayoutParams(params);
    }

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void initOnClickListener() {
        //为SurfaceView设置监听器
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                log("TextureView.SurfaceTextureListener -- onSurfaceTextureAvailable()");
                mSurfaceTexture = surface;
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    takePreview();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                log( "TextureView.SurfaceTextureListener -- onSurfaceTextureSizeChanged()");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                log( "TextureView.SurfaceTextureListener -- onSurfaceTextureDestroyed()");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                
            }
        });
        mTextureView.setOnTouchListener(this);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = getIntent().getStringExtra("userName");
//                Bitmap tmp = bitmapSoftReference.get();
                if (faceBitmap == null) {
                    log("faceBitmap is null");
                    return;
                }
                Toast.makeText(CameraActivity.this, "正在注册，请稍候...", Toast.LENGTH_LONG).show();
                Matrix matrix = new Matrix();
                matrix.postRotate(ivShowRotateAngle);
                //实际发给服务器是旋转过后图像
                Bitmap target = Bitmap.createBitmap(faceBitmap, 0, 0,
                        faceBitmap.getWidth(), faceBitmap.getHeight(), matrix, true);

                log("base64:" + ImageUtils.encodeBitmapToBase64(target, Bitmap.CompressFormat.JPEG, 100));
                presenter.register(Util.makeUserNameToHex(userName),
                        ImageUtils.encodeBitmapToBase64(target, Bitmap.CompressFormat.JPEG, 100));
            }
        });

        btRecapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reCapture();
                isPreviewing = true;
            }
        });

        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        
        btCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
                isPreviewing = false;
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPreviewing) {
                    return;
                }
                if (mCameraDevice != null) {
                    mCameraDevice.close();
                }

                if (mCameraCaptureSession != null) {
                    mCameraCaptureSession.close();
                }

                if (cameraFacingMode == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraFacingMode =  CameraCharacteristics.LENS_FACING_FRONT;
                } else {
                    cameraFacingMode =  CameraCharacteristics.LENS_FACING_BACK;
                }
                if (mTextureView.isAvailable()) {
                    initCamera();
                }
            }
        });

        View.OnClickListener rotateBtListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rotateAngle =0;
                switch (v.getId()) {
                    case R.id.id_bt_rotate_left:
                        rotateAngle = -90;
                        break;
                    case R.id.id_bt_rotate_right:
                        rotateAngle = 90;
                        break;
                    default:break;
                }
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivShow, "rotation",
                        ivShowRotateAngle, ivShowRotateAngle+rotateAngle);
                objectAnimator.setDuration(500);
                objectAnimator.setInterpolator(new LinearInterpolator());
                objectAnimator.start();
                ivShowRotateAngle += rotateAngle;

            }
        };
        btRotateLeft.setOnClickListener(rotateBtListener);
        btRotateRight.setOnClickListener(rotateBtListener);

    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            log( "CameraDevice.StateCallback -- onOpened()");
            mCameraDevice = camera;
            //在切换前后摄像头时，会触发onOpen方法，如果这里textureview可用，则开启预览
            if (mTextureView.isAvailable()) {
                takePreview();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            log( "CameraDevice.StateCallback -- onDisconnected()");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            log( "CameraDevice.StateCallback -- onError()");
        }
    };

    //初始化摄像头
    private void initCamera() {
        log("initCamera()");
        metrics = Util.getScreenInfo(this);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        log( "DisplayMetrics width:" + width + ",height:" + height);
        mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
        log( "ImageReader width:" + mImageReader.getWidth() + ",height:" + mImageReader.getHeight());

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mTextureView.setVisibility(GONE);
                btCapture.setVisibility(GONE);
                ivShow.setVisibility(View.VISIBLE);
                llRotateLayout.setVisibility(View.VISIBLE);
                llBtHolder.setVisibility(View.VISIBLE);

                Image image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap tmpBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                if (tmpBitmap != null) {
                    log("Bitmap info: [width:"+tmpBitmap.getWidth() +",height:"+ tmpBitmap.getHeight()+"]");

                    //调节bitmap的尺寸大小
                    int width = tmpBitmap.getWidth();
                    int height = tmpBitmap.getHeight();
                    Matrix smallMatrix = new Matrix();
                    Matrix matrix1 = new Matrix();

                    //如果是前置摄像头，则拍出来的照片要进行镜像水平翻转
                    if (cameraFacingMode == CameraCharacteristics.LENS_FACING_BACK) {
                        if (width >= 1500 || height >= 1500) {
                            smallMatrix.postScale(-0.3F, 0.3F);
                        } else {
                            smallMatrix.postScale(-0.5F, 0.5F);
                        }

                        matrix1.postScale(-1, 1);
                    } else {
                        if (width >= 1500 || height >= 1500) {
                            smallMatrix.postScale(0.3F, 0.3F);
                        } else {
                            smallMatrix.postScale(0.5F, 0.5F);
                        }
                        matrix1.postScale(1, 1);
                    }
                    //将图片缩小以发送给人脸识别服务端
//                    bitmapSoftReference = new SoftReference<Bitmap>(
//                            Bitmap.createBitmap(tmpBitmap, 0, 0, width, height, smallMatrix, true));
                    faceBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, width, height, smallMatrix, true);
                    WeakReference<Bitmap> bitmapWeakReference =
                            new WeakReference<Bitmap>(
                                    Bitmap.createBitmap(tmpBitmap, 0, 0, width, height, matrix1, true));
                    //在ImageView中展示原图
                    ivShow.setImageBitmap(bitmapWeakReference.get());

                    image.close();
                }
            }
        },null);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        openCamera();
    }

    @TargetApi(23)
    public void openCamera() {
        log("openCamera:" + cameraFacingMode);
        mCameraID = ""+ cameraFacingMode;
        try{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                //打开摄像头
                mCameraManager.openCamera(mCameraID,stateCallback,null);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED) {
                try {
                    mCameraManager.openCamera(mCameraID,stateCallback,null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                    Toast.makeText(this, "无法获取打开摄像头权限，请到手机设置中进行授权", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "无法获取打开摄像头权限,请进行授权" , Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void takePreview() {
        log("takePreview()");
        try{
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraID);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            int width = mTextureView.getWidth();
            int height = mTextureView.getHeight();
            //获得最合适的预览尺寸
            mPreviewSize = Util.getPreferredPreviewSize(configMap.getOutputSizes(ImageFormat.JPEG), width, height);
//            mPreviewSize = Util.getPreferredPreviewSize(configMap.getOutputSizes(SurfaceTexture.class), width, height);
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            log("mPreviewSize:" + mPreviewSize.getWidth() + "x" + mPreviewSize.getHeight());

            final Surface surface = new Surface(mSurfaceTexture);
            //创建预览需要的CaptureRequest.Builder
            requestBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            if (surface.isValid()) {
                requestBuilder.addTarget(surface);
            }
            log( "mTextureView info:" + mTextureView.getWidth() + "x" + mTextureView.getHeight());

            //创建CameraSession,该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(surface,mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    mCameraCaptureSession = session;

                    //设置自动对焦点
                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                    //打开自动曝光
//                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                    //显示预览
                    CaptureRequest previewRequest = requestBuilder.build();

                    try {
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                   log("onConfigureFailed");

                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //拍照
    private void takePicture() {
        if (mCameraDevice == null) {
            return;
        }
        log("takePicture()");
        //创建Request.Builder()
        final CaptureRequest.Builder requestBuilder ;
        try{
            requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            //将ImageReader的surface作为CaptureRequest.Builder的目标
            requestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
//            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            if (cameraFacingMode == CameraCharacteristics.LENS_FACING_BACK) {
                //如果是后置摄像头
                requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);
            } else {
                requestBuilder.set(CaptureRequest.JPEG_ORIENTATION, 90);
            }

            //拍照
            CaptureRequest mCaptureRequest = requestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void reCapture() {
        ivShow.setVisibility(GONE);
        llBtHolder.setVisibility(GONE);
        llRotateLayout.setVisibility(GONE);
        mTextureView.setVisibility(View.VISIBLE);
        btCapture.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("CameraActivity--onDestroy--");
//        if (bitmapSoftReference != null) {
//            bitmapSoftReference.clear();
//        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        if (mCameraCaptureSession != null) {
            try {
                mCameraCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCameraCaptureSession.close();
        }
        mImageReader.getSurface().release();
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    @Override
    public void showInfo(String s) {
        if (dialog != null) {
            fragmentManager.beginTransaction().remove(dialog).commit();
        }
        dialog = new AnimateDialog();
        dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                s, false,
                new AnimateDialog.OnButtonClickListener() {
                    @Override
                    public void onCancel() {}
                    @Override
                    public void onConfirm() {
                        dialog.dismiss();
                    }
                });
        dialog.show(fragmentManager, "dialog");
    }

    @Override
    public void showSuccess() {
//        Toast.makeText(CameraActivity.this, "注册成功", Toast.LENGTH_LONG).show();
        if (dialog != null) {
            fragmentManager.beginTransaction().remove(dialog).commit();
        }
        dialog = new AnimateDialog();
        dialog.setModeAndContent(AnimateDialog.DIALOG_STYLE_SHOW_CONTENT,
                "注册成功", false,
                new AnimateDialog.OnButtonClickListener() {
                    @Override
                    public void onCancel() {}
                    @Override
                    public void onConfirm() {
                        dialog.dismiss();
                    }
                });
        dialog.show(fragmentManager, "dialog");
        btOk.setVisibility(GONE);
        btRecapture.setVisibility(GONE);
        btHome.setVisibility(View.VISIBLE);
    }

    private void log(String s) {
        Log.i(TAG, s);
    }

    private void focus(int x, int y) {
        int squareWith = 10;
        int left = x - squareWith <= 0 ? 0 : x - squareWith;
        int right = x + squareWith > metrics.widthPixels ? metrics.widthPixels : x + squareWith;
        int top = y - squareWith <= 0 ? 0 : y - squareWith;
        int bot = y + squareWith >= metrics.heightPixels ? metrics.heightPixels : y + squareWith;
        Rect rect = new Rect(left, top, right,bot);
        Toast.makeText(this, "正在自动聚焦", Toast.LENGTH_SHORT).show();

        requestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        requestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        requestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);

        CaptureRequest request = requestBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    log("CameraCaptureSession.CaptureCallback -- onCaptureStarted()");
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    log("CameraCaptureSession.CaptureCallback -- onCaptureCompleted()");
                    super.onCaptureCompleted(session, request, result);
                    requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                    requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    request = requestBuilder.build();
                    try {
                        mCameraCaptureSession.setRepeatingRequest(request, null, null);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "setRepeatingRequest failed, errMsg: " + e.getMessage());
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "setRepeatingRequest failed, " + e.getMessage());
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            focus((int)event.getX(), (int)event.getY());
        }
        return false;
    }
}
