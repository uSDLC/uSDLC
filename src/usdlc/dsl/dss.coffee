dss = (statement, action) ->
	[adverbs..., noun, param] = statement.split(/\s/)
	base = globals
	key = ''
	for adverb in adverbs
		dsf = (method) -> (target) -> method target.args...
		key += "_#{adverb}"
		base[key] = (target) -> dsf target[key]
