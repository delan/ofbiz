<%@ page import="org.ofbiz.entitygen.*" %><%String ejbName=request.getParameter("ejbName"); String defFileName=request.getParameter("defFileName"); Entity entity=DefReader.getEntity(defFileName,ejbName); int i;%>