<#--
 *  Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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
 *@author     Johan Isacsson (conversion of jsp created by Dustin Caldwell (from code by David Jones))
 *@created    May 13, 2003
 *@version    1.0
-->

<TABLE border=0 cellspacing='0' cellpadding='0' class='boxoutside' width='45%'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <TD align=left>
            <div class='boxhead'>&nbsp;<b>Project:</b>&nbsp;${projectWorkEffort.workEffortName}</div>
          </TD>
          <TD align=right>
            <A href='<@ofbizUrl>/phaselist?projectWorkEffortId=${projectWorkEffortId}</@ofbizUrl>' class='lightbuttontext'>[All&nbsp;Phases]</A>
          </TD>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
                <tr>
                  <td align=right valign=top><div class='tabletext'><nobr>Project Status:</nobr></div></td>
                  <td>&nbsp;</td>
                  <td valign=top>${projectWorkEffortStatus.description}
                  </td>
                </tr>
                <tr>
                  <td align=right valign=top><div class='tabletext'><nobr>Description:</nobr></div></td>
                  <td>&nbsp;</td>
                  <td valign=top>${projectWorkEffort.description}
                </tr>
                <tr>
                  <td align=right valign=top><div class='tabletext'><nobr>Start Date/Time:</nobr></div></td>
                  <td>&nbsp;</td>
                  <td valign=top>${projectWorkEffort.estimatedStartDate?datetime?string.short}
                  </td>
                </tr>
                <tr>
                  <td align=right valign=top><div class='tabletext'><nobr>End Date/Time:</nobr></div></td>
                  <td>&nbsp;</td>
                  <td valign=top>${projectWorkEffort.estimatedCompletionDate?datetime?string.short}
                  </td>
                </tr>
      </table>
    </TD>
  </TR>
</TABLE>
<br>
<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <TD align=left>
            <div class='boxhead'>&nbsp;All Tasks</div>
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
<!--              <div class='head3'>Assigned Tasks</div>-->
              <TABLE width='100%' cellpadding='2' cellspacing='0' border='0'>
                <TR>
                  <TD><DIV class='tabletext'>&nbsp;</DIV></TD>
                  <TD><DIV class='tabletext'><b>Name</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Description</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Start Date/Time</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Priority</b></DIV></TD>
                  <TD><DIV class='tabletext'><b>Status</b></DIV></TD>
                  <TD align=right><DIV class='tabletext'><b>Edit</b></DIV></TD>
                </TR>
                <TR><TD colspan='7'><HR class='sepbar'></TD></TR>
                <#list phases as phase>
                  <TR>
                    <td valign=top class='tabletext'><b>Phase</b></td>
                    <TD valign=top><DIV class='tabletext'><b>${phase.workEffort.workEffortName}</b></div></TD>
                    <TD valign=top><DIV class='tabletext'><b>${phase.workEffort.description?if_exists}</b></div></TD>
                    <TD valign=top><DIV class='tabletext'><b>${phase.workEffort.estimatedStartDate?datetime?string.short}</b></DIV></TD>
                    <#assign currentStatusItem = delegator.findByPrimaryKeyCache("StatusItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("statusId", phase.workEffort.currentStatusId))>
                    <TD valign=top>&nbsp;</TD>
                    <TD valign=top><b><DIV class='tabletext'>${(currentStatusItem.description)?if_exists}</b></DIV></TD>
                    <TD  valign=top align=right width='1%'><A class='buttontext' href='<@ofbizUrl>/editphase?workEffortId=${phase.workEffort.workEffortId}&projectWorkEffortId=${projectWorkEffortId}</@ofbizUrl>'>
                        [Edit]</a></DIV></TD>
                  </TR>
                    <#list phase.tasks as workEffortTask>
                      <TR>
                        <td class='tabletext' valign=top>Task</td>
                        <TD valign=top><DIV class='tabletext'>${workEffortTask.workEffortName}</DIV></TD>
                        <TD valign=top><DIV class='tabletext'>${workEffortTask.description}</DIV></TD>
                        <TD valign=top><DIV class='tabletext'>${workEffortTask.estimatedStartDate?datetime?string.short}</DIV></TD>
                        <TD valign=top><DIV class='tabletext'>${workEffortTask.priority}</DIV></TD>
                        <#assign currentStatusItem = delegator.findByPrimaryKeyCache("StatusItem", Static["org.ofbiz.base.util.UtilMisc"].toMap("statusId", workEffortTask.currentStatusId))>
                        <TD valign=top><DIV class='tabletext'>${(currentStatusItem.description)?if_exists}</DIV></TD>
                        <TD  valign=top align=right width='1%'><A class='buttontext' href='<@ofbizUrl>/editphasetask?workEffortId=${workEffortTask.workEffortId}&phaseWorkEffortId=${phase.workEffort.workEffortId}</@ofbizUrl>'>
                            [Edit]</a></DIV></TD>
                      </TR>
                    </#list>
                  <TR><TD colspan='7'><HR class='sepbar'></TD></TR>
                </#list>
              </TABLE>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>
