dsl 'dss'

result = ''
check = (expected) ->
    assert result is expected, "ERROR: '#{result}'\nexpected '#{expected}'"

# Definitions first
dss 'test calling method!', ->
    result = 'test calling method!'

# Now as the domain specialist sees it...
test calling method
check 'test calling method!'