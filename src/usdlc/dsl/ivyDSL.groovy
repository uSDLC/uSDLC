package usdlc.dsl

import usdlc.drivers.Ivy

//exchange.response.html(false)
worker = new Ivy(exchange.response.&write)

organisation = { worker.args.organisation = it; this }
module = { worker.args.module = it; this }

update = { worker.conf(it ?: 'default').group('jars').fetch().source(); this }

def source(boolean fetchSource) { worker.fetchSource = fetchSource; this }
def source(String path) { module(path); worker.source(); this }
source = this.&source

download = { String url, String to = '' -> worker.download(url, to) }
