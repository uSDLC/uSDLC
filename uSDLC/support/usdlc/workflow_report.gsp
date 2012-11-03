<html>
<head>
	<title>${exchange.request.query.project} Workflow Report</title>
	<link rel="STYLESHEET" type="text/css"
		href="/usdlc/lib/dhtmlx/dhtmlxgrid_std_full/dhtmlxgrid_std.css">
	<script type="text/javascript" src='/.store/js/dhtmlx.js'></script>
	<script>
		var mygrid, _sortCore
		function sort_basic(a, b, context) {
			if (a == b) {
				next = context.next
				context.next += 1
				return sorters[next](mygrid.cells(context.aid, next).getValue(),
							mygrid.cells(context.bid, next).getValue(), context)
			}
			if (context.order == "asc")
				return a > b ? 1 : -1
			else
				return a < b ? 1 : -1
		}
		function sort_string(a, b, context) {
			return sort_basic(a.toLowerCase(), b.toLowerCase(), context)
		}
		function sort_link(a, b, context) {
			var re = /<.*?>/g
			return sort_basic(a.replace(re, ''), b.replace(re, ''), context)
		}
		function sort_number(a, b, context) {
			return sort_basic(+a, +b, context)
		}
		function sort_date(a, b, context) {
			return sort_basic(Date.parse(a), Date.parse(b), context)
		}
		var headings =
		'Context,Title,State,Users,Pri,Est,Start,Due,Tags,Ord'
		var filters =
		'#text_filter,#text_filter,#combo_filter,#text_filter,#numeric_filter,#text_filter,#text_filter,#text_filter,#text_filter,#numeric_filter'
		var sorters = [
			sort_string, //Context
			sort_link, // Title
			sort_string, // State
			sort_string, // 'Users'
			sort_number, // 'Priority'
			sort_string, // 'Estimate'
			sort_date, // 'Start'
			sort_date, // 'Due'
			sort_string, // 'Tags'
			sort_number // 'Order'
		]
		function drop_f(movingRowId, moveToRowId) {
		}
		function onLoadEvent() {
			cols = mygrid.getColumnsNum()
			mygrid.setColWidth(0,"270")
			mygrid.setColWidth(1,"200")
			for (var i = 2; i < cols; i++) {
				mygrid.adjustColumnSize(i)
			}
		}
		function init() {
			mygrid = new dhtmlXGridObject('gridbox')
			mygrid.setImagePath("/usdlc/lib/dhtmlx/dhtmlxgrid_std_full/imgs/")
			mygrid.enableAutoWidth(true)
			mygrid.enableAutoHeight(true)
			mygrid.enableMultiselect(true)
			mygrid.enableDragAndDrop(true)
//			mygrid.enableCSVHeader(true)
			mygrid.attachEvent("onDrop", drop_f)
			mygrid.setHeader(headings)
			mygrid.attachHeader(filters)
			mygrid.setColSorting(headings)
			mygrid.init();//initialize grid
			mygrid.setSkin("dhx_skyblue");//set grid skin
			mygrid.attachEvent('onXLE',onLoadEvent)
			mygrid.load("workflow_csv.groovy?$exchange.request.header.query", 'csv')
			_sortCore = mygrid._sortCore
			mygrid._sortCore = function (col, type, order, arrTS, s) {
				s.sort(function(a,b) {
					return sorters[col](
						arrTS[a.idd],arrTS[b.idd],{
							order: order, aid: a.idd, bid: b.idd, next: 0})
				})
			}
		}
		function openLink(href) {
			window.opener.usdlc.absolutePageContents(href)
			window.opener.focus()
		}
	</script>
</head>
<body onload="init()">
<div id="gridbox"></div>
</body>
</html>
