package usdlc.actor

/**
 * A DSL is loaded implicitly using the script extension or explicitly with a
 * dsls command.
 * To load DSLs for csv and database processing
 *
 * e.g. dsl.csv.database
 */
class DslInclusions {
	/** set by GroovyActor when adding dsl inclusion to the script. Used for
	 * DslActor to update  */
	Binding binding
	/** getting the property is an alias for loading the DSL  */
	def propertyMissing(String language) {
		def dsl = "${language}DSL"
		DslActor actor = DslActor.newInstance(dsl).clone() as DslActor
		assert actor, "Cannot find a DSL for $language"
		actor.context = binding.variables
		actor.dslContext = binding.dslContext
		actor.run(binding.variables)
		this
	}

	def include(String inclusion) {
		if (binding._includes) binding._includes << inclusion;
		gse.run binding.script.rebase(inclusion).path, binding
	}
}
