delegate.add usdlc.Screencast

dsc 'note', /^/, "''",
    'Display a note on the screencast prompter.'
dsc 'prompt', /^/, "''",
    'Display a prompt and wait for presenter input before continuing'
    
dsc 'create', /^/, 'screencast', 
    'Create a new screencast. Provide title, subtitle and synopsis.'
    
dsc 'sleep', /^/, '1',
    'Sleep for the specified number of seconds before continuing'
dsc 'timeout', /^/, '1',
    'Wait at prompt for specified number of seconds or "timeout off" for indefinitely'

dsc 'click', /^/, "''",
    'Click on the defined element.
    Enter id, name, link text path, css selector, xpath, class name, 
    tag name or partial link text. Link text is a path with links separated by ->.'
    
dsc 'check', /^/, ['title','section','source'],
    'Checks text content or title, section or source for string or pattern.'
dsc 'title', /check/, "''",
    'Set context to page title'

dsc 'insert', /^/, ['section','link','text'],
    'Insert something into the page - section, link or text.'
dsc 'link', /insert/, "''",
    'Insert a link into the current section - providing text string to link, 
    type of link (html, groovy, etc.) and contents for link. For a
    html link contexts consists of subtitle and synopsis.'
    
dsc 'select', /^/, ['section','source'],
    'Select a section or source block to place into focus.'
dsc 'next', /^/, 'section',
    'Go to the section after the one with focus'

dsc 'section', /check|insert|select|next|menu/, "''",
    'Define the section to operate on'
dsc 'source', /edit|check|select/, "''",
    'Define source component to operate on'

dsc 'keys', /^/, "''",
    'Send keystrokes directly - mostly used for control keys. 
    Separate controls with spaces. Use ^ for ctrl, alt+ or meta+. 
    Named keys are as expected - Up, Tab or F1. 
    To send printable keys, place in double-quotes. 
    Example: ^End Up End Enter "add this line to end of section"'
	
menu = (what) -> screencast.menu what
    
step = (speed) -> screencast.pause speed
slow = -> 'slow'
fast = -> 'fast'