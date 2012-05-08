dsl 'dss'

result = ''
check = (expected) ->
    assert result is expected, "ERROR: '#{result}'\nexpected '#{expected}'"

# Definitions first
dss 'test boolean', (bool) ->
    result = "0: #{bool}"
dss "test subdomain name", (name) ->
    result = "1: #{name}"
dss "test user domain name, domain", (name, domain) ->
    result = "2: #{name} in #{domain}"
dss "test domain statuses name, values...", (name, values...) ->
    result = "3: #{name} => #{values.join(' or ')}"
dss "test domain name, status values...", (name, values) ->
    result = "4: #{name} => #{values?.join(' or ') ? ''}"
dss "test domain includes map", (map) ->
    items = for k,v of map
        "#{k}: #{v}"
    items.sort()
    result = "5: #{items.join(', ')}"
dss 'test calling method!', ->
    result = 'test calling method!'

# Now as the domain specialist sees it...
test 'nothing'
check '0: nothing'
test subdomain 'mine'
check '1: mine'
test user domain "yours", "theirs"
check "2: yours in theirs"
test domain statuses "mine also", 2, 4, 6
check "3: mine also => 2 or 4 or 6"
test domain "another", status 55, 'never'
check "4: another => 55 or never"
test domain 'lonely'
check '4: lonely => ' 
test domain includes
    you: 1
    me: 1
    them: 33
    us: 2
check "5: me: 1, them: 33, us: 2, you: 1"
test calling method
check 'test calling method!'