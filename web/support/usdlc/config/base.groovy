package usdlc.config

home = '../..'	// for ~/path - defaults to one directory above uSDLC
urlBase = ''
srcPath = ['./', 'support/']
libPath = ['lib/jars/']
newsUrl = 'http://usdlc.wordpress.com/feed/'

port = 9000
environmentRegister = [db: 'usdlc.db.Database']
compressCss = false
compressJs = false
noCompression = ~".*/rt/.*"

startupScripts = [
		"rt/css_and_js_builder.groovy"
]
