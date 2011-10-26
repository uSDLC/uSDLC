package usdlc.dsl
out "usdlc.dsls = {"
usdlc.Store.base('/support').dirs(~/.*DSL\.\w*/) {
	out it.path.find(~/([^\/]*)DSL\.(.*)$/) { match, dsl, language ->
		"$dsl: '$language',"
	}
}
out '}'