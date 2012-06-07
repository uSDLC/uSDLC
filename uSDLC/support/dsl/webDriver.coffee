dsl 'dss'

client =
	driver: session.instance usdlc.drivers.WebDriver
	browse: (url) -> client.driver.load url

click = (targets) -> client.driver.click targets
find = (target) -> client.driver.resetBaseElement target
list = (target) -> strings javaArray client.driver.listValues target

dss 'check target, contents...', (target, contents...) ->
	client.driver.checkAll target, contents
dss 'check all target, contents...', (target, contents...) ->
	client.driver.checkAll target, contents
dss 'check only target, contents...', (target, contents...) ->
	client.driver.checkOnly target, contents
dss 'check some target, contents...', (target, contents...) ->
	client.driver.checkSome target, contents
dss 'check none target, contents...', (target, contents...) ->
	client.driver.checkNone target, contents
dss 'check empty target', (target) ->
	client.driver.checkEmpty target

enter = (fields) -> client.driver.enter fields
