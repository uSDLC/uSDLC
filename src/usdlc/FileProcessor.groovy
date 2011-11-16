package usdlc
import usdlc.Store
import static usdlc.Config.config

class FileProcessor {
	static Store fileProcessor(String type, files, Closure processor) {
		def outputFile = Store.base("store/usdlc.$type")
		files = files.collect { Store.base(it) }
		def newest = files.inject(outputFile) { l, r ->
			l.newer(r) ? l : r
		}

		if (newest != outputFile) {
			outputFile.mkdirs()
			outputFile.file.withWriter { output ->
				files.each { processor(it, output) }
			}
		}
		outputFile
	}
}