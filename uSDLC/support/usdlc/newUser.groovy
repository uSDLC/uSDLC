package usdlc

if (!exchange.request.user.isAdmin) {
	write 'Error: Administrator privileges required'
	return
}
query = exchange.request.query

if (! Store.base("~$query.project").exists()) {
	write "Error: unknown project '$query.project'"
	return
}
userPathName = Store.camelCase(query.username)
usersDir = Store.base("~$query.project/usdlc/Environment/Users")
userStore = usersDir.rebase(userPathName)
if (userStore.exists()) {
	write "Error: user '$query.username' already exists"
	return
}

usersPage = new Page(usersDir)
usersPage.createChild(query.username, usersPage.nextSectionId())

Store.base(
	"/usdlc/Environment/Configuration/Templates/Users/Default").
		copyTo(userStore.path)

userPage = new Page(userStore)
userPage.title = query.username
userPage.synopsis = query.notes ?: ""
userPage.forceSave();

login = ["name '$query.username'","email '$query.email'"]
if (query.noAccessGroups) {
	login << "no access\ngroups '$query.noAccessGroups'"
}
if (query.readGroups) {
	login << "read access\ngroups '$query.readGroups'"
}
if (query.writeGroups) {
	login << "write access\ngroups '$query.writeGroups'"
}
if (query.runGroups) {
	login << "execute access\ngroups '$query.runGroups'"
}
userStore.rebase('Login.groovy').text = login.join('\n')
write 'ok'
