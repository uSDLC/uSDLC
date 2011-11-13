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
   
create = (target) -> target.create(target.args...)
cut = (target) -> target.cut(target.args...)
append = (target) -> target.append(target.args...)
insert = (target) -> target.insert(target.args...)
select = (target) -> target.select(target.args...)
check = (target) -> target.check(target.args...)
next = (target) -> target.next(target.args...)
edit = (target) -> target.edit(target.args...)

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

screencast = ->
	args: arguments
	create: (title, subtitle, synopsis) -> 
		server.createScreencast title, subtitle, synopsis
page = ->
	args: arguments
	create: (title, subtitle, synopsis) -> 
		server.createPage title, subtitle, synopsis
title = ->
	args: arguments
	check: (contents) -> server.check 'div#pageTitle h1', contents
element = ->
	args: arguments
	check: (selector, contents) -> server.check selector, contents
section = ->
	args: arguments
	insert: (title, paragraphs...) -> client 'insertSection', title, paragraphs
	append: (title, paragraphs...) -> client 'appendSection', title, paragraphs
	select: (regex) -> client 'setFocus', server.findSection(regex)
	cut: (regex) -> 
		client 'setFocus', server.findSection(regex)
		client 'deleteSection'
	check: (regex) -> server.findSection(regex)
	next: -> client 'setFocus', server.nextSection()
code = ->
	args: arguments
	select: (linkText) -> server.code(linkText).click()
	check: (regex) -> server.check(regex)
	edit: (linkText, command) ->
		client 'editCode', server.codeId(linkText), command.split /\r*\n/g
