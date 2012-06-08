package usdlc

name = root = exchange.request.query.project
store = Store.base("~$root")
if (!store.exists()) {
	root = Store.camelCase(root)
	store = Store.base("~$root")
}
if (!store.exists() || !store.rebase('usdlc/index.html')) {
	// todo: copy in the new project template
}
