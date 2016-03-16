<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fnx" uri="http://java.sun.com/jsp/jstl/functionsx"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://www.jspxcms.com/tags/form"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags"%>
<%@page import="java.util.ArrayList"%>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport"
	content="initial-scale=1.0, user-scalable=no, width=device-width">
<title></title>
<link rel="stylesheet"
	href="http://cache.amap.com/lbs/static/main1119.css" />
<script type="text/javascript"
	src="http://webapi.amap.com/maps?v=1.3&key=59e63f33fe426364bb67f8bc90415f43"></script>
<script type="text/javascript"
	src="http://cache.amap.com/lbs/static/addToolbar.js"></script>
<script type="text/javascript"
	src="/static/commons/scripts/jquery-2.2.1.min.js"></script>

<script>
	var t;
	var line;
	function timedShow(begin) {
		htmlobj = $.ajax({
			url : "/geo.servlet?type=geo&line=" + line,
			context : document.body,
			success : function(data) {
				map.clearMap(); // 清除地图覆盖物
				drawPoint(data, begin);
			}
		});
		/* $("#myDiv").html(htmlobj.responseText); */
		t = setTimeout("timedShow()", 60000);
	}

	function showBus(val) {
		if (t) {
			clearTimeout(t);
		}
		if (map) {
			map.clearMap();
		}
		if (val != "" && typeof (val) != "undefined") {
			line = val;
			timedShow(true);
		}

	}

	function drawPoint(data, begin) {
		for (var i = 0; i < data.length; i++) {
			var marker = new AMap.Marker({
				map : map,
				icon : "/static/commons/images/file/car_02.png",
				position : [ data[i].longitude, data[i].latitude ],
				//offset: new AMap.Pixel(-12, -36)
				angle : data[i].direction - 90,
				//label:{content:data[i].busNum,offset: new AMap.Pixel(20, 30)}
				title : data[i].busNum
			});
		}
		if (begin)
			map.setFitView();
	}
</script>
</head>
<body>
	<div style="width: 200px;">
		<label for="lines"><span
			style="font: 12px Verdana, Arial, Tahoma;">选择线路：</span></label> <select
			name="lines" id="lines" onchange="showBus(this.value);">
			<%
				try{
							ArrayList<String[]> lines = (ArrayList<String[]>)request.getAttribute("lines");
							out.println("<option value=\"\">请选择线路</option>");
							for(String[] s:lines){
								out.println("<option value=\""+s[1]+"\">"+s[0]+"</option>");
							}
					}catch(Exception e){
					}
			%>
		</select>
	</div>
	<!-- <div id="container" style="top: 20px; width: 712px; height: 712px"></div> -->
	<div id="container" style="top: 20px;"></div>
	<script type="text/javascript">
		//初始化地图对象，加载地图
		var map = new AMap.Map("container", {
			resizeEnable : true,
			center : [ 113.663221, 34.7568711 ],//地图中心点
			zoom : 13
		//地图显示的缩放级别
		});
	</script>
</body>
</html>
