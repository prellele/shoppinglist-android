-- Command to read the test_inserts with sqlite3 (shell)
-- .read C:/<Folder>/<Folder>/test_inserts.sql

-----------------------------
-- STORE
-----------------------------

INSERT INTO STORE 
            (STO_NAME) 
VALUES      ('Aldi'); 

INSERT INTO STORE 
            (STO_NAME) 
VALUES      ('Lidl'); 

INSERT INTO STORE 
            (STO_NAME) 
VALUES      ('Rewe'); 

-----------------------------
-- UNIT
-----------------------------

INSERT INTO UNIT
            (UNI_NAME) 
VALUES      ('Stück'); 

INSERT INTO UNIT
            (UNI_NAME) 
VALUES      ('Gramm'); 

INSERT INTO UNIT
            (UNI_NAME) 
VALUES      ('Kilo'); 

INSERT INTO UNIT
            (UNI_NAME) 
VALUES      ('Paket(e)'); 

INSERT INTO UNIT
            (UNI_NAME) 
VALUES      ('Liter'); 

-----------------------------
-- SHOPPINGLIST 
-- !NOTE! 	current_timestamp is the time in GMT + 0:00
-- 			so we have to convert the time in Java.
-----------------------------

INSERT INTO SHOPPINGLIST 
            (SHO_CREATED_TIME) 
VALUES      (CURRENT_TIMESTAMP); 

-----------------------------
-- FAVORITE
-----------------------------

INSERT INTO FAVORITE 
            (FAV_NAME) 
VALUES      ('Wöchentlicher Einkauf'); 

-----------------------------
-- PRODUCT
-----------------------------

INSERT INTO PRODUCT 
            (PRO_NAME, 
             PRO_UNI_ID) 
VALUES      ('Milch', 
             (SELECT UNI_ID 
              FROM   UNIT 
              WHERE  UNI_NAME = 'Liter')); 

INSERT INTO PRODUCT 
            (PRO_NAME, 
             PRO_UNI_ID) 
VALUES      ('Gemischtes Hack', 
             (SELECT UNI_ID 
              FROM   UNIT 
              WHERE  UNI_NAME = 'Kilo')); 

INSERT INTO PRODUCT 
            (PRO_NAME, 
             PRO_UNI_ID) 
VALUES      ('Mineralwasser', 
             (SELECT UNI_ID 
              FROM   UNIT 
              WHERE  UNI_NAME = 'Paket(e)')); 
			  
-----------------------------
-- SHOPPINGLIST_PRODUCT_MAPPING
-----------------------------			  

INSERT INTO SHOPPINGLIST_PRODUCT_MAPPING 
            (SPM_SHO_ID, 
             SPM_STO_ID, 
             SPM_PRO_ID,
			 SPM_QUANTITY) 
VALUES      ((SELECT Max(SHO_ID) 
              FROM   SHOPPINGLIST), 
             (SELECT STO_ID 
              FROM   STORE 
              WHERE  STO_NAME = 'Rewe'), 
             (SELECT PRO_ID 
              FROM   PRODUCT 
              WHERE  PRO_NAME = 'Milch'),
			  1); 

INSERT INTO SHOPPINGLIST_PRODUCT_MAPPING 
            (SPM_SHO_ID, 
             SPM_STO_ID, 
             SPM_PRO_ID,
			 SPM_QUANTITY) 
VALUES      ((SELECT Max(SHO_ID) 
              FROM   SHOPPINGLIST), 
             (SELECT STO_ID 
              FROM   STORE 
              WHERE  STO_NAME = 'Lidl'), 
             (SELECT PRO_ID 
              FROM   PRODUCT 
              WHERE  PRO_NAME = 'Mineralwasser'),
			  2); 

INSERT INTO SHOPPINGLIST_PRODUCT_MAPPING 
            (SPM_SHO_ID, 
             SPM_STO_ID, 
             SPM_PRO_ID,
			 SPM_QUANTITY) 
VALUES      ((SELECT Max(SHO_ID) 
              FROM   SHOPPINGLIST), 
             (SELECT STO_ID 
              FROM   STORE 
              WHERE  STO_NAME = 'Aldi'), 
             (SELECT PRO_ID 
              FROM   PRODUCT 
              WHERE  PRO_NAME = 'Gemischtes Hack'),
			  0.5); 

-----------------------------
-- FAVORITE_PRODUCT_MAPPING
-----------------------------			

INSERT INTO FAVORITE_PRODUCT_MAPPING 
            (FPM_FAV_ID, 
             FPM_STO_ID, 
             FPM_PRO_ID, 
             FPM_QUANTITY) 
VALUES      ((SELECT FAV_ID 
              FROM   FAVORITE 
              WHERE  FAV_NAME = 'Wöchentlicher Einkauf'), 
             (SELECT STO_ID 
              FROM   STORE 
              WHERE  STO_NAME = 'Rewe'), 
             (SELECT PRO_ID 
              FROM   PRODUCT 
              WHERE  PRO_NAME = 'Milch'), 
             5); 

INSERT INTO FAVORITE_PRODUCT_MAPPING 
            (FPM_FAV_ID, 
             FPM_STO_ID, 
             FPM_PRO_ID, 
             FPM_QUANTITY) 
VALUES      ((SELECT FAV_ID 
              FROM   FAVORITE 
              WHERE  FAV_NAME = 'Wöchentlicher Einkauf'), 
             (SELECT STO_ID 
              FROM   STORE 
              WHERE  STO_NAME = 'Rewe'), 
             (SELECT PRO_ID 
              FROM   PRODUCT 
              WHERE  PRO_NAME = 'Mineralwasser'), 
             2); 