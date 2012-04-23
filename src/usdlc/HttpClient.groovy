package usdlc

import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.message.BasicNameValuePair
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import org.apache.http.client.ResponseHandler

class HttpClient {
	def client = new org.apache.http.impl.client.DefaultHttpClient()

	def post(url, args) {
		def entries = []
		args.each {
			if (it.key != 'form') {
				entries << new BasicNameValuePair(it.key, it.value)
			}
		}
		def poster = new HttpPost(url)
		poster.entity = new UrlEncodedFormEntity(entries, 'UTF-8')
		def semaphore = new Semaphore(30, 0)
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
			semaphore.release()
		} as ResponseHandler
		client.execute(poster, responder)
		semaphore.waitForRelease()
		return response ?: [retcode: 403, status: 'no response']
	}
}
