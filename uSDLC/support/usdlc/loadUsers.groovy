package usdlc

class UserStore {
	def list, creationTime = new Timer();

	UserStore() { load() }

	def load() {
		list = "['${User.list().join(/','/)}']"
		creationTime.reset()
	}

	def getUserList() {
		if (creationTime.minutes > 60) load()
		return list
	}
}

class UserGroupStore {
	def list, creationTime = new Timer();

	UserGroupStore() { load() }

	def load() {
		def groups = [] as Set
		Store.projects().each { projectName ->
			Store.base("~$projectName/").dirs(~/Groups.csv/) { store ->
				store.file.eachLine { line ->
					line.split(/,/).each { group ->
						group = Store.decamel(group.trim())
						groups.add(group)
					}
				}
			}
		}
		groups.removeAll(User.list())
		list = "['${(groups as List).join(/','/)}']"
		creationTime.reset()
	}

	def getGroupList() {
		if (creationTime.minutes > 60) load()
		return list
	}
}

if (exchange.request.query.includeGroups) {
	def groups = exchange.request.session.instance(UserGroupStore).groupList
	if (exchange.request.query.excludeUsers) {
		write groups
	} else {
		def users = exchange.request.session.instance(UserStore).userList
		write "[${users[1..-2]},${groups[1..-2]}]"
	}
} else {
	write exchange.request.session.instance(UserStore).userList
}

