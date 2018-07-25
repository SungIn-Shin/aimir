<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="PRAGMA" content="NO-CACHE">
<meta http-equiv="Expires" content="-1">
<%-- <link href="${ctx}/css/style.css" rel="stylesheet" type="text/css"> --%>
<style type="text/css">
    span {
        float:none;
    }
    div#receipt-form {
        font-size: 10pt;
        width: 100%;
    }
    #receipt-form tr{
        height: 20px;
    }
    #receipt-form .logo-wrapper{
    	border:1px solid black;
    	width:calc(100% - 2px);
    }
    #receipt-form td{
        padding-left: 5px;
    }
    #receipt-form td.total-amount {
        text-align: center;
        font-weight: normal;
        padding-top: 10px;
        padding-bottom: 10px;
    }
    img.logo {
        width: 85px;
        height: 85px;
    }
    #receipt-no table{
    	width:100%;
    }
    .contents-wrapper{
    	border:1px solid black;
    	width:calc(100% - 2px);
    }
    .Buttons{
    	border:1px solid black;
    	width:calc(50% - 2px);
    	float:left;
    	text-align:center;
    	cursor:pointer;
    }
    .Buttons:hover{
    	border:1px solid black;
    	cursor:pointer;
    	color:#fff;
    	background:#000;
    }
    #control-form{
    	margin-top:2px;
    	width:100%;
    }
</style>
<style type="text/css" media="print">
   @page {
        margin:0;
    }
    #receipt-form {
        padding: 0px;
        font-size: 10pt;
    }
    .contents-wrapper {
        margin-bottom: 20px;
    }
    .hidden {
        display: none;
    } 
</style>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/public-customer.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/jquery.tablescroll.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/jZebra/html2canvas.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/jZebra/jquery.plugin.html2canvas.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/jZebra/PluginDetect.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/jquery.qrcode.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/js/qrcode.js"></script>
</head>
<body style="overflow: hidden">

    <%-- <applet name="jzebra" code="jzebra.PrintApplet.class"
        archive="${ctx}/lib/jzebra.jar" width="0" height="0"></applet>
    <canvas id="screenshot" style="display:none;"></canvas> --%>

        <div id="receipt-form">

            <div class="logo-wrapper" >
                <table  ><tr><td>
                    <table border="0">
                        <tr>
                            <td>
                                Electricity Company Of Ghana
                            </td>
                            <td rowspan=4>
                                <img class="logo" src="${ctx}/images/ECG_logo.gif"/>
                            </td>
                        </tr>
                        <tr>
                            <td>${vendorName}</td>
                        </tr>
                        <tr>
                            <td>${vendorLocation}</td>
                        </tr>
                        <tr>
                            <td>
                                ${casherName}
                            </td>
                        </tr>
                    </table>
                </td></tr></table>
            </div>

            <div id="receipt-no">
                <table>
                    <tr>
                        <td>Receipt No.</td>
                        <td>SC-- ${logId}</td>
                    </tr>
                </table>
            </div>

            <div class="contents-wrapper">
                <table>
                    <tr><td>
                    <table border="0" cellspacing="0">
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.date'/></b>
                            </td>
                        </tr>
                        <tr>
                        	<td>
                                ${date}
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <b>Cancel <fmt:message key='aimir.date'/></b>
                            </td>
                        </tr>
						<tr>
	                    	<td>
	                            ${cancelDate}
							</td>
	                    </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.customer'/></b>
                            </td>
                        </tr>
                        <tr>
                        	<td>
                                ${customer}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.code.g'/></b>
                            </td>
                        </tr>
                        <tr>
                        	<td>
                                ${gCode}
                            </td>
                        </tr>
                       </table>
                        
                        <div>
                        <table>
	                        <c:if test="${token != '' && token != null }">
		                       	<tr>
			                        <td>
			                            <b>Token</b>
			                        </td>
		                    	</tr>
		                    	<tr>
		                    		<td>
			                            ${token}
			                        </td>
		                    	</tr>
	                    	</c:if>
	                    	<c:if test="${cancelToken != '' && cancelToken != null }">
		                       	<tr>
			                        <td>
			                            <b>Cancel Token</b>
			                        </td>
		                    	</tr>
		                    	<tr>
						<td>
			                            <b>${cancelToken}</b>
			                        </td>
		                    	</tr>
	                    	</c:if>
                    	</table>
                    	</div>
                        
                        <tr><td>
						<table border="0" cellspacing="0">
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.accountNo'/></b>
                            </td>
                            <td>
                                ${customerNumber}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.meterid'/></b>
                            </td>
                            <td>
                                ${meter}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.residental.activity'/></b>
                            </td>
                            <td>
                                ${activity}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.location.district'/></b>
                            </td>
                            <td>
                                ${distinct}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.address'/></b>
                            </td>
                            <td>
                                ${address}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.payment'/></b>
                            </td>
                            <td>
                                ${payType}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.date.last.charge.date'/></b>
                            </td>
                            <td>
                                ${daysFromCharge}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.amount.paid'/></b>
                            </td>
                            <td>
                                ${amount}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.chargeAmount'/></b>
                            </td>
                            <td>
                                ${amount}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.prepayment.beforebalance'/></b>
                            </td>
                            <td>
                                ${preBalance}
                            </td>
                        </tr>
                        
                        <tr>
                            <td>
                                <b><fmt:message key='aimir.prepayment.currentbalance'/></b>
                            </td>
                            <td>
                                ${currentBalance}
                            </td>
                        </tr>
                    </table>
                </td></tr></table>
            </div>
            <c:if test="${token != '' && token != null }">
				<div id="qrcode" style="padding-top:5px;padding-bottom:5px"></div>
	        </c:if>

        </div>

    <div id="control-form" class="hidden">
    	<div id="printBtn" class="Buttons">PRINT</div>
    	<div id="closeBtn" class="Buttons">CLOSE</div>
    </div>
    <script type="text/javascript" charset="utf-8">/*<![CDATA[*/
        /* var eventHandler = {
            receiptPrint: function() {
                document.jzebra.findPrinter();
                //document.jzebra.findPrinter("SEWOO Lite #1");
                document.jzebra.setEncoding("UTF-8");
                document.jzebra.setEndOfDocument("\r\n");

                var logo = "<img src=\"";
                logo += window.location.origin + "${ctx}/images/ECG_logo.gif\" width=\"60\" height=\"60\" />";

                var print = function() {
                    if(document.jzebra && document.jzebra.findPrinter) {
                        var html = "<html><div style='font-size:9pt;'>" + $("#receipt-form").html()
                            + "</div></html>";

                        if(html.indexOf('<img class="logo" src="${ctx}/images/ECG_logo.gif">') > -1) {
                            html = html.replace('<img class="logo" src="${ctx}/images/ECG_logo.gif">', logo);
                        }
                        document.jzebra.appendHTML(html);
                        document.jzebra.printHTML();
                    } else {
                        window.print();
                    }
                }
                print();
            },

            close: function() {
                window.close();
            }
        }; */
        var bind = function () {
            //$("span.print").click(eventHandler.receiptPrint);
            //$("span.close").click(eventHandler.close);
            $("#printBtn").click(function(){
            	window.print();
            });
            $("#closeBtn").click(function(){
            	window.close();
            });
        };
        var init = function () {
        	var winWidth = 304;
        	var winHeight = 960;
            if($("#qrcode").length == 0){
            	winHeight = winHeight - 320;
            }
            window.resizeTo(winWidth, winHeight);
            bind();
        };
        window.onload = function() {
            init();
            if($("#qrcode").length > 0){
            	jQuery('#qrcode').qrcode({
            		text	: '${qrValue}',
            		width	: 250,
            		height	: 250,
            		correctLevel : QRErrorCorrectLevel.L
            	});	
            }
        };
        window.resize = function(){
        	
        }
    /*]]>*/
    </script>
    
</body>
</html>
