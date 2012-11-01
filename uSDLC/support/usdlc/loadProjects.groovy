package usdlc

unassigned = exchange.request.query.unassigned ? true : false
projects = Store.projects(unassigned).collect() { Store.decamel(it) }
write "['${projects.join(/','/)}']"
