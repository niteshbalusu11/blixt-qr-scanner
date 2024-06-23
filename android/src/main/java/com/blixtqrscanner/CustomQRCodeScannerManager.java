package com.blixtqrscanner;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.common.MapBuilder;
import java.util.Map;

public class CustomQRCodeScannerManager extends SimpleViewManager<CustomQRCodeScannerView> {
    public static final String REACT_CLASS = "CustomQRCodeScanner";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CustomQRCodeScannerView createViewInstance(ThemedReactContext reactContext) {
        return new CustomQRCodeScannerView(reactContext, null);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
            "onQRCodeRead", MapBuilder.of("registrationName", "onQRCodeRead")
        );
    }
}
