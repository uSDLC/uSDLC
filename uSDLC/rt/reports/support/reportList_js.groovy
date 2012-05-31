package usdlc

reports = []
Store.with {
	base('usdlc/rt/reports/').dir(~/.*\..*/) { reportScript ->
		def split = usdlc.Store.split(reportScript)
		reports << "['${decamel(split.name)}','/usdlc/rt/reports/${split.name}${split.ext}']"
	}
}
write "usdlc.reportItems([['',''],${reports.join(',')}]);"
