package usdlc.actor

import usdlc.Store
import usdlc.server.Execute
import static usdlc.FileProcessor.fileProcessor
import static usdlc.config.Config.config

class CompilerActor extends Actor {
	void run(Store script) {
		String ext = script.parts.ext
		if (!context.compiles) context.compiles = [:]
		if (!context.compiles[ext]) context.compiles[ext] = []
		context.compiles[ext] << script
	}

	void close() {
		context.compiles.each { String language ->
			Store tmpDir = Store.tmp(language)
			Store full = tmpDir.rebase context.compiles[language].collect {
				Store file -> file.parts.name
			}.join('-') + "." + language

			Compiler compiler = compilers[language] as Compiler
			def binding = [
					outputDir: tmpDir.absolutePath,
					name: full.parts.name,
					Source: full.absolutePath,
			]

			fileProcessor(full.pathFromWebBase, context.compiles[language], {
				Store source, Writer out -> out.write(source.text)
			}, { Store source -> // only compile on change
				compiler.compile.execute(binding, out)
			})
			// run every time
			compiler.run.execute(binding, out)
		}
	}

	void init() {
	}

	static compilers = {
		def list = [:]
		config.compilers.each { String language, Map map ->
			Compiler compiler = list[language] =
				new Compiler(language: language)
			compiler.compile = new Execute(map.compile)
			compiler.run = new Execute(map.run)
		}
		list
	}()

	static class Compiler {
		String language;
		Execute compile, run;
	}
}
