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

import org.apache.tools.ant.BuildEvent
import org.apache.tools.ant.BuildListener

/**
 * User: Paul Marrington
 * Date: 12/03/11
 * Time: 4:25 PM
 */
class Ant extends AntBuilder {
	static builder(log, level = 2) {
		def ant = new Ant()
		ant.log = log
		ant.level = level
		ant.reset()
		return ant
	}

	private Ant() {
		ant = new AntBuilder()
		//ant.project.basedir = config.baseDirectory
	}

	def reset(level = 0) {
		// removing all registered build listeners, including default (that
		// writes to console)
		project.buildListeners.each {
			project.removeBuildListener(it)
		}
		// and adding our own
		project.addBuildListener(new UsdlcBuildListener(log: log,
				level: level ?: this.level))
	}

	def log = {}, ant, level
}

class UsdlcBuildListener implements BuildListener {
	void buildStarted(BuildEvent buildEvent) {}

	void buildFinished(BuildEvent buildEvent) {}

	void targetStarted(BuildEvent buildEvent) {}

	void targetFinished(BuildEvent buildEvent) {}

	void taskStarted(BuildEvent buildEvent) {}

	void taskFinished(BuildEvent buildEvent) {}

	void messageLogged(BuildEvent buildEvent) {
		if (buildEvent.priority <= level) {
			def message = "${buildEvent.message.replaceAll(/\.{2,}/, '')}\n"
			if (!(message ==~ /[\s\.]*/)) {
				log message
				if (level > 2) { Log.inf(message) }
			}
		}
	}

	def log, level
}
