/*
 * Copyright 2011 Paul Marrington for http://usdlc.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package usdlc
/**
 This module does more than just decipher the mime type - it uses the information to act on it. It uses the file extension(s) to decide what action is to be taken. All files can have one or two extensions. The first, or only, is the classic client-side mime-type. The toString fact will return the browser recognised mime-type string - as in text/html or application/javascript.There is more.. a second extension specifies the usdlc.server.servletengine.server side script that will produce this code. The actor entry tells us whether there is a usdlc.server.servletengine.server component and what to run if there is.

 <ul>
 <li>index.txt will send a static text/plain file to the usdlc.server.servletengine.server.
 <li>index.js.groovy will run a groovy script that will produce javascript for the client.
 </ul>

 * User: Paul
 * Date: 23/11/10
 * Time: 12:44 PM
 */
class Filer {
	String serverExt = ''
	String clientExt = ''
	String fullExt = ''
	String filePath
	@Lazy String basePath = filePath[0..-(fullExt.size() + 2)]

	Class actor

	/**
	 * The constructor requires the path and name of the command to be evaluation.
	 * @param path
	 * @return
	 */
	Filer(path) {
		filePath = path
		if (filePath[0] == '/') {
			filePath = filePath.substring(1)
		}
		// Mpst of this work is around how to process a file. If it has one extension treat it as usdlc.server.servletengine.server if it has an actor or client otherwise. With two extensions, the first is client and the second is usdlc.server.servletengine.server (most of the time). An example is index.html.groovy
		def match = (path =~ extRE)
		if (match) {
			actor = getActor('actors', serverExt = match[-1][1])
			if (match.size() == 1) {
				fullExt = serverExt
				if (actor) { // If we have an actor then it is a usdlc.server.servletengine.server extension
					clientExt = ''
				} else { // is client - check for a filter or send as content bytes if none
					actor = getActor('filters', clientExt = serverExt)
					serverExt = ''
				}
			} else {
				clientExt = match[-2][1]
				if (!actor && mimeTypes[serverExt]) {
					fullExt = clientExt = serverExt
					serverExt = ''
				} else {
					fullExt = "${clientExt}.${serverExt}"
				}
			}
		}
	}
	// Used to pull the file extension out of a file name.
	private static final extRE = /\.(\w+)/
	/**
	 * Given a package (actors or filters), see if there is an actor with the matching name.
	 * @param type package name relative to usdlc
	 * @param language Language we are looking for an actor (as in groovy).
	 * @return
	 */
	def getActor(type, language) {
		// no language to check, so no actor possible
		if (!language) { return null }
		def className = "usdlc.${type}.${language[0].toUpperCase()}${language[1..-1]}Actor"
		// We have found out before that this type does not have an actor
		if (noActors.contains(className)) { return null }
		try {
			return Class.forName(className)
		} catch (exception) {
			noActors << className
			return null
		}
	}
	/**
	 * Called to run an actor or set of actors on a page.
	 */
	def pageRunner(toRun) {
		runFiles(~/^Setup\..*/)
		toRun()
		runFiles(~/^Teardown\..*/)
	}
	/**
	 * Run the actor with the correct parameters.
	 */
	void actorRunner() {
		actor?.newInstance()?.run(filePath)
	}
	/**
	 * Run a set of files in the same directory matching a pattern. Used for setup and teardown.
	 * @param matching Pattern to match
	 */
	def runFiles(matching) {
		Store.root(pathTo).dir(matching).each {
			new Filer(it).actorRunner()
		}
	}
	/**
	 * Retrieve the path to the file pointed to by filer.
	 * @return
	 */
	@Lazy pathTo = {
		int slash = filePath.lastIndexOf('/')
		return (slash == -1) ? '/' : filePath.substring(0, slash)
	}()

	static HashSet noActors = new HashSet()

	def save(newContents) {
		// If we don't have a history file for any reason, then we should save the contents of the full file first.
		String before = (history.store.size() < 3) ? "" : new String(contents)
		// Save changed contents to disk
		contents = newContents.bytes
		// Create a history file so we can rebuild any version if and when we want to.
		history.save(env.userId, before, newContents)
	}

	@Lazy history = new History(path: store.path, type: 'updates')
	@Lazy env = Environment.session()

	/**
	 * Return the client-side mime-type to put in a response header.
	 * @return mime-type as in text/plain
	 */
	String mimeType() {
		return mimeTypes[clientExt] ?: "text/html"
	}

	/**
	 * Give a file extension retrieve the template file used if a real fule does not exist.
	 * @param ext Extension to check from the list of usdlc is the config file.
	 * @return The contents of the template.
	 */
	byte[] template(ext) {
		if (!ext) return [] as byte[]
		if (!Config.template[ext]) return [] as byte[]
		return Store.runtime("${Config.template[ext]}.$fullExt").read()
	}

	/**
	 * Call this if the file does not exist and we want to return/display a substitute. It checks full, usdlc.server.servletengine.server and client extensions in order.
	 * @return
	 */
	byte[] getTemplate() {
		return template(fullExt) ?: template(serverExt) ?: template(clientExt)
	}
	/**
	 * Retrieve the static contents of a file if it exists and the contents of a matching template if it doesn't.
	 * @return
	 */
	byte[] getContents() {
		return store.read() ?: template
	}
	/**
	 * Retrieve the static contents of a file if it exists and an empty string if it doesn't.
	 * @return
	 */
	byte[] getRawContents() {
		return store.read()
	}
	/**
	 * Create or replace a file with the bytes provided.
	 * @param contents
	 */
	void setContents(byte[] contents) {
		store.write(contents)
	}

	@Lazy Store store = Store.root(filePath)
	@Lazy String path = store.path

	/**
	 * Mime-types to look for.
	 */
	private static final mimeTypes = [
			323: "text/h323",
			acx: "application/internet-property-stream",
			ai: "application/postscript",
			aif: "audio/x-aiff",
			aifc: "audio/x-aiff",
			aiff: "audio/x-aiff",
			asf: "video/x-ms-asf",
			asr: "video/x-ms-asf",
			asx: "video/x-ms-asf",
			au: "audio/basic",
			avi: "video/x-msvideo",
			axs: "application/olescript",
			bas: "text/plain",
			bcpio: "application/x-bcpio",
			bin: "application/octet-stream",
			bmp: "image/bmp",
			c: "text/plain",
			cat: "application/vnd.ms-pkiseccat",
			cdf: "application/x-cdf",
			cer: "application/x-x509-ca-cert",
			"class": "application/octet-stream",
			clp: "application/x-msclip",
			cmx: "image/x-cmx",
			cod: "image/cis-cod",
			cpio: "application/x-cpio",
			crd: "application/x-mscardfile",
			crl: "application/pkix-crl",
			crt: "application/x-x509-ca-cert",
			csh: "application/x-csh",
			css: "text/css",
			dcr: "application/x-director",
			der: "application/x-x509-ca-cert",
			dir: "application/x-director",
			dll: "application/x-msdownload",
			dms: "application/octet-stream",
			doc: "application/msword",
			dot: "application/msword",
			dvi: "application/x-dvi",
			dxr: "application/x-director",
			eps: "application/postscript",
			etx: "text/x-setext",
			evy: "application/envoy",
			exe: "application/octet-stream",
			fif: "application/fractals",
			flr: "x-world/x-vrml",
			gif: "image/gif",
			gtar: "application/x-gtar",
			gz: "application/x-gzip",
			h: "text/plain",
			hdf: "application/x-hdf",
			hlp: "application/winhlp",
			hqx: "application/mac-binhex40",
			hta: "application/hta",
			htc: "text/x-component",
			htm: "text/html",
			html: "text/html",
			htt: "text/webviewhtml",
			ico: "image/x-icon",
			ief: "image/ief",
			iii: "application/x-iphone",
			ins: "application/x-internet-signup",
			isp: "application/x-internet-signup",
			jfif: "image/pipeg",
			jpe: "image/jpeg",
			jpeg: "image/jpeg",
			jpg: "image/jpeg",
			js: "application/javascript",
			latex: "application/x-latex",
			lha: "application/octet-stream",
			lsf: "video/x-la-asf",
			lsx: "video/x-la-asf",
			lzh: "application/octet-stream",
			m13: "application/x-msmediaview",
			m14: "application/x-msmediaview",
			m3u: "audio/x-mpegurl",
			man: "application/x-troff-man",
			mdb: "application/x-msaccess",
			me: "application/x-troff-me",
			mht: "message/rfc822",
			mhtml: "message/rfc822",
			mid: "audio/mid",
			mny: "application/x-msmoney",
			mov: "video/quicktime",
			movie: "video/x-sgi-movie",
			mp2: "video/mpeg",
			mp3: "audio/mpeg",
			mpa: "video/mpeg",
			mpe: "video/mpeg",
			mpeg: "video/mpeg",
			mpg: "video/mpeg",
			mpp: "application/vnd.ms-project",
			mpv2: "video/mpeg",
			ms: "application/x-troff-ms",
			mvb: "application/x-msmediaview",
			nws: "message/rfc822",
			oda: "application/oda",
			p10: "application/pkcs10",
			p12: "application/x-pkcs12",
			p7b: "application/x-pkcs7-certificates",
			p7c: "application/x-pkcs7-mime",
			p7m: "application/x-pkcs7-mime",
			p7r: "application/x-pkcs7-certreqresp",
			p7s: "application/x-pkcs7-signature",
			pbm: "image/x-portable-bitmap",
			pdf: "application/pdf",
			pfx: "application/x-pkcs12",
			pgm: "image/x-portable-graymap",
			pko: "application/ynd.ms-pkipko",
			pma: "application/x-perfmon",
			pmc: "application/x-perfmon",
			pml: "application/x-perfmon",
			pmr: "application/x-perfmon",
			pmw: "application/x-perfmon",
			png: "image/png",
			pnm: "image/x-portable-anymap",
			pot: "application/vnd.ms-powerpoint",
			ppm: "image/x-portable-pixmap",
			pps: "application/vnd.ms-powerpoint",
			ppt: "application/vnd.ms-powerpoint",
			prf: "application/pics-rules",
			ps: "application/postscript",
			pub: "application/x-mspublisher",
			qt: "video/quicktime",
			ra: "audio/x-pn-realaudio",
			ram: "audio/x-pn-realaudio",
			ras: "image/x-cmu-raster",
			rgb: "image/x-rgb",
			rmi: "audio/mid",
			roff: "application/x-troff",
			rtf: "application/rtf",
			rtx: "text/richtext",
			scd: "application/x-msschedule",
			sct: "text/scriptlet",
			setpay: "application/set-payment-initiation",
			setreg: "application/set-registration-initiation",
			sh: "application/x-sh",
			shar: "application/x-shar",
			sit: "application/x-stuffit",
			snd: "audio/basic",
			spc: "application/x-pkcs7-certificates",
			spl: "application/futuresplash",
			src: "application/x-wais-source",
			sst: "application/vnd.ms-pkicertstore",
			stl: "application/vnd.ms-pkistl",
			stm: "text/html",
			svg: "image/svg+xml",
			sv4cpio: "application/x-sv4cpio",
			sv4crc: "application/x-sv4crc",
			swf: "application/x-shockwave-flash",
			t: "application/x-troff",
			tar: "application/x-tar",
			tcl: "application/x-tcl",
			tex: "application/x-tex",
			texi: "application/x-texinfo",
			texinfo: "application/x-texinfo",
			tgz: "application/x-compressed",
			tif: "image/tiff",
			tiff: "image/tiff",
			tr: "application/x-troff",
			trm: "application/x-msterminal",
			tsv: "text/tab-separated-values",
			txt: "text/plain",
			uls: "text/iuls",
			ustar: "application/x-ustar",
			vcf: "text/x-vcard",
			vrml: "x-world/x-vrml",
			wav: "audio/x-wav",
			wcm: "application/vnd.ms-works",
			wdb: "application/vnd.ms-works",
			wks: "application/vnd.ms-works",
			wmf: "application/x-msmetafile",
			wps: "application/vnd.ms-works",
			wri: "application/x-mswrite",
			wrl: "x-world/x-vrml",
			wrz: "x-world/x-vrml",
			xaf: "x-world/x-vrml",
			xbm: "image/x-xbitmap",
			xla: "application/vnd.ms-excel",
			xlc: "application/vnd.ms-excel",
			xlm: "application/vnd.ms-excel",
			xls: "application/vnd.ms-excel",
			xlt: "application/vnd.ms-excel",
			xlw: "application/vnd.ms-excel",
			xof: "x-world/x-vrml",
			xpm: "image/x-xpixmap",
			xwd: "image/x-xwindowdump",
			z: "application/x-compress",
			zip: "application/zip"
	]
}
