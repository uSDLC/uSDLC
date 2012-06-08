package usdlc

class Desktop {
	/**
	 * Open a defined URL on the default browser for the current operating system.
	 * Requires Java 6. Should work on Windows, OS X and *nix
	 */
	static openURL(url) {
		try {
			java.awt.Desktop.desktop.browse(URI.create(url))
		} catch (e) {
			System.out.println "\tHeaderless (no local browser)"
		}
	}

	static os = [windows:checkOS('win'), osx:checkOS('mac'),
		linux:checkOS('ux'), unix:checkOS('nix'), solaris:checkOS('sunos')]
	@Lazy static osName = System.getProperty("os.name").toLowerCase()
	@Lazy static isWindows = os.windows
	@Lazy static isOSX = os.osx
	@Lazy static isLinux = os.linux
	@Lazy static isUnix = os.unix
	@Lazy static isSolaris = os.solaris
	static checkOS(against) {osName.indexOf(against) != -1}
	static isOS(name) {os[name.toLowerCase()]}
}
