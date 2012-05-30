import usdlc.Page
import usdlc.Store

query = exchange.request.query

switch (query.command) {
	case 'rename':
		page(query.page) { parent, name -> parent.rename(name, query.newName) }
		break
	case 'create':
		new Page(query.parent).createChild(query.name, query.id)
		break
	case 'delete':
		if (query.page.indexOf('/Environment/Trash') != -1) {
			page(query.page) { parent, name -> parent.delete(name) }
		} else {
			def store = Store.base(query.page)
			def to = Store.base("~home/usdlc/Environment/Trash", store.project)
			paste(query.page, to.pathFromWebBase, 'first', true)
		}
		break
	case 'move':
		paste(query.moving, query.reference, query.position, true)
		break
	case 'paste':
		paste(query.toPaste, query.target, 'last', query.cut)
		break
}

def page(path, closure) {
	path = path.split('/')
	if (path[-1] == 'index.html') path = path[0..-2]
	def parent = new Page(path[0..-2].join('/'))
	def name =path[-1]
	closure(parent, name)
}

def paste(moving, to, position, cut) {
	page(moving) { parent, name ->
		page(to) { toParent, toName ->
			parent.paste(name, toParent, toName, position, cut)
		}
	}
}
