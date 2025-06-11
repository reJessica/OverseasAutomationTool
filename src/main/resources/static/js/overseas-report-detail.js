// 导出相关功能
const ExportService = {
    // 导出为Excel
    exportToExcel: function() {
        const formData = new FormData(document.getElementById('reportForm'));
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });

        // 创建工作簿
        const wb = XLSX.utils.book_new();
        
        // 准备数据
        const wsData = [
            ['报告基本情况'],
            ['报告编号', data.report_no, 'Report No.', data.report_no_en],
            ['报告日期', data.report_date, 'Report Date', data.report_date_en],
            ['报告人', data.reporter, 'Reporter', data.reporter_en],
            ['单位名称', data.customer_name, 'Customer name', data.customer_name_en],
            ['联系地址', data.address, 'Address', data.address_en],
            ['联系人', data.contact_person, 'Contact Person', data.contact_person_en],
            ['联系电话', data.tel, 'Telphone No.', data.tel_en],
            ['发生地', data.occurrence_place, 'Occurrence place', data.occurrence_place_en],
            [''],
            ['医疗器械情况'],
            ['产品名称', data.product_name, 'Product Name', data.product_name_en],
            ['注册证编号', data.registration_no, 'Registration no.', data.registration_no_en],
            ['型号', data.module, 'Module', data.module_en],
            ['规格', data.package, 'Package', data.package_en],
            ['产地', data.origin_country, 'Origin of Country', data.origin_country_en],
            ['管理类别', data.class_type, 'Class Type', data.class_type_en],
            ['产品类别', data.product_type, 'Product type', data.product_type_en],
            ['产品批号', data.product_lot, 'Product Lot', data.product_lot_en],
            ['产品编号', data.product_no, 'Product No.', data.product_no_en],
            ['UDI', data.udi, 'UDI', data.udi_en],
            ['生产日期', data.manufacturing_date, 'Manufacturing Date', data.manufacturing_date_en],
            ['有效期至', data.expiration_date, 'Expiration Date', data.expiration_date_en],
            [''],
            ['不良事件情况'],
            ['事件发生日期', data.event_occurrence_date, 'Event Occurrence Date', data.event_occurrence_date_en],
            ['发现或获知日期', data.knowledge_date, 'Knowledge Date', data.knowledge_date_en],
            ['伤害程度', data.injury_type, 'Injury Type', data.injury_type_en],
            ['伤害表现', data.injury, 'Injury', data.injury_en],
            ['器械故障表现', data.device_malfunction_desc, 'Device Malfunction Description', data.device_malfunction_desc_en],
            [''],
            ['患者信息'],
            ['姓名', data.patient_name, 'Patient Name', data.patient_name_en],
            ['出生日期', data.birth_date, 'Date of Birth', data.birth_date_en],
            ['年龄', data.age, 'Age', data.age_en],
            ['性别', data.gender, 'Gender', data.gender_en],
            ['病历号', data.medical_record_no, 'Medical Record No.', data.medical_record_no_en],
            ['既往病史', data.medical_history, 'Medical History', data.medical_history_en],
            [''],
            ['使用情况'],
            ['预期治疗疾病或作用', data.disease_intended, 'Disease intended to treat or effect', data.disease_intended_en],
            ['器械使用日期', data.usage_date, 'Usage Date', data.usage_date_en],
            ['使用场所', data.usage_site, 'Usage site', data.usage_site_en],
            ['场所名称', data.institution_name, 'Institution Name', data.institution_name_en],
            ['使用过程', data.usage_process, 'Usage Process', data.usage_process_en],
            ['合并用药/械情况说明', data.drug_device_comb_desc, 'Drug/device Combination Description', data.drug_device_comb_desc_en],
            [''],
            ['事件调查'],
            ['是否开展了调查', data.investigation_flag, 'If carry out investigation', data.investigation_flag_en],
            ['调查情况', data.investigation_desc, 'Investigation description', data.investigation_desc_en],
            [''],
            ['评价结果'],
            ['关联性评价', data.relative_evaluation, 'Relative Evaluation', data.relative_evaluation_en],
            ['事件原因分析', data.event_reason_analysis, 'Event Reason Analysis', data.event_reason_analysis_en],
            ['是否需要开展产品风险评价', data.need_risk_assessment, 'If need initiate Product Risk Assessment', data.need_risk_assessment_en],
            ['计划提交时间', data.plan_submit_date, 'Plan submission Date', data.plan_submit_date_en],
            [''],
            ['控制措施'],
            ['是否已采取控制措施', data.has_control_measure, 'If has taken control measure', data.has_control_measure_en],
            ['具体控制措施', data.control_measure_details, 'Control measure details', data.control_measure_details_en],
            ['未采取控制措施原因', data.no_control_measure_reason, 'No control measure reason', data.no_control_measure_reason_en]
        ];

        // 创建工作表
        const ws = XLSX.utils.aoa_to_sheet(wsData);
        
        // 添加工作表到工作簿
        XLSX.utils.book_append_sheet(wb, ws, "报告详情");
        
        // 导出文件
        XLSX.writeFile(wb, "报告详情.xlsx");
    },

    // 导出为Word
    exportToWord: function() {
        const formData = new FormData(document.getElementById('reportForm'));
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });

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
                    this.createInfoParagraph("规格", data.package, "Package", data.package_en),
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
            a.download = "报告详情.docx";
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

    // 表单提交处理
    $('#reportForm').on('submit', function(e) {
        e.preventDefault();
        console.log('表单提交事件触发');
        
        // 获取表单数据
        const formData = {};
        $(this).serializeArray().forEach(item => {
            formData[item.name] = item.value;
        });
        
        // 设置 report_no_en 与 report_no 相同
        formData.report_no_en = formData.report_no;
        
        // 发送更新请求
        $.ajax({
            url: `/overseas/report/update/${reportId}`,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                if (response.success) {
                    showToast('success', response.message || '更新成功');
                } else {
                    showToast('error', response.message || '更新失败');
                }
            },
            error: function(xhr, status, error) {
                let errorMsg = '更新失败';
                try {
                    const response = JSON.parse(xhr.responseText);
                    errorMsg = response.message || errorMsg;
                } catch (e) {
                    console.error('解析错误响应失败:', e);
                }
                showToast('error', errorMsg);
            }
        });
    });
    
    // 导出功能
    window.exportToExcel = function() {
        showToast('info', '导出Excel功能开发中...');
    };
    
    window.exportToWord = function() {
        showToast('info', '导出Word功能开发中...');
    };
});

// 获取报告详情
function loadReportDetail(id) {
    $.ajax({
        url: '/overseas/api/report/info/' + id,
        method: 'GET',
        success: function(response) {
            if (response.success) {
                const report = response.data;
                // 填充表单数据
                $('#reportId').val(report.id);
                $('#reportName').val(report.name);
                $('#reportType').val(report.type);
                $('#reportStatus').val(report.status);
                $('#reportContent').val(report.content);
                $('#reportPath').val(report.path);
                $('#reportCreateTime').val(report.createTime);
                $('#reportUpdateTime').val(report.updateTime);
            } else {
                showToast('获取报告详情失败：' + response.message);
            }
        },
        error: function(xhr) {
            showToast('获取报告详情失败：' + (xhr.responseJSON?.error || '未知错误'));
        }
    });
}

// 更新报告
function updateReport() {
    const formData = new FormData($('#reportForm')[0]);
    const id = $('#reportId').val();
    
    $.ajax({
        url: '/overseas/api/report/modify/' + id,
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                showToast('更新成功');
                window.location.href = '/overseas/table';
            } else {
                showToast('更新失败：' + response.message);
            }
        },
        error: function(xhr) {
            showToast('更新失败：' + (xhr.responseJSON?.error || '未知错误'));
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
function showToast(type, message) {
    const toast = $('<div>')
        .addClass('toast')
        .addClass(type === 'success' ? 'bg-success' : 'bg-danger')
        .addClass('text-white')
        .css({
            'position': 'fixed',
            'top': '20px',
            'right': '20px',
            'padding': '10px 20px',
            'border-radius': '4px',
            'z-index': '9999'
        })
        .text(message);
        
    $('body').append(toast);
    
    setTimeout(() => {
        toast.fadeOut(() => toast.remove());
    }, 3000);
}

function showModal(message) {
    document.getElementById('messageModalBody').textContent = message;
    const modal = new bootstrap.Modal(document.getElementById('messageModal'));
    modal.show();
} 