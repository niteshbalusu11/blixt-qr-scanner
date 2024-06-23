package com.blixtqrscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.util.List;

public class CustomQRCodeScannerView extends TextureView implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private static final String TAG = "CustomQRCodeScannerView";
    private Camera camera;
    private MultiFormatReader multiFormatReader;

    public CustomQRCodeScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        multiFormatReader = new MultiFormatReader();
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera(surface, width, height);
        } else {
            Log.e(TAG, "Camera permission not granted");
            // Notify React Native about the missing permission
            ReactContext reactContext = (ReactContext) getContext();
            WritableMap event = Arguments.createMap();
            event.putString("error", "Camera permission not granted");
            reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                "onQRCodeError",
                event
            );
        }
    }

    private void openCamera(SurfaceTexture surface, int width, int height) {
        try {
            camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = getOptimalPreviewSize(previewSizes, width, height);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            parameters.setPreviewFormat(ImageFormat.NV21);

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            camera.setParameters(parameters);
            setCameraDisplayOrientation();
            camera.setPreviewTexture(surface);
            camera.setPreviewCallback(this);
            camera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera: " + e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        int width = size.width;
        int height = size.height;

        Log.d(TAG, "onPreviewFrame: Frame received, width: " + width + ", height: " + height);

        try {
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = multiFormatReader.decodeWithState(bitmap);
            if (result != null) {
                Log.d(TAG, "QR Code detected: " + result.getText());
                sendQRCodeResult(result.getText());
            } else {
                Log.d(TAG, "No QR Code detected");
            }
        } catch (Exception e) {
            Log.d(TAG, "Error decoding QR Code: " + e.getMessage());
        } finally {
            multiFormatReader.reset();
        }
    }

    private void sendQRCodeResult(String result) {
        ReactContext reactContext = (ReactContext) getContext();
        WritableMap event = Arguments.createMap();
        event.putString("data", result);
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
            getId(),
            "onQRCodeRead",
            event
        );
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        camera.setDisplayOrientation(result);
    }
}
