package usdlc

import static usdlc.config.Config.config

class User {
	String id

	User(id) {
		this.id = (id != 'anon') ? id : config.userId ?: 'anon'
	}

	boolean authorised(Store path, String action) {
		return !(id == 'anon' && (action == 'save' || action == 'run'))
	}
}
