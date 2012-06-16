def now = new Date().time
def base = usdlc.Store.base('.store/clipboard')
if (base && base.exists()) {
	base.dir().sort().reverse().each { clip ->
		def item = usdlc.Store.parseUnique(clip)
		if (item) {
			long age = now - item.date.time
			int shade = 0x0000FF - (age / 60000)   // blue to black over 4 hours
			def colour = Integer.toHexString((shade < 0) ? 0 : shade).padLeft(6, '0')

			out "<a href='$item.path' style='color:#$colour'>$item.title</a>"
		}
	}
}
