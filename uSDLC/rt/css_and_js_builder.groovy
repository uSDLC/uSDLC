package usdlc

import usdlc.drivers.CoffeeScript

import static usdlc.FileProcessor.fileProcessorWithGzip
import static usdlc.drivers.JavaScript.javascriptBuilder

jqueryUIbase = 'usdlc/lib/jquery/css/start'
files = [
		"$jqueryUIbase/jquery-ui-1.8.21.custom.css",
		'usdlc/lib/jquery/css/fg.menu.css',
		'usdlc/lib/CodeMirror/lib/codemirror.css',
		'usdlc/lib/CodeMirror/theme/default.css',
		'usdlc/lib/CodeMirror/theme/elegant.css',
		'usdlc/lib/CodeMirror/theme/neat.css',
		'usdlc/lib/CodeMirror/theme/night.css',
		'usdlc/rt/css/body.css',
		'usdlc/rt/css/title.css',
		'usdlc/rt/css/section.css',
		'usdlc/rt/css/paragraph.css',
		'usdlc/rt/css/ui.css',
		'usdlc/rt/css/menu.css',
		'usdlc/rt/css/borders.css',
		'usdlc/rt/css/sausage.css',
		'usdlc/rt/css/editor.css',
]

fileProcessorWithGzip('.store/css/usdlc.css', files) {
	Store inputFile, Writer writer -> writer.write(inputFile.text)
	// copy jQueryUI images as they are expected to be below the css
	Store.base("$jqueryUIbase/images").copyTo('.store/css/images')
}

files = [
		'usdlc/lib/jquery/js/jquery-1.7.2.js',
		'usdlc/lib/underscore.js',
		'usdlc/lib/jquery/js/jquery-ui-1.8.21.custom.js',
		'usdlc/lib/jquery/js/jquery.cookie.js',
		'usdlc/lib/jquery/js/jquery.sausage.js',
		'usdlc/lib/jquery/js/jquery.hotkeys.js',
		'usdlc/lib/jquery/js/jquery.url.js',
		'usdlc/rt/js/base.js',
		'usdlc/rt/js/init.js',
		'usdlc/rt/js/menu.coffeescript',
		'usdlc/rt/js/section.coffeescript',
		'usdlc/rt/js/template.js',
		'usdlc/lib/jquery/js/jquery.scrollTo.js',
		'usdlc/lib/jquery/js/jquery.jstree.js',
		'usdlc/rt/js/server.js',
		'usdlc/rt/js/contentTree.coffeescript',
		'usdlc/lib/CodeMirror/lib/codemirror.js',
		'usdlc/rt/js/run.coffeescript',
		'usdlc/rt/js/sourceEditor.coffeescript',
		'usdlc/rt/js/synopses.coffeescript',
		'/usdlc/rt/js/footer.coffeescript',
		'usdlc/rt/js/postLoader.coffeescript',
]

coffeeCompiler = new CoffeeScript()
javascriptBuilder('.store/js/usdlcPre.js', files, coffeeCompiler)

files = [
		'usdlc/lib/jquery/js/jquery.easytabs.js',
		'usdlc/lib/ckeditor/ckeditor.js',
		'usdlc/lib/ckeditor/adapters/jquery.js',
		'usdlc/rt/js/footer.coffeescript',
		'usdlc/rt/js/dialog.coffeescript',
		'usdlc/rt/js/new.coffeescript',
		'usdlc/rt/js/moveSection.js',
		'usdlc/rt/js/clipboard.coffeescript',
		'usdlc/rt/js/htmlEditor.js',
		'usdlc/rt/js/ex.coffeescript',
		'usdlc/rt/js/screencast.coffeescript',
		'usdlc/rt/js/news.coffeescript',
		'usdlc/lib/jquery/js/jquery.tagcanvas.min.js'
]
Store.base('usdlc/lib/CodeMirror/mode').dirs(~/\w+\.js/) { Store store ->
	files += store.path;
}

javascriptBuilder('.store/js/usdlcPost.js', files, coffeeCompiler)
