<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/taglibs.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta content='IE=EmulateIE8' http-equiv='X-UA-Compatible'/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link href="${ctx}/css/style.css" rel="stylesheet" type="text/css" />
    <!-- 스타일 추가 extjs css -->
    <link href="${ctx}/js/extjs/resources/css/ext-all.css" rel="stylesheet" type="text/css" title="blue" />
    <link href="${ctx}/css/jquery.tablescroll.css" rel="stylesheet" type="text/css"/>
    <link href="${ctx}/css/jquery.cluetip.css" rel="stylesheet" type="text/css" />
    
   <script type="text/javascript" charset="utf-8" src="${ctx}/js/public2.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/googleMap.jsp"></script>
    <script type="text/javascript" charset="utf-8"  src="${ctx}/js/jquery-ajaxQueue.js"></script>
    <script type="text/javascript" src="${ctx}/js/tree/jquery.tree.min.js"></script>
    <script type="text/javascript" src="${ctx}/js/tree/location.tree.js"></script>
    <script type="text/javascript" src="${ctx}/js/upload/ajaxupload.js"></script>
    <script type="text/javascript" src="${ctx}/js/upload/ajaxupload.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/adapter/ext/ext-base.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctx}/js/extjs/ext-all.js"></script>
    <style type="text/css">
         /* Ext-Js Grid 하단 페이징툴바 버튼 간격이 벌어지는 것을 방지 */
        TABLE{border-collapse: collapse; width:auto;}
        .remove {
            background-image:url(../../images/allOff.gif) !important;
        }
        .accept {
            background-image:url(../../images/allOn.png) !important;
        }

        @media screen and (-webkit-min-device-pixel-ratio:0) {
            .x-grid3-row td.x-grid3-cell {
                padding-left: 0px;
                padding-right: 0px;
            }
        }
        /* ext-js grid header 정렬 */
        .x-grid3-hd-inner{
            text-align: center !important;
            font-weight: bold;
        }
    </style>
</head>
<body>

<script type="text/javascript" charset="utf-8">

	$(document).ready(function () {
		
		var result = false;
		// data
	    var obj = window.opener.obj;
		contractNumber = obj.contractNumber;
		cancelReason = obj.cancelReason;
		lastTokenId = obj.lastTokenId;

		$('#id').val(lastTokenId);
		$('#lastTokenId').val(lastTokenId); 
		$('#contractNumber').val(contractNumber);
		$('#cancelReason').val(cancelReason);
		
	})
	
	function updateDescr(){
		var options = {
                success : descrUpdateResult,
                url : '${ctx}/gadget/prepaymentMgmt/updateEditDescr.do',
                type : 'post',
                datatype : 'json'
            };
            $('#descrEditForm').ajaxSubmit(options);
	}
	
	function cancelDescr(){
		window.close();
		result=false;
		window.opener.getReturnValue(result);
	}

	function descrUpdateResult(responseText, status) {
        alert(responseText.result);
        window.close();
        if (responseText.errors && responseText.errors.errorCount > 0) {
            var i, fieldErrors = responseText.errors.fieldErrors;
            for (i=0 ; i < fieldErrors.length; i++) {
                var temp = '#'+fieldErrors[i].objectName+' :input[name=\"'+fieldErrors[i].field+'\"]';
                $(temp).val(''+fieldErrors[i].defaultMessage);
            }
        } else {
            $('#descrEditForm').resetForm();
        }
        result=true;
        window.opener.getReturnValue(result);
    }
</script>
<div id="wrapper" class="max" style="margin-left: 50px;margin-top: 20px" >
	<div id="descrInfoEdit" style="display: block;" >
	 	<form:form id="descrEditForm" modelAttribute="prepaymentLog">
			<span class="padding-r10px"><b>Contract No.</b></span><input type="text" id="contractNumber" readonly="readonly" ><input type="hidden" id="id" name='id' /></li><br><br>
			<span class="padding-r10px"><b>Transaction Number</b></span><input type="text" id="lastTokenId" name="lastTokenId" style="width: 150px;" readonly="readonly" ><br><br>
			<span class="padding-r10px"><b>Description</b></span><textarea id="cancelReason" name="cancelReason" style="width: 250px;height:50px;" ></textarea><br><br>
		    <ul id="modeBtn" style="margin-left: 210px">
				<li ><a href="javascript:updateDescr()" class='btn_blue'><span>Update</span></a></li>
				<li style="margin-left:10px;"><a href="javascript:cancelDescr()" class='btn_blue'><span>Cancel</span></a></li>
			</ul>
		</form:form>
	</div>
</div>
</body>
</html>