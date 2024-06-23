import React, { useEffect, useState } from 'react';
import {
  View,
  StyleSheet,
  PermissionsAndroid,
  Platform,
  Text,
  Alert,
  requireNativeComponent,
} from 'react-native';

const CustomQRCodeScanner = requireNativeComponent('CustomQRCodeScanner');

const App = () => {
  const [hasPermission, setHasPermission] = useState(false);

  useEffect(() => {
    const requestCameraPermission = async () => {
      if (Platform.OS === 'android') {
        try {
          const cameraPermission = PermissionsAndroid.PERMISSIONS.CAMERA;
          if (!cameraPermission) {
            throw new Error('Camera permission is undefined');
          }
          const granted = await PermissionsAndroid.request(cameraPermission, {
            title: 'Camera Permission',
            message: 'This app needs camera permission to scan QR codes',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          });
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
