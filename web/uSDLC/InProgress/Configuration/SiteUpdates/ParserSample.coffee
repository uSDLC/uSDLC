pages = changes = 0
page = (page) ->
  pages++
  for link in jsArray page.select 'a[action=page]'
    href = "#{link.attr 'href'}"
    changed = href.replace /\/index.(html|gsp)$/, ''
    if changed isnt href
      link.attr 'href', changed
      page.updated = true
      changes++
  page.save()
section = (section) ->
run "uSDLC_PageParser"
print "#{changes} changes in #{pages} pages"