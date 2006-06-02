<!doctype html public "-//w3c//dtd html 4.01 transitional//en">
<!--
 Copyright 2004 ArcMind, Inc. All Rights Reserved.
 from http://www-106.ibm.com/developerworks/library/j-jsf1/
-->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:loadBundle basename="sample.messages" var="msg"/>
<html>
    <head> <title>Calculator</title> 
    <%= request.getAttribute("sakai.html.head") %>
    </head>
    
    <body bgcolor="white" onload="<%= request.getAttribute("sakai.html.body.onload") %>">
    <h2>Acme Calculator</h2>

    <f:view>
      <p><h:messages /></p>
      <h:form id="calcForm">
      
        <h:panelGrid columns="3" >
		      	<h:outputLabel value="#{msg.first}" for="firstNumber" />
		      	<h:inputText id="firstNumber" value="#{CalcBean.firstNumber}"
		      							 required="true" />
		      	<h:message for="firstNumber" />	
		      						 
		      	<h:outputLabel value="#{msg.second}" for="secondNumber" />
		      	<h:inputText id="secondNumber" value="#{CalcBean.secondNumber}"
		      							required="true" /><br />
		      	<h:message for="secondNumber" />
      	</h:panelGrid>
      	
      	<h:panelGroup>
		        <h:commandButton id="submitAdd" action="#{CalcBean.add}" 
							value="#{msg.plus}" />
		        <h:commandButton id="submitMultiply" action="#{CalcBean.multiply}" 
		        	value="#{msg.times}" />
        </h:panelGroup>
        
      </h:form>
    </f:view>
</html>  
