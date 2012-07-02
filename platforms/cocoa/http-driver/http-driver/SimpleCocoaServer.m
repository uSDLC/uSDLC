//  
//  SimpleCocoaServer, a basic server class written in objectiv-c for use in cocoa applications
//   -- v1.0 --
//   SimpleCocoaServer.m
//   ------------------------------------------------------
//  | Created by David J. Koster, release 26.08.2009.      |
//  | Copyright 2008 David J. Koster. All rights reserved. |
//  | http://www.david-koster.de/code/simpleserver         |
//  | code@david-koster.de for help or see:                |
//  | http://sourceforge.net/projects/simpleserver         |
//   ------------------------------------------------------
// 
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
// 
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
// 
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>. */
//
// Usage server = [[[[[SimpleCocoaServer alloc] init] setPort: 9010] setListenAddress: @"0.0.0.0"] start]


#import "SimpleCocoaServer.h"
#import <sys/socket.h>
#import <netinet/in.h>
#import <arpa/inet.h>


@implementation SimpleCocoaServer

#pragma mark Instance Methods

- (id)init
{
	if(self = [super init]) {
		port = 9010;
        [self setListenAddress: @"127.0.0.1"];
		connections = [[NSMutableArray alloc] init];
	}
	return self;	
}

- (SimpleCocoaServer*)setListenAddress:(NSString *)to
{   // @"127.0.0.1" or @"0.0.0.0"
	strncpy(listenAddress, [to UTF8String], 15);
	return self;	
}

- (SimpleCocoaServer*)setPort:(int)to
{
	port = to;
	return self;	
}

- (SimpleCocoaServer*)start
{
	CFSocketRef socket = CFSocketCreate(kCFAllocatorDefault, PF_INET, SOCK_STREAM, IPPROTO_TCP, 1, NULL, NULL);
	NSAssert(socket, @"Cannot create socket connection");
		
    int filedescriptor = CFSocketGetNative(socket);
    
    //this code prevents the socket from existing after the server has crashed or been forced to close
    int yes = 1;
    setsockopt(filedescriptor, SOL_SOCKET, SO_REUSEADDR, (void *)&yes, sizeof(yes));
    
    struct sockaddr_in addr4;
    memset(&addr4, 0, sizeof(addr4));
    addr4.sin_len = sizeof(addr4);
    addr4.sin_family = AF_INET;
    addr4.sin_port = htons(port);
    inet_pton(AF_INET, listenAddress, &addr4.sin_addr);
    //addr4.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
    //addr4.sin_addr.s_addr = htonl(INADDR_ANY); //any network address, e.g. 127.0.0.1, 168.192.2.101 etc;
    NSData *address4 = [NSData dataWithBytes:&addr4 length:sizeof(addr4)];
    
    NSAssert(kCFSocketSuccess == CFSocketSetAddress(socket, (CFDataRef)address4), @"Cannot set socket address");
		
	fileHandle = [[NSFileHandle alloc] initWithFileDescriptor:filedescriptor closeOnDealloc:YES];
	NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
	[nc addObserver:self
		   selector:@selector(newConnection:)
			   name:NSFileHandleConnectionAcceptedNotification
			 object:nil];
	[fileHandle acceptConnectionInBackgroundAndNotify];
    return self;	
}

- (void)newConnection:(NSNotification *)notification
{
	NSDictionary *userInfo = [notification userInfo];
	NSFileHandle *remoteFileHandle = [userInfo objectForKey: NSFileHandleNotificationFileHandleItem];
	NSNumber *errorNo = [userInfo objectForKey:@"NSFileHandleError"];
    NSAssert(!errorNo, @"Notification error");
	
	[fileHandle acceptConnectionInBackgroundAndNotify];

    SimpleCocoaConnection *connection = [[SimpleCocoaConnection alloc] 
                                         initWithFileHandle:remoteFileHandle server:self];
    NSIndexSet *insertedIndexes = [NSIndexSet indexSetWithIndex:[connections count]];
    [self willChange:NSKeyValueChangeInsertion
     valuesAtIndexes:insertedIndexes forKey:@"connections"];
    [connections addObject:connection];
    [self didChange:NSKeyValueChangeInsertion
    valuesAtIndexes:insertedIndexes forKey:@"connections"];
    [connection release];
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    CFSocketRef socket = CFSocketCreateWithNative(kCFAllocatorDefault,[fileHandle fileDescriptor],1,NULL,NULL);
    CFSocketInvalidate(socket);
    CFRelease(socket);
    [fileHandle release];
	[connections release];
	[super dealloc];
}

- (NSArray *)connections
{
	return connections;
}

- (void)closeConnection:(SimpleCocoaConnection *)con
{
	int connectionIndex = [connections indexOfObjectIdenticalTo:con];
    if(connectionIndex == NSNotFound) return;
	NSIndexSet *connectionIndexSet = [NSIndexSet indexSetWithIndex:connectionIndex];
    [self willChange:NSKeyValueChangeRemoval valuesAtIndexes:connectionIndexSet
              forKey:@"connections"];
    [connections removeObjectsAtIndexes:connectionIndexSet];
	//the connection was released when added to the array
    [self didChange:NSKeyValueChangeRemoval valuesAtIndexes:connectionIndexSet
             forKey:@"connections"];
}

@end

@implementation SimpleCocoaConnection

- (SimpleCocoaConnection*)initWithFileHandle:(NSFileHandle *)fh server:(SimpleCocoaServer*)serverRef
{
    if(self = [super init]) {
		fileHandle = [fh retain];
		server = [serverRef retain];
		
		// Get IP address of remote client
		CFSocketRef socket = CFSocketCreateWithNative(kCFAllocatorDefault, [fileHandle fileDescriptor], kCFSocketNoCallBack, NULL, NULL);
		CFDataRef addrData = CFSocketCopyPeerAddress(socket);
		CFRelease(socket);
		
		if(addrData) {
			struct sockaddr_in *sock = (struct sockaddr_in *)CFDataGetBytePtr(addrData);
			remotePort = (sock->sin_port);
			char *naddr = inet_ntoa(sock->sin_addr);
			remoteAddress = [NSString stringWithCString:naddr encoding:NSUTF8StringEncoding];
			CFRelease(addrData);
		} else {
			remoteAddress = @"NULL";
		}
		
		// Register for notification when data arrives
		NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];
		[nc addObserver:self
			   selector:@selector(dataReceivedNotification:)
				   name:NSFileHandleReadCompletionNotification
				 object:fileHandle];
		[fileHandle readInBackgroundAndNotify];
	}
	return self;
}

- (void)dealloc
{
	[[NSNotificationCenter defaultCenter] removeObserver:self];
	[server release];
	[fileHandle closeFile];
	[fileHandle release];
	[remoteAddress release];
	[super dealloc];
}

- (void)dataReceivedNotification:(NSNotification *)notification
{
	NSData *data = [[notification userInfo] objectForKey:NSFileHandleNotificationDataItem];
	
	if ([data length] == 0) {
		// NSFileHandle's way of telling us that the client closed the connection
		[server closeConnection:self];
		return;
	} else {
		[fileHandle readInBackgroundAndNotify];
		NSString *received = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
		if([received characterAtIndex:0] == 0x04) { // End-Of-Transmission sent by client
			return;
		}
        NSLog(@"RECEIVED:\n\n%@", received);
	}
}

- (void)sendData:(NSData *)data
{
    [fileHandle writeData:data];
}

- (void)sendString:(NSString *)string
{
    [fileHandle writeData:[string dataUsingEncoding:NSASCIIStringEncoding]];
}

@end
