<%--
 *  Copyright (c) 2004-2005 The Open For Business Project and respected authors.
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
 * @author Brian Johnson (bmj@camfour.com)
 * @author Ray Barlow (ray.barlow@makeyour-point.com)
 * @author David E. Jones (jonesde@ofbiz.org)
--%>

<%@ page import="java.util.*, java.net.*, java.io.*" %>
<%@ page import="org.w3c.dom.*" %>
<%@ page import="org.ofbiz.security.*, org.ofbiz.entity.*, org.ofbiz.base.util.*, org.ofbiz.webapp.pseudotag.*" %>
<%@ page import="org.ofbiz.entity.model.*, org.ofbiz.entity.util.*, org.ofbiz.entity.condition.*" %>

<%@ taglib uri="ofbizTags" prefix="ofbiz" %>

<jsp:useBean id="security" type="org.ofbiz.security.Security" scope="request" />
<jsp:useBean id="delegator" type="org.ofbiz.entity.GenericDelegator" scope="request" />
<%
  String path = request.getParameter("path");
  boolean mostlyInserts = request.getParameter("mostlyInserts") != null;
  boolean keepStamps = request.getParameter("maintainTimeStamps") != null;
  boolean createDummyFks = request.getParameter("createDummyFks") != null;
  boolean deleteFiles = request.getParameter("deleteFiles") != null;

  String txTimeoutStr = UtilFormatOut.checkEmpty(request.getParameter("txTimeout"), "7200");
  String filePauseStr = UtilFormatOut.checkEmpty(request.getParameter("filePause"), "0");
  Integer txTimeout = null;
  try {
      txTimeout = Integer.valueOf(txTimeoutStr);
  } catch (Exception e) {
      txTimeout = new Integer(7200);
      %><div class="tabletext">ERROR: TX Timeout not a valid number, setting to 7200 seconds (2 hours): <%=e%><%
  }

  Long filePause = null;
  try {
      filePause = Long.valueOf(filePauseStr);
  } catch (Exception e) {
      filePause = new Long(0);
      %><div class="tabletext">ERROR: File Pause not a valid number, setting to 0 seconds (no pause): <%=e%><%
  }
%>

<div class="head1">XML Import to DataSource(s)</div>
<div class="tabletext">This page can be used to import exported Entity Engine XML documents. These documents all have a root tag of "&lt;entity-engine-xml&gt;".</div>
<hr>
<%if(security.hasPermission("ENTITY_MAINT", session)){%>
  <div class="head2">Import:</div>

  <form method="post" action="<ofbiz:url>/xmldsimportdir</ofbiz:url>">
    <div class="tabletext">Absolute directory path:</div>
    <div><input type="text" class="inputBox" size="60" name="path" value="<%=UtilFormatOut.checkNull(path)%>"/></div>
    <div class="tabletext"><input type="checkbox" name="mostlyInserts" <%=mostlyInserts?"checked":""%>/>Mostly Inserts?</div>
    <div class="tabletext"><input type="checkbox" name="maintainTimeStamps" <%=keepStamps?"checked":""%>/>Maintain Timestamps?</div>
    <div class="tabletext"><input type="checkbox" name="createDummyFks" <%=createDummyFks?"checked=\"checked\"":""%>/>Create "Dummy" FKs?</div>
    <div class="tabletext"><input type="checkbox" name="deleteFiles" <%=(deleteFiles || path == null || path.length() == 0)?"checked":""%>/>Delete Files Afterwards?</div>
    <div class="tabletext">TX Timeout Seconds:<input type="text" size="6" value="<%=txTimeoutStr%>" name="txTimeout"/></div>
    <div class="tabletext">Pause (secs) between files:<input type="text" size="6" value="<%=filePauseStr%>" name="filePause"/></div>
    <div><input type="submit" value="Import Files"/></div>
  </form>
  <hr>
    <div class="head1">Results:</div>

  <%if (path != null && path.length() > 0) {%>
  <%
    long pauseLong = filePause != null ? filePause.longValue() : 0;
    File baseDir = new File(path);

    if (baseDir.isDirectory() && baseDir.canRead()) {
        File[] fileArray = baseDir.listFiles();
        ArrayList files = new ArrayList(fileArray.length);
        for (int a=0; a<fileArray.length; a++){
            if (fileArray[a].getName().toUpperCase().endsWith("XML")) {
                files.add(fileArray[a]);
            }
        }
        boolean importedOne = false;
        int fileListMarkedSize = files.size();
        int passes = 0;
        for (int a=0; a<files.size(); a++){
            // Infinite loop defense
            if (a == fileListMarkedSize) {
                passes++;
                fileListMarkedSize = files.size();
                %> <div class="tabletext">Pass <%=passes%> complete</div> <%
                // This means we've done a pass
                if ( false == importedOne ) {
                    // We've failed to make any imports
                    %> <div class="tabletext">Dropping out as we failed to make any imports on the last pass</div> <%
                    a = files.size();
                    continue;
                }
                importedOne = false;
            }
            File curFile = (File)files.get(a);
            try{
                URL url = curFile.toURL();
                EntitySaxReader reader = new EntitySaxReader(delegator);
                if (mostlyInserts) {
                    reader.setUseTryInsertMethod(true);
                }
                if (txTimeout != null) {
                    reader.setTransactionTimeout(txTimeout.intValue());
                }
                if (createDummyFks) {
                    reader.setCreateDummyFks(true);
                }
                long numberRead = reader.parse(url);
                %><div class="tabletext">Got <%=numberRead%> entities from <%=curFile%></div><%
                importedOne = true;
                if (deleteFiles) {
                    curFile.delete();    
                }
            } catch (Exception ex){
                %> <div class="tabletext">Error trying to read from <%=curFile%>: <%=ex%> <%
                if (ex.toString().indexOf("referential integrity violation") > -1 ||
                        ex.toString().indexOf("Integrity constraint violation") > -1){
                    //It didn't work because object it depends on are still
                    //missing from the DB. Retry later.
                    //
                    //FIXME: Of course this is a potential infinite loop.

                    %> <div class="tabletext">Looks like referential integrity violation, will retry</div> <%
                    files.add(curFile);
                }
            }

            // pause in between files
            if (pauseLong > 0) {
                Debug.log("Pausing for [" + pauseLong + "] seconds - " + UtilDateTime.nowTimestamp());
                Thread.sleep((pauseLong * 1000));
                Debug.log("Pause finished - " + UtilDateTime.nowTimestamp());
            }
        }
    } else {
        %><div class="tabletext"> path not found or can't be read </div> <%
    }
  %>
  <%} else {%>
    <div class="tabletext">No path specified, doing nothing.</div>
  <%}%>
<%}else{%>
  <div class="tabletext">You do not have permission to use this page (ENTITY_MAINT needed)</div>
<%}%>

