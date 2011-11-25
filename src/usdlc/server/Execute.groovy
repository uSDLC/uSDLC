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
	 */
	public execute(Map options, OutputStream out) {
		commands.each { Template template ->
			String command = template.make(options)
			Process proc = command.execute()
			proc.consumeProcessOutput(out, out)
		}
	}
}
