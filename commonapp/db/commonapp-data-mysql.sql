
INSERT INTO PERSON (USERNAME,PASSWORD,FIRST_NAME,LAST_NAME) VALUES ('admin','ofbiz','Cool','Administrator');
INSERT INTO PERSON (USERNAME,PASSWORD,FIRST_NAME,LAST_NAME) VALUES ('flexadmin','ofbiz','Flexible','Administrator');

INSERT INTO SECURITY_GROUP (GROUP_ID,DESCRIPTION) VALUES ('FULLADMIN','Full Admin group, has all general permissions.');
INSERT INTO PERSON_SECURITY_GROUP (USERNAME,GROUP_ID) VALUES ('admin','FULLADMIN');
INSERT INTO SECURITY_GROUP (GROUP_ID,DESCRIPTION) VALUES ('FLEXADMIN','Flexible Administrator, has all granular permissions.');
INSERT INTO PERSON_SECURITY_GROUP (USERNAME,GROUP_ID) VALUES ('flexadmin','FLEXADMIN');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('ENTITY_MAINT','Permission to View the Entity Maintenance page.');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','ENTITY_MAINT');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','ENTITY_MAINT');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PROPERTY_CACHE_MAINT','Permission to View the Property Cache Maintenance page.');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PROPERTY_CACHE_MAINT');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PROPERTY_CACHE_MAINT');


-- Begin Generated Section --
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ADMIN','Permission to Administer a Person entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_VIEW','Permission to View a Person entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_CREATE','Permission to Create a Person entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_UPDATE','Permission to Update a Person entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_DELETE','Permission to Delete a Person entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ATTRIBUTE_ADMIN','Permission to Administer a PersonAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ATTRIBUTE_VIEW','Permission to View a PersonAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ATTRIBUTE_CREATE','Permission to Create a PersonAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ATTRIBUTE_UPDATE','Permission to Update a PersonAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_ATTRIBUTE_DELETE','Permission to Delete a PersonAttribute entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_ATTRIBUTE_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_ATTRIBUTE_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_ATTRIBUTE_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_ATTRIBUTE_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_ATTRIBUTE_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ADMIN','Permission to Administer a PersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_VIEW','Permission to View a PersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_CREATE','Permission to Create a PersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_UPDATE','Permission to Update a PersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_DELETE','Permission to Delete a PersonType entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_TYPE_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ATTRIBUTE_ADMIN','Permission to Administer a PersonTypeAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ATTRIBUTE_VIEW','Permission to View a PersonTypeAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ATTRIBUTE_CREATE','Permission to Create a PersonTypeAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ATTRIBUTE_UPDATE','Permission to Update a PersonTypeAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_TYPE_ATTRIBUTE_DELETE','Permission to Delete a PersonTypeAttribute entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_TYPE_ATTRIBUTE_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_ATTRIBUTE_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_ATTRIBUTE_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_ATTRIBUTE_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_TYPE_ATTRIBUTE_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_PERSON_TYPE_ADMIN','Permission to Administer a PersonPersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_PERSON_TYPE_VIEW','Permission to View a PersonPersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_PERSON_TYPE_CREATE','Permission to Create a PersonPersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_PERSON_TYPE_UPDATE','Permission to Update a PersonPersonType entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_PERSON_TYPE_DELETE','Permission to Delete a PersonPersonType entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_PERSON_TYPE_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_PERSON_TYPE_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_PERSON_TYPE_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_PERSON_TYPE_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_PERSON_TYPE_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_ADMIN','Permission to Administer a SecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_VIEW','Permission to View a SecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_CREATE','Permission to Create a SecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_UPDATE','Permission to Update a SecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_DELETE','Permission to Delete a SecurityGroup entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','SECURITY_GROUP_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_PERMISSION_ADMIN','Permission to Administer a SecurityPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_PERMISSION_VIEW','Permission to View a SecurityPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_PERMISSION_CREATE','Permission to Create a SecurityPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_PERMISSION_UPDATE','Permission to Update a SecurityPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_PERMISSION_DELETE','Permission to Delete a SecurityPermission entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','SECURITY_PERMISSION_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_PERMISSION_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_PERMISSION_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_PERMISSION_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_PERMISSION_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_PERMISSION_ADMIN','Permission to Administer a SecurityGroupPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_PERMISSION_VIEW','Permission to View a SecurityGroupPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_PERMISSION_CREATE','Permission to Create a SecurityGroupPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_PERMISSION_UPDATE','Permission to Update a SecurityGroupPermission entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('SECURITY_GROUP_PERMISSION_DELETE','Permission to Delete a SecurityGroupPermission entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','SECURITY_GROUP_PERMISSION_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_PERMISSION_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_PERMISSION_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_PERMISSION_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','SECURITY_GROUP_PERMISSION_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_SECURITY_GROUP_ADMIN','Permission to Administer a PersonSecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_SECURITY_GROUP_VIEW','Permission to View a PersonSecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_SECURITY_GROUP_CREATE','Permission to Create a PersonSecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_SECURITY_GROUP_UPDATE','Permission to Update a PersonSecurityGroup entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PERSON_SECURITY_GROUP_DELETE','Permission to Delete a PersonSecurityGroup entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PERSON_SECURITY_GROUP_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_SECURITY_GROUP_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_SECURITY_GROUP_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_SECURITY_GROUP_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PERSON_SECURITY_GROUP_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PRODUCT_ADMIN','Permission to Administer a Product entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PRODUCT_VIEW','Permission to View a Product entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PRODUCT_CREATE','Permission to Create a Product entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PRODUCT_UPDATE','Permission to Update a Product entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('PRODUCT_DELETE','Permission to Delete a Product entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','PRODUCT_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PRODUCT_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PRODUCT_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PRODUCT_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','PRODUCT_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ADMIN','Permission to Administer a FixedAsset entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_VIEW','Permission to View a FixedAsset entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_CREATE','Permission to Create a FixedAsset entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_UPDATE','Permission to Update a FixedAsset entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_DELETE','Permission to Delete a FixedAsset entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','FIXED_ASSET_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_DELETE');

INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ATTRIBUTE_ADMIN','Permission to Administer a FixedAssetAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ATTRIBUTE_VIEW','Permission to View a FixedAssetAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ATTRIBUTE_CREATE','Permission to Create a FixedAssetAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ATTRIBUTE_UPDATE','Permission to Update a FixedAssetAttribute entity.');
INSERT INTO SECURITY_PERMISSION (PERMISSION_ID,DESCRIPTION) VALUES ('FIXED_ASSET_ATTRIBUTE_DELETE','Permission to Delete a FixedAssetAttribute entity.');

INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FULLADMIN','FIXED_ASSET_ATTRIBUTE_ADMIN');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_ATTRIBUTE_VIEW');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_ATTRIBUTE_CREATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_ATTRIBUTE_UPDATE');
INSERT INTO SECURITY_GROUP_PERMISSION (GROUP_ID,PERMISSION_ID) VALUES ('FLEXADMIN','FIXED_ASSET_ATTRIBUTE_DELETE');


COMMIT;

