package usdlc

import au.com.bytecode.opencsv.CSVReader
/**
 * DSL support class for processing CSV files. Wrapper for opencsv.
 * e.g. new CSV(store : store, context: [headerLines : 1, separator : ',', quote : '"'])
 * these are the defaults
 */
class CSV {
	static process(Store store, Closure closure) {
		new CSV(store:store, context: [perRow : closure]).load()
	}
	/** Called by csvDSL to run the CSV through the provided or default closure */
	CSV load() {
		def process
		context = defaults + context
		store.withReader {
			CSVReader reader = new CSVReader(it)
			if (context.headerLines) {
				def header = reader.readNext().collect { it.trim() }
				if (!header) return
					if (context.headerLines > 1) {
						for (line in [1..<context.headerLines]) {
							if (! reader.readNext()) return
						}
					}
				process = { row ->
					[header, row].transpose().collectEntries{it}
				}
			} else {
				process = { row -> row }
			}
			def row
			while (row = reader.readNext()) {
				perRow(process(row.collect {it.trim()}))
			}
		}
		this
	}
	/** Retrieval will return each row as a map */
	def getAt(int row) {
		list[row]
	}
	/** Number of rows is the size of the list retrieved */
	def size() {
		list.size()
	}
	/** Base directory reference to the CSV location on disk */
	Store store
	/** Closure to execute per row processed */
	Closure perRow = { list << it }

	def context = [:], defaults = [
		/** number of lines before we get to data */
		headerLines : 1,
		/** separator between variables - comma, tab or | */
		separator: ',',
		/** quoting character - ', " or % */
		quote: '"'
	]
	/** List to hold results in memory (if no iterator) */
	def list = []
	/** Name value pairs - one per line */
	static nvp(store) {
		def map = [:]
		if (store.exists()) {
			store.file.eachLine {
				def nvp = it.split(',')
				def value = (nvp.size() > 1) ? nvp[1] : null
				map[nvp[0]] = value
			}
		}
		return map
	}
	static nvp(store, map) {
		store.file.withPrintWriter { PrintWriter writer ->
			map.each { key, value -> writer.println "$key,$value" }
		}
	}
	static nvp(store, key, value) {
		def map = nvp(store)
		map[key] = value
		nvp(store, map)
	}
}
