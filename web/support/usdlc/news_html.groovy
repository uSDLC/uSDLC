package usdlc

action = exchange.request.query.action ?: 'headline'
write session.instance(News).next."$action"
