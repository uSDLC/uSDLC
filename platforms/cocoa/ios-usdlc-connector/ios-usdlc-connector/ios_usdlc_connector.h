#import <Foundation/Foundation.h>
#import "GCDAsyncSocket.h"

@interface ios_usdlc_connector : NSObject {
    NSURL *url;
    GCDAsyncSocket *socket;
    NSMutableArray *header, *params;
    NSString *command;
}

- (id)initWithUrl:(NSString *)urlString;
- (bool)connect;
- (void)processCommand;
- (NSString *)usdlc:(NSArray *) params;
@end
