package usdlc
/**
 * Code to keep state of page with respect to instrumentation.
 */
class PageState {
	def linkStates = [:], runStateStore
	/** initialise link states from disk. */
	PageState(Store page) {
		runStateStore = page.rebase('runstates.csv')
		linkStates = CSV.nvp(runStateStore)
	}
	/** Helper to set a name/value and save */
	def save(key, value) {
		if (linkStates[key] != value) {
			linkStates[key] = value
			save()
		}
	}
	/** Save the link states and walk up parents telling everyone */
	def save() {
		// save link states - used to display colour for all links
		CSV.nvp(runStateStore, linkStates)
		// save page state - calculate the worst result on the page just run
		setPageState(runStateStore, linkStates.values())
	}
	// States have a value such that the worst is higher
	static states = [
			unknown: 2,
			finalising: 6,
			running: 8,
			empty: 10,
			data: 20,
			succeeded: 50,
			changed: 70,
			created: 80,
			failed: 100,
	]
	def setPageState(page, children) {
		def worst = children.inject('empty') { worst, current ->
			if (!(current in states)) current = 'unknown'
			return (states[worst] > states[current]) ? worst : current
		}
		def pagestate = page.rebase('pagestate.txt')
		if (pagestate.exists()) {
			if (pagestate.text == worst) return
		}
		pagestate.text = worst
	}
}
