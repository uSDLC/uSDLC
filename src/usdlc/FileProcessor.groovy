package usdlc

import java.util.zip.GZIPOutputStream

class FileProcessor {
	static Store fileProcessor(String outputName, files, Closure processor) {
		def outputFile = Store.base(outputName)
		files = files.collect { Store.base(it) }
		def newest = files.inject(outputFile) { l, r ->
			l.newer(r) ? l : r
		}

		if (newest != outputFile) {
			outputFile.mkdirs()
			outputFile.file.withWriter { output ->
				files.each { processor(it, output) }
			}
			gzip(outputFile, outputFile)
		}
		outputFile
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
