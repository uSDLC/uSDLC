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

@class SimpleCocoaConnection;

@interface SimpleCocoaServer : NSObject {
	@private
	int port; //Port on which server runs
    NSFileHandle *fileHandle; //Server socket
    NSMutableArray *connections; //all connections are saved in here
	char listenAddress[16]; //if listen address is SCSListenOther;
}

- (id)init;
- (SimpleCocoaServer*)start;
- (SimpleCocoaServer*)setPort:(int)port;
- (SimpleCocoaServer*)setListenAddress:(NSString *)to;

- (NSArray *)connections;
- (void)closeConnection:(SimpleCocoaConnection *)con;

@end



@interface SimpleCocoaConnection : NSObject {
	@private
    SimpleCocoaServer *server;
	NSFileHandle *fileHandle; //Socket for the connection
    NSString *remoteAddress;  // client IP address
	int remotePort; //client port

}

- (SimpleCocoaConnection*)initWithFileHandle:(NSFileHandle *)fh server:(SimpleCocoaServer*)serverRef;

- (void)sendData:(NSData *)data;
- (void)sendString:(NSString *)string;

@end

