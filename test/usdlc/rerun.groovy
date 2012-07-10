package usdlc

resp = new HttpClient().get('http://127.0.0.1:9000?action=rerun&mode=text')
println resp.body
