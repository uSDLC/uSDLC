package usdlc

Store.projectRoots.each {
	def href = it.path
	def project = it.project.name
	write "<a href='$href' class='usdlc' action='page'>$project</a>\n"
}
write Store.usdlcRoot.text
