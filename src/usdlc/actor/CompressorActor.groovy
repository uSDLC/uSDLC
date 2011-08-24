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
package usdlc.actor

import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException;
import usdlc.History
import usdlc.Log
import usdlc.Store
import static usdlc.Config.config

/**
 * Uses a closure to filter input to output - saving the result
 */
abstract class CompressorActor extends Actor {
	/**
	 Use to generate HTML to display on the screen.
	 */
	void filter(String type, Closure compress) {
		def store
		if (config."compress${type.capitalize()}") {
			store = Store.base("store/$type/base").rebase(exchange.store.path)
			if (! store.exists()  ||  store.lastModified() < exchange.store.lastModified()) {
				store.mkdirs()
				exchange.store.file.withReader { input ->
					store.file.withWriter { output ->
						compress input, output
					}
				}
			}
		} else {
			store = exchange.store
		}
		exchange.response.write store.read() ?: ''
	}
}
