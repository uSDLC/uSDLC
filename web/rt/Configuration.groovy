@Typed package rt
/*
 For any but the stand-alone usdlc.server.servletengine.server there will be a URL on the usdlc.server.servletengine.server that is redirected to uSDLC. We need to remove this common base path to work out where in the structure we are. This regex should be set up so that the first group is the one we want to use.One for the address in the browser location bar and one to find the same path on disk (or in database)
 */
class Configuration {
	String urlBase = ""
	String rootFile = "/rt/template.html.groovy"
	String usdlcDatabase = 'jdbc:h2:.db/usdlc'
	/*
	 Path to use for defining Java and Groovy files. Point to the source for static (unchanging) code files and the web directory for files that will change as part of the installation.
	 */
	def srcPath = ['', 'Actors']
	def libPath = ['lib/jars']
	/*
	 If the file does not exist, use a template file for that usdlc.server.servletengine.server and client side extension.
	 */
	def template = [
			html: 'template',
			'html.groovy': 'template',
			groovy: 'template',
			gradle: 'template'
	]
	/*
	 Define the script language to use for a address line url that specifies the path only.
	 */
	String defaultScriptLanguage = 'groovy'
	boolean alwaysCheckForRecompile = true
	/*
	 * What local port is used for the local usdlc.server.standalone?
	 */
	int port = 9000
	/*
	 * Environment registrations
	 */
	def environmentRegister = [
			db: 'usdlc.db.Database'
	]
	/**
	 * Always set by start-up code
	 */
	String baseDirectory = 'web'
	String environment = 'standalone'
}