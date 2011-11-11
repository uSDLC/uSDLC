/*
 * Copyright 2011 the Authors for http://usdlc.net
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
