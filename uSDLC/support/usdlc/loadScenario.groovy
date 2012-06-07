import usdlc.Store

query =  exchange.request.query
givenWhenOrThen = query.gwt
statements = [] as Set
Store.base(query.page).onParentPath { dirStore ->
	dirStore.dir(~/${givenWhenOrThen}_.*\.gwt/) { file ->
		statements.addAll(Store.base(file).file.readLines())
	}
}
write "['${statements.join(/','/)}']"
