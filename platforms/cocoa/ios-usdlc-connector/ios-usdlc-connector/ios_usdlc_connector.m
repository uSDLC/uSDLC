#import "ios_usdlc_connector.h"

@implementation ios_usdlc_connector

- (id) init {
    // default is emulator and uSDLC well-known port
    return [self initWithUrl:@"http://127.0.0.1:9000/socket"];
}

- (id)initWithUrl:(NSString *)urlString {
    self = [super init];
    if (! self) return nil;
    url = [NSURL URLWithString:urlString];
    return self;
}

- (bool)connect {
    socket = [[GCDAsyncSocket alloc] initWithDelegate:self
                                  delegateQueue:dispatch_get_main_queue()];
    NSString *host = [url host];
    uint16_t port = (uint16_t) [[url port] intValue];
    NSError *err = nil;
    if (![socket connectToHost:host onPort:port error:&err]) { // Asynchronous!
        NSLog(@"uSDLC connection failed: %@", err);
        return false;
    }
    return true;
}

- (void)socket:(GCDAsyncSocket *)sender
        didConnectToHost:(NSString *)host port:(UInt16)portNo {
    header = [NSMutableArray array];
    params = [NSMutableArray array];
    NSString *requestTemplate = @"GET %@ HTTP/1.1\r\nUser-Agent: uSDLC/1.0\r\n\r\n";
    NSString *query = [url.pathComponents objectAtIndex:0];
    NSString *requestStr = [NSString stringWithFormat:requestTemplate, query];
    NSData *requestData = [requestStr dataUsingEncoding:NSUTF8StringEncoding];
    [socket writeData:requestData withTimeout:-1.0 tag:0];
    // read response header (one line at a time)
    [socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1.0 tag:0];
}

- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag {
    NSString *line = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    bool blankLine = ([data length] == 2);
    switch (tag) {
        case 0: // reading header
            if (blankLine) {
                tag = 1;    // command
            } else {
                [header addObject:line];    // more header
            }
            break;
        case 1: // command
            if (!blankLine) {
                command = line;
                tag = 2;    // params
            }
            break;
        case 2: // params
            if (blankLine) {
                [self processCommand];   // synchronous
                tag = 1;    // next command
            } else {
                [params addObject:line];    // more header
            }
            break;
    }
    [socket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1.0 tag:tag];
}

- (void)processCommand {
    NSString *response;
    id cmd = [[NSClassFromString(command) alloc] init];
    if (cmd) {
        if ([cmd respondsToSelector:@selector(usdlc:)]) {
            response = [cmd usdlc:params];
        } else {
            response = @"ok";
        }
    } else {
        response = @"error,not implemented";
    }
    [response stringByAppendingString:@"\r\n\r\n"];
    NSData *requestData = [response dataUsingEncoding:NSUTF8StringEncoding];
    [socket writeData:requestData withTimeout:-1.0 tag:0];
}

- (NSString *)usdlc:(NSArray *) data { return @""; }

- (void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err {}
@end
