if not gwt
  statements = []
  gwt = (pattern, action) -> statements.unshift {pattern:pattern, action:action}
  gwt.setup = -> gwt.problems = []
  gwt.processor = (statement) ->
    for item in statements
      match = item.pattern.exec(statement)
      if match
        print statement
        item.action(match...)
        return # only runs first match
    gwt.problems.push(statement)
  gwt.cleanup = ->
    if gwt.problems.length
      rerun = Packages.usdlc.GwtProcessor.update exchange.store, gwt.problems
      if rerun
        exchange.data.put('Rerun', 'Rerun')
      else
        exchange.data.put('refresh', 'refresh')

  gwt /wait (\d+) seconds?/, (all, seconds) -> sleep seconds
  gwt /[sS]ame setup as for (.*)/, (all, name) ->
    run "Given_#{camelCase name}.gwt"
  gwt /[sS]ame scenario as for (.*)/, (all, name) ->
    run "Given_#{camelCase name}.gwt"
    run "When_#{camelCase name}.gwt"
    run "Then_#{camelCase name}.gwt"
  gwt /[sS]ame script as for (.*)/, (all, name) ->
    run camelCase name
