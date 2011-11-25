package usdlc

import java.util.zip.GZIPOutputStream

class FileProcessor {
	static Store fileProcessor(String outputName, files,
			Closure inputProcessor, Closure outputProcessor = {}) {
		def outputFile = Store.base(outputName)
		files = files.collect { Store.base(it) }
		def newest = files.inject(outputFile) { Store l, r ->
			l.newer(r) ? l : r
		}

		if (newest != outputFile) {
			outputFile.mkdirs()
			outputFile.file.withWriter { Writer output ->
				files.each { Store file -> inputProcessor file, output }
			}
			outputProcessor(outputFile)
		}
		outputFile
	}

	static Store fileProcessorWithGzip(String outputName, files,
			Closure inputProcessor) {
		fileProcessor(outputName, files, inputProcessor) { Store store ->
			gzip(store, store)
		}
	}

	static gzip(Store inputFile, Store outputFile) {
		def gzipFile = Store.base("${outputFile.path}.gzip")
		gzipFile.file.withOutputStream {
			def gzos = new GZIPOutputStream(it)
			def bytes = inputFile.read()
			gzos.write(bytes, 0, bytes.length)
			gzos.finish()
		}
	}
}
