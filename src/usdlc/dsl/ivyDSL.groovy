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
package usdlc.dsl

//exchange.response.html(false)
worker = new usdlc.Ivy(exchange.response.&write)

organisation = { worker.args.organisation = it; this }
module = { worker.args.module = it; this }

required = { worker.conf(it ?: 'default').group('jars/required').fetch().source(); this }
optional = { worker.conf(it ?: 'default').group('jars/optional').fetch().source(); this }

def source(boolean fetchSource) { worker.fetchSource = fetchSource; this }
def source(String path) { module(path); worker.source(); this }
source = this.&source

download = { String url, String to = '' -> worker.download(url, to) }
