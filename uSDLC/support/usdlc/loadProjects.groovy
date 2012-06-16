package usdlc

unassigned = exchange.request.query.unassigned ? true : false
projects = Store.projects(unassigned)
write "['${projects.join(/','/)}']"

