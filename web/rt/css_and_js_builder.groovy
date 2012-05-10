package usdlc

import usdlc.drivers.CoffeeScript

import static usdlc.FileProcessor.fileProcessorWithGzip
import static usdlc.drivers.JavaScript.javascriptBuilder

jqueryUIbase = 'lib/jquery/css/redmond'
files = [
		"$jqueryUIbase/jquery-ui-1.8.16.custom.css",
		'lib/jquery/css/fg.menu.css',
		'lib/CodeMirror/lib/codemirror.css',
		'lib/CodeMirror/theme/default.css',
		'lib/CodeMirror/theme/elegant.css',
		'lib/CodeMirror/theme/neat.css',
		'lib/CodeMirror/theme/night.css',
		'rt/css/body.css',
		'rt/css/title.css',
		'rt/css/section.css',
		'rt/css/paragraph.css',
		'rt/css/ui.css',
		'rt/css/menu.css',
		'rt/css/borders.css',
		'rt/css/sausage.css',
		'rt/css/editor.css',
]

fileProcessorWithGzip('.store/css/usdlc.css', files) {
	Store inputFile, Writer writer -> writer.write(inputFile.text)
}
// copy jQueryUI images as they are expected to be below the css
Store.base("$jqueryUIbase/images").copy('.store/css')

files = [
		'lib/jquery/js/jquery.js',
		'lib/underscore.js',
		'lib/jquery/js/jquery-ui.js',
		'lib/jquery/js/jquery.cookie.js',
		'lib/jquery/js/jquery.sausage.js',
		'lib/jquery/js/jquery.hotkeys.js',
		'lib/jquery/js/jquery.url.js',
		'rt/js/base.js',
		'rt/js/init.js',
		'rt/js/section.coffeescript',
		'rt/js/template.js',
		'lib/jquery/js/jquery.scrollTo.js',
		'lib/jquery/js/jquery.jstree.js',
		'rt/js/server.js',
		'rt/js/contentTree.coffeescript',
		'lib/CodeMirror/lib/codemirror.js',
		'rt/js/run.js',
		'rt/js/sourceEditor.coffeescript',
		'rt/js/synopses.js',
		'rt/js/postLoader.coffeescript',
]

coffeeCompiler = new CoffeeScript()
javascriptBuilder('.store/js/usdlcPre.js', files, coffeeCompiler)

files = [
		'lib/ckeditor/ckeditor.js',
		'lib/ckeditor/adapters/jquery.js',
		'rt/js/dialog.coffeescript',
		'rt/js/menu.coffeescript',
		'rt/js/moveSection.js',
		'rt/js/clipboard.js',
		'rt/js/htmlEditor.js',
		'rt/js/ex.coffeescript',
		'rt/js/screencast.coffeescript',
		'rt/js/news.coffeescript'
]
Store.base('lib/CodeMirror/mode').dirs(~/\w+\.js/) { Store store ->
	files += store.pathFromWebBase;
	return
}

javascriptBuilder('.store/js/usdlcPost.js', files, coffeeCompiler)
