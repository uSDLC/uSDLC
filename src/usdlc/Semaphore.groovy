package usdlc

import java.util.concurrent.TimeUnit

class Semaphore extends java.util.concurrent.Semaphore {
	/**
	 * Default constructor requires a release before first acquire. No
	 * fairness to order applied.
	 * Default timeout is 30 minutes
	 */
	Semaphore(timeout = 1800, permits = -1) {
		super(permits, false)
		this.timeout = timeout
	}
	/**
	 * Drop any outstanding permits and wait for another thread to call
	 * release() in this object. If the
	 * timeout is exceeded, give up and return false.
	 */
	boolean wait(Closure action) {
		try {
			drainPermits()
			if (tryAcquire(timeout, TimeUnit.SECONDS)) {
				try {
					action()
				} finally {
					release()
				}
				return true
			}
			return false
		} catch (e) { return false }
	}
	/**
	 * Wait until another thread has called release (unless already done)
	 */
	boolean waitForRelease() {
		try {
			return tryAcquire(timeout, TimeUnit.SECONDS)
		} catch (e) { return false }
	}
	/**
	 * The maximum amount of time to wait for a release() in seconds
	 */
	int timeout
}
