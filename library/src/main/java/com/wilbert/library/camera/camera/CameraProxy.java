
package com.wilbert.library.camera.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CameraProxy {

    private static final String TAG = "CameraProxy";

    private boolean isDebug = true;

    private Context mContext;

    private int mCameraId;

    private Camera mCamera;

    private boolean isCameraOpen = false;

    private boolean mCameraOpenFailed = false;

    private SurfaceTexture mSurfaceTexture;

    private int mCameraDirection;

    private int maxExposureCompensation, minExposureCompensation;

    private CameraInfo mCameraInfo = new CameraInfo();

    public CameraProxy(Context context) {
        mContext = context;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean openCamera(int cameraId) {
        try {
            releaseCamera();
            mCamera = Camera.open(cameraId);
            mCamera.getParameters();
            mCameraId = cameraId;
            mCamera.getCameraInfo(cameraId, mCameraInfo);

            setDefaultParameters();

            isCameraOpen = true;
            mCameraOpenFailed = false;
        } catch (Exception e) {
            mCameraOpenFailed = true;
            mCamera = null;
            Log.i(TAG, "openCamera fail msg=" + e.getMessage());
            return false;
        }
        return true;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture, PreviewCallback previewcallback) {
        try {
            if (mCamera == null) {
                return;
            }
            if (surfaceTexture != null && mCamera != null) {
            }
            mCamera.setPreviewTexture(surfaceTexture);
            if (previewcallback != null && mCamera != null) {
                mCamera.setPreviewCallback(previewcallback);
                Parameters parameters = mCamera.getParameters();
                Size previewSize = parameters.getPreviewSize();
                byte[] buffer = new byte[previewSize.height * previewSize.height * 3/2];
                mCamera.addCallbackBuffer(buffer);
            }
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPreview() {
        if (mCamera != null)
            mCamera.startPreview();
    }

    public void stopPreview() {
        if (mCamera != null)
            mCamera.stopPreview();
    }

    public Size getPreviewSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        mCamera.setOneShotPreviewCallback(callback);
    }

    public void addPreviewCallbackBuffer(byte[] callbackBuffer) {
        mCamera.addCallbackBuffer(callbackBuffer);
    }

    public int getOrientation() {
        if (mCameraInfo == null) {
            return 0;
        }
        return mCameraInfo.orientation;
    }

    public boolean isFlipHorizontal() {
        if (mCameraInfo == null) {
            return false;
        }
        return mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
    }

    public boolean isFlipVertical() {
        if (mCameraInfo == null) {
            return false;
        }
        return (mCameraInfo.orientation == 90 || mCameraInfo.orientation == 270) ? true : false;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public boolean isFrontCamera() {
        return mCameraId == CameraInfo.CAMERA_FACING_FRONT;
    }

    public void setRotation(int rotation) {
        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            params.setRotation(rotation);
            mCamera.setParameters(params);
        }
    }

    public void takePicture(Camera.ShutterCallback shutterCallback,
                            Camera.PictureCallback rawCallback, Camera.PictureCallback jpegCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }

    public int getDisplayOrientation(int dir) {
        /**
         * 请注意前置摄像头与后置摄像头旋转定义不同 请注意不同手机摄像头旋转定义不同
         */
        int newdir = dir;
        if (isFrontCamera() && ((mCameraInfo.orientation == 270 && (dir & 1) == 1)
                || (mCameraInfo.orientation == 90 && (dir & 1) == 0))) {
            newdir = (dir ^ 2);
        }
        return newdir;
    }

    public boolean needMirror() {
        if (isFrontCamera()) {
            return true;
        } else {
            return false;
        }
    }

    private void setDefaultParameters() {
        Parameters parameters = mCamera.getParameters();
        Log.e(TAG, "parameters: " + parameters.flatten());
        if (parameters.getSupportedFocusModes()
                .contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Parameters.FLASH_MODE_OFF)) {
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        }

        Point previewSize = getSuitablePreviewSize();
        // parameters.setPreviewSize(previewSize.x, previewSize.y);

        parameters.setPreviewSize(1280, 720);
        Point pictureSize = getSuitablePictureSize();
        parameters.setPictureSize(pictureSize.x, pictureSize.y);
        mCamera.setFaceDetectionListener(new MyFaceDetectionListener());
        mCamera.setParameters(parameters);
        maxExposureCompensation = parameters.getMaxExposureCompensation();
        minExposureCompensation = parameters.getMinExposureCompensation();
    }

    public Parameters getParameters() {
        return mCamera.getParameters();
    }

    public void setPreviewSize(int width, int height) {
        if (mCamera == null)
            return;
        Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width, height);
        if (mCamera == null)
            return;
        mCamera.setParameters(parameters);
    }

    private Point getSuitablePreviewSize() {
        Point defaultsize = new Point(1920, 1080);
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
            }
            return new Point(640, 480);
        }
        return null;
    }

    public ArrayList<String> getSupportedPreviewSize(String[] previewSizes) {
        ArrayList<String> result = new ArrayList<String>();
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (String candidate : previewSizes) {
                int index = candidate.indexOf('x');
                if (index == -1)
                    continue;
                int width = Integer.parseInt(candidate.substring(0, index));
                int height = Integer.parseInt(candidate.substring(index + 1));
                for (Size s : sizes) {
                    if ((s.width == width) && (s.height == height)) {
                        result.add(candidate);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据给定宽高取比例相同，尺寸相近的size
     * @param width
     * @param height
     * @return
     */
    public Size getSupportedRatioPreviewSize(int width, int height) {
        Size size = null;
        Size defaultSize = null;
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for(Size s:sizes){
                if(defaultSize == null){
                    defaultSize = s;
                }
                if(s.width == height && s.height == width){
                    size = s;
                    break;
                }else if(isSameRatio(width,height,s)){
                    if(size == null){
                        size = s;
                    }else if(Math.abs(width - s.height) < Math.abs(width - size.height)){
                        size = s;
                    }
                }
                if(s.width == 1280 && s.height == 720){
                    defaultSize = size;
                }
            }
            if(size == null){
                size = defaultSize;
            }
        }
        return size;
    }

    private boolean isSameRatio(int width,int height,Size size){
        return 1.0f * width/height == 1.0f*size.height/size.width;
    }
    /**
     * 以720p为中心取最近切比例相同的size
     *
     * @param ratios
     * @return
     */
    public ArrayList<String> getSupportedPreviewSize(List<Float> ratios) {
        ArrayList<String> result = new ArrayList<String>();
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            LinkedHashMap<Float, Size> sizemap = new LinkedHashMap<>();
            for (float ratio : ratios) {
                for (Size s : sizes) {
                    if ((s.height >= 480) && (s.height <= 1080)) {
                        if (1.0f * s.width / s.height == ratio) {
                            Size sizeOld = sizemap.get(ratio);
                            if (sizeOld == null) {
                                sizemap.put(ratio, s);
                            } else if (Math.abs(s.height - 720) < Math.abs(sizeOld.height - 720)) {
                                sizemap.put(ratio, s);
                            }
                        }

                    }
                }
            }
            Iterator<Map.Entry<Float, Size>> iterator = sizemap.entrySet().iterator();
            while (iterator.hasNext()) {
                Size size = iterator.next().getValue();
                result.add(size.width + "x" + size.height);
            }
        }
        return result;
    }

    private Point getSuitablePictureSize() {
        Point defaultsize = new Point(4608, 3456);
        // Point defaultsize = new Point(3264, 2448);
        if (mCamera != null) {
            Point maxSize = new Point(0, 0);
            List<Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
            for (Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
                if (maxSize.x < s.width) {
                    maxSize.x = s.width;
                    maxSize.y = s.height;
                }
            }
            return maxSize;
        }
        return null;
    }

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public boolean cameraOpenFailed() {
        return mCameraOpenFailed;
    }

    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    public void setMeteringArea(Rect rect) {
        List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
        meteringAreas.add(new Camera.Area(rect, 1));

        if (mCamera != null) {
            try {
                Parameters parameters = mCamera.getParameters();
                parameters.setMeteringAreas(meteringAreas);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                Log.e(TAG, "onFaceDetection exception: " + e.getMessage());
            }
        }

    }

    class MyFaceDetectionListener implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                setMeteringArea(faces[0].rect);

                if (mCamera != null) {
                    try {
                        mCamera.stopFaceDetection();
                    } catch (Exception e) {
                        Log.e(TAG, "setMeteringArea exception: " + e.getMessage());
                    }
                }

            }
        }
    }

    public void startFaceDetection() {
        // Try starting Face Detection
        Parameters params = mCamera.getParameters();

        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0 && mCamera != null) {
            // camera supports face detection, so can start it:

            try {
                mCamera.startFaceDetection();
            } catch (Exception e) {
                Log.e(TAG, "startFaceDetection exception: " + e.getMessage());
            }
        }
    }

    public void handleZoom(boolean isZoomOut) {
        Parameters params = mCamera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomOut && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            mCamera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public void setExposureCompensation(int progerss) {
        Parameters params = mCamera.getParameters();
        int value = 0;
        if (progerss >= 50) {
            int tmp = progerss - 50;
            tmp = tmp * maxExposureCompensation;
            value = tmp / 50;
        } else {
            int tmp = 50 - progerss;
            tmp = minExposureCompensation * tmp;
            value = tmp / 50;
        }
        params.setExposureCompensation(value);
        mCamera.setParameters(params);
    }
}
