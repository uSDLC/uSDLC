﻿/*
 Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.html or http://ckeditor.com/license
 */

(function() {
	var a = 1,b = 2,c = 4,d = {id:[
		{type:a,name:'id'}
	],classid:[
		{type:a,name:'classid'}
	],codebase:[
		{type:a,name:'codebase'}
	],pluginspage:[
		{type:c,name:'pluginspage'}
	],src:[
		{type:b,name:'movie'},
		{type:c,name:'src'}
	],name:[
		{type:c,name:'name'}
	],align:[
		{type:a,name:'align'}
	],title:[
		{type:a,name:'title'},
		{type:c,name:'title'}
	],'class':[
		{type:a,name:'class'},
		{type:c,name:'class'}
	],width:[
		{type:a,name:'width'},
		{type:c,name:'width'}
	],height:[
		{type:a,name:'height'},
		{type:c,name:'height'}
	],hSpace:[
		{type:a,name:'hSpace'},
		{type:c,name:'hSpace'}
	],vSpace:[
		{type:a,name:'vSpace'},
		{type:c,name:'vSpace'}
	],style:[
		{type:a,name:'style'},
		{type:c,name:'style'}
	],type:[
		{type:c,name:'type'}
	]},e = ['play','loop','Menu','quality','scale','salign','wmode','bgcolor','base','flashvars','allowScriptAccess','allowFullScreen'];
	for (var f = 0; f < e.length; f++)d[e[f]] = [
		{type:c,name:e[f]},
		{type:b,name:e[f]}
	];
	e = ['allowFullScreen','play','loop','Menu'];
	for (f = 0; f < e.length; f++)d[e[f]][0]['default'] = d[e[f]][1]['default'] = true;
	function g(i, j, k) {
		var q = this;
		var l = d[q.id];
		if (!l)return;
		var m = q instanceof CKEDITOR.ui.dialog.checkbox;
		for (var n = 0; n < l.length; n++) {
			var o = l[n];
			switch (o.type) {case a:if (!i)continue;if (i.getAttribute(o.name) !== null) {
				var p = i.getAttribute(o.name);
				if (m)q.setValue(p.toLowerCase() == 'true'); else q.setValue(p);
				return;
			} else if (m)q.setValue(!!o['default']);break;case b:if (!i)continue;if (o.name in k) {
				p = k[o.name];
				if (m)q.setValue(p.toLowerCase() == 'true'); else q.setValue(p);
				return;
			} else if (m)q.setValue(!!o['default']);break;case c:if (!j)continue;if (j.getAttribute(o.name)) {
				p = j.getAttribute(o.name);
				if (m)q.setValue(p.toLowerCase() == 'true'); else q.setValue(p);
				return;
			} else if (m)q.setValue(!!o['default']);
			}
		}
	}

	;
	function h(i, j, k) {
		var s = this;
		var l = d[s.id];
		if (!l)return;
		var m = s.getValue() === '',n = s instanceof CKEDITOR.ui.dialog.checkbox;
		for (var o = 0; o < l.length; o++) {
			var p = l[o];
			switch (p.type) {case a:if (!i)continue;var q = s.getValue();if (m || n && q === p['default'])i.removeAttribute(p.name); else i.setAttribute(p.name, q);break;case b:if (!i)continue;q = s.getValue();if (m || n && q === p['default']) {
				if (p.name in k)k[p.name].remove();
			} else if (p.name in k)k[p.name].setAttribute('value', q); else {
				var r = CKEDITOR.dom.element.createFromHtml('<cke:param></cke:param>', i.getDocument());
				r.setAttributes({name:p.name,value:q});
				if (i.getChildCount() < 1)r.appendTo(i); else r.insertBefore(i.getFirst());
			}break;case c:if (!j)continue;q = s.getValue();if (m || n && q === p['default'])j.removeAttribute(p.name); else j.setAttribute(p.name, q);
			}
		}
	}

	;
	CKEDITOR.dialog.add('flash', function(i) {
		var j = !i.config.flashEmbedTagOnly,k = i.config.flashAddEmbedTag || i.config.flashEmbedTagOnly,l,m = '<div>' + CKEDITOR.tools.htmlEncode(i.lang.common.preview) + '<br>' + '<div id="cke_FlashPreviewLoader' + CKEDITOR.tools.getNextNumber() + '" style="display:none"><div class="loading">&nbsp;</div></div>' + '<div id="cke_FlashPreviewBox' + CKEDITOR.tools.getNextNumber() + '" class="FlashPreviewBox"></div></div>';
		return{title:i.lang.flash.title,minWidth:420,minHeight:310,onShow:function() {
			var z = this;
			z.fakeImage = z.objectNode = z.embedNode = null;
			l = new CKEDITOR.dom.element('embed', i.document);
			var n = z.getSelectedElement();
			if (n && n.getAttribute('_cke_real_element_type') && n.getAttribute('_cke_real_element_type') == 'flash') {
				z.fakeImage = n;
				var o = i.restoreRealElement(n),p = null,q = null,r = {};
				if (o.getName() == 'cke:object') {
					p = o;
					var s = p.getElementsByTag('embed', 'cke');
					if (s.count() > 0)q = s.getItem(0);
					var t = p.getElementsByTag('param', 'cke');
					for (var u = 0,v = t.count(); u < v; u++) {
						var w = t.getItem(u),x = w.getAttribute('name'),y = w.getAttribute('value');
						r[x] = y;
					}
				} else if (o.getName() == 'cke:embed')q = o;
				z.objectNode = p;
				z.embedNode = q;
				z.setupContent(p, q, r, n);
			}
		},onOk:function() {
			var x = this;
			var n = null,o = null,p = null;
			if (!x.fakeImage) {
				if (j) {
					n = CKEDITOR.dom.element.createFromHtml('<cke:object></cke:object>', i.document);
					var q = {classid:'clsid:d27cdb6e-ae6d-11cf-96b8-444553540000',codebase:'http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0'};
					n.setAttributes(q);
				}
				if (k) {
					o = CKEDITOR.dom.element.createFromHtml('<cke:embed></cke:embed>', i.document);
					o.setAttributes({type:'application/x-shockwave-flash',pluginspage:'http://www.macromedia.com/go/getflashplayer'});
					if (n)o.appendTo(n);
				}
			} else {
				n = x.objectNode;
				o = x.embedNode;
			}
			if (n) {
				p = {};
				var r = n.getElementsByTag('param', 'cke');
				for (var s = 0,t = r.count(); s < t; s++)p[r.getItem(s).getAttribute('name')] = r.getItem(s);
			}
			var u = {},v = {};
			x.commitContent(n, o, p, u, v);
			var w = i.createFakeElement(n || o, 'cke_flash', 'flash', true);
			w.setAttributes(v);
			w.setStyles(u);
			if (x.fakeImage) {
				w.replace(x.fakeImage);
				i.getSelection().selectElement(w);
			} else i.insertElement(w);
		},onHide:function() {
			if (this.preview)this.preview.setHtml('');
		},contents:[
			{id:'info',label:i.lang.common.generalTab,accessKey:'I',elements:[
				{type:'vbox',padding:0,children:[
					{type:'hbox',widths:['280px','110px'],align:'right',children:[
						{id:'src',type:'text',label:i.lang.common.url,required:true,validate:CKEDITOR.dialog.validate.notEmpty(i.lang.flash.validateSrc),setup:g,commit:h,onLoad:function() {
							var n = this.getDialog(),o = function(p) {
								l.setAttribute('src', p);
								n.preview.setHtml('<embed height="100%" width="100%" src="' + CKEDITOR.tools.htmlEncode(l.getAttribute('src')) + '" type="application/x-shockwave-flash"></embed>');
							};
							n.preview = n.getContentElement('info', 'preview').getElement().getChild(3);
							this.on('change', function(p) {
								if (p.data && p.data.value)o(p.data.value);
							});
							this.getInputElement().on('change', function(p) {
								o(this.getValue());
							}, this);
						}},
						{type:'button',id:'browse',filebrowser:'info:src',hidden:true,style:'display:inline-block;margin-top:10px;',label:i.lang.common.browseServer}
					]}
				]},
				{type:'hbox',widths:['25%','25%','25%','25%','25%'],children:[
					{type:'text',id:'width',style:'width:95px',label:i.lang.flash.width,validate:CKEDITOR.dialog.validate.integer(i.lang.flash.validateWidth),setup:function(n, o, p, q) {
						g.apply(this, arguments);
						if (q) {
							var r = parseInt(q.$.style.width, 10);
							if (!isNaN(r))this.setValue(r);
						}
					},commit:function(n, o, p, q) {
						h.apply(this, arguments);
						if (this.getValue())q.width = this.getValue() + 'px';
					}},
					{type:'text',id:'height',style:'width:95px',label:i.lang.flash.height,validate:CKEDITOR.dialog.validate.integer(i.lang.flash.validateHeight),setup:function(n, o, p, q) {
						g.apply(this, arguments);
						if (q) {
							var r = parseInt(q.$.style.height, 10);
							if (!isNaN(r))this.setValue(r);
						}
					},commit:function(n, o, p, q) {
						h.apply(this, arguments);
						if (this.getValue())q.height = this.getValue() + 'px';
					}},
					{type:'text',id:'hSpace',style:'width:95px',label:i.lang.flash.hSpace,validate:CKEDITOR.dialog.validate.integer(i.lang.flash.validateHSpace),setup:g,commit:h},
					{type:'text',id:'vSpace',style:'width:95px',label:i.lang.flash.vSpace,validate:CKEDITOR.dialog.validate.integer(i.lang.flash.validateVSpace),setup:g,commit:h}
				]},
				{type:'vbox',children:[
					{type:'html',id:'preview',style:'width:95%;',html:m}
				]}
			]},
			{id:'Upload',hidden:true,filebrowser:'uploadButton',label:i.lang.common.upload,elements:[
				{type:'file',id:'upload',label:i.lang.common.upload,size:38},
				{type:'fileButton',id:'uploadButton',label:i.lang.common.uploadSubmit,filebrowser:'info:src','for':['Upload','upload']}
			]},
			{id:'properties',label:i.lang.flash.propertiesTab,elements:[
				{type:'hbox',widths:['50%','50%'],children:[
					{id:'scale',type:'select',label:i.lang.flash.scale,'default':'',style:'width : 100%;',items:[
						[i.lang.common.notSet,''],
						[i.lang.flash.scaleAll,'showall'],
						[i.lang.flash.scaleNoBorder,'noborder'],
						[i.lang.flash.scaleFit,'exactfit']
					],setup:g,commit:h},
					{id:'allowScriptAccess',type:'select',label:i.lang.flash.access,'default':'',style:'width : 100%;',items:[
						[i.lang.common.notSet,''],
						[i.lang.flash.accessAlways,'always'],
						[i.lang.flash.accessSameDomain,'samedomain'],
						[i.lang.flash.accessNever,'never']
					],setup:g,commit:h}
				]},
				{type:'hbox',widths:['50%','50%'],children:[
					{id:'wmode',type:'select',label:i.lang.flash.windowMode,'default':'',style:'width : 100%;',items:[
						[i.lang.common.notSet,''],
						[i.lang.flash.windowModeWindow,'window'],
						[i.lang.flash.windowModeOpaque,'opaque'],
						[i.lang.flash.windowModeTransparent,'transparent']
					],setup:g,commit:h},
					{id:'quality',type:'select',label:i.lang.flash.quality,'default':'high',style:'width : 100%;',items:[
						[i.lang.common.notSet,''],
						[i.lang.flash.qualityBest,'best'],
						[i.lang.flash.qualityHigh,'high'],
						[i.lang.flash.qualityAutoHigh,'autohigh'],
						[i.lang.flash.qualityMedium,'medium'],
						[i.lang.flash.qualityAutoLow,'autolow'],
						[i.lang.flash.qualityLow,'low']
					],setup:g,commit:h}
				]},
				{type:'hbox',widths:['50%','50%'],children:[
					{id:'align',type:'select',label:i.lang.flash.align,'default':'',style:'width : 100%;',items:[
						[i.lang.common.notSet,''],
						[i.lang.flash.alignLeft,'left'],
						[i.lang.flash.alignAbsBottom,'absBottom'],
						[i.lang.flash.alignAbsMiddle,'absMiddle'],
						[i.lang.flash.alignBaseline,'baseline'],
						[i.lang.flash.alignBottom,'bottom'],
						[i.lang.flash.alignMiddle,'middle'],
						[i.lang.flash.alignRight,'right'],
						[i.lang.flash.alignTextTop,'textTop'],
						[i.lang.flash.alignTop,'top']
					],setup:g,commit:function(n, o, p, q, r) {
						var s = this.getValue();
						h.apply(this, arguments);
						s && (r.align = s);
					}},
					{type:'html',html:'<div></div>'}
				]},
				{type:'fieldset',label:CKEDITOR.tools.htmlEncode(i.lang.flash.flashvars),children:[
					{type:'vbox',padding:0,children:[
						{type:'checkbox',id:'Menu',label:i.lang.flash.chkMenu,'default':true,setup:g,commit:h},
						{type:'checkbox',id:'play',label:i.lang.flash.chkPlay,'default':true,setup:g,commit:h},
						{type:'checkbox',id:'loop',label:i.lang.flash.chkLoop,'default':true,setup:g,commit:h},
						{type:'checkbox',id:'allowFullScreen',label:i.lang.flash.chkFull,'default':true,setup:g,commit:h}
					]}
				]}
			]},
			{id:'advanced',label:i.lang.common.advancedTab,elements:[
				{type:'hbox',widths:['45%','55%'],children:[
					{type:'text',id:'id',label:i.lang.common.id,setup:g,commit:h},
					{type:'text',id:'title',label:i.lang.common.advisoryTitle,setup:g,commit:h}
				]},
				{type:'hbox',widths:['45%','55%'],children:[
					{type:'text',id:'bgcolor',label:i.lang.flash.bgcolor,setup:g,commit:h},
					{type:'text',id:'class',label:i.lang.common.cssClass,setup:g,commit:h}
				]},
				{type:'text',id:'style',label:i.lang.common.cssStyle,setup:g,commit:h}
			]}
		]};
	});
})();
