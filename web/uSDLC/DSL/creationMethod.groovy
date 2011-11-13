dslStore = usdlc.Store.base('dsl/testDSL.groovy')
dslStore.delete()
assert usdlc.DSL.associate('test', 'groovy')
assert dslStore.exists()
assert ! usdlc.DSL.associate('test', 'groovy')
dslStore.delete()