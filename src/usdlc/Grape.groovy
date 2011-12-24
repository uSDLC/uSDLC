package usdlc

/**
 * Grape.grab(
 *   classLoader, refObject, calleeDepth, noExceptions, validate,
 *   autoDownload, preserveFiles, excludes: [[group:'', module: ''],...],
 *   group, groupId, organisation, organization, org, module,
 *   artifactId, artifact, version, revision, rev, conf, scope,
 *   configuration
 * )
 *
 * Grape.addResolver(name: '', root: '', m2compatible: true)
 */
class Grape extends groovy.grape.Grape {

	static grab(Map dependency) {
		// todo: think I'll go back to @Grab?
		groovy.grape.Grape.grab dependency
		// todo: grab source, etc if possible
	}
}
