package usdlc

class DSL {
	static boolean associate(dsl, actor) {
		def store = Store.base("usdlc/support/usdlc/${dsl}DSL.$actor")
		if (! store.exists()) {
			store.append '\n'
			return true
		}
		return false
	}
}
