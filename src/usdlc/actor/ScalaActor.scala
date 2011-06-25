//package usdlc.actor
///*
//* Copyright 2011 Paul Marrington for http://usdlc.net
//*
//*  Licensed under the Apache License, Version 2.0 (the "License");
//*  you may not use this file except in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*  http://www.apache.org/licenses/LICENSE-2.0
//*
//*  Unless required by applicable law or agreed to in writing, software
//*  distributed under the License is distributed on an "AS IS" BASIS,
//*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//*  See the License for the specific language governing permissions and
//*  limitations under the License.
//*/
///**
//* User: paul
//* Date: 21/06/11
//* Time: 5:59 PM
//*/
///**
// * This is like all other actors with a run method to do all the work.
// */
//class ScalaActor {
//	def run(script: String) {}
//}
//
//import usdlc.CompilingClassLoader
//
//object ScalaActor {
//
//	def apply(script: String) { new ScalaActor().run(script) }
//
//	val scalaClassLoader = new CompilingClassLoader("scala", new ScalaCompiler())
//}
//
//class ScalaCompiler extends CompilingClassLoader.Compiler {
//
//	import usdlc.Filer
//
//	def compile(sourceFile: Filer) {
//		def error(message: String) {
//			println(message)
//		}
//		import usdlc.Store
//		import scala.tools.nsc.reporters.ConsoleReporter
//		import scala.tools.nsc.{Global, Settings}
//		val settings = new Settings(error)
//		settings.outdir.value = Store.base("").getAbsolutePath
//		settings.deprecation.value = true
//		settings.unchecked.value = true
//		val reporter = new ConsoleReporter(settings)
//
//		val compiler = new Global(settings, reporter)
//		(new compiler.Run).compile(List(sourceFile.getStore.getAbsolutePath))
//
//		reporter.printSummary()
//		//if (reporter.hasErrors || reporter.WARNING.count > 0)
//	}
//}