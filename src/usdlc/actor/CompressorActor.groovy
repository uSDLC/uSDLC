package usdlc.actor

import usdlc.Store
import static usdlc.FileProcessor.gzip
import static usdlc.config.Config.config

/**
 * Uses a closure to filter input to output - saving the result
 */
abstract class CompressorActor extends Actor {
	void filter(String type, Closure compress) {
		Store source = exchange.store, compressed
		if (config."compress${type.capitalize()}" &&
				!config.noCompression.matcher(source.path).matches()) {
			compressed = Store.base("store/$type/base").rebase(source.path)
			if (source.newer(compressed)) {
				compressed.mkdirs()
				source.file.withReader { Reader input ->
					compressed.file.withWriter { Writer output ->
						compress input, output
					}
				}
				gzip(compressed, source)
			}
		} else {
			compressed = exchange.store
		}
		exchange.response.write compressed.read() ?: ''
	}
}
