<%@include file="init.jsp"%>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<portlet:resourceURL var="updateUser" />
<portlet:renderURL var="horizontal_chart">
   <portlet:param name="cmd" value="l2r"/>
</portlet:renderURL>
<portlet:renderURL var="vertical_chart">
   <portlet:param name="cmd" value="t2b"/>
</portlet:renderURL>
<div>
   <a href="<%=vertical_chart%>"><input type="button" name="Verticle" value="Verticle"/></a> 
   <a href="<%=horizontal_chart%>"><input type="button" name="Horizontal" value="Horizontal"/></a>
 </div>
 
<c:choose>
 <c:when test="${direction=='l2r'}">
  <div id="chart-container" style=" text-align: left !important;"></div></c:when>
 <c:otherwise>
     <div id="chart-container"></div>
 </c:otherwise>
</c:choose>
<aui:script>
var datascource=JSON.parse('${data}');
   $('#chart-container').orgchart({
     'data' :datascource,
     'nodeContent': 'title',
     'draggable': true,
     'parentNodeSymbol': '',
     'direction': '${direction}'
   }) .children('.orgchart').on('nodedropped.orgchart', function(event) {
     // console.log('draggedNode:' + event.draggedNode.attr("id") + ', dragZone:' + event.dragZone.attr("id") + ', dropZone:' + event.dropZone.attr("id") );
 
      var level=parseInt(event.dropZone.attr("treeLevel"))+1;
      var type=event.dropZone.attr("nodeLevel");
      var cssClass="node level_"+level+"-type_"+type;
    //  console.log("cssClass : "+cssClass);
      event.draggedNode.removeClass(event.draggedNode.attr("class"));
      event.draggedNode.addClass(cssClass); 
     updateUser(event.draggedNode.attr("id"),event.dropZone.attr("id"));
   });
function updateUser(userId,parentUserId){
	 $.ajax({
         type: 'POST',
         data:{
       	  <portlet:namespace/>userId:userId,
       	  <portlet:namespace/>parentUserId:parentUserId
         },
         url: "<%= updateUser %>"
       });
}
</aui:script>