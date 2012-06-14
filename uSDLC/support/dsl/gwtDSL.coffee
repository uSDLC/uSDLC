if not gwt
  statements = []
  gwt = (pattern, action) -> statements.unshift {pattern:pattern, action:action}
  gwt.setup = -> gwt.problems = []
  gwt.processor = (statement) ->
    for item in statements
      match = item.pattern.exec(statement)
      if match
        item.action(match...)
        return # only runs first match
    gwt.problems.push(statement)
  gwt.cleanup = ->
    if gwt.problems.length
      Packages.usdlc.GwtProcessor.update exchange.store, gwt.problems
      exchange.data.put('refresh', 'true')
