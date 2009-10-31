<%-----------------------------------------------------------------------------
	Copyright (c) 2004 Actuate Corporation and others.
	All rights reserved. This program and the accompanying materials 
	are made available under the terms of the Eclipse Public License v1.0
	which accompanies this distribution, and is available at
	http://www.eclipse.org/legal/epl-v10.html
	
	Contributors:
		Actuate Corporation - Initial implementation.
-----------------------------------------------------------------------------%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />
 
<%-----------------------------------------------------------------------------
	Report content fragment
-----------------------------------------------------------------------------%>
<TR VALIGN='top'>
	<TD id="documentView">
		<TABLE cellpadding="0" cellspacing="0" border="0">
		<TR>
			<TD style="vertical-align: top;">
				<%
					if ( fragment != null )
					{
						fragment.callBack( request, response );
					}
				%>
			</TD>
			<TD style="vertical-align: top;">
				<DIV ID="Document" CLASS="birtviewer_document_fragment">
				</DIV>
			</TD>
		</TR>
		</TABLE>
	</TD>
</TR>