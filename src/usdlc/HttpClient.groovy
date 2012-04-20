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
		def semaphore = new Semaphore(30)
		def response = null
		def responder = {
			response = it
			semaphore.release()
		} as ResponseHandler
		client.execute(poster, responder)
		semaphore.wait {}
		if (!response) { return [retcode: 403, status: 'no response'] }
		def sl = response.statusLine
		def headers = response.allHeaders.collectEntries {[it.name, it.value]}
		def entity = response.entity
		def body = entity ? EntityUtils.toString(entity) : ''
		return [
				retcode: sl.statusCode,
				status: sl.reasonPhrase,
				header: headers,
				body: body,
		]
	}
}
