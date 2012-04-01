# Domain Specific Statement Compiler
dss = (line, action) ->
	statement = line
	# Create a function to be called for each word in the statement
	# First word is active and calls this straight away. For later
	# words it is only called by context (earlier words in the statement)
	dsf = (preposition) -> (nextWord, args...) ->
		# This is called when a statement executes.
		func = nextWord?.action?[preposition]
		if func # inner words return map with fields 'action' and 'args'
			func nextWord?.args...
		else if arguments.callee._defaultActor
			# last word has _defaultActor set as it has no following context
			arguments.callee._defaultActor arguments...
		else # word order not specified in a dss definition
			nw = nextWord?.name ? nextWord
			throw "No preposition '#{preposition}' for '#{nw}'"
	# Inner words return a function that when executed will provide a map
	# or action and arguments.
	dsw = (word) -> ->
		action: globals[word]
		args: arguments
		name: word
	# adds new domain words to the global dictionary so that executing
	# a domain statement will find it.
	actor = (word, methodCreator = dsw) ->
		isFirst = (methodCreator == dsf) # first is only immediate execute
		if not globals[word]
			globals[word] = methodCreator word
			globals[word].isFirst = isFirst
		else if globals[word].isFirst != isFirst
			throw "'#{word}' cannot be both left and middle of a command"
	# We have put a word in the dictionary - now set the action against
	# the next word expected.
	activate = (word, key, value, terminus) ->
		return globals[word][key] = value if not globals[word][key]
		if (terminus)
			# last word in domain statement
			if globals[word][key]._defaultActor
				throw """'#{key} #{word}' already used in an earlier DSS for
				<<< #{line} >>>\n"""
			globals[word][key]._defaultActor = value

	rest = statement
	inParameters = false
	# Domain statement is made of of comma separated parts
	part = ->
		return rest if ! rest # all done
		[all, left, right] = /(?:(.*?),)?\s*(.*)/.exec rest
		if left
			[statement, rest] = [left, right]
		else # no left, so finish off the last part
			[statement, rest] = [right, left]
		return statement

	while part()
		# first word drives the boat, inner words collect context,
		# last word hooks to the provided action. One parameter only
		# as we have already split at the comma.
		[first, words..., last, param] = statement.split(/\s/)
		if last isnt undefined
			if param is undefined
				# we have a last, but no param - make last param
				[last, param] = [param, last]
		else # last is undefined
			# just a param after the comma, so no need to do anything
			continue if inParameters # a,b - b is just a parameter
		if /^.*!$/.test(param)  # param! is actually last()
			# make last from param and have no parameters
			words.push last
			[last, param] = [param[0..param.length-2], '']

		actor first, dsf  # drop the first word into the global dictionary

		key = first
		for word in words
			actor word
			method = "#{key}_#{word}"
			# each word has a map of context (being all words before)
			# so in 'a b c d', c has an entry called 'a_b' that will call
			# the user defined code with 'd' as a parameter
			activate word, key, dsf method
			key = method

		if last
			actor last  # activate the last word with the user defined code
			activate last, key, action, true
		else  # activate the only word with the user defined code
			activate first, '_defaultActor', action, true
		inParameters = true
		# for parameters, the same parsing occurs, but we just return the
		# arguments for the user defined code to process.
		action = (args...) -> args
