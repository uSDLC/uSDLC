package usdlc

from = exchange.request.query.from
page = new Page(from)
user = exchange.request.user
page.children().each {
	def store = it.store, name = it.displayName
	def state = it.state ? "state='$it.state'" : ''
	if (user.authorised(store)) {
		write "<a href='$store.href' class='usdlc' action='page' $state>$name</a>\n"
	}
}
