editAreaLoader.load_syntax["groovy"] = editAreaLoader.load_syntax["gradle"] = {
	'DISPLAY_NAME' : 'Groovy'
	,'COMMENT_SINGLE': { 1: '//', 2: '@' }
	, 'COMMENT_MULTI': { '/*': '*/' }
	, 'QUOTEMARKS': { 1: "'", 2: '"', 3: "/" }
	, 'KEYWORD_CASE_SENSITIVE': true
	, 'KEYWORDS': {
	    'constants': [
			'null', 'false', 'true'
		]
		, 'types': [
			'String', 'int', 'short', 'long', 'char', 'double', 'byte',
			'float', 'static', 'void', 'private', 'boolean', 'protected',
			'public', 'const', 'class', 'final', 'abstract', 'volatile',
			'enum', 'transient', 'interface', 'Boolean', 'Double', 'Integer', 'Long', 'Byte', 'Long', 'Char'
		]
		, 'statements': [
            'this', 'extends', 'if', 'do', 'while', 'try', 'catch', 'finally',
            'throw', 'throws', 'else', 'for', 'switch', 'continue', 'implements',
            'break', 'case', 'default', 'goto'
		]
 		, 'keywords': [
           'new', 'return', 'import', 'native', 'super', 'package', 'assert', 'synchronized',
           'instanceof', 'strictfp'
		]
		, 'groovy': [
				 'as', 'in', "property", 'def', 'allproperties', 'count', 'get', 'size', 'collect', 'each', 'eachProperty', 'eachPropertyName', 'eachWithIndex', 'find', 'findall', 'findIndexOf', 'grep', 'inject', 'max', 'min', 'reverse', 'reverseEach', 'sort', 'isImmutable', 'asSynchronized', 'flatten', 'intersect', 'join', 'pop', 'reverse', 'subMap', 'toList', 'center', 'padLeft', 'contains', 'eachMatch', 'toCharacter', 'toList', 'toLong', 'toURL', 'tokenize', 'eachFile', 'eachFileRecurse', 'eachByte', 'eachLine', 'readBytes', 'readLine', 'readLines', 'splitEachLine', 'withReader', 'append', 'encodeBase64', 'decodeBase64', 'lterLine', 'transformChar', 'transformLine', 'withOutputStream', 'withPrintWriter', 'withStream', 'withStreams', 'withWriter', 'withWriterAppend', 'write', 'writeLine', 'dump', 'inspect', 'print', 'println', 'step', 'times', 'upto', 'use', 'waitForOrKill', 'text', 'start', 'startDaemon', 'getLastMatcher', 'it', 'toString'
		]
		/*
		 */
	}
	, 'OPERATORS': [
		'+', '-', '/', '*', '=', '<', '>', '%', '!', '?', ':', '&'
	]
	, 'DELIMITERS': [
		'(', ')', '[', ']', '{', '}'
	]
	, 'REGEXPS': {
	    'precompiler': {
	        'search': '()(#[^\r\n]*)()'
			, 'class': 'precompiler'
			, 'modifiers': 'g'
			, 'execute': 'before'
	    }
//		,'gstring': {
//		    'search': '()(\\$[\\w\\b]*)()'
//			, 'class': 'gstring'
//			, 'modifiers': 'gi'
//			, 'execute': 'after'
//		}
	}
	, 'STYLES': {
	    'COMMENTS': 'color: #AAAAAA;'
		, 'QUOTESMARKS': 'color: #6381F8;'
		, 'KEYWORDS': {
		    'constants': 'color: #EE0000;'
			, 'types': 'color: #0000EE;'
			, 'statements': 'color: #60CA00;'
			, 'keywords': 'color: #48BDDF;'
			, 'groovy': 'color: #800000'
		}
		, 'OPERATORS': 'color: #FF00FF;'
		, 'DELIMITERS': 'color: #0038E1;'
		, 'REGEXPS': {
		    'precompiler': 'color: #009900;'
			, 'precompilerstring': 'color: #994400;'
			, 'gstring': 'color: #800000'
		}
	}
};
