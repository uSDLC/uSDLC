#import <UIKit/UIKit.h>

#import "AppDelegate.h"
#import "ios_usdlc_connector.h"

int main(int argc, char *argv[])
{
    @autoreleasepool {
        [[ios_usdlc_connector alloc] init];
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
    }
}
