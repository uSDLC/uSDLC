<html>
<head>
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<title></title>
	<link type="text/css" rel="stylesheet" href="/store/css/usdlc.css">
</head>
<body>

<table id="pageTitleTable">
	<tr>
		<td id="pageTitleTd" class="section editable" contextmenu="title"></td>
		<td rowspan="2">
			<img id="pageTitleImage" src="rt/base.logo.small.png" alt="Unifying the Software Development
				Lifecycle using Document Driven Development">
			<div id='pageTitleVersion'>${usdlc.config.Config.version}</div>
		</td>
	</tr>
	<tr>
		<td>
			<table width="98%"><tr><td>
				<div href="rt/toolbar.html" class="toolbar"></div>
			</td><td>
				<div id='newsBar'></div>
			</td></tr></table>
		</td>
	</tr>
</table>

<table id="pageContentsTable">
	<tr>
		<td id="contentTreeTd">
			<img id="treeHider" src="lib/circular-icons/arrows_east_west.png"
			     alt="">

			<div id="contentTree" class="hidden">
				<ul>
					<li class='jstree-closed'><a href="/"
					                             class="contentLink"></a></li>
				</ul>
			</div>
		</td>
		<td id="pageContentsTd">
			<div id="pageContents">
				<div style="text-align: center">
					<a href="/frontPage.html">uSDLC</a> -
					<a href="uSDLC/Screencasts/Slideshare/index.html">
						Presentations</a> -
					<a href="http://usdlc.wordpress.com/">News</a> -
					<a href="http://usdlc.proboards.com/">Forum</a> -
					<a href="http://github.usdlc.net">GitHub</a> -
					<a href="/uSDLC/License.html">OSS License</a>

					<p class='newsHeadline'>
						${usdlc.News.cache.headline}
					</p>
					${usdlc.News.cache.description}
				</div>
			</div>
		</td>
		<td id="pageContentsSausages"></td>
	</tr>
</table>

<div id="menuSection" class="contextMenu" href="rt/menuSection.html"
     action="loadContextMenu"></div>
<div id="menuTitle" class="contextMenu" href="rt/menuTitle.html"
     action="loadContextMenu"></div>
<div id="menuSourceEditor" class="contextMenu" href="rt/menuSourceEditor.html"
     action="loadContextMenu"></div>

<div id="pasteList" href="rt/pasteList.html.groovy"></div>
<script type="text/javascript" src='/store/js/usdlcPre.js'></script>
</body>
</html>
