package usdlc

import java.util.concurrent.TimeUnit

class Semaphore extends java.util.concurrent.Semaphore {
	/**
	 * Default constructor requires a release before first acquire. No fairness to order applied.
	 * Default timeout is 30 minutes
	 */
	Semaphore(int timeout = 1800) {
		super(-1, false)
		this.timeout = timeout
	}
	/**
	 * Drop any outstanding permits and wait for another thread to call release() in this object. If the
	 * timeout is exceeded, give up and return false.
	 */
	boolean wait(Closure action) {
		drainPermits()
		if (tryAcquire(timeout, TimeUnit.SECONDS)) {
			try {
				action()
			} finally {
				release()
			}
			true
		}
		false
	}
	/**
	 * The maximum amount of time to wait for a release() in seconds
	 */
	int timeout
}
