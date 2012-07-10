//
//  HttpDriver.m
//  http-driver
//
//  Created by Paul Marrington on 17/06/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "HttpDriver.h"
#import "HTTPServer.h"
#import "DDLog.h"
#import "DDTTYLogger.h"

// Log levels: off, error, warn, info, verbose
static const int ddLogLevel = LOG_LEVEL_VERBOSE;

@implementation HttpDriver

@synthesize port;

- (id)init {
    self = [super init];

    // Default port is 9010 for uSDLC connections
    port = 9010;

    // Configure our logging framework.
    // To keep things simple and fast, we're just going to log to the Xcode console.
    [DDLog addLogger:[DDTTYLogger sharedInstance]];

    // Create server using our custom MyHTTPServer class
    httpServer = [[HTTPServer alloc] init];

    // Tell the server to broadcast its presence via Bonjour.
    // This allows browsers such as Safari to automatically discover our service.
    [httpServer setType:@"_http._tcp."];
    return self;
}

- (void)start {
    [httpServer setPort:(UInt16) port];

    // Serve files from our embedded Web folder
    NSString *webPath = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"Web"];
    DDLogInfo(@"Setting document root: %@", webPath);

    [httpServer setDocumentRoot:webPath];

    // Start the server (and check for problems)

    NSError *error;
    if([httpServer start:&error])
    {
        DDLogInfo(@"Started HTTP Server on port %hu", [httpServer listeningPort]);
    }
    else
    {
        DDLogError(@"Error starting HTTP Server: %@", error);
    }
}

@end
