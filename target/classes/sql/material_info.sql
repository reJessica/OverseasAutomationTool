-- 查询产品信息的SQL语句
SELECT 
    product_name,
    product_name_en,
    origin_country,
    origin_country_en,
    class_type,
    class_type_en,
    product_type,
    product_type_en,
    udi,
    udi_en
FROM material_list 
WHERE SMN = :productNo; 