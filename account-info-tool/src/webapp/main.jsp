<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="java.util.List, java.util.Iterator" %>
<f:loadBundle basename="sample.messages" var="msg"/>
<HTML>
	<HEAD> <title>Main: JSF Test Tool</title> 
		<%= request.getAttribute("sakai.html.head") %>
	</HEAD>
	<body onload="<%= request.getAttribute("sakai.html.body.onload") %>">
 		<f:view>
			<h:form id="helloForm">
				<h:messages />
				<h:outputText value='placement=#{requestScope["sakai.tool.placement"]}' /><br />

				<h:outputText value="#{msg.hello}" />
				<h:inputText id="userName" value="" required="true">
					<f:validateLength minimum="2" maximum="10"/>
				</h:inputText><br />

				<h:commandButton id="submit" action="list" value="#{msg.say_hi}" /><br />

				<h:commandLink action="list" immediate="true">
					<h:outputText value="#{msg.go_list}"/>
				</h:commandLink>
				
				<h:graphicImage value="/sakai.jpg" />
			</h:form>
		</f:view>
	</body>
</HTML>  
