server = session.instance usdlc.Screencast
client = (cmd, params...) -> server.client(cmd, params)

snap = -> client 'snap'
recover = -> client 'recover'
zoom = (zoomContents) -> client 'zoomContents', zoomContents
note = (title, content) -> client 'note', title, content
prompt = (title, content) ->
	snap()
	note title, content
	server.waitForResponse()
	recover()

timeout = (seconds) -> server.timeout seconds
click = (targets) -> server.click targets
keys = (keys) -> client 'keys', keys
type = (keys) -> server.currentElement.sendKeys keys
step = (seconds) -> server.stepDelay = Math.floor seconds * 1000
menu = (path) -> client 'menu', path.split /\s*->\s*/
slow = 2
fast = 0.001
left = 'left'
top = 'top'

dsl 'dss'
dss 'create screencast', (title, subtitle, synopsis) ->
		server.createScreencast title, subtitle, synopsis
dss 'create page', (title, subtitle, synopsis) ->
		server.createPage title, subtitle, synopsis
dss 'check title', (contents) ->
		server.check 'div#pageTitle h1', contents
dss 'check element', (selector, contents) ->
		server.check selector, contents

$setFocus = (regex) -> client 'setFocus', server.findSection regex
dss 'insert section', (title, paragraphs...) ->
		client 'insertSection', title, paragraphs
dss 'append section', (title, paragraphs...) ->
		client 'appendSection', title, paragraphs
dss 'select section', (regex) -> $setFocus regex
dss 'cut section', (regex) -> $setFocus regex; client 'deleteSection'
dss 'check section', (regex) -> server.findSection regex
dss 'next section', -> client 'setFocus', server.nextSection()
dss 'run section', (regex) -> $setFocus regex; client 'runSection'
dss 'select code', (linkText) -> server.code(linkText).click()
dss 'check code', (regex) -> server.check(regex)
dss 'edit code', (linkText, command) ->
		client 'editCode', server.codeId(linkText), command.split /\r*\n/g
