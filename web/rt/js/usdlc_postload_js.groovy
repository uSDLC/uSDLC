package usdlc

import static usdlc.FileProcessor.fileProcessor
import static usdlc.Config.config
import usdlc.JavaScript

files = [
	'/lib/jquery/js/fg.menu.js',
	'/lib/jquery/js/fg.menu.js',
	'/lib/ckeditor/ckeditor.js',
	'/lib/jquery/js/jquery.scrollTo.js',
	'/lib/jquery/js/jquery.jstree.js',
	'/rt/js/server.js'
	'/lib/CodeMirror/lib/codemirror.js',
	'/rt/js/contentTree.coffeescript',
	'/rt/js/synopses.js', '/lib/ckeditor/adapters/jquery.js',
	'/rt/js/menu.js', '/rt/js/moveSection.js', '/rt/js/clipboard.js',
	'/rt/js/run.js', '/rt/js/htmlEditor.js',
	'/rt/js/sourceEditor.coffeescript', '/rt/js/ex.coffeescript',
	'/rt/js/screencast.coffeescript'
]

usdlc.Store.base('/lib/CodeMirror/mode').dirs(~/\w+\.js/) {
	files += it.path
}
coffeeCompiler = exchange.request.session.instance CoffeeScript
exchange.response.write javascriptBuilder(files, coffeeCompiler).read()

out "usdlc.dsls = {"
usdlc.Store.base('/support').dirs(~/.*DSL\.\w*/) {
	out it.path.find(~/([^\/]*)DSL\.(.*)$/) { match, dsl, language ->
		"$dsl: '$language',"
	}
}
out '}'


