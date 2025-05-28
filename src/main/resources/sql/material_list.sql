-- 清理过程
-- 1. 删除已存在的表（如果存在）
DROP TABLE IF EXISTS material_list;

-- 2. 创建新表
CREATE TABLE material_list (
    `Product_Line` VARCHAR(255) COMMENT '产品线',
    `SFDALicense_ProductCNname` VARCHAR(255) COMMENT 'SFDA许可证:产品中文名称',
    `SFDALicense_Product_EN_name` VARCHAR(255) COMMENT 'SFDA许可证:产品英文名称',
    `SFDALicense_First_Approved_Date` VARCHAR(255) COMMENT 'SFDA许可证:首次批准日期',
    `Abbr` VARCHAR(255) COMMENT '缩写',
    `REF` VARCHAR(255) COMMENT '参考号',
    `SMN` VARCHAR(255) COMMENT 'SMN',
    `EAN_UPC_DI` VARCHAR(255) COMMENT 'EAN/UPC(DI)',
    `最小销售单元中使用单元的数量` VARCHAR(255) COMMENT '最小销售单元中使用单元的数量',
    `Unit_of_Use_Device_Identifier` VARCHAR(255) COMMENT '使用单元设备标识符',
    `Package` VARCHAR(255) COMMENT '包装',
    `SFDALicense` VARCHAR(255) COMMENT 'SFDA许可证',
    `Approved_Date` VARCHAR(255) COMMENT '批准日期',
    `Effective_Date` VARCHAR(255) COMMENT '生效日期',
    `SFDALicense_ExpiredDate` VARCHAR(255) COMMENT 'SFDA许可证:过期日期',
    `SFDALicense_Chinese_name_of_Certificate_Holder` VARCHAR(255) COMMENT 'SFDA许可证:证书持有人中文名称',
    `SFDALicense_English_name_of_Certificate_Holder` VARCHAR(255) COMMENT 'SFDA许可证:证书持有人英文名称',
    `SFDALicense_Certificate_Holder` VARCHAR(255) COMMENT 'SFDA许可证:证书持有人',
    `SFDALicense_Holders_Address` VARCHAR(255) COMMENT 'SFDA许可证:持有人地址',
    `SFDALicense_Holders_Address2` VARCHAR(255) COMMENT 'SFDA许可证:持有人地址2',
    `manufactures_Address` VARCHAR(255) COMMENT '制造商地址',
    `Manufacturer_city` VARCHAR(255) COMMENT '制造商城市',
    `IFU_valid_from_Date` VARCHAR(255) COMMENT 'IFU有效期开始日期',
    `IFU_valid_to_Date` VARCHAR(255) COMMENT 'IFU有效期结束日期',
    `CN_IFU_IssuedDate` VARCHAR(255) COMMENT '中文IFU发布日期',
    `Label_valid_from_Date` VARCHAR(255) COMMENT '标签有效期开始日期',
    `Label_valid_to_Date` VARCHAR(255) COMMENT '标签有效期结束日期',
    `CN_Label_IssuedDate` VARCHAR(255) COMMENT '中文标签发布日期',
    `Reviewer` VARCHAR(255) COMMENT '审核人',
    `ReviewDate` VARCHAR(255) COMMENT '审核日期',
    `EN_IFU_Version` VARCHAR(255) COMMENT '英文IFU版本',
    `EXP_Month` VARCHAR(255) COMMENT '有效期(月)',
    `RA_Check_Status` VARCHAR(255) COMMENT 'RA检查状态',
    `CER_History_FY_TYPE_Class_CER` VARCHAR(255) COMMENT '证书历史(FY-TYPE-Class-CER)',
    `Status` VARCHAR(255) COMMENT '状态',
    `SFDALicense_产品类别_设备_耗材` VARCHAR(255) COMMENT 'SFDA许可证:产品类别(设备/耗材)',
    `SFDALicense_器械类别_器械_体外诊断` VARCHAR(255) COMMENT 'SFDA许可证:器械类别(器械/体外诊断)',
    `SFDALicense_代理人住所` VARCHAR(255) COMMENT 'SFDA许可证:代理人住所',
    `SFDALicense_代理人名称` VARCHAR(255) COMMENT 'SFDA许可证:代理人名称',
    `SFDALicense_IVD注册申请表分类` VARCHAR(255) COMMENT 'SFDA许可证:IVD注册申请表分类',
    `SFDALicense_备注_注册证备注信息` VARCHAR(255) COMMENT 'SFDA许可证:备注(注册证备注信息)',
    `SFDALicense_Abbreviation` VARCHAR(255) COMMENT 'SFDA许可证:缩写',
    `SFDALicense_生产标识是否包含失效日期` VARCHAR(255) COMMENT 'SFDA许可证:生产标识是否包含失效日期',
    `SFDALicense_生产标识是否包含序列号` VARCHAR(255) COMMENT 'SFDA许可证:生产标识是否包含序列号',
    `SFDALicense_生产标识是否包含批号` VARCHAR(255) COMMENT 'SFDA许可证:生产标识是否包含批号',
    `SFDALicense_生产标识是否包含生产日期` VARCHAR(255) COMMENT 'SFDA许可证:生产标识是否包含生产日期',
    `UDI_Database_Add_Updated` VARCHAR(255) COMMENT 'UDI数据库添加/更新',
    `RA_Owner` VARCHAR(255) COMMENT 'RA所有者',
    `Released_IFU_Date` VARCHAR(255) COMMENT '发布IFU日期',
    `Change_EN_VS_CN` VARCHAR(255) COMMENT '英文与中文变更',
    `Released_IFU_no` VARCHAR(255) COMMENT '发布IFU编号',
    `Released_Label_No` VARCHAR(255) COMMENT '发布标签编号',
    `Released_Lable_Date` VARCHAR(255) COMMENT '发布标签日期',
    `UDI_Database_Description` VARCHAR(255) COMMENT 'UDI数据库描述',
    `Change_Description` VARCHAR(255) COMMENT '变更描述',
    `ID` VARCHAR(255) COMMENT 'ID',
    `Modified` VARCHAR(255) COMMENT '修改时间',
    `Modified_By` VARCHAR(255) COMMENT '修改人',
    `BU` VARCHAR(255) COMMENT '业务单元',
    `License` VARCHAR(255) COMMENT '许可证',
    `Item_Type` VARCHAR(255) COMMENT '项目类型',
    `Path` VARCHAR(255) COMMENT '路径'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 添加索引（如果需要）
ALTER TABLE material_list ADD INDEX idx_product_line (`Product_Line`);
ALTER TABLE material_list ADD INDEX idx_sfda_license (`SFDALicense`);
ALTER TABLE material_list ADD INDEX idx_id (`ID`);

-- 4. 添加注释
ALTER TABLE material_list COMMENT '物料清单表';

-- 5. 验证表是否创建成功
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name = 'material_list';

-- 6. 显示表结构
DESCRIBE material_list; 