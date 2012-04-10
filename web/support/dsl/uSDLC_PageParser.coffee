walkPage = (pg) ->
	walkSection = (sect) ->
		section sect
		walkPage subpg for subpg in usdlc.Page.pages sect
	page? pg
	if section
		walkSection pg.synopsis
		walkSection sect for sect in jsArray pg.sections
		walkSection pg.footer

walkPage pg for pg in usdlc.Page.pages()
