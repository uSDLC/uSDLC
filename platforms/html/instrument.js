(function() {
	var span = document.createElement('span')
	span.setAttr('style', 'display:none;')
	document.body.append(span)
	src = 'http://localhost:9000/usdlc/support/instrumentation.groovy'
	span.innerHTML =
			'<iframe height="0" width="0" frameborder="0" src="'
					+ src + '" id="uSDLC_Instrumentation"></iframe>'
})()
