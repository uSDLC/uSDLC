package usdlc.dsl
write "usdlc.dsls = {"
usdlc.Store.base('/support').dirs(~/.*DSL\.\w*/) {
	write it.path.find(~/([^\/]*)DSL\.(.*)$/) { match, dsl, language ->
		"$dsl: '$language',"
	}
}
write '}'
