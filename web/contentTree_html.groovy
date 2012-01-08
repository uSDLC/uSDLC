package usdlc

Store.base('~/').file.eachDir { File file ->
	if (file.name.toLowerCase() != 'usdlc' &&
	  new File("$file.absolutePath/usdlc").exists()) {
		def href = "~/$file.name/usdlc/index.html"
		def project = Store.decamel(file.name.capitalize())
		write "<a href='$href' class='usdlc' action='page'>$project<a/>\n"
	}
}

write Store.base('frontPage.html').text
