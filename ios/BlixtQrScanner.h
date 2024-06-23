
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNBlixtQrScannerSpec.h"

@interface BlixtQrScanner : NSObject <NativeBlixtQrScannerSpec>
#else
#import <React/RCTBridgeModule.h>

@interface BlixtQrScanner : NSObject <RCTBridgeModule>
#endif

@end
