package usdlc

from = exchange.request.query.from
page = new Page(from)
user = exchange.request.user
page.children().each {
	def store = it.store, name = it.displayName
	if (user.authorised(store)) {
		write "<a href='$store.href' class='usdlc' action='page'>$name</a>\n"
	}
}
