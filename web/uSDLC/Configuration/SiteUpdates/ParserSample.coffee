page = (page) ->
  for link in jsArray page.select 'a[action=page]'
    href = link.attr 'href'
    changed = href.replace /\/index.(html|gsp)$/, ''
    if changed isnt href
      link.attr 'href', changed
      page.updated = true
  page.save()
section = (section) ->
run "uSDLC_PageParser"