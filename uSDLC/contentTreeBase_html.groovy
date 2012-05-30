package usdlc

user = exchange.request.user
Store.projectIndexes.each {
	if (user.authorised(it)) {
		def href = it.path
		def project = it.project.name
		write "<a href='$href' class='usdlc' action='page'>$project</a>\n"
	}
}
