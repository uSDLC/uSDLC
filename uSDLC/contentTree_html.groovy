package usdlc

from = exchange.request.query.from
if (from[0] != '/') from = "/$from"
page = new Page(from)
user = exchange.request.user
page.children().each {
	def store = it.store, name = it.displayName
	if (user.authorised(store)) {
		def href = store.path
		write "<a href='$href' class='usdlc' action='page'>$name</a>\n"
	}
}
