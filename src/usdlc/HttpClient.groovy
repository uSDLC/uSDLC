package usdlc

import org.apache.http.client.ResponseHandler
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

class HttpClient {
	def client = new org.apache.http.impl.client.DefaultHttpClient()
	def secondsTimeout = 30

	def get(url) {
		return execute(new HttpGet(url))
	}

	def post(url, args) {
		def entries = []
		args.each {
			if (it.key != 'form') {
				entries << new BasicNameValuePair(it.key, it.value)
			}
		}
		def request = new HttpPost(url)
		request.entity = new UrlEncodedFormEntity(entries, 'UTF-8')
		return execute(request)
	}

	def execute(request) {
		def semaphore = new Semaphore(secondsTimeout, 0)
		def response = null
		def responder = {
			def sl = it.statusLine
			def entity = it.entity
			response = [
					retcode: sl.statusCode,
					status: sl.reasonPhrase,
					header: it.allHeaders.collectEntries{[it.name, it.value]},
					body: entity ? EntityUtils.toString(entity) : '',
			]
			if (entity) entity.content.close()
			semaphore.release()
		} as ResponseHandler
		try {
			client.execute(request, responder)
			semaphore.waitForRelease()
		} catch (e) {
			semaphore.release()
			response = [retcode: '506', status: 'Service Down', body: '']
		}
		return response ?: [retcode: '500', status: 'no response', body: '']
	}
}
