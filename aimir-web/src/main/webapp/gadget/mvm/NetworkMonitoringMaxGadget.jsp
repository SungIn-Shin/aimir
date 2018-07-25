<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="PRAGRA" content="NO-CACHE">
<meta http-equiv="Expires" content="-1">
	<link href="${ctx}/css/style.css" rel="stylesheet" type="text/css">
	<link href="${ctx}/js/extjs/resources/css/ext-all.css" rel="stylesheet" type="text/css" title="blue" />
	<link href="${ctx}/js/extjs/resources/css/treegrid.css" rel="stylesheet" type="text/css"/>
	    <style type="text/css">
	    	/* Ext-Js Grid 하단 페이징툴바 버튼 간격이 벌어지는 것을 방지 */
	        TABLE{border-collapse: collapse; width:auto;}
	        /* chrome 에서 ext-js 의 grid cell layout 이 어긋나는 부분 보완 */
	        @media screen and (-webkit-min-device-pixel-ratio:0) {
	            .x-grid3-row td.x-grid3-cell {
	                padding-left: 0px;
	                padding-right: 0px;
	            }
	        }
	        
	        /* Ext-Js Grid 하단 페이징툴바 버튼 간격이 벌어지는 것을 방지 */
	        .x-panel-bbar table {border-collapse: collapse; width:auto;}
	        /* grid 안에 button 이 있는 경우 높이조정 */
	        .x-grid3-cell-inner {
	            padding-top: 0px;
	            padding-bottom: 0px;
	        }
	        /* ext-js group grid header 정렬 */
	        .x-grid3-header-offset table {
	          border-collapse: separate;
	          border-spacing: 0px;      
	        } 
	        /* ext-js grid header 정렬 */
	        .x-grid3-hd-inner{
	            text-align: center !important;
	            font-weight: bold !important;
	        }
	        .chartContainer{
	        	/* margin-left: 500px; */
	        }
	        #chartContainer0{
	        	align:center;
	        }
	        #chartContainer1, #chartContainer4{
	        	align:center;
	        }
	        #chartContainer2, #chartContainer3{
	        	align:center;
	        }
	    </style>
	<title>NetworkMonitoringGadget</title>
	<script type="text/javascript" charset="utf-8" src="${ctx}/js/public2.js"></script>
	<script type="text/javascript" charset="utf-8" src="${ctx}/js/jquery.tablescroll.js"></script>
	<script type="text/javascript" src="${ctx}/js/tree/jquery.tree.js"></script>
	<script type="text/javascript" src="${ctx}/js/tree/jquery.tree.min.js"></script>
    <script type="text/javascript" src="${ctx}/js/tree/location.tree.js"></script>
    <script type="text/javascript" src="${ctx}/js/tree/sic.tree.js"></script>
    <%-- Ext-JS 관련 --%>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/ext-all.js"></script>
    <script type="text/javascript" charset="utf-8"  src="${ctx}/js/extjs/ux/GroupHeaderPlugin.js"></script>
    <%-- TreeGrid 관련 js --%>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGridSorter.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGridColumnResizer.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGridNodeUI.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGridLoader.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGridColumns.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/treegrid/TreeGrid.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/util/commonUtil.js"></script>
</head>

 <script type="text/javascript" charset="utf-8">
 
		//공급사ID
    	var supplierId = "${supplierId}";
    	var supplierName = "${supplierName}";
    	var loginId = "";
    	var tabs = {};
        var tabNames = {};
        var i;
        var sType;
         
    	var searchCondition;
        var fcChartDataXml;
        var fcChartRateDataXml;
        var fcChartDataJson;
        var detailChartType;
        
        var searchStartDate;
        var searchEndDate;
        var gridWidth;
        var selectedMeter;
        var fmtMessageNo = "<fmt:message key="aimir.number"/>";
		
        var fmtMessage = new Array();
        fmtMessage[0] = "<fmt:message key='aimir.number'/>";
        fmtMessage[1] = "<fmt:message key='aimir.time.date'/>";
        fmtMessage[2] = "<fmt:message key='aimir.unknown'/>";
        
	    /**
	     * 유저 세션 정보 가져오기
	     */
	    $.getJSON('${ctx}/common/getUserInfo.do',
	            function(json) {
	                if(json.supplierId != ""){
	                    supplierId = json.supplierId;
	                    loginId = json.loginId;
	                }
	            }
	    );
	    
	  /* Window Resize Event*/
	    $(window).resize(function() {
	    	gridWidth = $(window).width()-20;
	    	Ext.QuickTips.init();
            extColumnResize();
            searchList();
	    });
	
		$(function(){
			var locDateFormat = "yymmdd";
			
			 $("#startDate").datepicker({maxDate : '+0m', showOn: 'button', buttonImage: '${ctx}/themes/images/default/setting/calendar.gif',
				 buttonImageOnly: true, dateFormat:locDateFormat, onSelect: function(dateText, inst) { modifyDate(dateText, inst);}} );
			 $("#endDate").datepicker({maxDate : '+0m', showOn: 'button', buttonImage: '${ctx}/themes/images/default/setting/calendar.gif', 
				buttonImageOnly: true, dateFormat:locDateFormat, onSelect: function(dateText, inst) { modifyDate(dateText, inst);}} );
			
			var date = new Date();
	        var year = date.getFullYear();
	        var month = date.getMonth() + 1;
	        var day = date.getDate();
		
	        if(("" + month).length == 1) month = "0" + month;
	        if(("" + day).length == 1) day = "0" + day;
	
	        var setDate      = year + "" + month + "" + day;
	        var dateFullName = "";
		});
		
	    Ext.onReady(function(){
            Ext.QuickTips.init();
           // extColumnResize();
            gridWidth = $(window).width()-20;
            setPeriod(-6);
        });
	    
	    //컬럼 Tooltip
        function addTooltip(value, metadata) {
            if (value != null && value != "" && metadata != null) {
                metadata.attr = 'ext:qtip="' + value + '"';
            }
            return value;
        }
	    
        // Chrome 최선버전에서 Ext-JS Grid 컬럼사이즈 오류 수정
        function extColumnResize() {
            Ext.override(Ext.grid.ColumnModel, {
                getColumnWidth : function(col) {
                    var width = this.config[col].width;
                    var stsize = 4;
                    var chsize = stsize/this.config.length;

                    if (typeof width != 'number') {
                        width = this.defaultWidth;
                    }
                    
                    width = width - chsize;
                    return width;
                }
            });
        }
        
        /* Calendar Date Setting */
        function setPeriod(val, flag) {
			$.getJSON("${ctx}/common/getDate.do", {
				searchDate : '',
				addVal : val,
				supplierId : supplierId
			}, function(json) {
				$('#startDate').val(json.searchDate);
				$('#endDate').val(json.currDate);
				$('#startDateHidden').val(json.dbDate);
				$('#endDateHidden').val(json.dbDate2);
				updateMonitoringFCChart("METER");
				updateMonitoringFCChart("DCU");
				updateMonitoringFCChart("REPEATER");
				updateMonitoringFCChart("RF");
				updateMonitoringFCChart("MMIU");
				//getMeterList();
				getMonitoringData();
			});
		}
        
     	/* datepicker Select */
    	function modifyDate(setDate, inst) {
    		var dateId = '#' + inst.id;
    		var dateIdHidden = '#' + inst.id + 'Hidden';
    		$.getJSON("${ctx}/common/convertLocalDate.do", {
    			dbDate : setDate,
    			supplierId : supplierId
    		}, function(json) {
    			$(dateId).val(json.localDate);
    			$(dateIdHidden).val(setDate);
    			$(dateId).trigger('change');
    			
    		});
    	}

		/* Grid Data Start */
		var monitorSelectOn = false;
		var monitoringDataStore;
		var monitoringDataCol;
		var monitoringDataGrid;
		function getMonitoringData() {
			//gridWidth = $('#monitoringDataDiv').width();
            var pageSize = 20;
            var sStartDate = $("#startDateHidden").val();
            var sEndDate = $("#endDateHidden").val();
            	
			/* monitoringDataStore Start */
            monitoringDataStore = new Ext.data.JsonStore({
                autoLoad: {params:{start: 0, limit: pageSize}},
                url : "${ctx}/gadget/mvm/getMonitoringDataGrid.do",
                baseParams:{
                    supplierId: supplierId,
                    searchStartDate : sStartDate,
                    searchEndDate : sEndDate,
                	groupId: 1,
                },
                root: 'list',
                idProperty:'No',
                totalProperty: 'count',
                fields: [
                	{name:'No'					},
                	{name:'date'				,type:'text'},
                	{name:'energymeterA24'		,type:'text'},
                	{name:'energymeterNA24'		,type:'text'},
                	{name:'energymeterNA48'		,type:'text'},
                	{name:'energymeterUnknown'	,type:'text'},
                	{name:'zruA24'				,type:'text'},
                	{name:'zruNA24'				,type:'text'},
                	{name:'zruNA48'				,type:'text'},
                	{name:'zruUnknown'			,type:'text'},
                	{name:'mmiuA24'				,type:'text'},
                	{name:'mmiuNA24'			,type:'text'},
                	{name:'mmiuNA48'			,type:'text'},
                	{name:'mmiuUnknown'			,type:'text'},
                	{name:'indoorA24'			,type:'text'},
                	{name:'indoorNA24'			,type:'text'},
                	{name:'indoorNA48'			,type:'text'},
                	{name:'indoorUnknown'		,type:'text'},
                	{name:'zeuplsA24'			,type:'text'},
                	{name:'zeuplsNA24'			,type:'text'},
                	{name:'zeuplsNA48'			,type:'text'},
                	{name:'zeuplsUnknown'		,type:'text'},
                	{name:'meterTotal'			,type:'text'},
                	{name:'meterSuccess'		,type:'text'},
                	{name:'contractTotal'		,type:'text'},
                	{name:'contractSuccess'		,type:'text'},
                	{name:'dcuA24'				,type:'text'},
                	{name:'dcuNA24'				,type:'text'},
                	{name:'dcuNA48'				,type:'text'},
                	{name:'dcuUnknown'			,type:'text'},
                	{name:'plciuA24'			,type:'text'},
                	{name:'plciuNA24'			,type:'text'},
                	{name:'plciuNA48'			,type:'text'},
                	{name:'plciuUnknown'		,type:'text'},
                	{name:'ieiuA24'				,type:'text'},
                	{name:'ieiuNA24'			,type:'text'},
                	{name:'ieiuNA48'			,type:'text'},
                	{name:'ieiuUnknown'			,type:'text'},
                	{name:'zbrepeaterA24'		,type:'text'},
                	{name:'zbrepeaterNA24'		,type:'text'},
                	{name:'zbrepeaterNA48'		,type:'text'},
                	{name:'zbrepeaterUnknown'	,type:'text'}
                ],
                listeners : {
                    beforeload: function(store, options) {
                    	options.params || (options.params = {});
                        Ext.apply(options.params, {
                            page: Math.ceil((options.params.start + options.params.limit) / options.params.limit) 
                        });
                    }
                }
            });
            /* monitoringDataStore End */
            
            /* monitoringDataCol Start */            
            monitoringDataCol = new Ext.grid.ColumnModel({
            	defaults: {
                    sortable: true,
                    width: gridWidth-60,
                    menuDisabled: true,
                    renderer: addTooltip
                },
				columns : [
					{header: fmtMessage[0]	,dataIndex:'No'			,width: 50	,align:'center'},
					{header: fmtMessage[1]	,dataIndex:'date'		,width: 150	,align:'center'},
					{header: 'A24'			,dataIndex:'energymeterA24'		,width:60, align:'right'},
					{header: 'NA24'			,dataIndex:'energymeterNA24'	,width:60, align:'right'},
					{header: 'NA48'			,dataIndex:'energymeterNA48'	,width:60, align:'right'},
					{header: fmtMessage[2]	,dataIndex:'energymeterUnknown'	,width:90, align:'right'},
					{header: 'A24'			,dataIndex:'dcuA24'				,width:60, align:'right'},
					{header: 'NA24'			,dataIndex:'dcuNA24'			,width:60, align:'right'},
					{header: 'NA48'			,dataIndex:'dcuNA48'			,width:60, align:'right'},
					{header: fmtMessage[2]	,dataIndex:'dcuUnknown'			,width:90, align:'right'},
					{header: 'A24'			,dataIndex:'zbrepeaterA24'		,width:60, align:'right'},
					{header: 'NA24'			,dataIndex:'zbrepeaterNA24'		,width:60, align:'right'},
					{header: 'NA48'			,dataIndex:'zbrepeaterNA48'		,width:60, align:'right'},
					{header: fmtMessage[2]	,dataIndex:'zbrepeaterUnknown'	,width:90, align:'right'},
					{header: 'A24'			,dataIndex:'zruA24'				,width:60, align:'right'},
					{header: 'NA24'			,dataIndex:'zruNA24'			,width:60, align:'right'},
					{header: 'NA48'			,dataIndex:'zruNA48'			,width:60, align:'right'},
					{header: fmtMessage[2]	,dataIndex:'zruUnknown'			,width:90, align:'right'},
					{header: 'A24'			,dataIndex:'mmiuA24'			,width:60, align:'right'},
					{header: 'NA24'			,dataIndex:'mmiuNA24'			,width:60, align:'right'},
					{header: 'NA48'			,dataIndex:'mmiuNA48'			,width:60, align:'right'},
					{header: fmtMessage[2]	,dataIndex:'mmiuUnknown'		,width:90, align:'right'}
				],
				rows:[[
					{width:50},{width:151},
					{header: 'METER'		,colspan:4	,width:311,		align:'center'},
					{header: 'DCU'			,colspan:4	,width:311, 	align:'center'},
					{header: 'REPEATER'		,colspan:4	,width:311, 	align:'center'},
					{header: 'RF'			,colspan:4	,width:311, 	align:'center'},
					{header: 'MMIU'			,colspan:4	,width:311,		align:'center'}
				]]
            });
            /* monitoringDataCol End */  
            
            /* monitoringDataGrid Start */  
            if(!monitorSelectOn){
            	$('#monitoringDataDiv').html('');
	            monitoringDataGrid = new Ext.grid.GridPanel({
	            	plugins: [new Ext.ux.plugins.GroupHeaderGrid()],
					colModel : monitoringDataCol,
	            	store : monitoringDataStore,
	            	width: gridWidth-60,
					height : 422,
					renderTo: 'monitoringDataDiv',	
					autoScroll: false,
					stripeRows : true,
					columnLines : true,
					layout:{
						autoWidth:false
					},
					loadMask:{
						msg: 'Loading..'
					},
					viewConfig:{
	       				enableRowBody : true,
	                    showPreview : true,
	                    emptyText : 'No data to display'
	       			},
	                bbar : new Ext.PagingToolbar({
	                   pageSize: pageSize,
	                   store: monitoringDataStore,
	                   displayInfo: true,
	                   displayMsg: ' {0} - {1} / {2}'
	               })
	            });
	            monitorSelectOn = true;
            }else{
            	var bottomToolbar = monitoringDataGrid.getBottomToolbar();
            	monitoringDataGrid.reconfigure(monitoringDataStore, monitoringDataCol);
            	bottomToolbar.bindStore(monitoringDataStore);
        	}
            /* monitoringDataGrid End */  
        }
		/* Grid Data End */
		
		/* Search Grid, Chart by Date Condition */
		function searchList(){
			updateMonitoringFCChart("METER");
			updateMonitoringFCChart("DCU");
			updateMonitoringFCChart("REPEATER");
			updateMonitoringFCChart("RF");
			updateMonitoringFCChart("MMIU");
			getMonitoringData();
		}
		
		/* Chart Render */
	    function fcChartRender(i) {
			if(i==0){
			    var fcChart = new FusionCharts({
					type: 'msline',//'MSarea','stackedcolumn2d', 'stackedarea2d', 'msline'
					renderAt : 'chartContainer'+i,
					width : gridWidth-50,
					height : '300',
					dataFormat : 'json',
					dataSource : fcChartDataXml
				}).render();
			}else{
				var fcChart = new FusionCharts({
					type: 'msline',
					renderAt : 'chartContainer'+i,
					width : gridWidth/2-30,
					height : '300',
					dataFormat : 'json',
					dataSource : fcChartDataXml
				}).render();
			}
		};
		
		var tooltipCode = "<div id='headerdiv'>$label</div>"
						+"<div><table width='120' border='1'>"
						+"<tr><td class='labelDiv'>A24</td><td class='allpadding'>$A24</td></tr>"
						+"<tr><td class='labelDiv'>NA24</td><td class='allpadding'>$NA24</td></tr>"
						+"<tr><td class='labelDiv'>NA48</td><td class='allpadding'>$NA48</td></tr>"
						+"<tr><td class='labelDiv'>UNKNOWN</td><td class='allpadding'>$UNKNOWN</td></tr></table></div>";
		
		/* Update Fusion Chart Start */
		function updateMonitoringFCChart(sType){
			var sStartDate = $("#startDateHidden").val();
           	var sEndDate = $("#endDateHidden").val();
			$.getJSON("${ctx}/gadget/mvm/getMonitoringDataChart.do",
				{
					supplierId: supplierId,
                    selectMeter: sType,
                    searchStartDate : sStartDate,
                    searchEndDate : sEndDate,
                	groupId: 1,
				},
				function(json){
					var chartSeries = json.chartSeries;
					var chartData = json.chartData;
					fcChartDataXml = '{"chart": { '
		                   	//+'"plotToolText": "<div>A24:$datavalue</div>", ' +
		                    //+'"drawCrossLine": "1", '
		                    //+'"crossLineAnimation": "1", '
		                    //+'"crossLineColor": "#0d0d0d", '
		                    //+'"crossLineAlpha": "100", '
			                +'"caption": "'+sType+'", '
		                    +'"showvalues": "0", '
		                    +'"showBorder": "1", '
		                    
		                    +'"labeldisplay": "ROTATE", '
		                    +'"slantlabels": "1", '
		                    
		                    +'"animation": "1", '
		                    
		                    +'"paletteColors": "#0718D7,#A99903,#FC8F00,#F31523", '
		                    
		                    +'"exportEnabled" :"1", '
		                    +'"exportEnabled" :"1", '
	        				+'"exportMode": "client", '
	        				+'"exportShowMenuItem": "1", '
	        				+'"exportFileName":"'+ sType +'", '

	        				+'"formatnumberscale": "0", '
		                    +'"yAxisValueDecimals": "0", '
		                    +'"forceYAxisValueDecimals": "0", '

	        				+'"showXAxisLine": "1", '
		                    +'"xAxisLineColor": "#999999", '
		                    +'"axisLineAlpha": "25", '
		                    +'"divlineColor": "#999999", '
		                    +'"divLineDashed": "1", '
		                    +'"xAxisName": "Date", '
		                    +'"xAxisNameFontSize": "14", '
		                    +'"xAxisNameFontBold": "1", '

		                    +'"yAxisName": "Total", '
		                    +'"yAxisNameFontSize": "14", '
		                    +'"yAxisNameFontBold": "1", '

		                    +'"theme":"fint", '
		                    
		                    +'"canvasborderalpha": "0", '
		                    +'"bgColor": "#ffffff", '
		                    
		                    +'"usePlotGradientColor": "0", '
		                    
		                    +'"legendBorderAlpha": "0", '
		                    +'"legendPosition": "right", '
		                    +'"legendShadow": "0", '
		                    +'"legendIconScale":"1", '
		                    
		                   	+'"baseFontSize": "12", '
		                   	+'"baseFontBold": "1" '
			                + '},';
			                
			                /* Categories - YYYYMMDDHHMMSS */
			                var categories ='"categories":[{"category":[';
			                for(index in chartData){
	         					if (chartData[index].xCode != "") {
	         						if(isNaN(index)) continue;
	         						var sLabel = chartData[index].xCode;         						
	         						         						
		                            if(index!=0 ){                            	
		                            	 // 인덱스가 0이 아닌경우 ,를 앞에 붙여준다
		                            	categories += ',{"label": "'+sLabel+'"}';
		                            }else{
		                            	categories += '{"label": "'+sLabel+'"}';	
		                            }	                            	                            
	         					}	                    	 
	         				}
	         				categories += ']}],';
			                
	         			// "DATASET, SERIES"         				
	         				var dataset = '"dataset":[';
	         				var series0 = '{"seriesname": "A24",	"data":[';
	         				var series1 = '{"seriesname": "NA24",	"data":[';
	         				var series2 = '{"seriesname": "NA48",	"data":[';
	         				var series3 = '{"seriesname": "UNKNOWN","data":[';
	         				
	         				for(index in chartData){
	         					if(index==0){
		         					series0 += '{"value":"' + chartData[index].a24 + '"}';
		         					series1 += '{"value":"' + chartData[index].na24 + '"}';
		         					series2 += '{"value":"' + chartData[index].na48 + '"}';
		         					series3 += '{"value":"' + chartData[index].unknown + '"}';
	         					}else{
		         					series0 += ',{"value":"' + chartData[index].a24 + '"}';
		         					series1 += ',{"value":"' + chartData[index].na24 + '"}';
		         					series2 += ',{"value":"' + chartData[index].na48 + '"}';
		         					series3 += ',{"value":"' + chartData[index].unknown + '"}';
	         					}
	         				}
	         				dataset += series0 +']},'+ series1 +']},'+ series2 +']},'+ series3 +']}]';
	         				
	         				fcChartDataXml = fcChartDataXml + categories + dataset + '}';
	         				if(sType=='METER')fcChartRender(0);
	         				else if(sType=='DCU')fcChartRender(1);
	         				else if(sType=='REPEATER')fcChartRender(2);
	         				else if(sType=='RF')fcChartRender(3);
	         				else if(sType=='MMIU')fcChartRender(4);
	                        //hide();
			                
					}/* function(json) End */
				);
		}
		/* Update Fusion Chart End */
		
		/* Make Excel Start */
		var win;
		function makeMonitoringExcelFile() {
	        var opts = "width=600px, height=400px, left=100px, top=100px  resizable=no, status=no";
	        var obj = new Object();
	        var sStartDate = $("#startDateHidden").val();
       		var sEndDate = $("#endDateHidden").val();

       		obj.loginId = loginId;
       		obj.supplierId = supplierId;
       		obj.supplierName = supplierName;
	        obj.searchStartDate = sStartDate;
	        obj.searchEndDate = sEndDate;
	        obj.filePath = "<fmt:message key='aimir.report.fileDownloadDir'/>";

	        if(win)
	            win.close();

	        win = window.open("${ctx}/gadget/mvm/NetworkExcelDownloadPopup.do",
	                        "monitoringExcel", opts);
	        win.opener.obj = obj;
	    }
		/* Make Excel End */

		/* File Download */
		var fileDown = function(){
	    	var url = "${ctx}/common/fileDownload.do";
	        var downform = document.getElementsByName("reportDownloadForm")[0];
	        downform.action = url;
	        downform.submit();
	    }
		
</script>

<body>
<!-- search-background DIV (S) -->
<div class="search-bg-withtabs" style="height:30px;">
   	<div id="searchDateFormat" style="width:360px;" class="btn_left_top2 margin-t5px">
    <table>
	    <tr>
			<td class="withinput"><span class="bluebold11pt"><fmt:message key="aimir.button.search"/>&nbsp;<fmt:message key="aimir.date"/></span></td>
			<td id="search-date"><input id="startDate" name="startDate" type="text" class="day" readonly="readonly"></td>
			<td><input class="between" value="~" type="text"></td>
			<td id="search-date"><input id="endDate" name="endDate" type="text" class="day" readonly="readonly">
				<input id="startDateHidden" type="hidden" /> <input id="endDateHidden" type="hidden" /></td>
			<td>
				<div id="btn">
		            <ul style="margin-left: 0px">
		            	<li><a href="javascript:searchList();" class="on"><fmt:message key="aimir.button.search"/></a></li>
		            </ul>
	            </div>
            </td>
		</tr>
    </table>
	</div>
</div>
    
<!-- 차트 그리드  -->
<div id="chartGridDiv" class="gadget_body">
	<table align="center">
		<tr>
			<td align ="center">
			<table align="center">
			<tr><td colspan="2">
				<div style="margin-top:10px;"> </div>
			    <div id="chartContainer0">
					FusionCharts will render here 1/5
				 </div>
			</td></tr>
			<tr><td><div style="margin-top:10px;"></div></td></tr>
			<tr><td>
					 <div id="chartContainer1" style="margin-right:5px;">
						FusionCharts will render here 2/5
					 </div>
				</td>
				<td>	 
					 <div id="chartContainer2" style="margin-left:5px;">
						FusionCharts will render here 3/5
					 </div>
				</td>
			</td></tr>
			<tr><td><div style="margin-top:10px;"></div></td></tr>
			<tr><td>
					 <div id="chartContainer3" style="margin-right:5px;">
						FusionCharts will render here 4/5
					 </div>
				</td>
				<td>
					 <div id="chartContainer4" style="margin-left:5px;">
						FusionCharts will render here 5/5
					 </div>
				 </div>
			</td></tr>
			</table>
			 </td>
    	</tr>
    	<tr>
    		<td>
    			<div style="margin-top: 20px">
                    <ul style="margin-left: 0px">
                    	<li><a href="JavaScript:makeMonitoringExcelFile()" class="btn_blue" style="float: right;"><span style="padding-bottom: 5px;"><fmt:message key="aimir.button.excel" /></span></a></li>
            		</ul>
               </div>
    		</td>
    	</tr>
    	<tr>
    		<td><div id="monitoringDataDiv"></div> </td>
    	</tr>
    </table>
</div>
</body>
</html>