// 导出相关功能
const ExportService = {
    // 导出为Excel
    exportToExcel: function() {
        console.log('开始导出Excel...');
        const formData = new FormData(document.getElementById('reportForm'));
        const data = {};
        formData.forEach((value, key) => {
            // 统一转换为小写，确保与后端字段名匹配
            data[key.toLowerCase()] = value;
            console.log(`获取到字段 ${key}: ${value}`);
        });
        console.log('导出数据:', data);

        // 获取报告编号和PM编号
        const reportNo = $('input[name="report_no"]').val() || '未知报告编号';
        const pmNo = $('input[name="PM_no"]').val() || '未知PM编号';
        
        console.log('报告编号:', reportNo);
        console.log('PM编号:', pmNo);
        
        // 获取当前日期
        const currentDate = new Date().toISOString().split('T')[0].replace(/-/g, '-');
        
        // 构建文件名
        const fileName = `${reportNo}_${pmNo}_${currentDate}.xlsx`;
        console.log('生成的文件名:', fileName);

        // 发送请求到后端，使用模板生成Excel
        $.ajax({
            url: '/overseas/api/report/export-template',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(data),
            xhrFields: {
                responseType: 'blob'
            },
            success: function(blob) {
                // 创建下载链接
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
                showToast('Excel导出成功', 'success');
            },
            error: function(xhr, status, error) {
                console.error('导出失败:', error);
                showToast('导出失败：' + (xhr.responseJSON?.message || '未知错误'), 'error');
            }
        });
    },

    // 导出为Word
    exportToWord: function() {
        console.log('开始导出Word...');
        const formData = new FormData(document.getElementById('reportForm'));
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
            console.log(`获取到字段 ${key}: ${value}`);
        });
        console.log('导出数据:', data);

        // 获取报告编号和PM编号
        const reportNo = data.report_no || '未知报告编号';
        const pmNo = data.pm_no || '未知PM编号';
        // 获取当前日期
        const currentDate = new Date().toISOString().split('T')[0].replace(/-/g, '-');
        
        // 构建文件名
        const fileName = `${reportNo}_${pmNo}_${currentDate}.docx`;

        // 创建文档
        const doc = new docx.Document({
            sections: [{
                properties: {},
                children: [
                    new docx.Paragraph({
                        text: "报告详情",
                        heading: docx.HeadingLevel.HEADING_1,
                        alignment: docx.AlignmentType.CENTER
                    }),
                    new docx.Paragraph({
                        text: "报告基本情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("报告编号", data.report_no, "Report No.", data.report_no_en),
                    this.createInfoParagraph("报告日期", data.report_date, "Report Date", data.report_date_en),
                    this.createInfoParagraph("报告人", data.reporter, "Reporter", data.reporter_en),
                    this.createInfoParagraph("单位名称", data.customer_name, "Customer name", data.customer_name_en),
                    this.createInfoParagraph("联系地址", data.address, "Address", data.address_en),
                    this.createInfoParagraph("联系人", data.contact_person, "Contact Person", data.contact_person_en),
                    this.createInfoParagraph("联系电话", data.tel, "Telphone No.", data.tel_en),
                    this.createInfoParagraph("发生地", data.occurrence_place, "Occurrence place", data.occurrence_place_en),
                    new docx.Paragraph({
                        text: "医疗器械情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("产品名称", data.product_name, "Product Name", data.product_name_en),
                    this.createInfoParagraph("注册证编号", data.registration_no, "Registration no.", data.registration_no_en),
                    this.createInfoParagraph("型号", data.module, "Module", data.module_en),
                    this.createInfoParagraph("规格", data.product_package, "Package", data.product_package_en),
                    this.createInfoParagraph("产地", data.origin_country, "Origin of Country", data.origin_country_en),
                    this.createInfoParagraph("管理类别", data.class_type, "Class Type", data.class_type_en),
                    this.createInfoParagraph("产品类别", data.product_type, "Product type", data.product_type_en),
                    this.createInfoParagraph("产品批号", data.product_lot, "Product Lot", data.product_lot_en),
                    this.createInfoParagraph("产品编号", data.product_no, "Product No.", data.product_no_en),
                    this.createInfoParagraph("UDI", data.udi, "UDI", data.udi_en),
                    this.createInfoParagraph("生产日期", data.manufacturing_date, "Manufacturing Date", data.manufacturing_date_en),
                    this.createInfoParagraph("有效期至", data.expiration_date, "Expiration Date", data.expiration_date_en),
                    new docx.Paragraph({
                        text: "不良事件情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("事件发生日期", data.event_occurrence_date, "Event Occurrence Date", data.event_occurrence_date_en),
                    this.createInfoParagraph("发现或获知日期", data.knowledge_date, "Knowledge Date", data.knowledge_date_en),
                    this.createInfoParagraph("伤害程度", data.injury_type, "Injury Type", data.injury_type_en),
                    this.createInfoParagraph("伤害表现", data.injury, "Injury", data.injury_en),
                    this.createInfoParagraph("器械故障表现", data.device_malfunction_desc, "Device Malfunction Description", data.device_malfunction_desc_en),
                    new docx.Paragraph({
                        text: "患者信息",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("姓名", data.patient_name, "Patient Name", data.patient_name_en),
                    this.createInfoParagraph("出生日期", data.birth_date, "Date of Birth", data.birth_date_en),
                    this.createInfoParagraph("年龄", data.age, "Age", data.age_en),
                    this.createInfoParagraph("性别", data.gender, "Gender", data.gender_en),
                    this.createInfoParagraph("病历号", data.medical_record_no, "Medical Record No.", data.medical_record_no_en),
                    this.createInfoParagraph("既往病史", data.medical_history, "Medical History", data.medical_history_en),
                    new docx.Paragraph({
                        text: "使用情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("预期治疗疾病或作用", data.disease_intended, "Disease intended to treat or effect", data.disease_intended_en),
                    this.createInfoParagraph("器械使用日期", data.usage_date, "Usage Date", data.usage_date_en),
                    this.createInfoParagraph("使用场所", data.usage_site, "Usage site", data.usage_site_en),
                    this.createInfoParagraph("场所名称", data.institution_name, "Institution Name", data.institution_name_en),
                    this.createInfoParagraph("使用过程", data.usage_process, "Usage Process", data.usage_process_en),
                    this.createInfoParagraph("合并用药/械情况说明", data.drug_device_comb_desc, "Drug/device Combination Description", data.drug_device_comb_desc_en),
                    new docx.Paragraph({
                        text: "事件调查",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("是否开展了调查", data.investigation_flag, "If carry out investigation", data.investigation_flag_en),
                    this.createInfoParagraph("调查情况", data.investigation_desc, "Investigation description", data.investigation_desc_en),
                    new docx.Paragraph({
                        text: "评价结果",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("关联性评价", data.relative_evaluation, "Relative Evaluation", data.relative_evaluation_en),
                    this.createInfoParagraph("事件原因分析", data.event_reason_analysis, "Event Reason Analysis", data.event_reason_analysis_en),
                    this.createInfoParagraph("是否需要开展产品风险评价", data.need_risk_assessment, "If need initiate Product Risk Assessment", data.need_risk_assessment_en),
                    this.createInfoParagraph("计划提交时间", data.plan_submit_date, "Plan submission Date", data.plan_submit_date_en),
                    new docx.Paragraph({
                        text: "控制措施",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("是否已采取控制措施", data.has_control_measure, "If has taken control measure", data.has_control_measure_en),
                    this.createInfoParagraph("具体控制措施", data.control_measure_details, "Control measure details", data.control_measure_details_en),
                    this.createInfoParagraph("未采取控制措施原因", data.no_control_measure_reason, "No control measure reason", data.no_control_measure_reason_en)
                ]
            }]
        });

        // 生成文档
        docx.Packer.toBlob(doc).then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        });
    },

    // 创建信息段落
    createInfoParagraph: function(labelZh, valueZh, labelEn, valueEn) {
        return new docx.Paragraph({
            children: [
                new docx.TextRun({
                    text: `${labelZh}：${valueZh}`,
                    bold: true
                }),
                new docx.TextRun({
                    text: `\t${labelEn}：${valueEn}`
                })
            ]
        });
    }
};

// 获取当前页面路径
const currentPath = window.location.pathname;
const isAddPage = currentPath.includes('overseas-report-add.html');
const isDetailPage = currentPath.includes('overseas-report-detail.html');

// 等待文档加载完成
$(document).ready(function() {
    console.log('页面加载完成，初始化表单处理...');
    
    // 获取当前页面类型
    const isAddPage = window.location.pathname.includes('overseas-report-add.html');
    const isDetailPage = window.location.pathname.includes('overseas-report-detail.html');
    
    console.log('当前页面类型:', isAddPage ? '新增页面' : '详情页面');
    
    // 获取URL中的报告ID
    const urlParams = new URLSearchParams(window.location.search);
    const reportId = urlParams.get('id');

    if (reportId) {
        // 加载报告详情
        loadReportDetail(reportId);
    }

    // // 设置默认值
    // $('input[name="occurrence_place"]').val('境外');
    // $('input[name="occurrence_place_en"]').val('Out of China');
    // $('input[name="company_name"]').val('西门子医学诊断产品（上海）有限公司');
    // $('input[name="company_name_en"]').val('SHD');
    // $('input[name="address"]').val('中国（上海）自由贸易试验区英伦路38号四层410,411,412室');
    // $('input[name="address_en"]').val('Room 410,411,412,4 / F, No.38 Yinglun Road, China (Shanghai) Pilot Free Trade Zone');

    // 添加报告人选择联动
    $('select[name="reporter"]').on('change', function() {
        const selectedValue = $(this).val();
        let reporterEn = '';
        let tel = '';
        let contactPerson = '';
        let contactPersonEn = '';
        let companyName = '';
        let companyNameEn = '';
        let address = '';
        let addressEn = '';
        let occurrencePlace = '';
        let occurrencePlaceEn = '';
        
        switch(selectedValue) {
            case '卢梦佳':
                reporterEn = 'Lu Mengjia';
                tel = '13718044549';
                contactPerson = '卢梦佳';
                contactPersonEn = 'Lu Mengjia';
                companyName = '西门子医学诊断产品（上海）有限公司';
                companyNameEn = 'SHD';
                address = '中国（上海）自由贸易试验区英伦路38号四层410,411,412室';
                addressEn = 'Room 410,411,412,4 / F, No.38 Yinglun Road, China (Shanghai) Pilot Free Trade Zone';
                occurrencePlace = '境外';
                occurrencePlaceEn = 'Out of China';
                break;
            case '周辉':
                reporterEn = 'Zhou Hui';
                tel = '13911135196';
                contactPerson = '周辉';
                contactPersonEn = 'Zhou Hui';
                companyName = '西门子医学诊断产品（上海）有限公司';
                companyNameEn = 'SHD';
                address = '中国（上海）自由贸易试验区英伦路38号四层410,411,412室';
                addressEn = 'Room 410,411,412,4 / F, No.38 Yinglun Road, China (Shanghai) Pilot Free Trade Zone';
                occurrencePlace = '境外';
                occurrencePlaceEn = 'Out of China';
                break;
            case '新增':
                reporterEn = 'New';
                tel = '';
                contactPerson = '';
                contactPersonEn = '';
                companyName = '';
                companyNameEn = '';
                address = '';
                addressEn = '';
                occurrencePlace = '';
                occurrencePlaceEn = '';
                break;
            default:
                reporterEn = selectedValue;
                tel = '';
                contactPerson = selectedValue;
                contactPersonEn = reporterEn;
                companyName = '';
                companyNameEn = '';
                address = '';
                addressEn = '';
                occurrencePlace = '';
                occurrencePlaceEn = '';
        }
        
        // 更新所有相关字段
        $('input[name="reporter_en"]').val(reporterEn);
        $('input[name="tel"]').val(tel);
        $('input[name="contact_person"]').val(contactPerson);
        $('input[name="contact_person_en"]').val(contactPersonEn);
        $('input[name="customer_name"]').val(companyName);
        $('input[name="customer_name_en"]').val(companyNameEn);
        $('input[name="address"]').val(address);
        $('input[name="address_en"]').val(addressEn);
        $('input[name="occurrence_place"]').val(occurrencePlace);
        $('input[name="occurrence_place_en"]').val(occurrencePlaceEn);
        
    });

    // 表单提交处理
    $('#reportForm').on('submit', function(e) {
        e.preventDefault();
        console.log('表单提交事件触发');
        
        // 获取表单数据
        const formData = new FormData(this);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
            console.log(`表单字段 ${key}: ${value}`);
        });
        
        // 检查report_no字段
        const reportNoInput = $('input[name="report_no"]');
        console.log('report_no input元素:', reportNoInput.length > 0 ? '存在' : '不存在');
        console.log('report_no当前值:', reportNoInput.val());
        
        // 如果表单中没有report_no，尝试从URL参数获取
        if (!data.report_no) {
            const urlParams = new URLSearchParams(window.location.search);
            const reportId = urlParams.get('id');
            if (reportId) {
                // 从已加载的数据中获取report_no
                const currentReportNo = $('input[name="report_no"]').val();
                if (currentReportNo) {
                    data.report_no = currentReportNo;
                    console.log('从当前表单获取report_no:', currentReportNo);
                }
            }
        }
        
        // 验证必填字段
        if (!data.report_no) {
            showToast('报告编号不能为空', 'error');
            return;
        }
        
        // 设置 report_no_en 与 report_no 相同
        data.report_no_en = data.report_no;
        
        console.log('准备发送的数据:', data);
        console.log('请求URL:', `/overseas/api/report/modify/${reportId}`);
        
        // 发送更新请求
        $.ajax({
            url: `/overseas/api/report/modify/${reportId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function(response) {
                console.log('服务器响应:', response);
                if (response.success) {
                    showToast('保存成功', 'success', '报告已保存，即将跳转到列表页面...');
                    setTimeout(() => {
                        window.location.href = '/pages/overseas-table.html';
                    }, 1500);
                } else {
                    showToast('保存失败', 'error', response.message || '未知错误');
                }
            },
            error: function(xhr, status, error) {
                console.error('请求失败:', {xhr, status, error});
                let errorMsg = '更新失败';
                try {
                    const response = JSON.parse(xhr.responseText);
                    errorMsg = response.message || errorMsg;
                } catch (e) {
                    console.error('解析错误响应失败:', e);
                }
                showToast('保存失败', 'error', errorMsg);
            }
        });
    });
    
    // 导出功能
    $('#exportExcelBtn').on('click', function() {
        console.log('点击导出Excel按钮');
        ExportService.exportToExcel();
    });
    
    window.exportToWord = function() {
        console.log('开始导出Word...');
        const formData = new FormData(document.getElementById('reportForm'));
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
            console.log(`获取到字段 ${key}: ${value}`);
        });
        console.log('导出数据:', data);

        // 获取报告编号和PM编号
        const reportNo = data.report_no || '未知报告编号';
        const pmNo = data.pm_no || '未知PM编号';
        // 获取当前日期
        const currentDate = new Date().toISOString().split('T')[0].replace(/-/g, '-');
        
        // 构建文件名
        const fileName = `${reportNo}_${pmNo}_${currentDate}.docx`;

        // 创建文档
        const doc = new docx.Document({
            sections: [{
                properties: {},
                children: [
                    new docx.Paragraph({
                        text: "报告详情",
                        heading: docx.HeadingLevel.HEADING_1,
                        alignment: docx.AlignmentType.CENTER
                    }),
                    new docx.Paragraph({
                        text: "报告基本情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("报告编号", data.report_no, "Report No.", data.report_no_en),
                    this.createInfoParagraph("报告日期", data.report_date, "Report Date", data.report_date_en),
                    this.createInfoParagraph("报告人", data.reporter, "Reporter", data.reporter_en),
                    this.createInfoParagraph("单位名称", data.customer_name, "Customer name", data.customer_name_en),
                    this.createInfoParagraph("联系地址", data.address, "Address", data.address_en),
                    this.createInfoParagraph("联系人", data.contact_person, "Contact Person", data.contact_person_en),
                    this.createInfoParagraph("联系电话", data.tel, "Telphone No.", data.tel_en),
                    this.createInfoParagraph("发生地", data.occurrence_place, "Occurrence place", data.occurrence_place_en),
                    new docx.Paragraph({
                        text: "医疗器械情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("产品名称", data.product_name, "Product Name", data.product_name_en),
                    this.createInfoParagraph("注册证编号", data.registration_no, "Registration no.", data.registration_no_en),
                    this.createInfoParagraph("型号", data.module, "Module", data.module_en),
                    this.createInfoParagraph("规格", data.product_package, "Package", data.product_package_en),
                    this.createInfoParagraph("产地", data.origin_country, "Origin of Country", data.origin_country_en),
                    this.createInfoParagraph("管理类别", data.class_type, "Class Type", data.class_type_en),
                    this.createInfoParagraph("产品类别", data.product_type, "Product type", data.product_type_en),
                    this.createInfoParagraph("产品批号", data.product_lot, "Product Lot", data.product_lot_en),
                    this.createInfoParagraph("产品编号", data.product_no, "Product No.", data.product_no_en),
                    this.createInfoParagraph("UDI", data.udi, "UDI", data.udi_en),
                    this.createInfoParagraph("生产日期", data.manufacturing_date, "Manufacturing Date", data.manufacturing_date_en),
                    this.createInfoParagraph("有效期至", data.expiration_date, "Expiration Date", data.expiration_date_en),
                    new docx.Paragraph({
                        text: "不良事件情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("事件发生日期", data.event_occurrence_date, "Event Occurrence Date", data.event_occurrence_date_en),
                    this.createInfoParagraph("发现或获知日期", data.knowledge_date, "Knowledge Date", data.knowledge_date_en),
                    this.createInfoParagraph("伤害程度", data.injury_type, "Injury Type", data.injury_type_en),
                    this.createInfoParagraph("伤害表现", data.injury, "Injury", data.injury_en),
                    this.createInfoParagraph("器械故障表现", data.device_malfunction_desc, "Device Malfunction Description", data.device_malfunction_desc_en),
                    new docx.Paragraph({
                        text: "患者信息",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("姓名", data.patient_name, "Patient Name", data.patient_name_en),
                    this.createInfoParagraph("出生日期", data.birth_date, "Date of Birth", data.birth_date_en),
                    this.createInfoParagraph("年龄", data.age, "Age", data.age_en),
                    this.createInfoParagraph("性别", data.gender, "Gender", data.gender_en),
                    this.createInfoParagraph("病历号", data.medical_record_no, "Medical Record No.", data.medical_record_no_en),
                    this.createInfoParagraph("既往病史", data.medical_history, "Medical History", data.medical_history_en),
                    new docx.Paragraph({
                        text: "使用情况",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("预期治疗疾病或作用", data.disease_intended, "Disease intended to treat or effect", data.disease_intended_en),
                    this.createInfoParagraph("器械使用日期", data.usage_date, "Usage Date", data.usage_date_en),
                    this.createInfoParagraph("使用场所", data.usage_site, "Usage site", data.usage_site_en),
                    this.createInfoParagraph("场所名称", data.institution_name, "Institution Name", data.institution_name_en),
                    this.createInfoParagraph("使用过程", data.usage_process, "Usage Process", data.usage_process_en),
                    this.createInfoParagraph("合并用药/械情况说明", data.drug_device_comb_desc, "Drug/device Combination Description", data.drug_device_comb_desc_en),
                    new docx.Paragraph({
                        text: "事件调查",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("是否开展了调查", data.investigation_flag, "If carry out investigation", data.investigation_flag_en),
                    this.createInfoParagraph("调查情况", data.investigation_desc, "Investigation description", data.investigation_desc_en),
                    new docx.Paragraph({
                        text: "评价结果",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("关联性评价", data.relative_evaluation, "Relative Evaluation", data.relative_evaluation_en),
                    this.createInfoParagraph("事件原因分析", data.event_reason_analysis, "Event Reason Analysis", data.event_reason_analysis_en),
                    this.createInfoParagraph("是否需要开展产品风险评价", data.need_risk_assessment, "If need initiate Product Risk Assessment", data.need_risk_assessment_en),
                    this.createInfoParagraph("计划提交时间", data.plan_submit_date, "Plan submission Date", data.plan_submit_date_en),
                    new docx.Paragraph({
                        text: "控制措施",
                        heading: docx.HeadingLevel.HEADING_2
                    }),
                    this.createInfoParagraph("是否已采取控制措施", data.has_control_measure, "If has taken control measure", data.has_control_measure_en),
                    this.createInfoParagraph("具体控制措施", data.control_measure_details, "Control measure details", data.control_measure_details_en),
                    this.createInfoParagraph("未采取控制措施原因", data.no_control_measure_reason, "No control measure reason", data.no_control_measure_reason_en)
                ]
            }]
        });

        // 生成文档
        docx.Packer.toBlob(doc).then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        });
    };
});

// 获取报告详情
function loadReportDetail(id) {
    $.ajax({
        url: '/overseas/api/report/info/' + id,
        method: 'GET',
        success: function(response) {
            console.log('获取到的原始响应:', response);
            
            const report = response;
            console.log('获取到的报告数据:', report);
            
            // 基本信息
            $('input[name="id"]').val(report.id || '');
            $('input[name="report_no"]').val(report.reportNo || report.reportNoEn || '');
            $('input[name="PM_no"]').val(report.pmno || report.PMNo || '');
            $('input[name="report_date"]').val(report.reportDate ? report.reportDate.split('T')[0] : '');
            $('select[name="reporter"]').val(report.reporter || '');
            $('input[name="customer_name"]').val(report.customerName || '');
            $('input[name="address"]').val(report.address || '');
            $('input[name="contact_person"]').val(report.contactPerson || '');
            $('input[name="tel"]').val(report.tel || '');
            $('input[name="occurrence_place"]').val(report.occurrencePlace || '');
            
            // 医疗器械情况
            $('input[name="product_name"]').val(report.productName || '');
            $('input[name="registration_no"]').val(report.registrationNo || '');
            $('input[name="module"]').val(report.module || '');
            $('input[name="product_package"]').val(report.productPackage || '');
            $('input[name="origin_country"]').val(report.originCountry || '');
            $('input[name="class_type"]').val(report.classType || '');
            $('input[name="product_type"]').val(report.productType || '');
            $('input[name="product_lot"]').val(report.productLot || '');
            $('input[name="product_no"]').val(report.productNo || '');
            $('input[name="udi"]').val(report.udi || '');
            $('input[name="manufacturing_date"]').val(report.manufacturingDate ? report.manufacturingDate.split('T')[0] : '');
            $('input[name="expiration_date"]').val(report.expirationDate ? report.expirationDate.split('T')[0] : '');
            
            // 不良事件情况
            $('input[name="event_occurrence_date"]').val(report.eventOccurrenceDate ? report.eventOccurrenceDate.split('T')[0] : '');
            $('input[name="knowledge_date"]').val(report.knowledgeDate ? report.knowledgeDate.split('T')[0] : '');
            $('input[name="injury_type"]').val(report.injuryType || '');
            $('input[name="injury"]').val(report.injury || '');
            $('textarea[name="device_malfunction_desc"]').val(report.deviceMalfunctionDesc || '');
            
            // 患者信息
            $('input[name="patient_name"]').val(report.patientName || '');
            $('input[name="birth_date"]').val(report.birthDate ? report.birthDate.split('T')[0] : '');
            $('input[name="age"]').val(report.age || '');
            $('input[name="gender"]').val(report.gender || '');
            $('input[name="medical_record_no"]').val(report.medicalRecordNo || '');
            $('textarea[name="medical_history"]').val(report.medicalHistory || '');
            
            // 使用情况
            $('textarea[name="disease_intended"]').val(report.diseaseIntended || '');
            $('input[name="usage_date"]').val(report.usageDate ? report.usageDate.split('T')[0] : '');
            $('input[name="usage_site"]').val(report.usageSite || '');
            $('input[name="institution_name"]').val(report.institutionName || '');
            $('textarea[name="usage_process"]').val(report.usageProcess || '');
            $('textarea[name="drug_device_comb_desc"]').val(report.drugDeviceCombDesc || '');
            
            // 事件调查
            $('input[name="investigation_flag"]').val(report.investigationFlag || '');
            $('textarea[name="investigation_desc"]').val(report.investigationDesc || '');
            
            // 评价结果
            $('input[name="relative_evaluation"]').val(report.relativeEvaluation || '');
            $('textarea[name="event_reason_analysis"]').val(report.eventReasonAnalysis || '');
            $('input[name="need_risk_assessment"]').val(report.needRiskAssessment || '');
            $('input[name="plan_submit_date"]').val(report.planSubmitDate ? report.planSubmitDate.split('T')[0] : '');
            
            // 控制措施
            $('input[name="has_control_measure"]').val(report.hasControlMeasure || '');
            $('textarea[name="control_measure_details"]').val(report.controlMeasureDetails || '');
            $('textarea[name="no_control_measure_reason"]').val(report.noControlMeasureReason || '');
            
            // 英文字段
            $('input[name="report_no_en"]').val(report.reportNoEn || '');
            $('input[name="report_date_en"]').val(report.reportDateEn ? report.reportDateEn.split('T')[0] : '');
            $('input[name="reporter_en"]').val(report.reporterEn || '');
            $('input[name="customer_name_en"]').val(report.customerNameEn || '');
            $('input[name="address_en"]').val(report.addressEn || '');
            $('input[name="contact_person_en"]').val(report.contactPersonEn || '');
            $('input[name="tel_en"]').val(report.telEn || '');
            $('input[name="occurrence_place_en"]').val(report.occurrencePlaceEn || '');
            
            // 医疗器械情况（英文）
            $('input[name="product_name_en"]').val(report.productNameEn || '');
            $('input[name="registration_no_en"]').val(report.registrationNoEn || '');
            $('input[name="module_en"]').val(report.moduleEn || '');
            $('input[name="product_package_en"]').val(report.productPackageEn || '');
            $('input[name="origin_country_en"]').val(report.originCountryEn || '');
            $('input[name="class_type_en"]').val(report.classTypeEn || '');
            $('input[name="product_type_en"]').val(report.productTypeEn || '');
            $('input[name="product_lot_en"]').val(report.productLotEn || '');
            $('input[name="product_no_en"]').val(report.productNoEn || '');
            $('input[name="udi_en"]').val(report.udiEn || '');
            $('input[name="manufacturing_date_en"]').val(report.manufacturingDateEn ? report.manufacturingDateEn.split('T')[0] : '');
            $('input[name="expiration_date_en"]').val(report.expirationDateEn ? report.expirationDateEn.split('T')[0] : '');
            
            // 不良事件情况（英文）
            $('input[name="event_occurrence_date_en"]').val(report.eventOccurrenceDateEn ? report.eventOccurrenceDateEn.split('T')[0] : '');
            $('input[name="knowledge_date_en"]').val(report.knowledgeDateEn ? report.knowledgeDateEn.split('T')[0] : '');
            $('input[name="injury_type_en"]').val(report.injuryTypeEn || '');
            $('input[name="injury_en"]').val(report.injuryEn || '');
            $('textarea[name="device_malfunction_desc_en"]').val(report.deviceMalfunctionDescEn || '');
            
            // 患者信息（英文）
            $('input[name="patient_name_en"]').val(report.patientNameEn || '');
            $('input[name="birth_date_en"]').val(report.birthDateEn ? report.birthDateEn.split('T')[0] : '');
            $('input[name="age_en"]').val(report.ageEn || '');
            $('input[name="gender_en"]').val(report.genderEn || '');
            $('input[name="medical_record_no_en"]').val(report.medicalRecordNoEn || '');
            $('textarea[name="medical_history_en"]').val(report.medicalHistoryEn || '');
            
            // 使用情况（英文）
            $('textarea[name="disease_intended_en"]').val(report.diseaseIntendedEn || '');
            $('input[name="usage_date_en"]').val(report.usageDateEn ? report.usageDateEn.split('T')[0] : '');
            $('input[name="usage_site_en"]').val(report.usageSiteEn || '');
            $('input[name="institution_name_en"]').val(report.institutionNameEn || '');
            $('textarea[name="usage_process_en"]').val(report.usageProcessEn || '');
            $('textarea[name="drug_device_comb_desc_en"]').val(report.drugDeviceCombDescEn || '');
            
            // 事件调查（英文）
            $('input[name="investigation_flag_en"]').val(report.investigationFlagEn || '');
            $('textarea[name="investigation_desc_en"]').val(report.investigationDescEn || '');
            
            // 评价结果（英文）
            $('input[name="relative_evaluation_en"]').val(report.relativeEvaluationEn || '');
            $('textarea[name="event_reason_analysis_en"]').val(report.eventReasonAnalysisEn || '');
            $('input[name="need_risk_assessment_en"]').val(report.needRiskAssessmentEn || '');
            $('input[name="plan_submit_date_en"]').val(report.planSubmitDateEn ? report.planSubmitDateEn.split('T')[0] : '');
            
            // 控制措施（英文）
            $('input[name="has_control_measure_en"]').val(report.hasControlMeasureEn || '');
            $('textarea[name="control_measure_details_en"]').val(report.controlMeasureDetailsEn || '');
            $('textarea[name="no_control_measure_reason_en"]').val(report.noControlMeasureReasonEn || '');
            
            console.log('数据加载成功，表单已更新');
            showToast('数据加载成功', 'success');
        },
        error: function(xhr, status, error) {
            console.error('API调用失败:', {xhr, status, error});
            showToast('获取报告详情失败：' + (xhr.responseJSON?.error || '未知错误'), 'error');
        }
    });
}

// 预览报告
function previewReport(path) {
    $('#reportPreviewFrame').attr('src', '/overseas/api/report/view?path=' + encodeURIComponent(path));
    $('#reportPreviewModal').modal('show');
}

// 导出报告
function exportReport(path) {
    window.location.href = '/overseas/api/report/export?path=' + encodeURIComponent(path);
}

// Toast提示
function showToast(message, type = 'success', details = '') {
    // 创建toast元素
    const toast = document.createElement('div');
    toast.className = `toast-message ${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="bi ${type === 'success' ? 'bi-check-circle-fill' : type === 'error' ? 'bi-exclamation-circle-fill' : 'bi-info-circle-fill'}"></i>
            <div class="toast-text">
                <div class="toast-main-message">${message}</div>
                ${details ? `<div class="toast-details">${details}</div>` : ''}
            </div>
        </div>
    `;
    
    // 添加到页面
    document.body.appendChild(toast);
    
    // 添加显示动画
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // 5秒后移除
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

function showModal(message) {
    document.getElementById('messageModalBody').textContent = message;
    const modal = new bootstrap.Modal(document.getElementById('messageModal'));
    modal.show();
}

// 自动填写产品信息
function autoFillProductInfo() {
    const productNo = $('input[name="product_no"]').val();
    if (!productNo) {
        showToast('请输入产品编号', 'error');
        return;
    }

    $.ajax({
        url: '/overseas/api/report/auto-fill-product',
        method: 'GET',
        data: { productNo: productNo },
        success: function(response) {
            if (response.success) {
                // 填充产品信息
                $('input[name="product_name"]').val(response.data.product_name || '');
                $('input[name="product_name_en"]').val(response.data.product_name_en || '');
                $('input[name="registration_no"]').val(response.data.registration_no || '');
                $('input[name="registration_no_en"]').val(response.data.registration_no_en || '');
                $('input[name="origin_country"]').val('进口');
                $('input[name="origin_country_en"]').val('import');
                $('input[name="class_type"]').val(response.data.class_type || '');
                $('input[name="class_type_en"]').val(response.data.class_type_en || '');
                $('input[name="product_type"]').val(response.data.product_type || '');
                $('input[name="product_type_en"]').val(response.data.product_type_en || '');
                $('input[name="udi"]').val(response.data.udi || '');
                $('input[name="udi_en"]').val(response.data.udi_en || '');
                
                showToast('产品信息已自动填写', 'success');
            } else {
                showToast(response.message || '获取产品信息失败', 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('获取产品信息失败:', error);
            showToast('获取产品信息失败：' + (xhr.responseJSON?.message || '未知错误'), 'error');
        }
    });
} 