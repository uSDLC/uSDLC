package usdlc
write "usdlc.dsls = {"
usdlc.Store.base('usdlc/support').dirs(~/.*DSL\.\w*/) { Store store ->
	write store.path.find(~/([^\/\\]*)DSL\.(.*)$/) { match, dsl, language ->
		"$dsl: '$language',"
	}
}
write '"":""}'
