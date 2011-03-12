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
package rt.maintenance.Dependencies

import net.usdlc.Ivy

 /**
 * User: Paul Marrington
 * Date: 6/03/11
 * Time: 11:27 AM
 */

/*
 * See  http://ant.apache.org/ivy/history/latest-milestone/use/resolve.html and;
 * for parameters.
 *
 * Example Usage:
 *
 * http://rt/maintenance/Dependencies/ivy.groovy?group=jars/optional&amp;resolve=organisation:org.codehaus.groovy module:groovy conf:default
 */
doc.pre {
	Ivy.retriever(doc).load(env.query.group ?: 'jars/optional', env.query.resolve, env.query.remove)
}
