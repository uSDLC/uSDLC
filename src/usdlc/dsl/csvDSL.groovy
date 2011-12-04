package usdlc.dsl

import usdlc.CSV

def newCsv(String name, Map params = [:]) {
	new CSV(store: exchange.store.rebase("${name}.csv"), context: params)
}
def csvLoad(String name, Map params = [:]) {
	newCsv(name, params).load()
}
def csvLoad(String name, Map params, Closure perRow) {
	def csv = newCsv(name, params)
	csv.perRow = perRow
	csv.load()
}
def csvLoad(String name, Closure perRow) {
	csvLoad(name, [:], perRow)
}
csv = this.&csvLoad
dataSource = true
