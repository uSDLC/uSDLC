<html xmlns="">
<head>
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<title></title>
	<link type="text/css" rel="stylesheet" href="/.store/css/usdlc.css">
	<link rel="stylesheet" href="/rt/css/print.css" type="text/css" media="print" />
	<% if (usdlc.config.Config.config.port == "80") { %>
	<link
		href="https://plus.google.com/116900499382012938759"
		rel="publisher"/>
	<% } %>
	<!--link type="text/css" rel="stylesheet" href="/rt/css/menu.css"-->
	<script>
		testScripts = []//"/rt/js/menu.coffeescript"]
	</script>
	<link type="text/css" rel="stylesheet" href="/rt/css/menu.css">
</head>
<body>

<table id="pageTitleTable">
	<tr>
		<td id="pageTitleTd" class="section editable"></td>
		<td rowspan="2">
			<img id="pageTitleImage" src="rt/base.logo.small.png" alt="Unifying the Software Development
				Lifecycle using Document Driven Development">

			<div id='pageTitleVersion'>${usdlc.config.Config.version}</div>
		</td>
	</tr>
	<tr>
		<td>
			<table width="98%">
				<tr>
					<td>
						<div id='topMenu'>
							<div class='topMenu red-box rounded'
								href="/rt/top.menu"
								action="loadMainMenu"></div>
						</div>
					</td>
					<td>
						<div id='newsBar'></div>
					</td>
				</tr>
			</table>
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

<div id="pasteList" href="/rt/pasteList.html.groovy"></div>
<script type="text/javascript" src='/.store/js/usdlcPre.js'></script>
<% if (usdlc.config.Config.config.port == "80") { %>
<div style="position:absolute; bottom:10px; left:10px;">
	<a href="https://plus.google.com/116900499382012938759?prsrc=3"
	   style="text-decoration:none;" target="_blank"><img
		src="https://ssl.gstatic.com/images/icons/gplus-32.png" alt=""
		style="border:0;width:32px;height:32px;"/></a>
	<script type="text/javascript"
	        src="https://apis.google.com/js/plusone.js"></script>
	<plusone href="http://usdlc.net"> </plusone>
</div>
<span class="hideOnHover" style="font-size: 24px; display: inline; "><img
	alt="Fork me on GitHub"
	src="https://a248.e.akamai.net/assets.github.com/img/5d21241b64dc708fcbb701f68f72f41e9f1fadd6/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f6c6566745f7265645f6161303030302e706e67"
	style="position: absolute; top: -10; left: -10; border: 0;"/></span>
<% } %>
</body>
</html>
