<#--
 *  Copyright (c) 2003-2004 The Open For Business Project - www.ofbiz.org
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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Rev$
 *@since      2.1
-->
<#-- variable setup -->
<#assign uiLabelMap = requestAttributes.uiLabelMap>
<#assign product = requestAttributes.product?if_exists>
<#assign productContentWrapper = requestAttributes.productContentWrapper?if_exists>
<#assign price = requestAttributes.priceMap?if_exists>
<#assign nowTimestamp = Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp()>
<#-- end variable setup -->

<#-- virtual product javascript -->
${requestAttributes.virtualJavaScript?if_exists}
<script language="JavaScript">
<!--
    var detailImageUrl = null;
     function setAddProductId(name) {
        document.addform.add_product_id.value = name;
        if (document.addform.quantity == null) return;
        if (name == 'NULL' || isVirtual(name) == true) {
            document.addform.quantity.disabled = true;
        } else {
            document.addform.quantity.disabled = false;
        }
     }
     function isVirtual(product) {
        var isVirtual = false;
        <#if requestAttributes.virtualJavaScript?exists>
        for (i = 0; i < VIR.length; i++) {
            if (VIR[i] == product) {
                isVirtual = true;
            }
        }
        </#if>
        return isVirtual;
     }
    function addItem() {
       if (document.addform.add_product_id.value == 'NULL') {
           alert("Please enter all the required information.");
           return;
       } else {
             if (isVirtual(document.addform.add_product_id.value)) {
                document.location = '<@ofbizUrl>/product?category_id=${requestAttributes.categoryId?if_exists}&product_id=</@ofbizUrl>' + document.addform.add_product_id.value;
                return;
             } else {
                 document.addform.submit();
             }
       }
    }

    function popupDetail() {
        var defaultDetailImage = "${firstDetailImage?default(mainDetailImageUrl?default("_NONE_"))}";
        if (defaultDetailImage == null || defaultDetailImage == "null") {
            defaultDetailImage = "_NONE_";
        }

        if (detailImageUrl == null || detailImageUrl == "null") {
            detailImageUrl = defaultDetailImage;
        }

        if (detailImageUrl == "_NONE_") {
            alert("No detail image available to display.");
            return;
        }
        popUp("<@ofbizUrl>/detailImage?detail=" + detailImageUrl + "</@ofbizUrl>", 'detailImage', '400', '550');
    }

    function toggleAmt(toggle) {
        if (toggle == 'Y') {
            changeObjectVisibility("add_amount", "visible");
        }

        if (toggle == 'N') {
            changeObjectVisibility("add_amount", "hidden");
        }
    }

    function findIndex(name) {
        for (i = 0; i < OPT.length; i++) {
            if (OPT[i] == name) {
                return i;
            }
        }
        return -1;
    }

    function getList(name, index, src) {
        currentFeatureIndex = findIndex(name);

        if (currentFeatureIndex == 0) {
            // set the images for the first selection
            if (IMG[index] != null) {
                if (document.images['mainImage'] != null) {
                    document.images['mainImage'].src = IMG[index];
                    detailImageUrl = DET[index];
                }
            }

            // set the drop down index for swatch selection
            document.forms["addform"].elements[name].selectedIndex = (index*1)+1;
        }

        if (currentFeatureIndex < (OPT.length-1)) {
            // eval the next list if there are more
            var selectedValue = document.forms["addform"].elements[name].options[(index*1)+1].value;
            eval("list" + OPT[(currentFeatureIndex+1)] + selectedValue + "()");

            // set the product ID to NULL to trigger the alerts
            setAddProductId('NULL');
        } else {
            // this is the final selection -- locate the selected index of the last selection
            var indexSelected = document.forms["addform"].elements[name].selectedIndex;

            // using the selected index locate the sku
            var sku = document.forms["addform"].elements[name].options[indexSelected].value;

            // set the product ID
            setAddProductId(sku);

            // check for amount box
            toggleAmt(checkAmtReq(sku));
        }
    }
 //-->
 </script>

${jsscript}
<script language="JavaScript">
<!--
     function resetTotalPrice(name) {
     }
-->
</script>
<table border="0" width="100%" cellpadding="2" cellspacing='0'>

  <#-- Category next/previous -->
  <#if requestAttributes.category?exists>
    <tr>
      <td colspan="2" align="right">
        <#if requestAttributes.previousProductId?exists>
          <a href='<@ofbizUrl>/product/~category_id=${requestAttributes.categoryId?if_exists}/~product_id=${requestAttributes.previousProductId?if_exists}</@ofbizUrl>' class="buttontext">[${uiLabelMap.CommonPrevious}]</a>&nbsp;|&nbsp;
        </#if>
        <a href="<@ofbizUrl>/category/~category_id=${requestAttributes.categoryId?if_exists}</@ofbizUrl>" class="buttontext">${requestAttributes.category.description?if_exists}</a>
        <#if requestAttributes.nextProductId?exists>
          &nbsp;|&nbsp;<a href='<@ofbizUrl>/product/~category_id=${requestAttributes.categoryId?if_exists}/~product_id=${requestAttributes.nextProductId?if_exists}</@ofbizUrl>' class="buttontext">[${uiLabelMap.CommonNext}]</a>
        </#if>
      </td>
    </tr>
  </#if>

  <tr><td colspan="2"><hr class='sepbar'></td></tr>

  <#-- Product image/name/price -->
  <tr>
    <td align="left" valign="top" width="0">
      <#assign productLargeImageUrl = productContentWrapper.get("LARGE_IMAGE_URL")?if_exists>
      <#-- remove the next two lines to always display the virtual image first (virtual images must exist) -->
      <#if firstLargeImage?has_content>
        <#assign productLargeImageUrl = firstLargeImage>
      </#if>
      <#if productLargeImageUrl?has_content>
        <a href="javascript:popupDetail();"><img src='<@ofbizContentUrl>${requestAttributes.contentPathPrefix?if_exists}${productLargeImageUrl?if_exists}</@ofbizContentUrl>' name='mainImage' vspace='5' hspace='5' border='0' width='200' align='left'></a>
      </#if>
    </td>
    <td align="right" valign="top">
      <div class="head2">${productContentWrapper.get("PRODUCT_NAME")?if_exists}</div>
      <div class="tabletext">${productContentWrapper.get("DESCRIPTION")?if_exists}</div>
      <div class="tabletext"><b>${product.productId?if_exists}</b></div>
      <#-- example of showing a certain type of feature with the product -->
      <#if sizeProductFeatureAndAppls?has_content>
        <div class="tabletext">
          <#if (sizeProductFeatureAndAppls?size == 1)>
            Size:
          <#else>
            Sizes Available:
          </#if>
          <#list sizeProductFeatureAndAppls as sizeProductFeatureAndAppl>
            ${sizeProductFeatureAndAppl.description?default(sizeProductFeatureAndAppl.abbrev?default(sizeProductFeatureAndAppl.productFeatureId))}<#if sizeProductFeatureAndAppl_has_next>,</#if>
          </#list>
        </div>
      </#if>
      
      <#-- for prices:
              - if totalPrice is present, use it (totalPrice is the price calculated from the components)
              - if price < competitivePrice, show competitive or "Compare At" price
              - if price < listPrice, show list price
              - if price < defaultPrice and defaultPrice < listPrice, show default
              - if isSale show price with salePrice style and print "On Sale!"
      -->
      <#if totalPrice?exists>
        <div class="tabletext">${uiLabelMap.ProductAggregatedPrice}: <span class='basePrice'><@ofbizCurrency amount=totalPrice isoCode=price.currencyUsed/></span></div>
      <#else>
      <#if price.competitivePrice?exists && price.price?exists && price.price?double < price.competitivePrice?double>
        <div class="tabletext">${uiLabelMap.ProductCompareAtPrice}: <span class='basePrice'><@ofbizCurrency amount=price.competitivePrice isoCode=price.currencyUsed/></span></div>
      </#if>
      <#if price.listPrice?exists && price.price?exists && price.price?double < price.listPrice?double>
        <div class="tabletext">${uiLabelMap.ProductListPrice}: <span class='basePrice'><@ofbizCurrency amount=price.listPrice isoCode=price.currencyUsed/></span></div>
      </#if>
      <#if price.listPrice?exists && price.defaultPrice?exists && price.price?exists && price.price?double < price.defaultPrice?double && price.defaultPrice?double < price.listPrice?double>
        <div class="tabletext">${uiLabelMap.ProductRegularPrice}: <span class='basePrice'><@ofbizCurrency amount=price.defaultPrice isoCode=price.currencyUsed/></span></div>
      </#if>
      <div class="tabletext">
        <b>
          <#if price.isSale>
            <span class='salePrice'>${uiLabelMap.EcommerceOnSale}!</span>
            <#assign priceStyle = "salePrice">
          <#else>
            <#assign priceStyle = "regularPrice">
          </#if>
            ${uiLabelMap.EcommerceYourPrice}: <#if "Y" = product.isVirtual?if_exists> from </#if><span class='${priceStyle}'><@ofbizCurrency amount=price.price isoCode=price.currencyUsed/></span>
        </b>
      </div>
      <#if price.listPrice?exists && price.price?exists && price.price?double < price.listPrice?double>
        <#assign priceSaved = price.listPrice?double - price.price?double>
        <#assign percentSaved = (priceSaved?double / price.listPrice?double) * 100>
        <div class="tabletext">${uiLabelMap.EcommerceSave}: <span class="basePrice"><@ofbizCurrency amount=priceSaved isoCode=price.currencyUsed/> (${percentSaved?int}%)</span></div>
      </#if>
      </#if>

      <#-- Included quantities/pieces -->
      <#if product.quantityIncluded?exists && product.quantityIncluded?double != 0>
        <div class="tabletext">${uiLabelMap.EcommerceIncludes}:
          ${product.quantityIncluded?if_exists}
          ${product.quantityUomId?if_exists}
        </div>
      </#if>
      <#if product.piecesIncluded?exists && product.piecesIncluded?long != 0>
        <div class="tabletext">${uiLabelMap.EcommercePieces}:
          ${product.piecesIncluded}
        </div>
      </#if>
      <#if daysToShip?exists>
        <div class="tabletext"><b>${uiLabelMap.ProductUsuallyShipsIn} <font color='red'>${daysToShip}</font> ${uiLabelMap.CommonDays}!<b></div>
      </#if>

      <#-- tell a friend -->
      <div class="tabletext">&nbsp;</div>
      <div class="tabletext">
        <a href="javascript:popUpSmall('<@ofbizUrl>/tellafriend?productId=${product.productId}</@ofbizUrl>','tellafriend');" class="buttontext">${uiLabelMap.CommonTellAFriend}</a>
      </div>

      <p>&nbsp;</p>
      <#if requestAttributes.disFeatureList?exists && 0 < requestAttributes.disFeatureList.size()>
        <#list requestAttributes.disFeatureList as currentFeature>
            <div class="tabletext">
                ${currentFeature.productFeatureTypeId}:&nbsp;${currentFeature.description}
            </div>
        </#list>
            <div class="tabletext">&nbsp;</div>
      </#if>

      <form method="POST" action="<@ofbizUrl>/additem<#if requestAttributes._CURRENT_VIEW_?exists>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>" name="addform" style='margin: 0;'>
        <#assign inStock = true>
        <#-- Variant Selection -->
        <#if product.isVirtual?exists && product.isVirtual?upper_case == "Y">
          <#if requestAttributes.variantTree?exists && 0 < requestAttributes.variantTree.size()>
            <#list requestAttributes.featureSet as currentType>
              <div class="tabletext">
                <select name="FT${currentType}" class="selectBox" onchange="javascript:getList(this.name, (this.selectedIndex-1), 1);">
                  <option>${requestAttributes.featureTypes.get(currentType)}</option>
                </select>
              </div>
            </#list>
            <input type='hidden' name="product_id" value='${product.productId}'>
            <input type='hidden' name="add_product_id" value='NULL'>
          <#else>
            <input type='hidden' name="product_id" value='${product.productId}'>
            <input type='hidden' name="add_product_id" value='NULL'>
            <div class='tabletext'><b>${uiLabelMap.ProductItemOutofStock}.</b></div>
            <#assign inStock = false>
          </#if>
        <#else>
          <input type='hidden' name="product_id" value='${product.productId}'>
          <input type='hidden' name="add_product_id" value='${product.productId}'>
          <#if productNotAvailable?exists>
            <#if Static["org.ofbiz.product.store.ProductStoreWorker"].isStoreInventoryRequired(request, product)>
              <div class='tabletext'><b>${uiLabelMap.ProductItemOutofStock}.</b></div>
              <#assign inStock = false>
            <#else>
              <div class='tabletext'><b>${product.inventoryMessage?if_exists}</b></div>
            </#if>
          </#if>
        </#if>

        <p>&nbsp;</p>

        <#-- check to see if introductionDate hasn't passed yet -->
        <#if product.introductionDate?exists && nowTimestamp.before(product.introductionDate)>
          <div class='tabletext' style='color: red;'>${uiLabelMap.ProductProductNotYetMadeAvailable}.</div>
        <#-- check to see if salesDiscontinuationDate has passed -->
        <#elseif product.salesDiscontinuationDate?exists && nowTimestamp.after(product.salesDiscontinuationDate)>
          <div class='tabletext' style='color: red;'>${uiLabelMap.ProductProductNoLongerAvailable}.</div>
        <#-- check to see if the product requires inventory check and has inventory -->
        <#else>
          <#if inStock>
            <#if product.requireAmount?default("N") == "Y">
              <#assign hiddenStyle = "tabletext">
            <#else>
              <#assign hiddenStyle = "tabletexthidden">
            </#if>
            <div id="add_amount" class="${hiddenStyle}">
              <nobr><b>Amount:</b></nobr>&nbsp;
              <input type="text" class="inputBox" size="5" name="add_amount" value="">
            </div>
            <#if orderQuantityDisabled?exists>
              <div class="tabletext">[${uiLabelMap.EcommerceProductNotConfigured}]&nbsp;
              <input type="text" class="inputBox" size="5" name="quantity" value="0" disabled></div>
            <#else>
              <a href="javascript:addItem()" class="buttontext"><nobr>[${uiLabelMap.EcommerceAddToCart}]</nobr></a>&nbsp;
              <input type="text" class="inputBox" size="5" name="quantity" value="1" >
            </#if>
          </#if>
          <#if requestParameters.category_id?exists>
            <input type='hidden' name='category_id' value='${requestParameters.category_id}'>
          </#if>
        </#if>
      </form>
	<div class="tabletext">
      <#if sessionAttributes.userLogin?has_content && sessionAttributes.userLogin.userLoginId != "anonymous">
        <hr class="sepbar">
        <form name="addToShoppingList" method="post" action="<@ofbizUrl>/addItemToShoppingList<#if requestAttributes._CURRENT_VIEW_?exists>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>">
          <input type="hidden" name="productId" value="${requestParameters.product_id}">
          <input type="hidden" name="product_id" value="${requestParameters.product_id}">
          <select name="shoppingListId" class="selectBox">
            <#if shoppingLists?has_content>
              <#list shoppingLists as shoppingList>
                <option value="${shoppingList.shoppingListId}">${shoppingList.listName}</option>
              </#list>
            </#if>
            <option value="">---</option>
            <option value="">${uiLabelMap.EcommerceNewShoppingList}</option>
          </select>
          &nbsp;&nbsp;
          <input type="text" size="5" class="inputBox" name="quantity" value="1">
          <a href="javascript:document.addToShoppingList.submit();" class="buttontext">[${uiLabelMap.EcommerceAddToShoppingList}]</a>
        </form>
      <#else> <br>
        ${uiLabelMap.EcommerceYouMust} <a href="<@ofbizUrl>/checkLogin/showcart</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonLogin}</a>
        ${uiLabelMap.EcommerceToAddSelectedItemsToShoppingList}.&nbsp;
      </#if>
	  </div>
      <#-- Prefill first select box (virtual products only) -->
      <#if requestAttributes.variantTree?exists && 0 < requestAttributes.variantTree.size()>
        <script language="JavaScript">eval("list" + "${requestAttributes.featureOrderFirst}" + "()");</script>
      </#if>

      <#-- Swatches (virtual products only) -->
      <#if requestAttributes.variantSample?exists && 0 < requestAttributes.variantSample.size()>
        <#assign imageKeys = requestAttributes.variantSample.keySet()>
        <#assign imageMap = requestAttributes.variantSample>
        <p>&nbsp;</p>
        <table cellspacing="0" cellpadding="0">
          <tr>
            <#assign maxIndex = 7>
            <#assign indexer = 0>
            <#list imageKeys as key>
              <#assign swatchProduct = imageMap.get(key)>
              <#if swatchProduct?has_content && indexer < maxIndex>
                <#assign imageUrl = Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(swatchProduct, "SMALL_IMAGE_URL", request)?if_exists>
                <#if !imageUrl?has_content>
                  <#assign imageUrl = productContentWrapper.get("SMALL_IMAGE_URL")?if_exists>
                </#if>
                <#if !imageUrl?has_content>
                  <#assign imageUrl = "/images/defaultImage.jpg">
                </#if>
                <td align="center" valign="bottom">
                  <a href="javascript:getList('FT${requestAttributes.featureOrderFirst}','${indexer}',1);"><img src="<@ofbizContentUrl>${requestAttributes.contentPathPrefix?if_exists}${imageUrl}</@ofbizContentUrl>" border="0" width="60" height="60"></a>
                  <br>
                  <a href="javascript:getList('FT${requestAttributes.featureOrderFirst}','${indexer}',1);" class="buttontext">${key}</a>
                </td>
              </#if>
              <#assign indexer = indexer + 1>
            </#list>
            <#if (indexer > maxIndex)>
              <div class="tabletext"><b>More options available in drop down.</b></div>
            </#if>
          </tr>
        </table>
      </#if>
    </td>
  </tr>

  <tr><td colspan="2"><hr class='sepbar'></td></tr>

  <#-- Long description of product -->
  <tr>
    <td colspan="2">
      <div class="tabletext">${productContentWrapper.get("LONG_DESCRIPTION")?if_exists}</div>
    </td>
  </tr>

  <tr><td colspan="2"><hr class='sepbar'></td></tr>

  <#-- Any attributes/etc may go here -->
  <#-- Product Configurator -->
  <tr>
    <td colspan="2">
      <form method="post" action="<@ofbizUrl>/product<#if requestAttributes._CURRENT_VIEW_?exists>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>">
        <input type='hidden' name='product_id' value='${product.productId}'>
        <table width='100%'>
          <tr>
            <th><input type='submit' value='Validate'></th>
          </tr>
          <#assign counter = 0>
          <#assign questions = configwrapper.questions>
          <#list questions as question>
          <tr>
            <td>
              <div class="tableheadtext">${question.question}</div>
              <div class="tabletext">${question.content.get("LONG_DESCRIPTION")?if_exists}</div>
              <#assign instructions = question.content.get("INSTRUCTIONS")?if_exists>
              <#if instructions?has_content>
                <a href="javascript:alert('${instructions}');" class="buttontext">Instructions</a>
              </#if>
              <#assign image = question.content.get("IMAGE_URL")?if_exists>
              <#if image?has_content>
                <img src='<@ofbizContentUrl>${requestAttributes.contentPathPrefix?if_exists}${image?if_exists}</@ofbizContentUrl>' vspace='5' hspace='5' border='0' width='200' align='left'>
              </#if>
            </td>
          </tr>
          <tr>
            <td>
            <#if question.isStandard()>
              <#-- Standard item: all the options are always included -->
              <#assign options = question.options>
              <#list options as option>
                <div class="tabletext">${option.description} - <@ofbizCurrency amount=option.price isoCode=price.currencyUsed/> <#if !option.isAvailable()> (*)</#if></div>
              </#list>
            <#else>
              <#if question.isSingleChoice()>
                <#-- Single choice question -->
                <#-- The single choice input can be implemented with radio buttons or a select field -->
                <#-- This is the radio button implementation -->
                <#-- After this one, the select box implementation is also included (commented out) -->
                <#assign options = question.options>
                <#if !question.isMandatory()>
                  <div class="tabletext"><input type='RADIO' name='${counter}' value='<#if !question.isSelected()>checked</#if>'> No option</div>
                </#if>
                <#assign optionCounter = 0>
                <#list options as option>
                  <div class="tabletext">
                    <input type='RADIO' name='${counter}' value='${optionCounter}' <#if option.isSelected() || (!question.isSelected() && optionCounter == 0 && question.isMandatory())>checked</#if>>
                    ${option.description} - <@ofbizCurrency amount=option.price isoCode=price.currencyUsed/><#if !option.isAvailable()> (*)</#if>
                  </div>
                  <#assign optionCounter = optionCounter + 1>
                </#list>
                <#-- And this is the select box implementation -->
                <#-- Uncomment the lines below if you want to see it -->
<#--
                <select name='${counter}' class='selectBox'>
                <#if !question.isMandatory()>
                  <option value=''>---</option>
                </#if>
                <#assign options = question.options>
                <#assign optionCounter = 0>
                <#list options as option>
                  <#if option.isSelected()>
                    <#assign optionCounter = optionCounter + 1>
                  </#if>
                  <option value='${optionCounter}' <#if option.isSelected()>selected</#if>>
                    ${option.description} - <@ofbizCurrency amount=option.price isoCode=price.currencyUsed/> <#if !option.isAvailable()> (*)</#if>
                  </option>
                  <#assign optionCounter = optionCounter + 1>
                </#list>
                </select>
-->
              <#else>
                <#-- Multi choice question -->
                <#assign options = question.options>
                <#assign optionCounter = 0>
                <#list options as option>
                  <div class="tabletext">
                    <input type='CHECKBOX' name='${counter}' value='${optionCounter}' <#if option.isSelected()>checked</#if>>
                    ${option.description} - <@ofbizCurrency amount=option.price isoCode=price.currencyUsed/><#if !option.isAvailable()> (*)</#if>
                  </div>
                  <#assign optionCounter = optionCounter + 1>
                </#list>
              </#if>
            </#if>
            </td>
          </tr>
          <tr><td><hr></td></tr>
          <#assign counter = counter + 1>
        </#list>
        </table>
      </form>
    </td>
  </tr>
  <tr><td colspan="2"><hr class='sepbar'></td></tr>

  <#-- Product Reviews -->
  <tr>
    <td colspan="2">
      <div class="tableheadtext">${uiLabelMap.EcommerceCustomerReviews}:</div>
      <#if averageRating?exists && (averageRating?double > 0) && numRatings?exists && (numRatings?double > 1)>
          <div class="tabletext">${uiLabelMap.EcommerceAverageRating}: ${averageRating} <#if numRatings?exists>(${uiLabelMap.CommonFrom} ${numRatings} ${uiLabelMap.EcommerceRatings})</#if></div>
      </#if>
    </td>
  </tr>
  <tr><td colspan="2"><hr class='sepbar'></td></tr>
  <#if productReviews?has_content>
    <#list productReviews as productReview>
      <#assign postedUserLogin = productReview.getRelatedOne("UserLogin")>
      <#assign postedPerson = postedUserLogin.getRelatedOne("Person")?if_exists>
      <tr>
        <td colspan="2">
          <table border="0" width="100%" cellpadding="0" cellspacing='0'>
            <tr>
              <td>
                <div class="tabletext"><b>${uiLabelMap.CommonBy}: </b><#if productReview.postedAnonymous?default("N") == "Y">${uiLabelMap.EcommerceAnonymous}<#else>${postedPerson.firstName} ${postedPerson.lastName}</#if></div>
              </td>
              <td>
                <div class="tabletext"><b>${uiLabelMap.CommonOn}: </b>${productReview.postedDateTime?if_exists}</div>
              </td>
              <td>
                <div class="tabletext"><b>${uiLabelMap.EcommerceRanking}: </b>${productReview.productRating?if_exists?string}</div>
              </td>
            </tr>
            <tr>
              <td colspan="3">
                <div class="tabletext">&nbsp;</div>
              </td>
            </tr>
            <tr>
              <td colspan="3">
                <div class="tabletext">${productReview.productReview?if_exists}</div>
              </td>
            </tr>
            <tr><td colspan="3"><hr class="sepbar"/></td></tr>
          </table>
        </td>
      </tr>
    </#list>
    <tr>
      <td colspan="2">
        <a href="<@ofbizUrl>/reviewProduct?category_id=${requestAttributes.categoryId?if_exists}&product_id=${product.productId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductReviewThisProduct}!</a>
      </td>
    </tr>
  <#else>
    <tr>
      <td colspan="2">
        <div class="tabletext">${uiLabelMap.ProductProductNotReviewedYet}.</div>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <a href="<@ofbizUrl>/reviewProduct?category_id=${requestAttributes.categoryId?if_exists}&product_id=${product.productId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductBeTheFirstToReviewThisProduct}!</a>
      </td>
    </tr>
  </#if>
</table>

<#-- Upgrades/Up-Sell/Cross-Sell -->
  <#macro associated assocProducts beforeName showName afterName formNamePrefix targetRequestName>
  <#assign targetRequest = "product">
  <#if targetRequestName?has_content>
    <#assign targetRequest = targetRequestName>
  </#if>
  <#if assocProducts?has_content>
    <tr><td>&nbsp;</td></tr>
    <tr><td colspan="2"><div class="head2">${beforeName?if_exists}<#if showName == "Y">${productValue.productName}</#if>${afterName?if_exists}</div></td></tr>
    <tr><td><hr class="sepbar"/></td></tr>
    <#list assocProducts as productAssoc>
      <tr><td>
        <div class="tabletext">
          <a href='<@ofbizUrl>/${targetRequest}/<#if requestAttributes.categoryId?exists>~category_id=${requestAttributes.categoryId}/</#if>~product_id=${productAssoc.productIdTo?if_exists}</@ofbizUrl>' class="buttontext">
            ${productAssoc.productIdTo?if_exists}
          </a>
          - <b>${productAssoc.reason?if_exists}</b>
        </div>
      </td></tr>
      ${setRequestAttribute("optProductId", productAssoc.productIdTo)}
      ${setRequestAttribute("listIndex", listIndex)}
      ${setRequestAttribute("formNamePrefix", formNamePrefix)}
      <#if targetRequestName?has_content>
        ${setRequestAttribute("targetRequestName", targetRequestName)}
      </#if>
      <tr>
        <td>
          ${pages.get("/catalog/productsummary.ftl")}
        </td>
      </tr>
      <#local listIndex = listIndex + 1>
      <tr><td><hr class="sepbar"/></td></tr>
    </#list>
    ${setRequestAttribute("optProductId", "")}
    ${setRequestAttribute("formNamePrefix", "")}
    ${setRequestAttribute("targetRequestName", "")}
  </#if>
</#macro>
<#assign productValue = product>
<#assign listIndex = 1>
${setRequestAttribute("productValue", productValue)}

<table width="100%">
  <#-- obsolete -->
  <@associated assocProducts=obsoleteProducts beforeName="" showName="Y" afterName=" is made obsolete by these products:" formNamePrefix="obs" targetRequestName=""/>
  <#-- cross sell -->
  <@associated assocProducts=crossSellProducts beforeName="" showName="N" afterName="You might be interested in these as well:" formNamePrefix="cssl" targetRequestName="crosssell"/>
  <#-- up sell -->
  <@associated assocProducts=upSellProducts beforeName="Try these instead of " showName="Y" afterName=":" formNamePrefix="upsl" targetRequestName="upsell"/>
  <#-- obsolescence -->
  <@associated assocProducts=obsolenscenseProducts beforeName="" showName="Y" afterName=" makes these products obsolete:" formNamePrefix="obce" targetRequestName=""/>
</table>

<#-- special cross/up-sell area using commonFeatureResultIds (from common feature product search) -->
<#if commonFeatureResultIds?has_content>
  <div class="head2">Similar Products That Might Interest You...</div>
  <hr class="sepbar"/>

  <#list commonFeatureResultIds as commonFeatureResultId>
    <div class="tabletext">
      ${setRequestAttribute("optProductId", commonFeatureResultId)}
      ${setRequestAttribute("listIndex", commonFeatureResultId_index)}
      ${setRequestAttribute("formNamePrefix", "cfeatcssl")}
      <#-- ${setRequestAttribute("targetRequestName", targetRequestName)} -->
      ${pages.get("/catalog/productsummary.ftl")}
    </div>
    <#if commonFeatureResultId_has_next>
      <hr class="sepbar"/>
    </#if>
  </#list>
</#if>

