package usdlc.dsl

import usdlc.Ant
// We need to remove the include directive so it can become an ant command.
// use dsl.include instead.
binding.dslContext.remove('include')
ant = Ant.builder(write)
version = new Date().format('yy.MM.dd')
