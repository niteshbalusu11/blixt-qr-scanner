import React, { useEffect, useState } from 'react';
import {
  View,
  StyleSheet,
  PermissionsAndroid,
  Platform,
  Text,
  Alert,
  NativeModules,
  requireNativeComponent,
  NativeEventEmitter,
} from 'react-native';

const CustomQRCodeScanner = requireNativeComponent('CustomQRCodeScanner');

const App = () => {
  const [hasPermission, setHasPermission] = useState(false);

  useEffect(() => {
    const requestCameraPermission = async () => {
      if (Platform.OS === 'android') {
        try {
          const granted = await PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.CAMERA,
            {
              title: 'Camera Permission',
              message: 'This app needs camera permission to scan QR codes',
              buttonNeutral: 'Ask Me Later',
              buttonNegative: 'Cancel',
              buttonPositive: 'OK',
            }
          );
          if (granted === PermissionsAndroid.RESULTS.GRANTED) {
            setHasPermission(true);
          } else {
            Alert.alert('Camera permission denied');
          }
        } catch (err) {
          console.warn(err);
        }
      } else {
        // Handle iOS permissions if needed
        setHasPermission(true);
      }
    };

    requestCameraPermission();
  }, []);

  useEffect(() => {
    if (hasPermission) {
      const eventEmitter = new NativeEventEmitter(
        NativeModules.CustomQRCodeScanner
      );
      const subscription = eventEmitter.addListener('onQRCodeRead', (event) => {
        console.log('QR Code Scanned:', event.data);
        Alert.alert(`QR Code Scanned: ${event.data}`);
      });

      return () => {
        subscription.remove();
      };
    }
  }, [hasPermission]);

  if (!hasPermission) {
    return (
      <View style={styles.container}>
        <Text>No camera permission</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <CustomQRCodeScanner
        style={styles.preview}
        onQRCodeRead={(event) =>
          console.log('Component Prop:', event.nativeEvent.data)
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'black',
  },
  preview: {
    flex: 1,
    aspectRatio: 3 / 4,
  },
});

export default App;
