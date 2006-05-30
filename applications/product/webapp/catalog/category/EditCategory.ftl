<#--
 *  Copyright (c) 2003-2005 The Open For Business Project - www.ofbiz.org
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
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel )
 *@version    $Rev$
 *@since      2.1
-->
<script language="JavaScript" type="text/javascript">
function insertImageName(type,nameValue) {
  eval('document.productCategoryForm.' + type + 'ImageUrl.value=nameValue;');
};
</script>
    <#if fileType?has_content>
        <div class='head3'>${uiLabelMap.ProductResultOfImageUpload}</div>
        <#if !(clientFileName?has_content)>
            <div class='tabletext'>${uiLabelMap.ProductNoFileSpecifiedForUpload}.</div>
        <#else>
            <div class='tabletext'>${uiLabelMap.ProductTheFileOnYourComputer}: <b>${clientFileName?if_exists}</b></div>
            <div class='tabletext'>${uiLabelMap.ProductServerFileName}: <b>${fileNameToUse?if_exists}</b></div>
            <div class='tabletext'>${uiLabelMap.ProductServerDirectory}: <b>${imageServerPath?if_exists}</b></div>
            <div class='tabletext'>${uiLabelMap.ProductTheUrlOfYourUploadedFile}: <b><a href="<@ofbizContentUrl>${imageUrl?if_exists}</@ofbizContentUrl>">${imageUrl?if_exists}</a></b></div>
        </#if>
    <br/>
    </#if>


<br/>

<#if ! productCategory?has_content>
  <#if productCategoryId?has_content>
    <h3>${uiLabelMap.ProductCouldNotFindProductCategoryWithId} "${productCategoryId}".</h3>
    <form action="<@ofbizUrl>createProductCategory</@ofbizUrl>" method="post" style="margin: 0;" name="productCategoryForm">
    <table border="0" cellpadding="2" cellspacing="0">
    <tr>
      <td align="right"><div class="tabletext">${uiLabelMap.ProductProductCategoryId}</div></td>
      <td>&nbsp;</td>
      <td>
        <input type="text" name="productCategoryId" size="20" maxlength="40" value="${productCategoryId}" class="inputBox"/>
      </td>
    </tr>
  <#else>
    <form action="<@ofbizUrl>createProductCategory</@ofbizUrl>" method="post" style="margin: 0;" name="productCategoryForm">
    <table border="0" cellpadding="2" cellspacing="0">
    <tr>
      <td align="right"><div class="tabletext">${uiLabelMap.ProductProductCategoryId}</div></td>
      <td>&nbsp;</td>
      <td>
        <input type="text" name="productCategoryId" size="20" maxlength="40" value="" class="inputBox"/>
      </td>
    </tr>
  </#if>
<#else>
  <form action="<@ofbizUrl>updateProductCategory</@ofbizUrl>" method="post" style="margin: 0;" name="productCategoryForm">
  <input type="hidden" name="productCategoryId" value="${productCategoryId}"/>
  <table border="0" cellpadding="2" cellspacing="0">
  <tr>
    <td align="right"><div class="tabletext">${uiLabelMap.ProductProductCategoryId}</div></td>
    <td>&nbsp;</td>
    <td>
      <b>${productCategoryId}</b> (${uiLabelMap.ProductNotModificationRecrationCategory}.)
    </td>
  </tr>
</#if>
  <tr>
    <td width="26%" align="right"><div class="tabletext">${uiLabelMap.ProductProductCategoryType}</div></td>
    <td>&nbsp;</td>
    <td width="74%">
      <select name="productCategoryTypeId" size="1" class="selectBox">
        <option value="">&nbsp;</option>
        <#list productCategoryTypes as productCategoryTypeData>
          <option <#if productCategory?has_content><#if productCategory.productCategoryTypeId==productCategoryTypeData.productCategoryTypeId> selected="selected"</#if></#if> value="${productCategoryTypeData.productCategoryTypeId}">${productCategoryTypeData.get("description",locale)}</option>
       </#list>
      </select>
    </td>
  </tr>
   <tr>
    <td width="26%" align="right"><div class="tabletext">${uiLabelMap.ProductName}</div></td>
    <td>&nbsp;</td>
    <td width="74%"><input type="text" <#if productCategory?has_content>value="${productCategory.categoryName?if_exists}"</#if> name="categoryName" size="60" maxlength="60" class="inputBox"/></td>
  </tr>
  <tr>
    <td width="26%" align="right"><div class="tabletext">${uiLabelMap.ProductDescription}</div></td>
    <td>&nbsp;</td>
    <td width="74%"><textarea class="textAreaBox" name="description" cols="60" rows="2"><#if productCategory?has_content>${(productCategory.description)?if_exists}</#if></textarea></td>
  </tr>
<#--
  <tr>
    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.ProductLongDescription}</div></td>
    <td>&nbsp;</td>
    <td width="74%"><textarea cols="60" rows="3" name="longDescription" maxlength="2000" class="textAreaBox"><#if productCategory?has_content>${productCategory.longDescription?if_exists}</#if></textarea></td>
  </tr>
-->
        <tr>
            <td width="20%" align="right" valign="top">
                <div class="tabletext">${uiLabelMap.ProductCategoryImageUrl}</div>
                <#if (productCategory.categoryImageUrl)?exists>
                    <a href="<@ofbizContentUrl>${(productCategory.categoryImageUrl)?if_exists}</@ofbizContentUrl>" target="_blank"><img alt="Category Image" src="<@ofbizContentUrl>${(productCategory.categoryImageUrl)?if_exists}</@ofbizContentUrl>" height="40" width="40"></a>
                </#if>
            </td>
            <td>&nbsp;</td>
            <td width="80%" colspan="4" valign="top">
            <input type="text" class="inputBox" name="categoryImageUrl" value="${(productCategory.categoryImageUrl)?default('')}" size="60" maxlength="255"/>
            <#if productCategory?has_content>
                <div>
                <span class="tabletext">${uiLabelMap.ProductInsertDefaultImageUrl}: </span>
                <a href="javascript:insertImageName('category','${imageNameCategory}.jpg');" class="buttontext">[.jpg]</a>
                <a href="javascript:insertImageName('category','${imageNameCategory}.gif');" class="buttontext">[.gif]</a>
                <a href="javascript:insertImageName('category','');" class="buttontext">[${uiLabelMap.CommonClear}]</a>
                </div>
            </#if>
            </td>
        </tr>
        <tr>
            <td width="20%" align="right" valign="top">
                <div class="tabletext">${uiLabelMap.ProductLinkOneImageUrl}</div>
                <#if (productCategory.linkOneImageUrl)?exists>
                    <a href="<@ofbizContentUrl>${(productCategory.linkOneImageUrl)?if_exists}</@ofbizContentUrl>" target="_blank"><img alt="Link One Image" src="<@ofbizContentUrl>${(productCategory.linkOneImageUrl)?if_exists}</@ofbizContentUrl>" height="40" width="40"></a>
                </#if>
            </td>
            <td>&nbsp;</td>
            <td width="80%" colspan="4" valign="top">
            <input type="text" class="inputBox" name="linkOneImageUrl" value="${(productCategory.linkOneImageUrl)?default('')}" size="60" maxlength="255"/>
            <#if productCategory?has_content>
                <div>
                <span class="tabletext">${uiLabelMap.ProductInsertDefaultImageUrl}: </span>
                <a href="javascript:insertImageName('linkOne','${imageNameLinkOne}.jpg');" class="buttontext">[.jpg]</a>
                <a href="javascript:insertImageName('linkOne','${imageNameLinkOne}.gif');" class="buttontext">[.gif]</a>
                <a href="javascript:insertImageName('linkOne','');" class="buttontext">[${uiLabelMap.CommonClear}]</a>
                </div>
            </#if>
            </td>
        </tr>
        <tr>
            <td width="20%" align="right" valign="top">
                <div class="tabletext">${uiLabelMap.ProductLinkTwoImageUrl}</div>
                <#if (productCategory.linkTwoImageUrl)?exists>
                    <a href="<@ofbizContentUrl>${(productCategory.linkTwoImageUrl)?if_exists}</@ofbizContentUrl>" target="_blank"><img alt="Link One Image" src="<@ofbizContentUrl>${(productCategory.linkTwoImageUrl)?if_exists}</@ofbizContentUrl>" height="40" width="40"></a>
                </#if>
            </td>
            <td>&nbsp;</td>
            <td width="80%" colspan="4" valign="top">
            <input type="text" class="inputBox" name="linkTwoImageUrl" value="${(productCategory.linkTwoImageUrl)?default('')}" size="60" maxlength="255"/>
            <#if productCategory?has_content>
                <div>
                <span class="tabletext">${uiLabelMap.ProductInsertDefaultImageUrl}: </span>
                <a href="javascript:insertImageName('linkTwo','${imageNameLinkTwo}.jpg');" class="buttontext">[.jpg]</a>
                <a href="javascript:insertImageName('linkTwo','${imageNameLinkTwo}.gif');" class="buttontext">[.gif]</a>
                <a href="javascript:insertImageName('linkTwo','');" class="buttontext">[${uiLabelMap.CommonClear}]</a>
                </div>
            </#if>
            </td>
       </tr>

  <tr>
    <td width="26%" align="right"><div class="tabletext">${uiLabelMap.ProductDetailScreen}</div></td>
    <td>&nbsp;</td>
    <td width="74%">
        <input type="text" <#if productCategory?has_content>value="${productCategory.detailScreen?if_exists}"</#if> name="detailScreen" size="60" maxlength="250" class="inputBox"/>
        <br/><span class="tabletext">${uiLabelMap.ProductDefaultsTo} "/catalog/categorydetail.ftl"</span>
    </td>
  </tr>

  <tr>
    <td width="26%" align="right"><div class="tabletext">${uiLabelMap.ProductPrimaryParentCategory}</div></td>
    <td>&nbsp;</td>
    <td width="74%">
      <select name="primaryParentCategoryId" size="1" class="selectbox">
        <#if productCategory?has_content>
        <#if (productCategory.primaryParentCategoryId)?exists>
          <option value="${productCategory.primaryParentCategoryId}">${(primaryParentCategory.description)?if_exists} [${productCategory. primaryParentCategoryId}]</option>
        </#if>
        </#if>
        <option value="">&nbsp;</option>
        <#list categoryList as curProductCategory>
          <option value="${curProductCategory.productCategoryId}">${curProductCategory.description?if_exists} [${curProductCategory.productCategoryId}]</option>
        </#list>
      </select>
    </td>
  </tr>
  <tr>
    <td colspan="2">&nbsp;</td>
    <td><input type="submit" name="Update" value="${uiLabelMap.CommonUpdate}" style="font-size: x-small;"/></td>
  </tr>
</table>
</form>
<br/>
  <#if productCategoryId?has_content>
    <hr class="sepbar"/>

    <script language="JavaScript" type="text/javascript">
        function setUploadUrl(newUrl) {
        var toExec = 'document.imageUploadForm.action="' + newUrl + '";';
        eval(toExec);
        };
    </script>
    <div class="head3">${uiLabelMap.CategoryUploadImage}</div>
    <form method="post" enctype="multipart/form-data" action="<@ofbizUrl>UploadCategoryImage?productCategoryId=${productCategoryId?if_exists}&amp;upload_file_type=category</@ofbizUrl>" name="imageUploadForm">
        <input type="file" class="inputBox" size="50" name="fname"/>
        <br/>
        <span class="tabletext">
            <input class="radioButton" type="radio" name="upload_file_type_bogus" value="category" checked="checked" onclick='setUploadUrl("<@ofbizUrl>UploadCategoryImage?productCategoryId=${productCategoryId}&amp;upload_file_type=category</@ofbizUrl>");'/>${uiLabelMap.ProductCategoryImageUrl}
            <input class="radioButton" type="radio" name="upload_file_type_bogus" value="linkOne" onclick='setUploadUrl("<@ofbizUrl>UploadCategoryImage?productCategoryId=${productCategoryId}&amp;upload_file_type=linkOne</@ofbizUrl>");'/>${uiLabelMap.ProductLinkOneImageUrl}
            <input class="radioButton" type="radio" name="upload_file_type_bogus" value="linkTwo"onclick='setUploadUrl("<@ofbizUrl>UploadCategoryImage?productCategoryId=${productCategoryId}&amp;upload_file_type=linkTwo</@ofbizUrl>");'/>${uiLabelMap.ProductLinkTwoImageUrl}
        </span>
        <input type="submit" class="smallSubmit" value="${uiLabelMap.ProductUploadImage}"/>
    </form>
    <hr/>
    <div class="head2">${uiLabelMap.ProductDuplicateProductCategory}</div>
    <form action="/catalog/control/DuplicateProductCategory" method="post" style="margin: 0;">
        <span class="tabletext">${uiLabelMap.ProductDuplicateProductCategorySelected}:</span>
        <input type=hidden name="oldProductCategoryId" value="${productCategoryId}"/>
        <div>
            <input type="text" class="inputBox" size="20" maxlength="20" name="productCategoryId"/>&nbsp;<input type=submit class="smallSubmit" value="${uiLabelMap.CommonGo}"/>
        </div>
        <div class="tabletext">
            <b>${uiLabelMap.CommonDuplicate}:</b>
            ${uiLabelMap.ProductCategoryContent}&nbsp;<input type="checkbox" class="checkBox" name="duplicateContent" value="Y" checked />
            ${uiLabelMap.ProductCategoryRollupParentCategories}&nbsp;<input type="checkbox" class="checkBox" name="duplicateParentRollup" value="Y" checked />
            ${uiLabelMap.ProductCategoryRollupChildCategories}&nbsp;<input type="checkbox" class="checkBox" name="duplicateChildRollup" value="Y" />
            ${uiLabelMap.ProductProducts}&nbsp;<input type="checkbox" class="checkBox" name="duplicateMembers" value="Y" checked />
            ${uiLabelMap.ProductCatalogs}&nbsp;<input type="checkbox" class="checkBox" name="duplicateCatalogs" value="Y" checked />
            ${uiLabelMap.ProductFeatures}&nbsp;<input type="checkbox" class="checkBox" name="duplicateFeatures" value="Y" checked />
            ${uiLabelMap.PartyParties}&nbsp;<input type="checkbox" class="checkBox" name="duplicateRoles" value="Y" checked />
            ${uiLabelMap.ProductAttributes}&nbsp;<input type="checkbox" class="checkBox" name="duplicateAttributes" value="Y" checked />
        </div>
    </form>
  </#if>
