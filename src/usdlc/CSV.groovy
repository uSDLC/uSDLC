package usdlc

import au.com.bytecode.opencsv.CSVReader
/**
 * DSL support class for processing CSV files. Wrapper for opencsv.
 * @author paul
 */
class CSV {
	/** Called by csvDSL to run the CSV through the provided or default closure */
	CSV load() {
		context = defaults + context
		parent.rebase("${name}.csv").withReader {
			CSVReader reader = new CSVReader(it)
			if (context.headerLines) {
				if (! (header = reader.readNext().collect { it.trim() })) return
				if (context.headerLines > 1) {
					for (line in [1..<context.headerLines]) {
						if (! reader.readNext()) return
					}
				}
			}
			def row
			while (row = reader.readNext()) {
				perRow(build(row.collect { it.trim() }))
			}
		}
		this
	}
	/** Retrieval will return each row as a map */
	def getAt(int row) { list[row] }
	/** Turn a list into a map using the headers */
	def build(row) {
		if (context.headerLines) {
			[header,row].transpose().collectEntries{it}
		} else {
			row
		}
	}
	/** Number of rows is the size of the list retrieved */
	def size() { list.size() }
	/** Name for CSV file */
	String name
	/** Base directory reference to the CSV location on disk */
	Store parent
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
	/** header list if file has header */
	String[] header
}
