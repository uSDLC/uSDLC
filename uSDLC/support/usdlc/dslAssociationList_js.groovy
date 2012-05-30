package usdlc
write "usdlc.dsls = {"
usdlc.Store.base('/support').dirs(~/.*DSL\.\w*/) { Store store ->
	write store.pathFromWebBase.find(~/([^\/]*)DSL\.(.*)$/) {
		match, dsl, language ->
		"$dsl: '$language',"
	}
}
write '}'
