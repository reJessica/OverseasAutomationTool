-- 清理过程
-- 1. 删除已存在的表（如果存在）
DROP TABLE IF EXISTS overseas_reports;

-- 2. 创建新表，字段顺序、命名、注释与模板一致
CREATE TABLE overseas_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_path TEXT COMMENT '报告文件路径',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '报告状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    report_no VARCHAR(50) NOT NULL COMMENT '报告编号-抓取',
    report_no_en VARCHAR(50) COMMENT 'Report No.',
    PM_no VARCHAR(50)  COMMENT '报告编码',
    report_date DATE COMMENT '报告日期',
    report_date_en DATE COMMENT 'Report Date',
    reporter VARCHAR(50) COMMENT '报告人',
    reporter_en VARCHAR(50) COMMENT 'Reporter',
    customer_name VARCHAR(255) COMMENT '单位名称',
    customer_name_en VARCHAR(255) COMMENT 'Customer name',
    address VARCHAR(255) COMMENT '联系地址',
    address_en VARCHAR(255) COMMENT 'Address',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_person_en VARCHAR(100) COMMENT 'Contact Person',
    tel VARCHAR(50) COMMENT '联系电话',
    tel_en VARCHAR(50) COMMENT 'Telphone No.',
    occurrence_place VARCHAR(255) COMMENT '发生地',
    occurrence_place_en VARCHAR(255) COMMENT 'Occurrence place',

    -- 医疗器械情况
    product_name VARCHAR(255) COMMENT '产品名称',
    product_name_en VARCHAR(255) COMMENT 'Product name',
    registration_no VARCHAR(100) COMMENT '注册证号',
    registration_no_en VARCHAR(100) COMMENT 'Registration No.',
    module VARCHAR(255) COMMENT '规格',
    module_en VARCHAR(255) COMMENT 'Module',
    product_package VARCHAR(255) COMMENT '包装',
    product_package_en VARCHAR(255) COMMENT 'Package',
    origin_country VARCHAR(100) COMMENT '原产国',
    origin_country_en VARCHAR(100) COMMENT 'Origin country',
    class_type VARCHAR(50) COMMENT '管理类别',
    class_type_en VARCHAR(50) COMMENT 'Class Type',
    product_type VARCHAR(100) COMMENT '产品类别',
    product_type_en VARCHAR(100) COMMENT 'Product type',
    product_lot VARCHAR(100) COMMENT '产品批号',
    product_lot_en VARCHAR(100) COMMENT 'Product Lot',
    product_no VARCHAR(100) COMMENT '产品编号',
    product_no_en VARCHAR(100) COMMENT 'Product No.',
    udi VARCHAR(100) COMMENT 'UDI',
    manufacturing_date DATE COMMENT '生产日期',
    manufacturing_date_en DATE COMMENT 'Manufacturing Date',
    expiration_date DATE COMMENT '有效期至',
    expiration_date_en DATE COMMENT 'Expiration Date',

    -- 不良事件情况
    event_occurrence_date DATE COMMENT '事件发生日期',
    event_occurrence_date_en DATE COMMENT 'Event Occurrence Date',
    knowledge_date DATE COMMENT '发现或获知日期',
    knowledge_date_en DATE COMMENT 'Knowledge Date',
    injury_type VARCHAR(50) COMMENT '伤害程度',
    injury_type_en VARCHAR(50) COMMENT 'Injury Type',
    injury VARCHAR(255) COMMENT '伤害表现',
    injury_en VARCHAR(255) COMMENT 'Injury',
    device_malfunction_desc TEXT COMMENT '器械故障表现',
    device_malfunction_desc_en TEXT COMMENT 'Device Malfunction Description',
    patient_name VARCHAR(100) COMMENT '姓名',
    patient_name_en VARCHAR(100) COMMENT 'Patient Name',
    birth_date DATE COMMENT '出生日期',
    birth_date_en DATE COMMENT 'Date of Birth',
    age VARCHAR(20) COMMENT '年龄',
    age_en VARCHAR(20) COMMENT 'Age',
    gender VARCHAR(10) COMMENT '性别',
    gender_en VARCHAR(10) COMMENT 'Gender',
    medical_record_no VARCHAR(100) COMMENT '病历号',
    medical_record_no_en VARCHAR(100) COMMENT 'Medical Record No.',
    medical_history TEXT COMMENT '既往病史',
    medical_history_en TEXT COMMENT 'Medical History',

    -- 使用情况
    disease_intended TEXT COMMENT '预期治疗疾病或作用',
    disease_intended_en TEXT COMMENT 'Disease intended to treat or effect',
    usage_date DATE COMMENT '器械使用日期',
    usage_date_en DATE COMMENT 'Usage Date',
    usage_site VARCHAR(255) COMMENT '使用场所',
    usage_site_en VARCHAR(255) COMMENT 'Usage site',
    institution_name VARCHAR(255) COMMENT '场所名称',
    institution_name_en VARCHAR(255) COMMENT 'Institution Name',
    usage_process TEXT COMMENT '使用过程',
    usage_process_en TEXT COMMENT 'Usage Process',
    drug_device_comb_desc TEXT COMMENT '合并用药/械情况说明',
    drug_device_comb_desc_en TEXT COMMENT 'Drug/device Combination Description',

    -- 事件调查
    investigation_flag VARCHAR(10) COMMENT '是否开展了调查',
    investigation_flag_en VARCHAR(10) COMMENT 'If carry out investigation',
    investigation_desc TEXT COMMENT '调查情况',
    investigation_desc_en TEXT COMMENT 'Investigation description',

    -- 评价结果
    relative_evaluation TEXT COMMENT '关联性评价',
    relative_evaluation_en TEXT COMMENT 'Relative Evaluation',
    event_reason_analysis TEXT COMMENT '事件原因分析',
    event_reason_analysis_en TEXT COMMENT 'Event Reason Analysis',
    need_risk_assessment VARCHAR(10) COMMENT '是否需要开展产品风险评价',
    need_risk_assessment_en VARCHAR(10) COMMENT 'If need initiate Product Risk Assessment',
    plan_submit_date DATE COMMENT '计划提交时间',
    plan_submit_date_en DATE COMMENT 'Plan submission Date',

    -- 控制措施
    has_control_measure VARCHAR(10) COMMENT '是否已采取控制措施',
    has_control_measure_en VARCHAR(10) COMMENT 'If has taken control measure',
    control_measure_details TEXT COMMENT '具体控制措施',
    control_measure_details_en TEXT COMMENT 'Control measure details',
    no_control_measure_reason TEXT COMMENT '未采取控制措施原因',
    no_control_measure_reason_en TEXT COMMENT 'No control measure reason'


) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 添加索引
ALTER TABLE overseas_reports ADD INDEX idx_report_number (report_no);
ALTER TABLE overseas_reports ADD INDEX idx_PM_no (PM_no);
ALTER TABLE overseas_reports ADD INDEX idx_product_name (product_name);
ALTER TABLE overseas_reports ADD INDEX idx_registration_number (registration_no);
ALTER TABLE overseas_reports ADD INDEX idx_status (status);

-- 4. 添加注释
ALTER TABLE overseas_reports COMMENT '海外报告记录表';

-- 5. 验证表是否创建成功
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name = 'overseas_reports';

-- 6. 显示表结构
DESCRIBE overseas_reports; 