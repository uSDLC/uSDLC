package usdlc

files = [
		"lib/jquery/css/redmond/jquery-ui-1.8.16.custom.css",
		"lib/jquery/css/fg.menu.css",
		"lib/underscore.js",
		"lib/CodeMirror/lib/codemirror.css",
		"lib/CodeMirror/theme/default.css",
		"lib/CodeMirror/theme/elegant.css",
		"lib/CodeMirror/theme/neat.css",
		"lib/CodeMirror/theme/night.css",
		"rt/css/body.css",
		"rt/css/title.css",
		"rt/css/section.css",
		"rt/css/paragraph.css",
		"rt/css/ui.css",
		"rt/css/menu.css",
		"rt/css/borders.css",
		"rt/css/sausage.css",
		"rt/css/editor.css",
]

fileProcessorWithGzip('store/css/usdlc.css', files) {
	Store inputFile, Writer writer ->
	if (config.compressCss) {
		inputFile.file.withReader {new CssCompressor(it).compress(writer, 80)}
	} else {
		writer.write(inputFile.text)
	}
}

import com.yahoo.platform.yui.compressor.CssCompressor
import static usdlc.FileProcessor.fileProcessorWithGzip
import static usdlc.JavaScript.javascriptBuilder
import static usdlc.config.Config.config

files = [
		'lib/jquery/js/jquery.js',
		'lib/jquery/js/jquery-ui.js',
		'lib/jquery/js/jquery.cookie.js',
		'lib/jquery/js/jquery.sausage.js',
		'lib/jquery/js/jquery.hotkeys.js',
		'lib/jquery/js/jquery.url.js',
		'rt/js/base.js',
		'rt/js/init.js',
		'rt/js/section.js',
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
javascriptBuilder('store/js/usdlcPre.js', files, coffeeCompiler)

files = [
		'lib/jquery/js/fg.menu.js',
		'lib/jquery/js/fg.menu.js',
		'lib/ckeditor/ckeditor.js',
		'lib/ckeditor/adapters/jquery.js',
		'rt/js/menu.js',
		'rt/js/moveSection.js',
		'rt/js/clipboard.js',
		'rt/js/htmlEditor.js',
		'rt/js/ex.coffeescript',
		'rt/js/screencast.coffeescript',
		'rt/js/news.coffeescript'
]
Store.base('lib/CodeMirror/mode').dirs(~/\w+\.js/) { Store store ->
	files += store.path;
	return
}

javascriptBuilder('store/js/usdlcPost.js', files, coffeeCompiler)
