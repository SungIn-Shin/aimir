/*
This file is part of Ext JS 3.4

Copyright (c) 2011-2013 Sencha Inc

Contact:  http://www.sencha.com/contact

Commercial Usage
Licensees holding valid commercial licenses may use this file in accordance with the Commercial
Software License Agreement provided with the Software or, alternatively, in accordance with the
terms contained in a written agreement between you and Sencha.

If you are unsure which license is appropriate for your use, please contact the sales department
at http://www.sencha.com/contact.

Build date: 2013-04-03 15:07:25
*/
/* SWFObject v2.2 <http://code.google.com/p/swfobject/> 
    is released under the MIT License <http://www.opensource.org/licenses/mit-license.php> 
*/
var swfobject=function(){var D="undefined",r="object",S="Shockwave Flash",W="ShockwaveFlash.ShockwaveFlash",q="application/x-shockwave-flash",R="SWFObjectExprInst",x="onreadystatechange",O=window,j=document,t=navigator,T=false,U=[h],o=[],N=[],I=[],l,Q,E,B,J=false,a=false,n,G,m=true,M=function(){var aa=typeof j.getElementById!=D&&typeof j.getElementsByTagName!=D&&typeof j.createElement!=D,ah=t.userAgent.toLowerCase(),Y=t.platform.toLowerCase(),ae=Y?(/win/).test(Y):/win/.test(ah),ac=Y?(/mac/).test(Y):/mac/.test(ah),af=/webkit/.test(ah)?parseFloat(ah.replace(/^.*webkit\/(\d+(\.\d+)?).*$/,"$1")):false,X=!+"\v1",ag=[0,0,0],ab=null;if(typeof t.plugins!=D&&typeof t.plugins[S]==r){ab=t.plugins[S].description;if(ab&&!(typeof t.mimeTypes!=D&&t.mimeTypes[q]&&!t.mimeTypes[q].enabledPlugin)){T=true;X=false;ab=ab.replace(/^.*\s+(\S+\s+\S+$)/,"$1");ag[0]=parseInt(ab.replace(/^(.*)\..*$/,"$1"),10);ag[1]=parseInt(ab.replace(/^.*\.(.*)\s.*$/,"$1"),10);ag[2]=/[a-zA-Z]/.test(ab)?parseInt(ab.replace(/^.*[a-zA-Z]+(.*)$/,"$1"),10):0}}else{if(typeof O.ActiveXObject!=D){try{var ad=new ActiveXObject(W);if(ad){ab=ad.GetVariable("$version");if(ab){X=true;ab=ab.split(" ")[1].split(",");ag=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}}catch(Z){}}}return{w3:aa,pv:ag,wk:af,ie:X,win:ae,mac:ac}}(),k=function(){if(!M.w3){return}if((typeof j.readyState!=D&&j.readyState=="complete")||(typeof j.readyState==D&&(j.getElementsByTagName("body")[0]||j.body))){f()}if(!J){if(typeof j.addEventListener!=D){j.addEventListener("DOMContentLoaded",f,false)}if(M.ie&&M.win){j.attachEvent(x,function(){if(j.readyState=="complete"){j.detachEvent(x,arguments.callee);f()}});if(O==top){(function(){if(J){return}try{j.documentElement.doScroll("left")}catch(X){setTimeout(arguments.callee,0);return}f()})()}}if(M.wk){(function(){if(J){return}if(!(/loaded|complete/).test(j.readyState)){setTimeout(arguments.callee,0);return}f()})()}s(f)}}();function f(){if(J){return}try{var Z=j.getElementsByTagName("body")[0].appendChild(C("span"));Z.parentNode.removeChild(Z)}catch(aa){return}J=true;var X=U.length;for(var Y=0;Y<X;Y++){U[Y]()}}function K(X){if(J){X()}else{U[U.length]=X}}function s(Y){if(typeof O.addEventListener!=D){O.addEventListener("load",Y,false)}else{if(typeof j.addEventListener!=D){j.addEventListener("load",Y,false)}else{if(typeof O.attachEvent!=D){i(O,"onload",Y)}else{if(typeof O.onload=="function"){var X=O.onload;O.onload=function(){X();Y()}}else{O.onload=Y}}}}}function h(){if(T){V()}else{H()}}function V(){var X=j.getElementsByTagName("body")[0];var aa=C(r);aa.setAttribute("type",q);var Z=X.appendChild(aa);if(Z){var Y=0;(function(){if(typeof Z.GetVariable!=D){var ab=Z.GetVariable("$version");if(ab){ab=ab.split(" ")[1].split(",");M.pv=[parseInt(ab[0],10),parseInt(ab[1],10),parseInt(ab[2],10)]}}else{if(Y<10){Y++;setTimeout(arguments.callee,10);return}}X.removeChild(aa);Z=null;H()})()}else{H()}}function H(){var ag=o.length;if(ag>0){for(var af=0;af<ag;af++){var Y=o[af].id;var ab=o[af].callbackFn;var aa={success:false,id:Y};if(M.pv[0]>0){var ae=c(Y);if(ae){if(F(o[af].swfVersion)&&!(M.wk&&M.wk<312)){w(Y,true);if(ab){aa.success=true;aa.ref=z(Y);ab(aa)}}else{if(o[af].expressInstall&&A()){var ai={};ai.data=o[af].expressInstall;ai.width=ae.getAttribute("width")||"0";ai.height=ae.getAttribute("height")||"0";if(ae.getAttribute("class")){ai.styleclass=ae.getAttribute("class")}if(ae.getAttribute("align")){ai.align=ae.getAttribute("align")}var ah={};var X=ae.getElementsByTagName("param");var ac=X.length;for(var ad=0;ad<ac;ad++){if(X[ad].getAttribute("name").toLowerCase()!="movie"){ah[X[ad].getAttribute("name")]=X[ad].getAttribute("value")}}P(ai,ah,Y,ab)}else{p(ae);if(ab){ab(aa)}}}}}else{w(Y,true);if(ab){var Z=z(Y);if(Z&&typeof Z.SetVariable!=D){aa.success=true;aa.ref=Z}ab(aa)}}}}}function z(aa){var X=null;var Y=c(aa);if(Y&&Y.nodeName=="OBJECT"){if(typeof Y.SetVariable!=D){X=Y}else{var Z=Y.getElementsByTagName(r)[0];if(Z){X=Z}}}return X}function A(){return !a&&F("6.0.65")&&(M.win||M.mac)&&!(M.wk&&M.wk<312)}function P(aa,ab,X,Z){a=true;E=Z||null;B={success:false,id:X};var ae=c(X);if(ae){if(ae.nodeName=="OBJECT"){l=g(ae);Q=null}else{l=ae;Q=X}aa.id=R;if(typeof aa.width==D||(!(/%$/).test(aa.width)&&parseInt(aa.width,10)<310)){aa.width="310"}if(typeof aa.height==D||(!(/%$/).test(aa.height)&&parseInt(aa.height,10)<137)){aa.height="137"}j.title=j.title.slice(0,47)+" - Flash Player Installation";var ad=M.ie&&M.win?"ActiveX":"PlugIn",ac="MMredirectURL="+O.location.toString().replace(/&/g,"%26")+"&MMplayerType="+ad+"&MMdoctitle="+j.title;if(typeof ab.flashvars!=D){ab.flashvars+="&"+ac}else{ab.flashvars=ac}if(M.ie&&M.win&&ae.readyState!=4){var Y=C("div");X+="SWFObjectNew";Y.setAttribute("id",X);ae.parentNode.insertBefore(Y,ae);ae.style.display="none";(function(){if(ae.readyState==4){ae.parentNode.removeChild(ae)}else{setTimeout(arguments.callee,10)}})()}u(aa,ab,X)}}function p(Y){if(M.ie&&M.win&&Y.readyState!=4){var X=C("div");Y.parentNode.insertBefore(X,Y);X.parentNode.replaceChild(g(Y),X);Y.style.display="none";(function(){if(Y.readyState==4){Y.parentNode.removeChild(Y)}else{setTimeout(arguments.callee,10)}})()}else{Y.parentNode.replaceChild(g(Y),Y)}}function g(ab){var aa=C("div");if(M.win&&M.ie){aa.innerHTML=ab.innerHTML}else{var Y=ab.getElementsByTagName(r)[0];if(Y){var ad=Y.childNodes;if(ad){var X=ad.length;for(var Z=0;Z<X;Z++){if(!(ad[Z].nodeType==1&&ad[Z].nodeName=="PARAM")&&!(ad[Z].nodeType==8)){aa.appendChild(ad[Z].cloneNode(true))}}}}}return aa}function u(ai,ag,Y){var X,aa=c(Y);if(M.wk&&M.wk<312){return X}if(aa){if(typeof ai.id==D){ai.id=Y}if(M.ie&&M.win){var ah="";for(var ae in ai){if(ai[ae]!=Object.prototype[ae]){if(ae.toLowerCase()=="data"){ag.movie=ai[ae]}else{if(ae.toLowerCase()=="styleclass"){ah+=' class="'+ai[ae]+'"'}else{if(ae.toLowerCase()!="classid"){ah+=" "+ae+'="'+ai[ae]+'"'}}}}}var af="";for(var ad in ag){if(ag[ad]!=Object.prototype[ad]){af+='<param name="'+ad+'" value="'+ag[ad]+'" />'}}aa.outerHTML='<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"'+ah+">"+af+"</object>";N[N.length]=ai.id;X=c(ai.id)}else{var Z=C(r);Z.setAttribute("type",q);for(var ac in ai){if(ai[ac]!=Object.prototype[ac]){if(ac.toLowerCase()=="styleclass"){Z.setAttribute("class",ai[ac])}else{if(ac.toLowerCase()!="classid"){Z.setAttribute(ac,ai[ac])}}}}for(var ab in ag){if(ag[ab]!=Object.prototype[ab]&&ab.toLowerCase()!="movie"){e(Z,ab,ag[ab])}}aa.parentNode.replaceChild(Z,aa);X=Z}}return X}function e(Z,X,Y){var aa=C("param");aa.setAttribute("name",X);aa.setAttribute("value",Y);Z.appendChild(aa)}function y(Y){var X=c(Y);if(X&&X.nodeName=="OBJECT"){if(M.ie&&M.win){X.style.display="none";(function(){if(X.readyState==4){b(Y)}else{setTimeout(arguments.callee,10)}})()}else{X.parentNode.removeChild(X)}}}function b(Z){var Y=c(Z);if(Y){for(var X in Y){if(typeof Y[X]=="function"){Y[X]=null}}Y.parentNode.removeChild(Y)}}function c(Z){var X=null;try{X=j.getElementById(Z)}catch(Y){}return X}function C(X){return j.createElement(X)}function i(Z,X,Y){Z.attachEvent(X,Y);I[I.length]=[Z,X,Y]}function F(Z){var Y=M.pv,X=Z.split(".");X[0]=parseInt(X[0],10);X[1]=parseInt(X[1],10)||0;X[2]=parseInt(X[2],10)||0;return(Y[0]>X[0]||(Y[0]==X[0]&&Y[1]>X[1])||(Y[0]==X[0]&&Y[1]==X[1]&&Y[2]>=X[2]))?true:false}function v(ac,Y,ad,ab){if(M.ie&&M.mac){return}var aa=j.getElementsByTagName("head")[0];if(!aa){return}var X=(ad&&typeof ad=="string")?ad:"screen";if(ab){n=null;G=null}if(!n||G!=X){var Z=C("style");Z.setAttribute("type","text/css");Z.setAttribute("media",X);n=aa.appendChild(Z);if(M.ie&&M.win&&typeof j.styleSheets!=D&&j.styleSheets.length>0){n=j.styleSheets[j.styleSheets.length-1]}G=X}if(M.ie&&M.win){if(n&&typeof n.addRule==r){n.addRule(ac,Y)}}else{if(n&&typeof j.createTextNode!=D){n.appendChild(j.createTextNode(ac+" {"+Y+"}"))}}}function w(Z,X){if(!m){return}var Y=X?"visible":"hidden";if(J&&c(Z)){c(Z).style.visibility=Y}else{v("#"+Z,"visibility:"+Y)}}function L(Y){var Z=/[\\\"<>\.;]/;var X=Z.exec(Y)!=null;return X&&typeof encodeURIComponent!=D?encodeURIComponent(Y):Y}var d=function(){if(M.ie&&M.win){window.attachEvent("onunload",function(){var ac=I.length;for(var ab=0;ab<ac;ab++){I[ab][0].detachEvent(I[ab][1],I[ab][2])}var Z=N.length;for(var aa=0;aa<Z;aa++){y(N[aa])}for(var Y in M){M[Y]=null}M=null;for(var X in swfobject){swfobject[X]=null}swfobject=null;window.detachEvent("onunload",arguments.callee)})}}();return{registerObject:function(ab,X,aa,Z){if(M.w3&&ab&&X){var Y={};Y.id=ab;Y.swfVersion=X;Y.expressInstall=aa;Y.callbackFn=Z;o[o.length]=Y;w(ab,false)}else{if(Z){Z({success:false,id:ab})}}},getObjectById:function(X){if(M.w3){return z(X)}},embedSWF:function(ab,ah,ae,ag,Y,aa,Z,ad,af,ac){var X={success:false,id:ah};if(M.w3&&!(M.wk&&M.wk<312)&&ab&&ah&&ae&&ag&&Y){w(ah,false);K(function(){ae+="";ag+="";var aj={};if(af&&typeof af===r){for(var al in af){aj[al]=af[al]}}aj.data=ab;aj.width=ae;aj.height=ag;var am={};if(ad&&typeof ad===r){for(var ak in ad){am[ak]=ad[ak]}}if(Z&&typeof Z===r){for(var ai in Z){if(typeof am.flashvars!=D){am.flashvars+="&"+ai+"="+Z[ai]}else{am.flashvars=ai+"="+Z[ai]}}}if(F(Y)){var an=u(aj,am,ah);if(aj.id==ah){w(ah,true)}X.success=true;X.ref=an}else{if(aa&&A()){aj.data=aa;P(aj,am,ah,ac);return}else{w(ah,true)}}if(ac){ac(X)}})}else{if(ac){ac(X)}}},switchOffAutoHideShow:function(){m=false},ua:M,getFlashPlayerVersion:function(){return{major:M.pv[0],minor:M.pv[1],release:M.pv[2]}},hasFlashPlayerVersion:F,createSWF:function(Z,Y,X){if(M.w3){return u(Z,Y,X)}else{return undefined}},showExpressInstall:function(Z,aa,X,Y){if(M.w3&&A()){P(Z,aa,X,Y)}},removeSWF:function(X){if(M.w3){y(X)}},createCSS:function(aa,Z,Y,X){if(M.w3){v(aa,Z,Y,X)}},addDomLoadEvent:K,addLoadEvent:s,getQueryParamValue:function(aa){var Z=j.location.search||j.location.hash;if(Z){if(/\?/.test(Z)){Z=Z.split("?")[1]}if(aa==null){return L(Z)}var Y=Z.split("&");for(var X=0;X<Y.length;X++){if(Y[X].substring(0,Y[X].indexOf("="))==aa){return L(Y[X].substring((Y[X].indexOf("=")+1)))}}}return""},expressInstallCallback:function(){if(a){var X=c(R);if(X&&l){X.parentNode.replaceChild(l,X);if(Q){w(Q,true);if(M.ie&&M.win){l.style.display="block"}}if(E){E(B)}}a=false}}}}();Ext.FlashComponent=Ext.extend(Ext.BoxComponent,{flashVersion:"9.0.115",backgroundColor:"#ffffff",wmode:"opaque",flashVars:undefined,flashParams:undefined,url:undefined,swfId:undefined,swfWidth:"100%",swfHeight:"100%",expressInstall:false,initComponent:function(){Ext.FlashComponent.superclass.initComponent.call(this);this.addEvents("initialize")},onRender:function(){Ext.FlashComponent.superclass.onRender.apply(this,arguments);var b=Ext.apply({allowScriptAccess:"always",bgcolor:this.backgroundColor,wmode:this.wmode},this.flashParams),a=Ext.apply({allowedDomain:document.location.hostname,YUISwfId:this.getId(),YUIBridgeCallback:"Ext.FlashEventProxy.onEvent"},this.flashVars);new swfobject.embedSWF(this.url,this.id,this.swfWidth,this.swfHeight,this.flashVersion,this.expressInstall?Ext.FlashComponent.EXPRESS_INSTALL_URL:undefined,a,b);this.swf=Ext.getDom(this.id);this.el=Ext.get(this.swf)},getSwfId:function(){return this.swfId||(this.swfId="extswf"+(++Ext.Component.AUTO_ID))},getId:function(){return this.id||(this.id="extflashcmp"+(++Ext.Component.AUTO_ID))},onFlashEvent:function(a){switch(a.type){case"swfReady":this.initSwf();return;case"log":return}a.component=this;this.fireEvent(a.type.toLowerCase().replace(/event$/,""),a)},initSwf:function(){this.onSwfReady(!!this.isInitialized);this.isInitialized=true;this.fireEvent("initialize",this)},beforeDestroy:function(){if(this.rendered){swfobject.removeSWF(this.swf.id)}Ext.FlashComponent.superclass.beforeDestroy.call(this)},onSwfReady:Ext.emptyFn});Ext.FlashComponent.EXPRESS_INSTALL_URL="http://swfobject.googlecode.com/svn/trunk/swfobject/expressInstall.swf";Ext.reg("flash",Ext.FlashComponent);Ext.FlashEventProxy={onEvent:function(c,b){var a=Ext.getCmp(c);if(a){a.onFlashEvent(b)}else{arguments.callee.defer(10,this,[c,b])}}};Ext.chart.Chart=Ext.extend(Ext.FlashComponent,{refreshBuffer:100,chartStyle:{padding:10,animationEnabled:true,font:{name:"Tahoma",color:4473924,size:11},dataTip:{padding:5,border:{color:10075112,size:1},background:{color:14346230,alpha:0.9},font:{name:"Tahoma",color:1393291,size:10,bold:true}}},extraStyle:null,seriesStyles:null,disableCaching:Ext.isIE||Ext.isOpera,disableCacheParam:"_dc",initComponent:function(){Ext.chart.Chart.superclass.initComponent.call(this);if(!this.url){this.url=Ext.chart.Chart.CHART_URL}if(this.disableCaching){this.url=Ext.urlAppend(this.url,String.format("{0}={1}",this.disableCacheParam,new Date().getTime()))}this.addEvents("itemmouseover","itemmouseout","itemclick","itemdoubleclick","itemdragstart","itemdrag","itemdragend","beforerefresh","refresh");this.store=Ext.StoreMgr.lookup(this.store)},setStyle:function(a,b){this.swf.setStyle(a,Ext.encode(b))},setStyles:function(a){this.swf.setStyles(Ext.encode(a))},setSeriesStyles:function(b){this.seriesStyles=b;var a=[];Ext.each(b,function(c){a.push(Ext.encode(c))});this.swf.setSeriesStyles(a)},setCategoryNames:function(a){this.swf.setCategoryNames(a)},setLegendRenderer:function(c,b){var a=this;b=b||a;a.removeFnProxy(a.legendFnName);a.legendFnName=a.createFnProxy(function(d){return c.call(b,d)});a.swf.setLegendLabelFunction(a.legendFnName)},setTipRenderer:function(c,b){var a=this;b=b||a;a.removeFnProxy(a.tipFnName);a.tipFnName=a.createFnProxy(function(g,e,f){var d=a.store.getAt(e);return c.call(b,a,d,e,f)});a.swf.setDataTipFunction(a.tipFnName)},setSeries:function(a){this.series=a;this.refresh()},bindStore:function(a,b){if(!b&&this.store){if(a!==this.store&&this.store.autoDestroy){this.store.destroy()}else{this.store.un("datachanged",this.refresh,this);this.store.un("add",this.delayRefresh,this);this.store.un("remove",this.delayRefresh,this);this.store.un("update",this.delayRefresh,this);this.store.un("clear",this.refresh,this)}}if(a){a=Ext.StoreMgr.lookup(a);a.on({scope:this,datachanged:this.refresh,add:this.delayRefresh,remove:this.delayRefresh,update:this.delayRefresh,clear:this.refresh})}this.store=a;if(a&&!b){this.refresh()}},onSwfReady:function(b){Ext.chart.Chart.superclass.onSwfReady.call(this,b);var a;this.swf.setType(this.type);if(this.chartStyle){this.setStyles(Ext.apply({},this.extraStyle,this.chartStyle))}if(this.categoryNames){this.setCategoryNames(this.categoryNames)}if(this.tipRenderer){a=this.getFunctionRef(this.tipRenderer);this.setTipRenderer(a.fn,a.scope)}if(this.legendRenderer){a=this.getFunctionRef(this.legendRenderer);this.setLegendRenderer(a.fn,a.scope)}if(!b){this.bindStore(this.store,true)}this.refresh.defer(10,this)},delayRefresh:function(){if(!this.refreshTask){this.refreshTask=new Ext.util.DelayedTask(this.refresh,this)}this.refreshTask.delay(this.refreshBuffer)},refresh:function(){if(this.fireEvent("beforerefresh",this)!==false){var l=false;var h=[],c=this.store.data.items;for(var f=0,k=c.length;f<k;f++){h[f]=c[f].data}var e=[];var d=0;var m=null;var g=0;if(this.series){d=this.series.length;for(g=0;g<d;g++){m=this.series[g];var b={};for(var a in m){if(a=="style"&&m.style!==null){b.style=Ext.encode(m.style);l=true}else{b[a]=m[a]}}e.push(b)}}if(d>0){for(g=0;g<d;g++){m=e[g];if(!m.type){m.type=this.type}m.dataProvider=h}}else{e.push({type:this.type,dataProvider:h})}this.swf.setDataProvider(e);if(this.seriesStyles){this.setSeriesStyles(this.seriesStyles)}this.fireEvent("refresh",this)}},createFnProxy:function(a){var b="extFnProxy"+(++Ext.chart.Chart.PROXY_FN_ID);Ext.chart.Chart.proxyFunction[b]=a;return"Ext.chart.Chart.proxyFunction."+b},removeFnProxy:function(a){if(!Ext.isEmpty(a)){a=a.replace("Ext.chart.Chart.proxyFunction.","");delete Ext.chart.Chart.proxyFunction[a]}},getFunctionRef:function(a){if(Ext.isFunction(a)){return{fn:a,scope:this}}else{return{fn:a.fn,scope:a.scope||this}}},onDestroy:function(){if(this.refreshTask&&this.refreshTask.cancel){this.refreshTask.cancel()}Ext.chart.Chart.superclass.onDestroy.call(this);this.bindStore(null);this.removeFnProxy(this.tipFnName);this.removeFnProxy(this.legendFnName)}});Ext.reg("chart",Ext.chart.Chart);Ext.chart.Chart.PROXY_FN_ID=0;Ext.chart.Chart.proxyFunction={};Ext.chart.Chart.CHART_URL="http://yui.yahooapis.com/2.8.2/build/charts/assets/charts.swf";Ext.chart.PieChart=Ext.extend(Ext.chart.Chart,{type:"pie",onSwfReady:function(a){Ext.chart.PieChart.superclass.onSwfReady.call(this,a);this.setDataField(this.dataField);this.setCategoryField(this.categoryField)},setDataField:function(a){this.dataField=a;this.swf.setDataField(a)},setCategoryField:function(a){this.categoryField=a;this.swf.setCategoryField(a)}});Ext.reg("piechart",Ext.chart.PieChart);Ext.chart.CartesianChart=Ext.extend(Ext.chart.Chart,{onSwfReady:function(a){Ext.chart.CartesianChart.superclass.onSwfReady.call(this,a);this.labelFn=[];if(this.xField){this.setXField(this.xField)}if(this.yField){this.setYField(this.yField)}if(this.xAxis){this.setXAxis(this.xAxis)}if(this.xAxes){this.setXAxes(this.xAxes)}if(this.yAxis){this.setYAxis(this.yAxis)}if(this.yAxes){this.setYAxes(this.yAxes)}if(Ext.isDefined(this.constrainViewport)){this.swf.setConstrainViewport(this.constrainViewport)}},setXField:function(a){this.xField=a;this.swf.setHorizontalField(a)},setYField:function(a){this.yField=a;this.swf.setVerticalField(a)},setXAxis:function(a){this.xAxis=this.createAxis("xAxis",a);this.swf.setHorizontalAxis(this.xAxis)},setXAxes:function(c){var b;for(var a=0;a<c.length;a++){b=this.createAxis("xAxis"+a,c[a]);this.swf.setHorizontalAxis(b)}},setYAxis:function(a){this.yAxis=this.createAxis("yAxis",a);this.swf.setVerticalAxis(this.yAxis)},setYAxes:function(c){var b;for(var a=0;a<c.length;a++){b=this.createAxis("yAxis"+a,c[a]);this.swf.setVerticalAxis(b)}},createAxis:function(b,d){var e=Ext.apply({},d),c,a;if(this[b]){a=this[b].labelFunction;this.removeFnProxy(a);this.labelFn.remove(a)}if(e.labelRenderer){c=this.getFunctionRef(e.labelRenderer);e.labelFunction=this.createFnProxy(function(f){return c.fn.call(c.scope,f)});delete e.labelRenderer;this.labelFn.push(e.labelFunction)}if(b.indexOf("xAxis")>-1&&e.position=="left"){e.position="bottom"}return e},onDestroy:function(){Ext.chart.CartesianChart.superclass.onDestroy.call(this);Ext.each(this.labelFn,function(a){this.removeFnProxy(a)},this)}});Ext.reg("cartesianchart",Ext.chart.CartesianChart);Ext.chart.LineChart=Ext.extend(Ext.chart.CartesianChart,{type:"line"});Ext.reg("linechart",Ext.chart.LineChart);Ext.chart.ColumnChart=Ext.extend(Ext.chart.CartesianChart,{type:"column"});Ext.reg("columnchart",Ext.chart.ColumnChart);Ext.chart.StackedColumnChart=Ext.extend(Ext.chart.CartesianChart,{type:"stackcolumn"});Ext.reg("stackedcolumnchart",Ext.chart.StackedColumnChart);Ext.chart.BarChart=Ext.extend(Ext.chart.CartesianChart,{type:"bar"});Ext.reg("barchart",Ext.chart.BarChart);Ext.chart.StackedBarChart=Ext.extend(Ext.chart.CartesianChart,{type:"stackbar"});Ext.reg("stackedbarchart",Ext.chart.StackedBarChart);Ext.chart.Axis=function(a){Ext.apply(this,a)};Ext.chart.Axis.prototype={type:null,orientation:"horizontal",reverse:false,labelFunction:null,hideOverlappingLabels:true,labelSpacing:2};Ext.chart.NumericAxis=Ext.extend(Ext.chart.Axis,{type:"numeric",minimum:NaN,maximum:NaN,majorUnit:NaN,minorUnit:NaN,snapToUnits:true,alwaysShowZero:true,scale:"linear",roundMajorUnit:true,calculateByLabelSize:true,position:"left",adjustMaximumByMajorUnit:true,adjustMinimumByMajorUnit:true});Ext.chart.TimeAxis=Ext.extend(Ext.chart.Axis,{type:"time",minimum:null,maximum:null,majorUnit:NaN,majorTimeUnit:null,minorUnit:NaN,minorTimeUnit:null,snapToUnits:true,stackingEnabled:false,calculateByLabelSize:true});Ext.chart.CategoryAxis=Ext.extend(Ext.chart.Axis,{type:"category",categoryNames:null,calculateCategoryCount:false});Ext.chart.Series=function(a){Ext.apply(this,a)};Ext.chart.Series.prototype={type:null,displayName:null};Ext.chart.CartesianSeries=Ext.extend(Ext.chart.Series,{xField:null,yField:null,showInLegend:true,axis:"primary"});Ext.chart.ColumnSeries=Ext.extend(Ext.chart.CartesianSeries,{type:"column"});Ext.chart.LineSeries=Ext.extend(Ext.chart.CartesianSeries,{type:"line"});Ext.chart.BarSeries=Ext.extend(Ext.chart.CartesianSeries,{type:"bar"});Ext.chart.PieSeries=Ext.extend(Ext.chart.Series,{type:"pie",dataField:null,categoryField:null});