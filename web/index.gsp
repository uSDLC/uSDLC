<html>
<head>
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<title></title>
	<link type="text/css" rel="stylesheet" href="rt/base.css">
	<script type="text/javascript" src='rt/js/load.coffeescript'></script>
</head>
<body>

<table id="pageTitleTable">
	<tr>
		<td id="pageTitleTd" class="section editable" contextmenu="title"></td>
		<td rowspan="2">
			<img id="pageTitleImage" src="rt/base.logo.small.png" alt="Unifying the Software Development
				Lifecycle using Document Driven Development">
		</td>
	</tr>
	<tr>
		<td>
			<div href="rt/toolbar.html" class="toolbar"></div>
		</td>
	</tr>
</table>

<table id="pageContentsTable">
	<tr>
		<td id="contentTreeTd">
				<img id="treeHider" src="lib/circular-icons/arrows_east_west.png">
			<div id="contentTree" class="hidden">
				<ul>
					<li class='jstree-closed'><a href="/" class="contentLink"></a></li>
				</ul>
			</div>
		</td>
		<td id="pageContentsTd">
			<div id="pageContents">
				${usdlc.Store.base('frontPage.html').text}
			</div>
		</td>
		<td id="pageContentsSausages"></td>
	</tr>
</table>

<div id="menuSection" class="contextMenu" href="rt/menuSection.html" action="loadContextMenu"></div>
<div id="menuTitle" class="contextMenu" href="rt/menuTitle.html" action="loadContextMenu"></div>
<div id="menuSourceEditor" class="contextMenu" href="rt/menuSourceEditor.html" action="loadContextMenu"></div>

<div id="pasteList" href="rt/pasteList.html.groovy"></div>

</body>
</html>