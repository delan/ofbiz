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
 *@author     Johan Isacsson
 *@author     Eric.Barbier@nereide.biz (migration to uiLabelMap)
 *@created    May 19 2003
 *@version    1.0
-->
<#assign uiLabelMap = requestAttributes.uiLabelMap>

<table border='0' width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr> 
    <td width='100%'> 
	  <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
      	<tr> 
          <td align=left width='40%' class="boxhead">${uiLabelMap.WorkEffortCalendarDayView}</td>
          <td align=right width='60%'>		  
            <a href='<@ofbizUrl>/day?start=${start.time?string("#")}</@ofbizUrl>' class='submenutextdisabled'>${uiLabelMap.WorkEffortDayView}</a><a href='<@ofbizUrl>/week?start=${start.time?string("#")}</@ofbizUrl>' class='submenutext'>${uiLabelMap.WorkEffortWeekView}</a><a href='<@ofbizUrl>/month?start=${start.time?string("#")}</@ofbizUrl>' class='submenutext'>${uiLabelMap.WorkEffortMonthView}</a><a href='<@ofbizUrl>/upcoming</@ofbizUrl>' class='submenutext'>${uiLabelMap.WorkEffortUpcomingEvents}</a><a href='<@ofbizUrl>/event</@ofbizUrl>' class='submenutextright'>${uiLabelMap.WorkEffortNewEvent}</a>
		  </td>
        </tr>
      </table></td>
  </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="monthheadertable">
  <tr>
	<td width="100%" class="monthheadertext">${start?date?string("EEEE")?cap_first} ${start?date?string.long}</td>
    <td nowrap class="previousnextmiddle"><a href='<@ofbizUrl>/day?start=${prev.time?string("#")}</@ofbizUrl>' class="previousnext">${uiLabelMap.WorkEffortPreviousDay}</a> | <a href='<@ofbizUrl>/day?start=${next.time?string("#")}</@ofbizUrl>' class="previousnext">${uiLabelMap.WorkEffortNextDay}</a> | <a href='<@ofbizUrl>/day?start=${now.time?string("#")}</@ofbizUrl>' class="previousnext">${uiLabelMap.CommonToday}</a></td>
  </tr>
</table>
<#if periods?has_content>

<#if maxConcurrentEntries = 0>
  <#assign entryWidth = 100>
<#elseif maxConcurrentEntries < 2>
  <#assign entryWidth = (100 / (maxConcurrentEntries + 1))>
<#else> 
  <#assign entryWidth = (100 / (maxConcurrentEntries))>
</#if>
<table width="100%" cellspacing="1" border="0" cellpadding="1" class="calendar">                
  <tr>             
    <td nowrap class="monthdayheader">${uiLabelMap.CommonTime}<br>
      <img src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" alt="" height="1" width="88"></td>
    <td colspan=${maxConcurrentEntries} class="monthdayheader">${uiLabelMap.WorkEffortCalendarEntries}<br>
      <img src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" alt="" height="1" width="88"></td>
  </tr>
  <#list periods as period>              
  <tr>                  
    <td valign=top nowrap width="1%" class="monthweekheader" height="36"><span class="monthweeknumber">${period.start?time?string.short}</span><br>
      <a href='<@ofbizUrl>/event?estimatedStartDate=${period.start?string("yyyy-MM-dd HH:mm:ss")}&estimatedCompletionDate=${period.end?string("yyyy-MM-dd HH:mm:ss")}</@ofbizUrl>'>${uiLabelMap.CommonAddNew}</a></td>
    <#list period.calendarEntries as calEntry>
    <#if calEntry.startOfPeriod>			  
    <td class="calendarentry" rowspan='${calEntry.periodSpan}' colspan=1 width='${entryWidth?string("#")}%' valign=top>
	<#if (calEntry.workEffort.estimatedStartDate.compareTo(start)  <= 0 && calEntry.workEffort.estimatedCompletionDate.compareTo(next) >= 0)>
      All day
    <#elseif calEntry.workEffort.estimatedStartDate.before(start)>
	  Until ${calEntry.workEffort.estimatedCompletionDate?time?string.short}
    <#elseif calEntry.workEffort.estimatedCompletionDate.after(next)>
      From ${calEntry.workEffort.estimatedStartDate?time?string.short}
    <#else>
	  ${calEntry.workEffort.estimatedStartDate?time?string.short}-${calEntry.workEffort.estimatedCompletionDate?time?string.short}
    </#if>
	  <br><a href="<@ofbizUrl>/event?workEffortId=${calEntry.workEffort.workEffortId}</@ofbizUrl>" class="event">${calEntry.workEffort.workEffortName?default("Undefined")}</a>&nbsp;</td>
    </#if>
    </#list>	
    <#if period.calendarEntries?size < maxConcurrentEntries>
    <#assign emptySlots = (maxConcurrentEntries - period.calendarEntries?size)>			  	
	<#list 1..emptySlots as num>
	<td width='${entryWidth?string("#")}%'  class="calendarempty"><br></td>
	</#list>
    </#if>
    <#if maxConcurrentEntries < 2>			  
    <td width='${entryWidth?string("#")}' class="calendarempty">&nbsp;</td>
    </#if>
  </tr>
  </#list>                  
</table>
<#else>               
  <p>${uiLabelMap.WorkEffortFailedToGetCalendarEntries}!</p>
</#if>
