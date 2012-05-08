function formatCommands(commands) {
	var result = '';
	for (var i = 0; i < commands.length; i++) {
		var command = commands[i];
		if (command.type == 'command') {
			result += command.command
			if (command.target) {
				result += ' "' + command.target + '"'
				if (command.value) {
					result += ', "' + command.value + '"';
				}
			}
			result += "\n"
		}
	}
	return result;
}

function parse(testCase, source) {
	var doc = source;
	var commands = [];
	while (doc.length > 0) {
		var line = /(.*)(\r\n|[\r\n])?/.exec(doc);
		var array = /(\w+) \"([^\"]*)\", \"([^\"]+)/.exec(line)
		if (array.length >= 4) {
			var command = new Command();
			command.command = array[1];
			command.target = array[2] || '';
			command.value = array[3] || '';
			commands.push(command);
		}
		doc = doc.substr(line[0].length);
	}
	testCase.setCommands(commands);
}

function format(testCase, name) {
	return formatCommands(testCase.commands);
}
