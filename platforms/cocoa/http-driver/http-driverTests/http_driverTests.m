#import "http_driverTests.h"


@implementation http_driverTests

- (void)setUp
{
    [super setUp];
    
    // Set-up code here.
}

- (void)tearDown
{
    // Tear-down code here.
    
    [super tearDown];
}

- (void)testExample
{
    sleep(3600);
    STFail(@"Main timed out");
}

@end
