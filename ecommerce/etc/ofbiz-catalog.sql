
#
# Data for table 'PRODUCT'
#

INSERT INTO PRODUCT VALUES ('GZ-2644','101','','',100.000000,NULL,NULL,NULL,'Round Gizmo','','Round Gizmo w/ Lights','A small round gizmo with multi-colored lights. Works great in the dark. Small and compact.','','',47.99);
INSERT INTO PRODUCT VALUES ('GZ-8544','102','','',100.000000,NULL,NULL,NULL,'Big Gizmo','','Big Gizmo w/ Legs','A large gizmo with legs. Walks on its own. Does not require a power supply.','','',269.99);
INSERT INTO PRODUCT VALUES ('WG-5569','201','','',50.000000,NULL,NULL,NULL,'Tiny Chrome Widget','','Tiny Chrome Widget','This tiny chrome widget makes a perfect gift. The shine will last forever. No pollishing required.','','',59.99);
INSERT INTO PRODUCT VALUES ('WG-9943','202','','',10.000000,NULL,NULL,NULL,'Giant Widget','','Giant Widget With Wheels','This giant widget is mobile. It will seat one person safely. The wheels will never rust or break. Quite a unique item.','','',549.99);

#
# Data for table 'PRODUCT_CATEGORY'
#

INSERT INTO PRODUCT_CATEGORY VALUES ('CATALOG1',NULL,'Primary Catalog');
INSERT INTO PRODUCT_CATEGORY VALUES ('100','CATALOG1','Gizmos');
INSERT INTO PRODUCT_CATEGORY VALUES ('200','CATALOG1','Widgets');
INSERT INTO PRODUCT_CATEGORY VALUES ('101','100','Small Gizmos');
INSERT INTO PRODUCT_CATEGORY VALUES ('102','100','Large Gizmos');
INSERT INTO PRODUCT_CATEGORY VALUES ('201','200','Small Widgets');
INSERT INTO PRODUCT_CATEGORY VALUES ('202','200','Large Widgets');

#
# Data for table 'PRODUCT_CATEGORY_MEMBER'
#

INSERT INTO PRODUCT_CATEGORY_MEMBER VALUES ('202','WG-9943',NULL,NULL,'','');
INSERT INTO PRODUCT_CATEGORY_MEMBER VALUES ('101','GZ-2644',NULL,NULL,'','');
INSERT INTO PRODUCT_CATEGORY_MEMBER VALUES ('102','GZ-8544',NULL,NULL,'','');
INSERT INTO PRODUCT_CATEGORY_MEMBER VALUES ('201','WG-5569',NULL,NULL,'','');

#
# Data for table 'PRODUCT_CATEGORY_ROLLUP'
#

INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('100','CATALOG1');
INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('101','100');
INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('102','100');
INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('200','CATALOG1');
INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('201','200');
INSERT INTO PRODUCT_CATEGORY_ROLLUP VALUES ('202','200');
