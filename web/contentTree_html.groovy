package usdlc

Store.base('~/').file.eachDir { File file ->
	if (file.name.toLowerCase() != 'usdlc' &&
	  new File("$file.absolutePath/usdlc").exists()) {
		def href = "~/$file.name/usdlc/index.html"
		def project = file.name.capitalize().replaceAll(~/[\W_\$]/, ' ')
		write "<a href='$href' class='usdlc' action='page'>$project<a/>\n"
	}
}

write Store.base('frontPage.html').text
