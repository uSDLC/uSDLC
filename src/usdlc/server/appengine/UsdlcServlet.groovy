package usdlc.server.appengine

import usdlc.Config

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

/**
 * User: Paul Marrington
 * Date: 11/05/11
 * Time: 9:14 PM
 */
public class UsdlcServlet extends usdlc.server.servletengine.UsdlcServlet {
	static { Config.environment = "appengine" } // so web.groovy can set up for the cloud
}
