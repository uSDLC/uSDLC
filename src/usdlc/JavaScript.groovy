package usdlc

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import static usdlc.FileProcessor.fileProcessorWithGzip
import static usdlc.config.Config.config

class JavaScript {
	/**
	 * Set optimise to false to compile larger files
	 */
	def optimise = true, session
	/**
	 * Run a javascript file
	 */
	public run(Store js, binding = [:]) {
		Context context = Context.enter()
		Reader reader
		try {
			if (!optimise) {
				// Without this, Rhino hits a 64K byte-code limit and fails
				context.optimizationLevel = -1
			}
			def inputStream = js.file.newInputStream()
			reader = new InputStreamReader(inputStream, "UTF-8")
			def scope = scope(context, binding)
			return context.evaluateReader(
					scope, reader, js.pathFromWebBase, 0, null)
		} finally {
			reader?.close()
			Context.exit()
		}
	}
	/**
	 * Run javascript statements from a string.
	 */
	synchronized run(String js, binding = [:]) {
		Context context = Context.enter()
		try {
			def scope = scope(context, binding)
			return context.evaluateString(scope, js, 'inline', 0, null)
		} finally {
			Context.exit()
		}
	}
	/**
	 * The first scope is the global and all the rest hang off it.
	 * Load any set binding variables.
	 */
	private scope(context, binding) {
		def scope
		if (!globalScope) {
			scope = globalScope = context.initStandardObjects()
		} else if (binding.newScope) {
			Scriptable compileScope = context.newObject(globalScope)
			compileScope.parentScope = globalScope
			scope = compileScope
		} else {
			scope = globalScope
		}
		binding.each { key, value ->
			scope.put(key, scope, value)
		}
		scope
	}

	def globalScope = null
	/**
	 * Given javascript source, remove all the guff to make it as small as
	 * possible. Only useful if sending over a wire.
	 */
	static compress(input, output) {
		output << Reader.text
	}

	static Store javascriptBuilder(outputName, files, coffeeCompiler) {
		fileProcessorWithGzip(outputName, files) { inputFile, writer ->
			writer.write ";\n\n// $inputFile.name\n"
			switch (inputFile.parts.ext) {
				case 'js':
					if (config.compressJs) {
						inputFile.file.withReader {
							JavaScript.compress(it, writer)
						}
					} else {
						writer.write inputFile.text
					}
					break
				case 'coffeescript':
					def code = coffeeCompiler.compile(inputFile)
					if (config.compressJs) {
						JavaScript.compress(new StringReader(code), writer)
					} else {
						writer.write code
					}
					break
				default:
					break
			}
		}
	}
}
