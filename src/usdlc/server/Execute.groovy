package usdlc.server

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
/**
 * Operating System execution of commands.
 */
class Execute {
	def commands = []
	/**
	 * @param commandTemplates is a list of strings that have Groovy type
	 * replacements $a, ${b}, etc. We fill in the variables with an options
	 * map at run-time.
	 */
	Execute(Object... commandTemplates) {
		def engine = new SimpleTemplateEngine()
		commands = commandTemplates.collect { String template ->
			engine.createTemplate(template)
		}
	}
	/**
	 * Run a predefined list of commands - given options to fill in.
	 * Predefined options:
	 *
	 * currentDir:      directory to run from (defaults to same as calling app)
	 * outputStream:    steam to send stdout and stderr (defaults to console)
	 */
	def execute(Map options) {
		def currentDir = new File(options.currentDir ?: '.')
		def out = options.outputStream ?: System.out
		commands.each { Template template ->
			String command = template.make(options)
			Process proc = command.execute(null, currentDir)
			proc.consumeProcessOutput(out, out)
		}
	}
}
