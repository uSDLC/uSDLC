package usdlc

import org.apache.commons.lang.SystemUtils
/**
 * Used to execute operating sytem commands - as system independent as
 * possible.
 */
class Execute {
	File cwd = new File('.')
	Process process
	OutputStream out = System.out, err = System.err
	int timeout = 600000 // default 10 minute
	def envp = null // defaults to inherit
	/**
	 * Set the directory to be used for the following commands.
	 */
	Execute cd(to) {
		cwd = Store.base(to).file
		return this
	}
	/**
	 * Run a shell script - using the system shell (cmd for windows, etc)
	 */
	Execute shell(command) {
		execute("$shellCmd $command")

		return this
	}
	/**
	 * Raw execute - use for UI or simple programs (not shell scripts or bat).
	 * Does not wait.
	 */
	Execute execute(command) {
		process = command.execute(envp, cwd)
		process.consumeProcessOutput(out, err)
		return this
	}
	/**
	 * Wait for the process to complete, or kill after the timeout (def 1m).
	 */
	Execute waitFor() {
		process.waitForOrKill(timeout)
		return this
	}
	/**
	 * Set the timeout in milliseconds
	 */
	Execute timeout(int milliseconds) {
		timeout = milliseconds
		return this
	}
	/**
	 * Retrieve the exit code for the command
	 */
	def exitCode() { return process.exitValue() }
	static shellCmd = SystemUtils.IS_OS_WINDOWS ? 'cmd /c' : 'bash -c'
}
