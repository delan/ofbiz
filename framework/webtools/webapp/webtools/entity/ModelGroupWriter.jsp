<%@ page contentType="text/plain" %><%@ page import="java.util.*, java.io.*, java.net.*, org.ofbiz.base.config.*, org.ofbiz.base.util.*, org.ofbiz.entity.*, org.ofbiz.entity.config.*, org.ofbiz.entity.model.*" %><jsp:useBean id="delegator" type="org.ofbiz.entity.GenericDelegator" scope="request" /><jsp:useBean id="security" type="org.ofbiz.security.Security" scope="request" /><%

  if("true-not-working".equals(request.getParameter("savetofile"))) {
    if(security.hasPermission("ENTITY_MAINT", session)) {
      //save to the file specified in the ModelReader config
      String controlPath=(String)request.getAttribute("_CONTROL_PATH_");
      String serverRootUrl=(String)request.getAttribute("_SERVER_ROOT_URL_");
      ModelGroupReader modelGroupReader = delegator.getModelGroupReader();

      ResourceHandler resourceHandler = null; //modelGroupReader.entityGroupResourceHandler;

      if (resourceHandler.isFileResource()) {
          String filename = resourceHandler.getFullLocation();

          java.net.URL url = new java.net.URL(serverRootUrl + controlPath + "/view/ModelGroupWriter");
          HashMap params = new HashMap();
          HttpClient httpClient = new HttpClient(url, params);
          InputStream in = httpClient.getStream();

          File newFile = new File(filename);
          FileWriter newFileWriter = new FileWriter(newFile);

          BufferedReader post = new BufferedReader(new InputStreamReader(in));
          String line = null;
          while((line = post.readLine()) != null) {
            newFileWriter.write(line);
            newFileWriter.write("\n");
          }
          newFileWriter.close();
          %>
          If you aren't seeing any exceptions, XML was written successfully to:
          <%=filename%>
          from the URL:
          <%=url.toString()%>
      <%
      } else {
        %>ERROR: This entity group information did not come from a file, so it is not being saved. It came from <%=resourceHandler.toString()%><%
      }
    } else {
      %>ERROR: You do not have permission to use this page (ENTITY_MAINT needed)<%
    }
  } else {
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE entitygroup PUBLIC "-//OFBiz//DTD Entity Group//EN" "http://www.ofbiz.org/dtds/entitygroup.dtd">
<!--
/**
 *  Title: Entity Generator Group Definitions for the General Data Model
 *  Description: None
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@author     Andy Zeneski (jaz@ofbiz.org)
 */
-->
<% 
  ModelReader reader = delegator.getModelReader();
  ModelGroupReader groupReader = delegator.getModelGroupReader();

  Map packages = new HashMap();
  TreeSet packageNames = new TreeSet();

  //put the entityNames TreeSets in a HashMap by packageName
  Collection ec = reader.getEntityNames();

  Iterator ecIter = ec.iterator();
  while(ecIter.hasNext()) {
    String eName = (String)ecIter.next();
    ModelEntity ent = reader.getModelEntity(eName);
    TreeSet entities = (TreeSet) packages.get(ent.getPackageName());
    if(entities == null) {
      entities = new TreeSet();
      packages.put(ent.getPackageName(), entities);
      packageNames.add(ent.getPackageName());
    }
    entities.add(eName);
  }%>
<entitygroup><%
  Iterator piter = packageNames.iterator();
  while(piter.hasNext()) {
    String pName = (String) piter.next();
    TreeSet entities = (TreeSet) packages.get(pName);
%>

  <!-- ========================================================= -->
  <!-- <%=pName%> -->
  <!-- ========================================================= -->
<%
    Iterator i = entities.iterator();
    while (i.hasNext()) {
      String entityName = (String)i.next();
      String groupName = groupReader.getEntityGroupName(entityName);
      if (groupName == null) groupName = "";
%>
    <entity-group group="<%=groupName%>" entity="<%=entityName%>" /><%
    }
  }%>
</entitygroup>
<%}%>
