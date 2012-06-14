package usdlc

root = exchange.request.query.project
store = Store.base("~$root")
if (!store.exists()) {
	root = Store.camelCase(root)
	store = Store.base("~$root")
}
if (!store.rebase('usdlc/').exists()) {
	def from = Store.base(
		"/usdlc/Environment/Configuration/Templates/Projects/Project_Default")
	from.copyTo("~$root/usdlc")
}

home = new Page(store.rebase('usdlc/index.html'))
home.title = root;
home.forceSave();
