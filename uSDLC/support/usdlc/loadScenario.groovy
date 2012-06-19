package usdlc

query =  exchange.request.query
givenWhenOrThen = query.gwt
statements = [] as Set
root = query.page
root = root[0..root.indexOf('/usdlc/')+5]
Store.base(root).dirs(~/${givenWhenOrThen}_.*\.gwt/) { file ->
		statements.addAll(Store.base(file).file.readLines())
}
write "['${statements.join(/','/)}']"
