-- 清理过程
-- 1. 删除已存在的表（如果存在）
DROP TABLE IF EXISTS certificate_list;

-- 2. 创建新表
CREATE TABLE certificate_list (
    ID VARCHAR(255) COMMENT 'ID',
    SFDALicense VARCHAR(255) COMMENT 'SFDA许可证号',
    SMN VARCHAR(255) COMMENT 'SMN',
    First_Approve_Date DATE NULL DEFAULT NULL COMMENT '首次批准日期',
    Approved_Date DATE NULL DEFAULT NULL COMMENT '批准日期',
    ExpiredDate DATE NULL DEFAULT NULL COMMENT '过期日期',
    ProductType VARCHAR(255) COMMENT '产品类型',
    BU VARCHAR(255) COMMENT '业务单元',
    `Class` VARCHAR(255) COMMENT '类别',
    Product_line VARCHAR(255) COMMENT '产品线',
    ProductCNname VARCHAR(255) COMMENT '产品中文名称',
    Product_EN_name VARCHAR(255) COMMENT '产品英文名称',
    CurrentState VARCHAR(255) COMMENT '当前状态',
    Chinese_name_of_Certificate_Holder VARCHAR(255) COMMENT '证书持有人中文名称',
    English_name_of_Certificate_Holder VARCHAR(255) COMMENT '证书持有人英文名称',
    Certificate_Holder VARCHAR(255) COMMENT '证书持有人',
    Holder_s_Address VARCHAR(255) COMMENT '持有人地址',
    Holder_s_Address_City VARCHAR(255) COMMENT '持有人地址城市',
    `产品类别_设备_耗材` VARCHAR(255) COMMENT '产品类别（设备/耗材）',
    `器械类别_器械_体外诊断` VARCHAR(255) COMMENT '器械类别（器械/体外诊断）',
    `主要组成成分` TEXT COMMENT '主要组成成分',
    `预期用途` TEXT COMMENT '预期用途',
    `代理人名称` VARCHAR(255) COMMENT '代理人名称',
    `代理人住所` VARCHAR(255) COMMENT '代理人住所',
    `器械目录分类` VARCHAR(255) COMMENT '器械目录分类',
    `器械目录分类_编码` VARCHAR(255) COMMENT '器械目录分类编码',
    `IVD注册申请表分类` VARCHAR(255) COMMENT 'IVD注册申请表分类',
    `生产标识是否包含批号` VARCHAR(255) COMMENT '生产标识是否包含批号',
    `生产标识是否包含序列号` VARCHAR(255) COMMENT '生产标识是否包含序列号',
    `体外诊断试剂产品分类_类别` VARCHAR(255) COMMENT '体外诊断试剂产品分类类别',
    `体外诊断试剂产品分类_名称` VARCHAR(255) COMMENT '体外诊断试剂产品分类名称',
    `生产标识是否包含生产日期` VARCHAR(255) COMMENT '生产标识是否包含生产日期',
    `生产标识是否包含失效日期` VARCHAR(255) COMMENT '生产标识是否包含失效日期',
    `备注_注册证备注信息` TEXT COMMENT '注册证备注信息',
    ChangeHistory TEXT COMMENT '变更历史',
    ALLSMN VARCHAR(255) COMMENT '所有SMN',
    ALLSMN_Effective_Date DATE NULL DEFAULT NULL COMMENT '所有SMN生效日期',
    ALLSMN_ApprovedDate DATE NULL DEFAULT NULL COMMENT '所有SMN批准日期',
    ALLSMN_Reviewer VARCHAR(255) COMMENT '所有SMN审核人',
    Abbreviation VARCHAR(255) COMMENT '缩写',
    Reviewer VARCHAR(255) COMMENT '审核人',
    RA_Owner VARCHAR(255) COMMENT 'RA负责人',
    Created DATETIME NULL DEFAULT NULL COMMENT '创建时间',
    Modified DATETIME NULL DEFAULT NULL COMMENT '修改时间',
    Storage_Condition VARCHAR(255) COMMENT '储存条件',
    `编辑次数` INT NULL DEFAULT NULL COMMENT '编辑次数',
    PM_No VARCHAR(255) COMMENT 'PM编号',
    PM_No_Product_Abbreviation VARCHAR(255) COMMENT 'PM编号产品缩写',
    ExpRenewIn2Years VARCHAR(255) COMMENT '两年内是否续期',
    Modified_By VARCHAR(255) COMMENT '修改人',
    CER_Link_Material_List VARCHAR(255) COMMENT '证书关联物料清单',
    Title VARCHAR(255) COMMENT '标题',
    Title_1 VARCHAR(255) COMMENT '标题(副本)',
    TriggerApproval VARCHAR(255) COMMENT '触发审批'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书列表表';

-- 3. 添加索引
ALTER TABLE certificate_list ADD INDEX idx_id (ID);
ALTER TABLE certificate_list ADD INDEX idx_sfda_license (SFDALicense);
ALTER TABLE certificate_list ADD INDEX idx_smn (SMN);
ALTER TABLE certificate_list ADD INDEX idx_product_line (Product_line);
ALTER TABLE certificate_list ADD INDEX idx_pm_no (PM_No);

-- 4. 验证表是否创建成功
SELECT COUNT(*) FROM certificate_list;

-- 5. 显示表结构
DESC certificate_list; 