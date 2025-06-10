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
            ['既往病史', data.medical_history, 'Medical History', data.medical_history_en]
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
                    this.createInfoParagraph("既往病史", data.medical_history, "Medical History", data.medical_history_en)
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

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 查看报告
    const reportPreviewModal = new bootstrap.Modal(document.getElementById('reportPreviewModal'));
    const reportPreviewFrame = document.getElementById('reportPreviewFrame');

    document.querySelectorAll('.view-report').forEach(button => {
        button.addEventListener('click', function() {
            const reportPath = this.dataset.path;
            reportPreviewFrame.src = `/overseas/report/preview?path=${encodeURIComponent(reportPath)}`;
            reportPreviewModal.show();
        });
    });
});

// Toast提示
function showToast(message) {
    const toast = document.getElementById('toast');
    const toastBody = toast.querySelector('.toast-body');
    toastBody.textContent = message;
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
} 