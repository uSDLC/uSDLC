//  
//  SimpleCocoaServer, a basic server class written in objectiv-c for use in cocoa applications
//   -- v1.0 --
//   SimpleCocoaServer.h
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

//#import <Cocoa/Cocoa.h>
@class SimpleCocoaConnection;

@interface SimpleCocoaServer : NSObject {
	@private
	int port; //Port on which server runs
	BOOL isListening; //is server running?
    NSFileHandle *fileHandle; //Server socket
    NSMutableArray *connections; //all connections are saved in here
	char listenAddress[16]; //if listen address is SCSListenOther;
}

- (id)init;
- (void)start;
- (void)start:(int)port;
- (BOOL)setListenAddress:(NSString *)to;

- (SCSInit)startListening;
- (void)stopListening;
- (BOOL)isListening;

- (BOOL)setServerPort:(int)newPort;
- (int)serverPort;
- (SCSListenAddress)listenAddress;
- (NSString *)listenAddressAsString;

- (void)processMessage:(NSString *)message orData:(NSData *)data fromConnection:(SimpleCocoaConnection *)con;
- (void)processNewConnection:(SimpleCocoaConnection *)con;
- (void)processClosingConnection:(SimpleCocoaConnection *)con;

- (NSArray *)connections;
- (void)closeConnection:(SimpleCocoaConnection *)con;

- (BOOL)sendData:(NSData *)data toConnection:(SimpleCocoaConnection *)con;
- (BOOL)sendString:(NSString *)string toConnection:(SimpleCocoaConnection *)con;
- (void)sendDataToAll:(NSData *)data;
- (void)sendStringToAll:(NSString *)string;

@end



@interface SimpleCocoaConnection : NSObject {
	@private
	NSFileHandle *fileHandle; //Socket for the connection
    id connectionDelegate; //always the server
    NSString *remoteAddress;  // client IP address
	int remotePort; //client port

}

- (id)initWithFileHandle:(NSFileHandle *)fh delegate:(id)initDelegate;

- (NSFileHandle *)fileHandle;
- (NSString *)remoteAddress;
- (int)remotePort;

@end

