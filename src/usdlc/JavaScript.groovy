/*
 * Copyright 2011 the Authors for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package usdlc

import org.mozilla.javascript.Context
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import org.mozilla.javascript.Scriptable
import com.yahoo.platform.yui.compressor.JavaScriptCompressor

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
		Reader reader = null
		try {
			if (! optimise) {
				// Without this, Rhino hits a 64K byte-code limit and fails
				context.optimizationLevel = -1 
			}
			def inputStream = js.file.newInputStream()
			reader = new InputStreamReader(inputStream, "UTF-8")
			def scope = scope(context, binding)
			return context.evaluateReader(scope, reader, js.path, 0, null)
		} finally {
			reader?.close()
			Context.exit()
		}
	}
	/**
	 * Run javascript statements from a string.
	 */
	public run(String js, binding = [:]) {
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
		def reporter = new CompressorErrorReporter()
		def compressor = new JavaScriptCompressor(input, reporter)
		compressor.compress(output, 80, false, false, false, false)
	}
}

public class CompressorErrorReporter implements ErrorReporter {

	public void warning(String message, String sourceName, 
		int line, String lineSource, int lineOffset) {
		Log.err "$line:$lineOffset:$message"
	}

	public void error(String message, String sourceName, 
		int line, String lineSource, int lineOffset) {
		Log.err "$line:$lineOffset:$message"
	}

	public EvaluatorException runtimeError(String message, String sourceName, 
		int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset)
		return new EvaluatorException(message)
	}
}