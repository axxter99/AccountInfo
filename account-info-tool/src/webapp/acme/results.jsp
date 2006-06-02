
<!DOCTYPE HTML PUBLIC "-//w3c//dtd html 4.01 transitional//en">
<!--
 Copyright 2004 ArcMind, Inc. All Rights Reserved.
 from http://www-106.ibm.com/developerworks/library/j-jsf1/
-->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<f:loadBundle basename="sample.messages" var="msg"/>
<html>
<head>
<title>Calculator Results</title>
<%= request.getAttribute("sakai.html.head") %>
</head>
<body bgcolor="#FFFFFF" onload="<%= request.getAttribute("sakai.html.body.onload") %>">
<f:view>
     <p><h:messages /></p>
     <h:form id="calcForm">
First Number: <h:outputText id="firstNumber" 
    			                  value="#{CalcBean.firstNumber}"/> 
    			                  <br />
Second Number: 
             <h:outputText id="secondNumber" 
    			                 value="#{CalcBean.secondNumber}"/> 
    			                 <br />
<h:outputText id="result" 
    			value="#{CalcBean.result}"/> <br />

		        <h:commandButton id="more" value="#{msg.again2}" 
							action="#{CalcBean.again}" />
      </h:form>
</f:view>
</body>
</html>