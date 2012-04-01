dsl 'dss'

client = ->
	driver: session.instance usdlc.Webdriver
	browse: (url) -> driver.load url

click = (targets) -> client.client.click(targets)

dss 'check target, contents...', (contents...) ->
	client.driver.checkAll target, contents
dss 'check all target, contents...', (contents...) ->
	client.driver.checkAll target, contents
dss 'check some target, contents...', (contents...) ->
	client.driver.checkSome target, contents
dss 'check none target, contents...', (contents...) ->
	client.driver.checkNone target, contents
dss 'check empty target', (contents...) ->
	client.driver.checkEmpty target
dss 'check selected target, contents...', (contents...) ->
	client.driver.checkSelected target, contents

enter = (fields) ->
	form = if fields.form then "form##{fields.form}" else "form"
	client.driver.enter form, fields
