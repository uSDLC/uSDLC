package usdlc

home = '../..'	// for ~/path - defaults to one directory above uSDLC
urlBase = ''
srcPath = ['./', 'support/']
dslClassPath = ['usdlc/dsl/']
dslSourcePath = ['support/usdlc/']
//dslPath = ['dsl/', '../src/usdlc/dsl/']
//dslPath = ['dsl/', 'jar:file:usdlc.jar!/usdlc/dsl/']
libPath = ['lib/jars/']
databases = [memory: 'jdbc:h2:mem:mem:mem', usdlc: 'jdbc:h2:.db/usdlc']
browserDriverList = 'chrome:firefox:ie:htmlunit'
alwaysCheckForRecompile = true
port = 9000
environmentRegister = [db: 'usdlc.db.Database']
compressCss = false
noCompression = ~".*/rt/.*"

webDrivers = [
		chrome: 'org.openqa.selenium.chrome.ChromeDriver',
		firefox: 'org.openqa.selenium.firefox.FirefoxDriver',
		ie: 'org.openqa.selenium.ie.InternetExplorerDriver',
		htmlunit: 'org.openqa.selenium.htmlunit.HtmlUnitDriver',
		iphone: 'org.openqa.selenium.iphone.IPhoneDriver',
		android: 'org.openqa.selenium.android.AndroidDriver',
]

screencast = [keys: "` F12"]

startupScripts = [
		"rt/css_and_js_builder.groovy"
]

environments {
	standalone {
		compressJs = false
		webDriver = 'chrome'
	}
	servlet {
		compressJs = false
		webDriver = 'htmlunit'
	}
}
