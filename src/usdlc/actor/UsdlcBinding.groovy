/*
 * Copyright 2011 the Authors for http://usdlc.net
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
package usdlc.actor

class UsdlcBinding extends Binding {
	Binding dslContext

	UsdlcBinding(Map binding, Map dslBinding) {
		super(binding)
		dslContext = dslBinding as Binding
	}

	def getVariable(String name) {
		use(CaseCategory) {
			switch (name) {
				case variables: variables[name]; break
				case dslContext: dslContext[name]; break
				case variables.getters: variables.getters[name](); break
				case dslContext.getters: dslContext.getters[name](); break
				default:
					throw new MissingPropertyException(name, this.class)
					break
			}
		}
	}

	void setVariable(String name, Object value) {
		use(CaseCategory) {
			switch (name) {
				case variables.setters: variables.setters[name](value); break
				case dslContext.setters:
					dslContext.setters[name](value);
					break
				default: variables[name] = value; break
			}
		}
	}

	public static class CaseCategory {
		public static boolean isCase(Map caseValue, Object switchValue) {
			return caseValue?.containsKey(switchValue)
		}
	}
}
