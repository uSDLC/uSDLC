package usdlc

import static usdlc.config.Config.config

// todo: once authenticating, remove anonymous full access

class User {
	String id

	User(id) {
		this.id = id ?: config.userId ?: 'anonymous'
	}
	/**
	 * save, raw, read, run
	 */
	boolean authorised(Store path, String action) {
		return !(id == 'anon' && (action == 'save' || action == 'run'))
	}
}
