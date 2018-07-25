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
<body style="overflow: scroll">
<%--     <applet name="jzebra" code="jzebra.PrintApplet.class"
    archive="${ctx}/lib/jzebra.jar" width="0" height="0"></applet> --%>
    <div id="receipt-form">

        <div class="logo-wrapper">
            <table><tr><td>
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

        <div>
            <table>
                <tr>
                    <td>
                        <fmt:message key='aimir.receipt'/>&nbsp;<fmt:message key='aimir.number'/>
                    </td>
                    <td>
                        SC-- ${logId}
                    </td>
                </tr>
            </table>
        </div>

        <div class="contents-wrapper">
            <table><tr><td>
                <table border='0' cellspacing="0">
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
                    
                    <tr>
                        <!-- <td colspan=2 class='td-table'>
                            <table border='1' class='inner-table' style="width: 80%;"> -->
                            <td colspan=3 >
                        <table border="1" style="table-layout: fixed; border-collapse: collapse;">
                                <tr>
                                    <td>
                                    </td>
                                    <td class='table-header'>
                                        <fmt:message key='aimir.prepayment.initialcredit'/>
                                    </td>
                                    <td class='table-header'>
                                        <fmt:message key='aimir.debt'/>
                                    </td>
                                    <td class='table-header'>
                                        <fmt:message key='aimir.prepayment.currentcredit'/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="aimir.meterid"/>
                                    </td>
                                    <td class='table-value'>
                                    </td>
                                    <td class='table-value'>
                                    	${lastMeter}
                                    </td>
                                    <td class='table-value'>
                                        ${meter}
                                    </td>
                                </tr>
                                <tr class='tr-credit'>
                                    <td>
                                        <fmt:message key='aimir.credit'/>
                                    </td>
                                    <td class='table-value'>
                                        ${preArrears}
                                    </td>
                                    <td class='table-value'>
                                        ${preDebts}
                                    </td>
                                    <td class='table-value'>
                                        ${preBalance}
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key='aimir.usageFee2'/>
                                    </td>
                                    <td class='table-value'>
                                        ${arrears}
                                    </td>
                                    <td class='table-value'>
                                        ${debts}
                                    </td>
                                    <td class='table-value'>
                                        ${amount}
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key='aimir.balance'/>
                                    </td>
                                    <td class='table-value'>
                                        ${currentArrears}
                                    </td>
                                    <td class='table-value'>
                                        ${currentDebts}
                                    </td>
                                    <td class='table-value'>
                                        ${currentBalance}
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    
                    <tr>
                        <td>
                            <b><fmt:message key='aimir.accountNo'/></b><%-- <fmt:message key='aimir.customerid'/> --%>
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
                            <b><fmt:message key="aimir.amount.paid"/></b>
                        </td>
                        <td>
                            ${totalAmount}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <b><fmt:message key="aimir.prepayment.chargedarrears"/></b>
                        </td>
                        <td>
                            ${arrears}
                        </td>
                    </tr>
                    <tr>
                        <td>
                        	<b><fmt:message key='aimir.chargedDebt'/></b>
                        </td>
                        <td>
                            ${debts}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <b><fmt:message key="aimir.chargeAmount"/></b>
                        </td>
                        <td class="charge-amount">
                            ${amount}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <b><fmt:message key="aimir.prepayment.beforebalance"/></b>
                        </td>
                        <td>
                            ${preBalance}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <b><fmt:message key="aimir.prepayment.currentbalance"/></b>
                        </td>
                        <td>
                            ${currentBalance}
                        </td>
                    </tr>
                </table>
            </td></tr></table>
        </div>

		<%-- <c:if test="${token != '' && token != null }"> --%>
			<div id="qrcode" style="padding-top:5px;padding-bottom:5px"></div>
	    <%-- </c:if> --%>
    
    
	    <div>
            <table>
                <div id="control-form" class="hidden">
				<div id="printBtn" class="Buttons">PRINT</div>
				<div id="closeBtn" class="Buttons">CLOSE</div>				
            </table>
	    </div>
    
    </div>
    <%-- <div class="control-form hidden">
        <center>
            <span class="am_button margin-l10 margin-t1px print">
                <a class="on"><fmt:message key="aimir.button.print" /></a>
            </span>
            <span class="am_button margin-l10 margin-t1px close">
                <a class="on"><fmt:message key="aimir.board.close" /></a>
            </span>
        </center>
    </div> --%>
    
    <script type="text/javascript" charset="utf-8">/*<![CDATA[*/
        var initCredit = Number(${initCredit});
/*         var eventHandler = {
            receiptPrint: function() {
                document.jzebra.findPrinter();
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
            },
            insertInitCredit: function() {
                if(!isNaN(initCredit)) {

                    var $tr =
                        $("<tr><td><fmt:message key='aimir.prepayment.init.credit'/></td><td></td></tr>");
                    var $val = $("<td></td>");
                    $val.addClass("table-value");
                    $val.text(initCredit);
                    $tr.append($val);
                    $tr.insertAfter(".tr-credit");

                    var chargeAmount = Number($('.charge-amount').text());
                    chargeAmount += initCredit;
                    var chargeAmountFix = chargeAmount.toFixed(2);
                    $('.charge-amount').text(chargeAmountFix);
                }
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
            //eventHandler.insertInitCredit();
            window.resizeTo(330, 900);
            if($("#qrcode").length > 0){
            	jQuery('#qrcode').qrcode({
            		text	: '${qrValue}',
            		width	: 250,
            		height	: 250,
            		correctLevel : QRErrorCorrectLevel.L
            	});	
            }
            bind();
        };
        window.onload = function() {
            init();
        };
		window.resize = function(){
        	
        };
    /*]]>*/
    </script>
</body>
</html>