package usdlc

import com.yahoo.platform.yui.compressor.CssCompressor
import static usdlc.FileProcessor.fileProcessor
import static usdlc.Config.config

files = [
	"lib/jquery/css/redmond/jquery-ui-1.8.16.custom.css",
	"lib/jquery/css/fg.menu.css",
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

cssFile = fileProcessor('css', files) { inputFile, writer ->
	if (config.compressCss) {
		inputFile.file.withReader {new CssCompressor(it).compress(writer, 80)}
	} else {
		writer.write(inputFile.read())
	}
}
exchange.response.write cssFile.read()

