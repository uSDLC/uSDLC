package usdlc

def roots = []
Store.base('~/').file.eachDir { File file ->
	def store = Store.base("~/$file.name/usdlc")
	if (!store.exists()) roots << store
}
write "['${roots.join(/','/)}']"

