package usdlc
import static usdlc.JavaScript.javascriptBuilder

files = [
	'/lib/jquery/js/jquery.js',
	'/lib/jquery/js/jquery-ui.js',
	'/lib/jquery/js/jquery.cookie.js',
	'/lib/jquery/js/jquery.sausage.js',
	'/lib/jquery/js/jquery.hotkeys.js',
	'/lib/jquery/js/jquery.url.js',
	'/rt/js/base.js',
	'/rt/js/init.js',
	'/rt/js/section.js',
	'/rt/js/template.js',
]

coffeeCompiler = exchange.request.session.instance CoffeeScript
exchange.response.write javascriptBuilder(files, coffeeCompiler).read()
out "usdlc.init.loadPage -> setTimeout usdlc.init.postLoader, 500"