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

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import groovy.lang.Closure;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor

class JavaScript {
	static compress(input, output) {
			def compressor = new JavaScriptCompressor(input, new CompressorErrorReporter())
			compressor.compress(output, 80, false, false, false, false)
	}
}

public class CompressorErrorReporter implements ErrorReporter {

	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		Log.err "$line:$lineOffset:$message"
	}

	public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
		Log.err "$line:$lineOffset:$message"
	}

	public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset)
		return new EvaluatorException(message)
	}
}