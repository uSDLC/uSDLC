package usdlc

groups = [] as Set
Store.projects().each { projectName ->
	Store.base("~$projectName/").dirs(~/Groups.csv/) { store ->
		store.file.eachLine { line ->
			line.split(/,/).each {group -> groups.add(group.trim())}
		}
	}
}
println "result $groups"
write "['${(groups as List).join(/','/)}']"
