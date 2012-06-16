package usdlc

roots = []
assigned = exchange.request.query.unassigned ? false : true
	Store.base('~/').file.eachDir { File file ->
		def store = Store.base("~/$file.name/usdlc")
		if (store.exists() == assigned) roots << file.name
	}
write "['${roots.join(/','/)}']"

