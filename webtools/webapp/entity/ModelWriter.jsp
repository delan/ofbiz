<%@ page contentType="text/plain" %><%@ page import="java.util.*, java.io.*, java.net.*, org.ofbiz.core.config.*, org.ofbiz.core.util.*, org.ofbiz.core.entity.*, org.ofbiz.core.entity.config.*, org.ofbiz.core.entity.model.*" %><jsp:useBean id="delegator" type="org.ofbiz.core.entity.GenericDelegator" scope="request" /><jsp:useBean id="security" type="org.ofbiz.core.security.Security" scope="request" /><%
try {
if (security.hasPermission("ENTITY_MAINT", session) || request.getParameter("originalLoaderName") != null) {
  if ("true".equals(request.getParameter("savetofile"))) {
    //save to the file specified in the ModelReader config
    String controlPath = (String) request.getAttribute(SiteDefs.CONTROL_PATH);
    String serverRootUrl = (String) request.getAttribute(SiteDefs.SERVER_ROOT_URL);
    ModelReader modelReader = delegator.getModelReader();

    Iterator handlerIter = modelReader.getResourceHandlerEntitiesKeyIterator();
    while (handlerIter.hasNext()) {
      ResourceHandler resourceHandler = (ResourceHandler) handlerIter.next();
      if (resourceHandler.isFileResource()) {
          String filename = resourceHandler.getFullLocation();

          java.net.URL url = new java.net.URL(serverRootUrl + controlPath + "/view/ModelWriter");
          HashMap params = new HashMap();
          params.put("originalLoaderName", resourceHandler.getLoaderName());
          params.put("originalLocation", resourceHandler.getLocation());
          HttpClient httpClient = new HttpClient(url, params);
          InputStream in = httpClient.getStream();

          File newFile = new File(filename);
          FileWriter newFileWriter = new FileWriter(newFile);

          BufferedReader post = new BufferedReader(new InputStreamReader(in));
          String line = null;
          while ((line = post.readLine()) != null) {
            newFileWriter.write(line);
            newFileWriter.write("\n");
          }
          newFileWriter.close();
          %>
              If you aren't seeing any exceptions, XML was written successfully to:
              <%=filename%>
              from the URL:
              <%=url.toString()%>?originalLoaderName=<%=resourceHandler.getLoaderName()%>&originalLocation=<%=resourceHandler.getLocation()%>
          <%
      } else {
          %>Cannot write to location <%=resourceHandler.getLocation()%> from 
          loader <%=resourceHandler.getLoaderName()%>, it is not a file.<%
      }
    }
  } else {
    String title = "Entity of an Open For Business Project Component";
    String description = "None";
    String copyright = "Copyright (c) 2002 The Open For Business Project - www.ofbiz.org";
    String author = "None";
    String version = "1.0";
%><?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE entitymodel PUBLIC "-//OFBiz//DTD Entity Model//EN" "http://www.ofbiz.org/dtds/entitymodel.dtd">
<!--
/**
 *  Title: Entity Generator Definitions for the General Data Model
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
 * @author David E. Jones (jonesde@ofbiz.org)
 * @author Andy Zeneski (jaz@jflow.net)
 * @version    1.0
 */
-->
<% 
  //GenericDelegator delegator = GenericHelperFactory.getDefaultHelper();
  ModelReader reader = delegator.getModelReader();
  Map packages = new HashMap();
  TreeSet packageNames = new TreeSet();

  //put the entityNames TreeSets in a HashMap by packageName
  Collection ec = null;

  String originalLoaderName = request.getParameter("originalLoaderName");
  String originalLocation = request.getParameter("originalLocation");
  if (originalLoaderName != null && originalLocation != null) {
    ec = reader.getResourceHandlerEntities(new ResourceHandler(EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME, originalLoaderName, originalLocation));
  } else {
    ec = reader.getEntityNames();
  }

  Iterator ecIter = ec.iterator();
  while(ecIter.hasNext()) {
    String eName = (String) ecIter.next();
    ModelEntity ent = reader.getModelEntity(eName);
    TreeSet entities = (TreeSet) packages.get(ent.getPackageName());
    if (entities == null) {
      entities = new TreeSet();
      packages.put(ent.getPackageName(), entities);
      packageNames.add(ent.getPackageName());
    }
    entities.add(eName);
  }%>
<entitymodel>
  <!-- ========================================================= -->
  <!-- ======================== Defaults ======================= -->
  <!-- ========================================================= -->
    <title><%=title%></title>
    <description><%=description%></description>
    <copyright><%=copyright%></copyright>
    <author><%=author%></author>
    <version><%=version%></version>

  <!-- ========================================================= -->
  <!-- ======================== Data Model ===================== -->
  <!-- The modules in this file are as follows:                  --><%
  Iterator packageNameIter = packageNames.iterator();
  while(packageNameIter.hasNext()) {
    String pName = (String)packageNameIter.next();%>
  <!--  - <%=pName%> --><%
  }%>
  <!-- ========================================================= -->
<%
  Iterator piter = packageNames.iterator();
  while(piter.hasNext()) {
    String pName = (String)piter.next();
    TreeSet entities = (TreeSet)packages.get(pName);
%>

  <!-- ========================================================= -->
  <!-- <%=pName%> -->
  <!-- ========================================================= -->
<%
    Iterator i = entities.iterator();
    while (i.hasNext()) {
      String entityName = (String)i.next();
      ModelEntity entity = reader.getModelEntity(entityName);
      if (entity instanceof ModelViewEntity) {
        ModelViewEntity viewEntity = (ModelViewEntity)entity;
%>
    <view-entity entity-name="<%=entity.getEntityName()%>"
            package-name="<%=entity.getPackageName()%>"<%if (entity.getDependentOn().length() > 0) {%>
            dependent-on="<%=entity.getDependentOn()%>"<%}%><%if (entity.getNeverCache()) {%>
            never-cache="true"<%}%><%if (!title.equals(entity.getTitle())) {%>
            title="<%=entity.getTitle()%>"<%}%><%if (!copyright.equals(entity.getCopyright())) {%>
            copyright="<%=entity.getCopyright()%>"<%}%><%if (!author.equals(entity.getAuthor())) {%>
            author="<%=entity.getAuthor()%>"<%}%><%if (!version.equals(entity.getVersion())) {%>
            version="<%=entity.getVersion()%>"<%}%>><%if (!description.equals(entity.getDescription())) {%>
      <description><%=entity.getDescription()%></description><%}%><%
  Iterator meIter = viewEntity.getAllModelMemberEntities().iterator();
  while(meIter.hasNext()) {
    ModelViewEntity.ModelMemberEntity modelMemberEntity = (ModelViewEntity.ModelMemberEntity) meIter.next();
%>
      <member-entity entity-alias="<%=modelMemberEntity.getEntityAlias()%>" entity-name="<%=modelMemberEntity.getEntityName()%>"/><%
  }
  for (int y = 0; y < viewEntity.getAliasesSize(); y++) {
    ModelViewEntity.ModelAlias alias = viewEntity.getAlias(y);%>
      <alias entity-alias="<%=alias.getEntityAlias()%>" name="<%=alias.getName()%>"<%if (!alias.getName().equals(alias.getField())) {
      %> field="<%=alias.getField()%>"<%}%><%if (alias.getIsPk() != null) {
      %> prim-key="<%=alias.getIsPk().toString()%>"<%}%><%if (alias.getGroupBy()) {
      %> group-by="true"<%}%><%if (UtilValidate.isNotEmpty(alias.getFunction())) {
      %> function="<%=alias.getFunction()%>"<%}%>/><%
  }
  for (int r = 0; r < viewEntity.getViewLinksSize(); r++) {
    ModelViewEntity.ModelViewLink viewLink = viewEntity.getViewLink(r);%>
      <view-link entity-alias="<%=viewLink.getEntityAlias()%>" rel-entity-alias="<%=viewLink.getRelEntityAlias()%>"<%
          if (viewLink.isRelOptional()) {%> rel-optional="true"<%}%>><%for (int km = 0; km < viewLink.getKeyMapsSize(); km++){ ModelKeyMap keyMap = viewLink.getKeyMap(km);%>
        <key-map field-name="<%=keyMap.getFieldName()%>"<%if (!keyMap.getFieldName().equals(keyMap.getRelFieldName())) {%> rel-field-name="<%=keyMap.getRelFieldName()%>"<%}%>/><%}%>
      </view-link><%
  }
  if (entity.getRelationsSize() > 0) {
    for (int r = 0; r < entity.getRelationsSize(); r++) {
      ModelRelation relation = entity.getRelation(r);%>
      <relation type="<%=relation.getType()%>"<%if (relation.getTitle().length() > 0) {%> title="<%=relation.getTitle()%>"<%}
              %> rel-entity-name="<%=relation.getRelEntityName()%>"><%for (int km = 0; km < relation.getKeyMapsSize(); km++){ ModelKeyMap keyMap = relation.getKeyMap(km);%>
        <key-map field-name="<%=keyMap.getFieldName()%>"<%if (!keyMap.getFieldName().equals(keyMap.getRelFieldName())) {%> rel-field-name="<%=keyMap.getRelFieldName()%>"<%}%>/><%}%>
      </relation><%
    }
  }%>
    </view-entity><%
      }
      else {
%>
    <entity entity-name="<%=entity.getEntityName()%>"<%if (!entity.getEntityName().equals(ModelUtil.dbNameToClassName(entity.getTableName()))){
          %> table-name="<%=entity.getTableName()%>"<%}%>
            package-name="<%=entity.getPackageName()%>"<%if (entity.getDependentOn().length() > 0) {%>
            dependent-on="<%=entity.getDependentOn()%>"<%}%><%if (entity.getDoLock()) {%>
            enable-lock="true"<%}%><%if (entity.getNeverCache()) {%>
            never-cache="true"<%}%><%if (!title.equals(entity.getTitle())) {%>
            title="<%=entity.getTitle()%>"<%}%><%if (!copyright.equals(entity.getCopyright())) {%>
            copyright="<%=entity.getCopyright()%>"<%}%><%if (!author.equals(entity.getAuthor())) {%>
            author="<%=entity.getAuthor()%>"<%}%><%if (!version.equals(entity.getVersion())) {%>
            version="<%=entity.getVersion()%>"<%}%>><%if (!description.equals(entity.getDescription())) {%>
      <description><%=entity.getDescription()%></description><%}%><%
  for (int y = 0; y < entity.getFieldsSize(); y++) {
    ModelField field = entity.getField(y);%>
      <field name="<%=field.getName()%>"<%if (!field.getName().equals(ModelUtil.dbNameToVarName(field.getColName()))){
      %> col-name="<%=field.getColName()%>"<%}%> type="<%=field.getType()%>"><%
    for (int v = 0; v<field.getValidatorsSize(); v++) {
      String valName = field.getValidator(v);
      %><validate name="<%=valName%>"/><%
    }%></field><%
  }
  for (int y = 0; y < entity.getPksSize(); y++) {
    ModelField field = entity.getPk(y);%>
      <prim-key field="<%=field.getName()%>"/><%
  }
  if (entity.getRelationsSize() > 0) {
    for (int r=0; r<entity.getRelationsSize(); r++) {
      ModelRelation relation = entity.getRelation(r);%>
      <relation type="<%=relation.getType()%>"<%if (relation.getFkName().length() > 0) {%> fk-name="<%=relation.getFkName()%>"<%}
              %><%if (relation.getTitle().length() > 0) {%> title="<%=relation.getTitle()%>"<%}
              %> rel-entity-name="<%=relation.getRelEntityName()%>"><%for (int km = 0; km < relation.getKeyMapsSize(); km++){ ModelKeyMap keyMap = relation.getKeyMap(km);%>
        <key-map field-name="<%=keyMap.getFieldName()%>"<%if (!keyMap.getFieldName().equals(keyMap.getRelFieldName())) {%> rel-field-name="<%=keyMap.getRelFieldName()%>"<%}%>/><%}%>
      </relation><%
    }
  }%>
    </entity><%
      }
    }
  }%>
</entitymodel>
<%
  }
} else {
  %>ERROR: You do not have permission to use this page (ENTITY_MAINT needed)<%
}
} catch (Exception e) {
    Debug.log(e);
}
%>
