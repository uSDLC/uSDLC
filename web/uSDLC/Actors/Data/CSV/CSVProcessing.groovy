list = csv 'Editing a CSV File'
assert list.size() == 2
assert list[0] == [
    'First Name': 'Fred',
    'Last Name': 'Flinstone',
    Company: 'Boulder Constructions',
    Comments: 'Time Watcher',
    Rate: '10 pebbles/week']

assert list[1]["First Name"] == 'Barney'

// Use iterator to load one line at a time
csv('Editing a CSV File') { person ->
    assert person."First Name" ==~ /(Fred|Barney)/
}