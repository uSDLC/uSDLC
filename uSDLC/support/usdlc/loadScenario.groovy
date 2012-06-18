package usdlc

query =  exchange.request.query
givenWhenOrThen = query.gwt
statements = [] as Set
root = query.page
root = root[0..root.indexOf('/usdlc/')+6]
Store.base(root).dirs(~/Groups.csv/) { dirStore ->
	dirStore.dir(~/${givenWhenOrThen}_.*\.gwt/) { file ->
		statements.addAll(Store.base(file).file.readLines())
	}
}
write "['${statements.join(/','/)}']"
