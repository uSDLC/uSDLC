package usdlc

reports = []
Store.with {
	base('rt/reports/').dir(~/.*\..*/) { reportScript ->
		def path = usdlc.Store.split(reportScript)
		reports << "['${decamel(path.name)}','/rt/reports/${path.name}${path.ext}']"
	}
}
write "usdlc.reportItems([['',''],${reports.join(',')}]);"
