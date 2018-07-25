<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="PRAGMA" content="NO-CACHE">
<meta http-equiv="Expires" content="-1">
<title>BEMS-Zone별 사용량 모니터링:Max</title>
<link href="${ctx}/css/style_bems.css" rel="stylesheet" type="text/css">

<%@ include file="/gadget/system/preLoading.jsp"%>

<script type="text/javascript" charset="utf-8" src="${ctx}/js/public.js"></script>
<script type="text/javascript" charset="utf-8">/*<![CDATA[*/
	
    var supplierId;
    //플렉스객체 
    var flex;

	var supplierId;
	var currentDate;

	
	$(document).ready(function() {

		flex = getFlexObject('zoneUsageMonitoringMax');

		var sId;
		
		/* 검색 조건 초기화  */
    	$.getJSON('${ctx}/common/getUserInfo.do',
    			function(json) {
    				if(json.supplierId != ""){
    					sId = json.supplierId;
    					if(sId != "") {
    						supplierId= sId;
    						
    						$("#dailyStartDate")    .datepicker({
    				    		maxDate:'+0m',showOn: 'button', 
    				    		dateFormat:'yymmdd',
    				    		buttonImage: '${ctx}/themes/images/default/setting/calendar.gif', 
    				        	onSelect: function(dateText, inst) {  modifyDate(dateText, inst);},
    				        	buttonImageOnly: true});

    						 $("#dailyStartDate").val($.datepicker.formatDate('yymmdd', new Date()));
    						 currentDate = $("#dailyStartDate").val();
    						 modifyDate($("#dailyStartDate").val(), '');    						 
    					}
    				}
    		}); 
		 
    	/* 검색 조건 화살표 이벤트  */
    	 $(function() { $('#dailyLeft')      .bind('click',  function(event) { dailyArrow($('#dailyStartDate').val(),-1); } ); });
    	 $(function() { $('#dailyRight')     .bind('click',  function(event) { dailyArrow($('#dailyStartDate').val(),1); } ); });

		$('#periodType').selectbox();
	});
	
    // datepicker로 선택한 날짜의 포맷 변경
    function modifyDate(setDate, inst){  
    	setDate.replace('/','').replace('/','');
    	currentDate = setDate; 
    		
        $.getJSON("${ctx}/common/convertLocalDate.do"
                ,{dbDate:setDate, supplierId:supplierId}
                ,function(json) {                
                    $("#dailyStartDate").val(json.localDate);
                    $("#dailyStartDate").trigger('change');
                    
                });
    }
    
    /**
     * 일별 화살표처리
     */
    function dailyArrow(bfDate,val){
       
        if(bfDate==currentDate && val>0){
           return;
        }
        bfDate = bfDate.replace('/','').replace('/','');
       
        $.getJSON("${ctx}/common/getDate.do"
                ,{searchDate:bfDate,addVal:val,supplierId:supplierId}
                ,function(json) {
                    
                    $('#dailyStartDate').val(json.searchDate);                    
                });
    }

    /**
     * 공통 send 거래
     * 개별 화면에서 각각 구현해야한다.
     * 조회버튼클릭시,조회데이터 변경시 최종적으로 호출하게 된다.
     */
    function send(){    	
        //http or flex Request Send
        if (flex != null) {
            flex.getUsage();
        }
    }

    /**
     * Flex 에서 메세지를 조회하기위한 함수
     */
    function getFmtMessage(){
        var fmtMessage = new Array();

        fmtMessage[0] = '<fmt:message key="aimir.alert"/>';                 // 장애
        fmtMessage[1] = 'Zone'+'<fmt:message key="aimir.classification"/>'; //분류
        fmtMessage[2] = '<fmt:message key="aimir.classificationusage"/>';//"분류별사용량";
        fmtMessage[3] = '<fmt:message key="aimir.py.currenttime"/> '+'<fmt:message key="aimir.usage"/>';//"현재시각사용량";
        fmtMessage[4] = '<fmt:message key="aimir.day"/> '+'<fmt:message key="aimir.usage"/>';//"일 사용량";
        fmtMessage[5] = '<fmt:message key="aimir.week"/> '+'<fmt:message key="aimir.usage"/>';//"주 사용량";
        fmtMessage[6] = '<fmt:message key="aimir.month"/> '+'<fmt:message key="aimir.usage"/>';//"월 사용량";
        fmtMessage[7] = '<fmt:message key="aimir.quarter"/> '+'<fmt:message key="aimir.usage"/>';//"분기 사용량";
        fmtMessage[8] = '<fmt:message key="aimir.day"/>'+'<fmt:message key="aimir.usage"/>';//"일 사용량";
        fmtMessage[9] = '<fmt:message key="aimir.electricity"/>';//"전기";
        fmtMessage[10] = '<fmt:message key="aimir.gas"/>';//"가스";
        fmtMessage[11] = '<fmt:message key="aimir.water"/>';//"수도";
        fmtMessage[12] = '<fmt:message key="aimir.co2formula2"/>';//"Co2배출량";
        fmtMessage[13] = '<fmt:message key="aimir.usage"/>';//"사용량";
        fmtMessage[14] = '<fmt:message key="aimir.min"/>';//"최소";
        fmtMessage[15] = '<fmt:message key="aimir.max"/>';//"최대";
        fmtMessage[16] = '<fmt:message key="aimir.date.yesterday"/>';//"전일";
        fmtMessage[17] = '<fmt:message key="aimir.date.today"/>';//"금일";
        fmtMessage[18] = '<fmt:message key="aimir.date.lastweek"/>';//"전주";
        fmtMessage[19] = '<fmt:message key="aimir.date.thisweek"/>';//"금주";
        fmtMessage[20] = '<fmt:message key="aimir.lastyear"/>';//"전년";
        //fmtMessage[21] = '<fmt:message key="aimirhems.label.thisYear"/>';//"올해";
        fmtMessage[21] = '<fmt:message key="aimir.date.thisYear"/>';//"금년";
        fmtMessage[22] = '<fmt:message key="aimir.lastyear"/>';//"전년";
        fmtMessage[23] = '<fmt:message key="aimir.price.unit"/>';//"원";
		fmtMessage[24] = '<fmt:message key="aimir.standard2"/>';//"기준";
		fmtMessage[25] = '<fmt:message key="aimir.hour2"/>';//"시";
		fmtMessage[26] = '<fmt:message key="aimir.dayofweek"/>';//"요일";
		fmtMessage[27] = '<fmt:message key="aimir.month"/>';//"월";
		fmtMessage[28] = '<fmt:message key="aimir.quarter"/>';//"분기";  
		fmtMessage[29] = '<fmt:message key="aimir.heatmeter"/>';//"열량";
				
        return fmtMessage;
    }

    /**
     * Flex 에서 조회조건을 조회하기위한 함수.
     */
    function getParams(){
    	var searchDate = currentDate;// 검색 시작일
        
        return searchDate;
    }
    
    //======================================================================================

/*]]>*/
    </script>
</head>
<body>
<div id="wrapper">
    
    <div id="container2">
    
     <!-- 주기 및 날짜 (S) -->
	 <div class="tapBg seachSpace mt10">
		<ul class="noTapSearch block header">
			<li class="tit_default pr5"><fmt:message key="aimir.bems.label.searchDate"/></li>
<%-- 			<li class="tit_default pr5"><fmt:message key="aimir.locationUsage.term"/></li> --%>
			<li><button id="dailyLeft" type="button" class="backicon srrow" ></button></li>
			<li><input id="dailyStartDate" type="text" readonly="readonly" style="width:70px"></li>
			<li><button id="dailyRight" type="button" class="nexticon srrow" ></button></li>
<!-- 			<li id="yearCompare"><input id="compare" type="checkbox" class="checkbox"></li> -->
<%-- 			<li class="pt2 pr5"><fmt:message key="aimir.locationUsage.lastYearCompare"/></li> --%>
			<li><em class="bems_button"><a href="javascript:send()" id="dailySearch"><fmt:message key="aimir.locationUsage.search"/></a></em></li>          
		</ul>
	 </div>
     <!-- 주기 및 날짜  (E) -->
        
      
    
        <!-- 탐색기 및 사용량 테이블 (S) -->
        <div class="Bchart" style="height:700px;">
            <object id="zoneUsageMonitoringMaxEx" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" width="100%" height="769" codebase="http://fpdownload.macromedia.com/get/flashplayr/current/swflash.cab">
                <param name="movie" value="${ctx}/flexapp/swf/bems/zoneUsageMonitoringMax.swf">
                <param name="quality" value="high">
                <param name="wmode" value="opaque">
                <param name="swfversion" value="9.0.45.0">
                <!-- This param tag prompts users with Flash Player 6.0 r65 and higher to download the latest version of Flash Player. Delete it if you don’t want users to see the prompt. -->
                <param name="expressinstall" value="Scripts/expressInstall.swf">
                <!-- Next object tag is for non-IE browsers. So hide it from IE using IECC. -->
                <!--[if !IE]>-->
                <object id="zoneUsageMonitoringMaxOt" type="application/x-shockwave-flash" data="${ctx}/flexapp/swf/bems/zoneUsageMonitoringMax.swf" width="100%" height="769">
                  <!--<![endif]-->
                  <param name="quality" value="high">
                  <param name="wmode" value="opaque">
                  <param name="swfversion" value="9.0.45.0">
                  <param name="expressinstall" value="Scripts/expressInstall.swf">
                  <param name="allowScriptAccess" value="always">
                  <!-- The browser displays the following alternative content for users with Flash Player 6.0 and older. -->
                  <div>
                    <h4>Content on this page requires a newer version of Adobe Flash Player.</h4>
                    <p><a href="http://www.adobe.com/go/getflashplayr"><img src="http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif" alt="Get Adobe Flash player" width="112" height="33" /></a></p>
                  </div>
                  <!--[if !IE]>-->
                </object>
                <!--<![endif]-->
              </object>
        </div>    
          
    </div> 
</div>
</body>
</html>
