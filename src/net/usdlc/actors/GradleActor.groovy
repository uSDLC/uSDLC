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
package net.usdlc.actors

//import net.usdlc.Browser
//import org.gradle.GradleLauncher
//import org.gradle.api.logging.StandardOutputListener
//
///**
// * User: Paul Marrington
// * Date: 2/03/11
// * Time: 6:46 PM
// */
//class GradleActor {
//	static run(env) {
//		new GradleActor(env: env).run()
//	}
//	/**
//	 * Environment as documented in /uSDLC/TechnicalArchitecture/Actors/
//	 */
//	HashMap env
//	GradleLauncher gradleLauncher
//
//	def run() {
//		def options = (["-b"] << env.script.split(' ')).flatten()
//		GradleLauncher gradle = GradleLauncher.newInstance(options as String[])
//		def doc = new Browser(env.out).html
//		doc.pre {
//			gradle.addStandardOutputListener({ text(it) } as StandardOutputListener)
//			gradle.addStandardErrorListener({ span(it, class: "error") } as StandardOutputListener)
//			gradle.run()
//		}
//	}
//}
