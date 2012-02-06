package usdlc.config

dslClassPath = ['usdlc/dsl/']
dslSourcePath = ['support/usdlc/dsl/', 'support/usdlc/', '../src/usdlc/dsl/']

alwaysCheckForRecompile = true

compilers = [
		hs: [
				compile: ['ghc -outputdir $outputDir ${source}'],
				run: ['$outputDir/$command'],
		],
		py: [
				compile: [],
				run: ['python $command'],
		]
]
