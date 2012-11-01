<html>
<head>
	<title>${exchange.request.query.project} Workflow Report</title>
	<link rel="STYLESHEET" type="text/css"
		href="/usdlc/lib/dhtmlx/dhtmlxgrid_std_full/dhtmlxgrid_std.css">
	<script type="text/javascript" src='/.store/js/dhtmlx.js'></script>
	<script>
		function init() {
			mygrid = new dhtmlXGridObject('gridbox')
			mygrid.setImagePath("/usdlc/lib/dhtmlx/dhtmlxgrid_std_full/imgs/")
			mygrid.enableCSVHeader(true)
			mygrid.enableAutoWidth(true)
			mygrid.enableAutoHeight(true)
			mygrid.init();//initialize grid
			mygrid.setSkin("dhx_skyblue");//set grid skin
			mygrid.load("workflow_csv.groovy?$exchange.request.header.query", 'csv');
			_sortCore = mygrid._sortCore
			mygrid._sortCore = function (col, type, order, arrTS, s) {
				if (col == 0) {
					s.sort(function (a, b) {
						var as = arrTS[a.idd].replace(/<.*?>/g, '')
						var bs = arrTS[b.idd].replace(/<.*?>/g, '')
						if (order == "asc")
							return as > bs ? 1 : -1
						else
							return as < bs ? 1 : -1
					});
				} else {
					if (type == 'na') type = 'str'
					_sortCore(col, type, order, arrTS, s)
				}
			}
			var setSort = function() {
				for (var i = 0; i < mygrid.fldSort.length; i++) {
					mygrid.fldSort[i] = 'str'
				}
			}
			setTimeout(setSort, 1000)
		}
	</script>
</head>
<body onload="init()">
<div id="gridbox"></div>
</body>
</html>
