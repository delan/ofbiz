<#--
 *  Description: None
 *  Copyright (c) 2002 The Open For Business Project - www.ofbiz.org
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
 *@author     Johan Isacsson (conversion of jsp created by Andy Zeneski) 
 *@version    $Revision: 1.1 $
 *@since      2.1
-->

<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <TD align=left width='40%' >
            <div class='boxhead'>&nbsp;Requirement List</div>
          </TD>
          <TD align=right width='60%'>
            <A href='<@ofbizUrl>/requirementlist</@ofbizUrl>' class='lightbuttontextdisabled'>[Requirement&nbsp;List]</A>
            <A href='<@ofbizUrl>/requirement</@ofbizUrl>' class='lightbuttontext'>[New&nbsp;Requirement]</A>
          </TD>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
              <TABLE width='100%' cellpadding='2' cellspacing='0' border='0'>
                <TR>
                  <TD><DIV class='tabletext'><b>Requirement Type</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Description</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Required By</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Estmated Budget</b></DIV></TD>
                  <TD align=right><DIV class='tabletext'><b>View Tasks / Edit</b></DIV></TD>
                </TR>
                <TR><TD colspan='5'><HR class='sepbar'></TD></TR>                
                <#if requirements?has_content>
                <#list requirements as requirement>
                  <#assign requirementType = requirement.getRelatedOne("RequirementType")>
                  <TR>
                    <TD><DIV class='tabletext'>${(requirementType.description)?if_exists}</DIV></TD>
                    <TD><DIV class='tabletext'>${requirement.description?if_exists}</DIV></TD>
                    <TD><DIV class='tabletext'>${requirement.requiredByDate?if_exists}</DIV></TD>
                    <TD><DIV class='tabletext'>${requirement.estimatedBudget?default(0)?string.currency}</DIV></TD>
                    <TD align=right NOWRAP><A class="buttontext" href="<@ofbizUrl>/workefforts?requirementId=${requirement.requirementId}</@ofbizUrl>">Tasks</a>&nbsp;/&nbsp;<A class='buttontext' href='<@ofbizUrl>/requirement?requirementId=${requirement.requirementId}</@ofbizUrl>'>
                        Edit&nbsp;[${requirement.requirementId?if_exists}]</a></DIV></TD>
                  </TR>
                </#list>
                <#else>
                	<div class="tabletext">No open requirements found.</div>
                </#if>
              </TABLE>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>
