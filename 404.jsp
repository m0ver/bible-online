<%@page contentType="text/html;charset=UTF-8" import="custom.application.error"%> 
<%
		org.tinystruct.system.ApplicationManager.install(new error());
		org.tinystruct.application.Context context=new org.tinystruct.ApplicationContext();
		context.setAttribute(HTTP_REQUEST,request);
		context.setAttribute(HTTP_RESPONSE,response);

		out.println(org.tinystruct.system.ApplicationManager.call("404", context));
%>