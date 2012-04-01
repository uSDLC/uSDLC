package usdlc
/**
 * Implement simplified dependency injection in Groovy
 */
class Dependency {
	/**
	 * For dependencies that are not to use the defaults, set them here.
	 *     Dependency.contexts[contextClass] = [(dependencyClass) : instantiationClass]
	 *     Dependency.contexts[contextClass][dependencyClass2] =instantiationClass2
	 */
	static contexts = [:]
	/**
	 * Global dependencies are used if there are no context class specific dependencies
	 *    Dependency.globals[dependencyClass3] = instantiationClass4
	 */
	static globals = [:]
	/**
	 * Any class with dependencies (called a context class) needs a static dependency creator.
	 *    static dependent = Dependency.injection(contextClass)
	 *    def instantiationClass = dependent(dependencyClass).newInstance()
	 *
	 * In production this is all you do, assuming that dependencyClass is the one to use for instantiation.
	 */
	static injection(context) {
		def dependents = contexts.containsKey(context) ? contexts[context] : globals
		return dependents ? { dependents.containsKey(it) ? dependents[it] : it } : { it }
	}
}
