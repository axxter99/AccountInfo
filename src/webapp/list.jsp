<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="java.util.List, java.util.Iterator" %>
<f:loadBundle basename="sample.messages" var="msg"/>
<HTML>
	<HEAD> <title>List: JSF Test Tool</title> 
	<%= request.getAttribute("sakai.html.head") %>
	</HEAD>
	<body onload="<%= request.getAttribute("sakai.html.body.onload") %>">
		<f:view>
			<h:form id="helloForm">
				<h:messages />
				<h:outputText value="#{msg.list}" /><br />

				<h:commandLink action="main">
					<h:outputText value="#{msg.main}"/>
				</h:commandLink>
			</h:form>
		</f:view>
	</body>
</HTML>  
