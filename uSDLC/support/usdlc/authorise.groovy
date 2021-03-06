/**
 * // Usage
 * name 'User Name'
 * email 'email@address.net'
 * read access
 * groups 'space separated list can see pages'
 * write access
 * groups 'space separated list can change pages'
 * instrument access
 * groups 'space separated list can run instrumentation'
 */
package usdlc

name = {name -> user.name = name}
email = {email -> user.email = email}
initials = {initials -> user.initials = initials}

access = 'none'
no = {access = 'none'}
read = {access = 'read raw'}
write = {access = 'read raw write'}
execute = {access = 'read raw write run'}
instrument = {access = 'read raw write run'}
admin = {user.isAdmin = true}

groups = { Object[] list ->
	list.each() {it.split().each{user.data.groups[it] = access}}
}
