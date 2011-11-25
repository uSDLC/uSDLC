package usdlc.config

dslClassPath = ['usdlc/dsl/']
dslSourcePath = ['support/usdlc/']

alwaysCheckForRecompile = true

compilers = [
		hs: [
				compile: ['ghc -outputdir $outputDir ${source}'],
				run: ['$outputDir/$name'],
		],
]
