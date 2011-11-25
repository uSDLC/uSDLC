package usdlc.config

home = '../..'	// for ~/path - defaults to one directory above uSDLC
urlBase = ''
srcPath = ['./', 'support/']
libPath = ['lib/jars/']
databases = [memory: 'jdbc:h2:mem:mem:mem', usdlc: 'jdbc:h2:.db/usdlc']

port = 9000
environmentRegister = [db: 'usdlc.db.Database']
compressCss = false
compressJs = false
noCompression = ~".*/rt/.*"

startupScripts = [
		"rt/css_and_js_builder.groovy"
]
