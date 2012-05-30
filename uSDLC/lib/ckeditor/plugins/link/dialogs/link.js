﻿/*
 Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.html or http://ckeditor.com/license
 */

CKEDITOR.dialog.add('link', function(a) {
    var b = CKEDITOR.plugins.link,c = function() {
        var E = this.getDialog(),F = E.getContentElement('target', 'popupFeatures'),G = E.getContentElement('target', 'linkTargetName'),H = this.getValue();
        if (!F || !G)return;
        F = F.getElement();
        F.hide();
        G.setValue('');
        switch (H) {
            case 'frame':
                G.setLabel(a.lang.link.targetFrameName);
                G.getElement().show();
                break;
            case 'popup':
                F.show();
                G.setLabel(a.lang.link.targetPopupName);
                G.getElement().show();
                break;
            default:
                G.setValue(H);
                G.getElement().hide();
                break;
        }
    },d = function() {
        var E = this.getDialog(),F = ['urlOptions','anchorOptions','emailOptions'],G = this.getValue(),H = E.definition.getContents('upload'),I = H && H.hidden;
        if (G == 'url') {
            if (a.config.linkShowTargetTab)E.showPage('target');
            if (!I)E.showPage('upload');
        } else {
            E.hidePage('target');
            if (!I)E.hidePage('upload');
        }
        for (var J = 0; J < F.length; J++) {
            var K = E.getContentElement('info', F[J]);
            if (!K)continue;
            K = K.getElement().getParent().getParent();
            if (F[J] == G + 'Options')K.show(); else K.hide();
        }
        E.layout();
    },e = /^javascript:/,f = /^mailto:([^?]+)(?:\?(.+))?$/,g = /subject=([^;?:@&=$,\/]*)/,h = /body=([^;?:@&=$,\/]*)/,i = /^#(.*)$/,j = /^((?:http|https|ftp|news):\/\/)?(.*)$/,k = /^(_(?:self|top|parent|blank))$/,l = /^javascript:void\(location\.href='mailto:'\+String\.fromCharCode\(([^)]+)\)(?:\+'(.*)')?\)$/,m = /^javascript:([^(]+)\(([^)]+)\)$/,n = /\s*window.open\(\s*this\.href\s*,\s*(?:'([^']*)'|null)\s*,\s*'([^']*)'\s*\)\s*;\s*return\s*false;*\s*/,o = /(?:^|,)([^=]+)=(\d+|yes|no)/gi,p = function(E, F) {
        var G = F && (F.data('cke-saved-href') || F.getAttribute('href')) || '',H,I,J,K,L = {};
        if (H = G.match(e))if (y == 'encode')G = G.replace(l, function(ab, ac, ad) {
            return 'mailto:' + String.fromCharCode.apply(String, ac.split(',')) + (ad && w(ad));
        }); else if (y)G.replace(m, function(ab, ac, ad) {
            if (ac == z.name) {
                L.type = 'email';
                var ae = L.email = {},af = /[^,\s]+/g,ag = /(^')|('$)/g,ah = ad.match(af),ai = ah.length,aj,ak;
                for (var al = 0; al < ai; al++) {
                    ak = decodeURIComponent(w(ah[al].replace(ag, '')));
                    aj = z.params[al].toLowerCase();
                    ae[aj] = ak;
                }
                ae.address = [ae.name,ae.domain].join('@');
            }
        });
        if (!L.type)if (J = G.match(i)) {
            L.type = 'anchor';
            L.anchor = {};
            L.anchor.name = L.anchor.id = J[1];
        } else if (I = G.match(f)) {
            var M = G.match(g),N = G.match(h);
            L.type = 'email';
            var O = L.email = {};
            O.address = I[1];
            M && (O.subject = decodeURIComponent(M[1]));
            N && (O.body = decodeURIComponent(N[1]));
        } else if (G && (K = G.match(j))) {
            L.type = 'url';
            L.url = {};
            L.url.protocol = K[1];
            L.url.url = K[2];
        } else L.type = 'url';
        if (F) {
            var P = F.getAttribute('target');
            L.target = {};
            L.adv = {};
            if (!P) {
                var Q = F.data('cke-pa-onclick') || F.getAttribute('onclick'),R = Q && Q.match(n);
                if (R) {
                    L.target.type = 'popup';
                    L.target.name = R[1];
                    var S;
                    while (S = o.exec(R[2])) {
                        if (S[2] == 'yes' || S[2] == '1')L.target[S[1]] = true; else if (isFinite(S[2]))L.target[S[1]] = S[2];
                    }
                }
            } else {
                var T = P.match(k);
                if (T)L.target.type = L.target.name = P; else {
                    L.target.type = 'frame';
                    L.target.name = P;
                }
            }
            var U = this,V = function(ab, ac) {
                var ad = F.getAttribute(ac);
                if (ad !== null)L.adv[ab] = ad || '';
            };
            V('advId', 'id');
            V('advLangDir', 'dir');
            V('advAccessKey', 'accessKey');
            L.adv.advName = F.data('cke-saved-name') || F.getAttribute('name') || '';
            V('advLangCode', 'lang');
            V('advTabIndex', 'tabindex');
            V('advTitle', 'title');
            V('advContentType', 'type');
            V('advCSSClasses', 'class');
            V('advCharset', 'charset');
            V('advStyles', 'style');
        }
        var W = E.document.getElementsByTag('img'),X = new CKEDITOR.dom.nodeList(E.document.$.anchors),Y = L.anchors = [];
        for (var Z = 0; Z < W.count(); Z++) {
            var aa = W.getItem(Z);
            if (aa.data('cke-realelement') && aa.data('cke-real-element-type') == 'anchor')Y.push(E.restoreRealElement(aa));
        }
        for (Z = 0; Z < X.count(); Z++)Y.push(X.getItem(Z));
        for (Z = 0; Z < Y.length; Z++) {
            aa = Y[Z];
            Y[Z] = {name:aa.getAttribute('name'),id:aa.getAttribute('id')};
        }
        this._.selectedElement = F;
        return L;
    },q = function(E, F) {
        if (F[E])this.setValue(F[E][this.id] || '');
    },r = function(E) {
        return q.call(this, 'target', E);
    },s = function(E) {
        return q.call(this, 'adv', E);
    },t = function(E, F) {
        if (!F[E])F[E] = {};
        F[E][this.id] = this.getValue() || '';
    },u = function(E) {
        return t.call(this, 'target', E);
    },v = function(E) {
        return t.call(this, 'adv', E);
    };

    function w(E) {
        return E.replace(/\\'/g, "'");
    }

    ;
    function x(E) {
        return E.replace(/'/g, '\\$&');
    }

    ;
    var y = a.config.emailProtection || '';
    if (y && y != 'encode') {
        var z = {};
        y.replace(/^([^(]+)\(([^)]+)\)$/, function(E, F, G) {
            z.name = F;
            z.params = [];
            G.replace(/[^,\s]+/g, function(H) {
                z.params.push(H);
            });
        });
    }
    function A(E) {
        var F,G = z.name,H = z.params,I,J;
        F = [G,'('];
        for (var K = 0; K < H.length; K++) {
            I = H[K].toLowerCase();
            J = E[I];
            K > 0 && F.push(',');
            F.push("'", J ? x(encodeURIComponent(E[I])) : '', "'");
        }
        F.push(')');
        return F.join('');
    }

    ;
    function B(E) {
        var F,G = E.length,H = [];
        for (var I = 0; I < G; I++) {
            F = E.charCodeAt(I);
            H.push(F);
        }
        return 'String.fromCharCode(' + H.join(',') + ')';
    }

    ;
    var C = a.lang.common,D = a.lang.link;
    return{title:D.title,minWidth:350,minHeight:230,contents:[
        {id:'info',label:D.info,title:D.info,elements:[
            {id:'linkType',type:'select',label:D.type,'default':'url',items:[
                [D.toUrl,'url'],
                [D.toAnchor,'anchor'],
                [D.toEmail,'email']
            ],onChange:d,setup:function(E) {
                if (E.type)this.setValue(E.type);
            },commit:function(E) {
                E.type = this.getValue();
            }},
            {type:'vbox',id:'urlOptions',children:[
                {type:'hbox',widths:['25%','75%'],children:[
                    {id:'protocol',type:'select',label:C.protocol,'default':'http://',items:[
                        ['http://‎','http://'],
                        ['https://‎','https://'],
                        ['ftp://‎','ftp://'],
                        ['news://‎','news://'],
                        [D.other,'']
                    ],setup:function(E) {
                        if (E.url)this.setValue(E.url.protocol || '');
                    },commit:function(E) {
                        if (!E.url)E.url = {};
                        E.url.protocol = this.getValue();
                    }},
                    {type:'text',id:'url',label:C.url,required:true,onLoad:function() {
                        this.allowOnChange = true;
                    },onKeyUp:function() {
                        var J = this;
                        J.allowOnChange = false;
                        var E = J.getDialog().getContentElement('info', 'protocol'),F = J.getValue(),G = /^(http|https|ftp|news):\/\/(?=.)/i,H = /^((javascript:)|[#\/\.\?])/i,I = G.exec(F);
                        if (I) {
                            J.setValue(F.substr(I[0].length));
                            E.setValue(I[0].toLowerCase());
                        } else if (H.test(F))E.setValue('');
                        J.allowOnChange = true;
                    },onChange:function() {
                        if (this.allowOnChange)this.onKeyUp();
                    },validate:function() {
                        var E = this.getDialog();
                        if (E.getContentElement('info', 'linkType') && E.getValueOf('info', 'linkType') != 'url')return true;
                        if (this.getDialog().fakeObj)return true;
                        var F = CKEDITOR.dialog.validate.notEmpty(D.noUrl);
                        return F.apply(this);
                    },setup:function(E) {
                        this.allowOnChange = false;
                        if (E.url)this.setValue(E.url.url);
                        this.allowOnChange = true;
                    },commit:function(E) {
                        this.onChange();
                        if (!E.url)E.url = {};
                        E.url.url = this.getValue();
                        this.allowOnChange = false;
                    }}
                ],setup:function(E) {
                    if (!this.getDialog().getContentElement('info', 'linkType'))this.getElement().show();
                }},
                {type:'button',id:'browse',hidden:'true',filebrowser:'info:url',label:C.browseServer}
            ]},
            {type:'vbox',id:'anchorOptions',width:260,align:'center',padding:0,children:[
                {type:'fieldset',id:'selectAnchorText',label:D.selectAnchor,setup:function(E) {
                    if (E.anchors.length > 0)this.getElement().show(); else this.getElement().hide();
                },children:[
                    {type:'hbox',id:'selectAnchor',children:[
                        {type:'select',id:'anchorName','default':'',label:D.anchorName,style:'width: 100%;',items:[
                            ['']
                        ],setup:function(E) {
                            var H = this;
                            H.clear();
                            H.add('');
                            for (var F = 0; F < E.anchors.length; F++) {
                                if (E.anchors[F].name)H.add(E.anchors[F].name);
                            }
                            if (E.anchor)H.setValue(E.anchor.name);
                            var G = H.getDialog().getContentElement('info', 'linkType');
                            if (G && G.getValue() == 'email')H.focus();
                        },commit:function(E) {
                            if (!E.anchor)E.anchor = {};
                            E.anchor.name = this.getValue();
                        }},
                        {type:'select',id:'anchorId','default':'',label:D.anchorId,style:'width: 100%;',items:[
                            ['']
                        ],setup:function(E) {
                            var G = this;
                            G.clear();
                            G.add('');
                            for (var F = 0; F < E.anchors.length; F++) {
                                if (E.anchors[F].id)G.add(E.anchors[F].id);
                            }
                            if (E.anchor)G.setValue(E.anchor.id);
                        },commit:function(E) {
                            if (!E.anchor)E.anchor = {};
                            E.anchor.id = this.getValue();
                        }}
                    ],setup:function(E) {
                        if (E.anchors.length > 0)this.getElement().show(); else this.getElement().hide();
                    }}
                ]},
                {type:'html',id:'noAnchors',style:'text-align: center;',html:'<div role="label" tabIndex="-1">' + CKEDITOR.tools.htmlEncode(D.noAnchors) + '</div>',focus:true,setup:function(E) {
                    if (E.anchors.length < 1)this.getElement().show();
                    else this.getElement().hide();
                }}
            ],setup:function(E) {
                if (!this.getDialog().getContentElement('info', 'linkType'))this.getElement().hide();
            }},
            {type:'vbox',id:'emailOptions',padding:1,children:[
                {type:'text',id:'emailAddress',label:D.emailAddress,required:true,validate:function() {
                    var E = this.getDialog();
                    if (!E.getContentElement('info', 'linkType') || E.getValueOf('info', 'linkType') != 'email')return true;
                    var F = CKEDITOR.dialog.validate.notEmpty(D.noEmail);
                    return F.apply(this);
                },setup:function(E) {
                    if (E.email)this.setValue(E.email.address);
                    var F = this.getDialog().getContentElement('info', 'linkType');
                    if (F && F.getValue() == 'email')this.select();
                },commit:function(E) {
                    if (!E.email)E.email = {};
                    E.email.address = this.getValue();
                }},
                {type:'text',id:'emailSubject',label:D.emailSubject,setup:function(E) {
                    if (E.email)this.setValue(E.email.subject);
                },commit:function(E) {
                    if (!E.email)E.email = {};
                    E.email.subject = this.getValue();
                }},
                {type:'textarea',id:'emailBody',label:D.emailBody,rows:3,'default':'',setup:function(E) {
                    if (E.email)this.setValue(E.email.body);
                },commit:function(E) {
                    if (!E.email)E.email = {};
                    E.email.body = this.getValue();
                }}
            ],setup:function(E) {
                if (!this.getDialog().getContentElement('info', 'linkType'))this.getElement().hide();
            }}
        ]},
        {id:'target',label:D.target,title:D.target,elements:[
            {type:'hbox',widths:['50%','50%'],children:[
                {type:'select',id:'linkTargetType',label:C.target,'default':'notSet',style:'width : 100%;',items:[
                    [C.notSet,'notSet'],
                    [D.targetFrame,'frame'],
                    [D.targetPopup,'popup'],
                    [C.targetNew,'_blank'],
                    [C.targetTop,'_top'],
                    [C.targetSelf,'_self'],
                    [C.targetParent,'_parent']
                ],onChange:c,setup:function(E) {
                    if (E.target)this.setValue(E.target.type || 'notSet');
                    c.call(this);
                },commit:function(E) {
                    if (!E.target)E.target = {};
                    E.target.type = this.getValue();
                }},
                {type:'text',id:'linkTargetName',label:D.targetFrameName,'default':'',setup:function(E) {
                    if (E.target)this.setValue(E.target.name);
                },commit:function(E) {
                    if (!E.target)E.target = {};
                    E.target.name = this.getValue().replace(/\W/gi, '');
                }}
            ]},
            {type:'vbox',width:'100%',align:'center',padding:2,id:'popupFeatures',children:[
                {type:'fieldset',label:D.popupFeatures,children:[
                    {type:'hbox',children:[
                        {type:'checkbox',id:'resizable',label:D.popupResizable,setup:r,commit:u},
                        {type:'checkbox',id:'status',label:D.popupStatusBar,setup:r,commit:u}
                    ]},
                    {type:'hbox',children:[
                        {type:'checkbox',id:'location',label:D.popupLocationBar,setup:r,commit:u},
                        {type:'checkbox',id:'toolbar',label:D.popupToolbar,setup:r,commit:u}
                    ]},
                    {type:'hbox',children:[
                        {type:'checkbox',id:'menubar',label:D.popupMenuBar,setup:r,commit:u},
                        {type:'checkbox',id:'fullscreen',label:D.popupFullScreen,setup:r,commit:u}
                    ]},
                    {type:'hbox',children:[
                        {type:'checkbox',id:'scrollbars',label:D.popupScrollBars,setup:r,commit:u},
                        {type:'checkbox',id:'dependent',label:D.popupDependent,setup:r,commit:u}
                    ]},
                    {type:'hbox',children:[
                        {type:'text',widths:['50%','50%'],labelLayout:'horizontal',label:C.width,id:'width',setup:r,commit:u},
                        {type:'text',labelLayout:'horizontal',widths:['50%','50%'],label:D.popupLeft,id:'left',setup:r,commit:u}
                    ]},
                    {type:'hbox',children:[
                        {type:'text',labelLayout:'horizontal',widths:['50%','50%'],label:C.height,id:'height',setup:r,commit:u},
                        {type:'text',labelLayout:'horizontal',label:D.popupTop,widths:['50%','50%'],id:'top',setup:r,commit:u}
                    ]}
                ]}
            ]}
        ]},
        {id:'upload',label:D.upload,title:D.upload,hidden:true,filebrowser:'uploadButton',elements:[
            {type:'file',id:'upload',label:C.upload,style:'height:40px',size:29},
            {type:'fileButton',id:'uploadButton',label:C.uploadSubmit,filebrowser:'info:url','for':['upload','upload']}
        ]},
        {id:'advanced',label:D.advanced,title:D.advanced,elements:[
            {type:'vbox',padding:1,children:[
                {type:'hbox',widths:['45%','35%','20%'],children:[
                    {type:'text',id:'advId',label:D.id,setup:s,commit:v},
                    {type:'select',id:'advLangDir',label:D.langDir,'default':'',style:'width:110px',items:[
                        [C.notSet,''],
                        [D.langDirLTR,'ltr'],
                        [D.langDirRTL,'rtl']
                    ],setup:s,commit:v},
                    {type:'text',id:'advAccessKey',width:'80px',label:D.acccessKey,maxLength:1,setup:s,commit:v}
                ]},
                {type:'hbox',widths:['45%','35%','20%'],children:[
                    {type:'text',label:D.name,id:'advName',setup:s,commit:v},
                    {type:'text',label:D.langCode,id:'advLangCode',width:'110px','default':'',setup:s,commit:v},
                    {type:'text',label:D.tabIndex,id:'advTabIndex',width:'80px',maxLength:5,setup:s,commit:v}
                ]}
            ]},
            {type:'vbox',padding:1,children:[
                {type:'hbox',widths:['45%','55%'],children:[
                    {type:'text',label:D.advisoryTitle,'default':'',id:'advTitle',setup:s,commit:v},
                    {type:'text',label:D.advisoryContentType,'default':'',id:'advContentType',setup:s,commit:v}
                ]},
                {type:'hbox',widths:['45%','55%'],children:[
                    {type:'text',label:D.cssClasses,'default':'',id:'advCSSClasses',setup:s,commit:v},
                    {type:'text',label:D.charset,'default':'',id:'advCharset',setup:s,commit:v}
                ]},
                {type:'hbox',children:[
                    {type:'text',label:D.styles,'default':'',id:'advStyles',setup:s,commit:v}
                ]}
            ]}
        ]}
    ],onShow:function() {
        var H = this;
        H.fakeObj = false;
        var E = H.getParentEditor(),F = E.getSelection(),G = null;
        if ((G = b.getSelectedLink(E)) && G.hasAttribute('href'))F.selectElement(G); else if ((G = F.getSelectedElement()) && G.is('img') && G.data('cke-real-element-type') && G.data('cke-real-element-type') == 'anchor') {
            H.fakeObj = G;
            G = E.restoreRealElement(H.fakeObj);
            F.selectElement(H.fakeObj);
        } else G = null;
        H.setupContent(p.apply(H, [E,G]));
    },onOk:function() {
        var E = {},F = [],G = {},H = this,I = this.getParentEditor();
        this.commitContent(G);
        switch (G.type || 'url') {
            case 'url':
                var J = G.url && G.url.protocol != undefined ? G.url.protocol : 'http://',K = G.url && G.url.url || '';
                E['data-cke-saved-href'] = K.indexOf('/') === 0 ? K : J + K;
                break;
            case 'anchor':
                var L = G.anchor && G.anchor.name,M = G.anchor && G.anchor.id;
                E['data-cke-saved-href'] = '#' + (L || M || '');
                break;
            case 'email':
                var N,O = G.email,P = O.address;
                switch (y) {
                    case '':
                    case 'encode':
                        var Q = encodeURIComponent(O.subject || ''),R = encodeURIComponent(O.body || ''),S = [];
                        Q && S.push('subject=' + Q);
                        R && S.push('body=' + R);
                        S = S.length ? '?' + S.join('&') : '';
                        if (y == 'encode') {
                            N = ["javascript:void(location.href='mailto:'+",B(P)];
                            S && N.push("+'", x(S), "'");
                            N.push(')');
                        } else N = ['mailto:',P,S];
                        break;
                    default:
                        var T = P.split('@', 2);
                        O.name = T[0];
                        O.domain = T[1];
                        N = ['javascript:',A(O)];
                }
                E['data-cke-saved-href'] = N.join('');
                break;
        }
        if (G.target)if (G.target.type == 'popup') {
            var U = ["window.open(this.href, '",G.target.name || '',"', '"],V = ['resizable','status','location','toolbar','menubar','fullscreen','scrollbars','dependent'],W = V.length,X = function(ai) {
                if (G.target[ai])V.push(ai + '=' + G.target[ai]);
            };
            for (var Y = 0; Y < W; Y++)V[Y] = V[Y] + (G.target[V[Y]] ? '=yes' : '=no');
            X('width');
            X('left');
            X('height');
            X('top');
            U.push(V.join(','), "'); return false;");
            E['data-cke-pa-onclick'] = U.join('');
            F.push('target');
        } else {
            if (G.target.type != 'notSet' && G.target.name)E.target = G.target.name; else F.push('target');
            F.push('data-cke-pa-onclick', 'onclick');
        }
        if (G.adv) {
            var Z = function(ai, aj) {
                var ak = G.adv[ai];
                if (ak)E[aj] = ak; else F.push(aj);
            };
            Z('advId', 'id');
            Z('advLangDir', 'dir');
            Z('advAccessKey', 'accessKey');
            if (G.adv.advName) {
                E.name = E['data-cke-saved-name'] = G.adv.advName;
                E['class'] = (E['class'] ? E['class'] + ' ' : '') + 'cke_anchor';
            } else F = F.concat(['data-cke-saved-name','name']);
            Z('advLangCode', 'lang');
            Z('advTabIndex', 'tabindex');
            Z('advTitle', 'title');
            Z('advContentType', 'type');
            Z('advCSSClasses', 'class');
            Z('advCharset', 'charset');
            Z('advStyles', 'style');
        }
        E.href = E['data-cke-saved-href'];
        if (!this._.selectedElement) {
            var aa = I.getSelection(),ab = aa.getRanges(true);
            if (ab.length == 1 && ab[0].collapsed) {
                var ac = new CKEDITOR.dom.text(G.type == 'email' ? G.email.address : E['data-cke-saved-href'], I.document);
                ab[0].insertNode(ac);
                ab[0].selectNodeContents(ac);
                aa.selectRanges(ab);
            }
            var ad = new CKEDITOR.style({element:'a',attributes:E});
            ad.type = CKEDITOR.STYLE_INLINE;
            ad.apply(I.document);
        } else {
            var ae = this._.selectedElement,af = ae.data('cke-saved-href'),ag = ae.getHtml();
            if (CKEDITOR.env.ie && E.name != ae.getAttribute('name')) {
                var ah = new CKEDITOR.dom.element('<a name="' + CKEDITOR.tools.htmlEncode(E.name) + '">', I.document);
                aa = I.getSelection();
                ae.copyAttributes(ah, {name:1});
                ae.moveChildren(ah);
                ah.replace(ae);
                ae = ah;
                aa.selectElement(ae);
            }
            ae.setAttributes(E);
            ae.removeAttributes(F);
            if (af == ag || G.type == 'email' && ag.indexOf('@') != -1)ae.setHtml(G.type == 'email' ? G.email.address : E['data-cke-saved-href']);
            if (ae.getAttribute('name'))ae.addClass('cke_anchor'); else ae.removeClass('cke_anchor');
            if (this.fakeObj)I.createFakeElement(ae, 'cke_anchor', 'anchor').replace(this.fakeObj);
            delete this._.selectedElement;
        }
    },onLoad:function() {
        if (!a.config.linkShowAdvancedTab)this.hidePage('advanced');
        if (!a.config.linkShowTargetTab)this.hidePage('target');
    },onFocus:function() {
        var E = this.getContentElement('info', 'linkType'),F;
        if (E && E.getValue() == 'url') {
            F = this.getContentElement('info', 'url');
            F.select();
        }
    }};
});