package usdlc.config

home = '..'	// for ~/path - defaults to one directory above uSDLC
srcPath = ['./', 'usdlc/', 'usdlc/support/',
		'usdlc/platforms/android/net.usdlc.android/']
libPath = ['usdlc/lib/jars/']
newsUrl = 'http://usdlc.wordpress.com/feed/'

port = 9000
environmentRegister = [db: 'usdlc.db.Database']
compressCss = false
compressJs = false
noCompression = ~"usdlc/.*/rt/.*"

startupScripts = [
		"usdlc/rt/css_and_js_builder.groovy"
]
