package org.example.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OverseasReport {
    private Long id;
    
    // 基本信息
    private String reportNumber;
    private String reportPath;
    private String status;
    private String reportNo;
    private String reportNoEn;
    private LocalDate reportDate;
    private LocalDate reportDateEn;
    private String reporter;
    private String reporterEn;
    private String customerName;
    private String customerNameEn;
    private String address;
    private String addressEn;
    private String contactPerson;
    private String contactPersonEn;
    private String tel;
    private String telEn;
    private String occurrencePlace;
    private String occurrencePlaceEn;

    // 医疗器械情况
    private String productName;
    private String productNameEn;
    private String registrationNo;
    private String registrationNoEn;
    private String module;
    private String moduleEn;
    private String productPackage;
    private String packageEn;
    private String originCountry;
    private String originCountryEn;
    private String classType;
    private String classTypeEn;
    private String productType;
    private String productTypeEn;
    private String productLot;
    private String productLotEn;
    private String productNo;
    private String productNoEn;
    private String udi;
    private String udiEn;
    private LocalDate manufacturingDate;
    private LocalDate manufacturingDateEn;
    private LocalDate expirationDate;
    private LocalDate expirationDateEn;

    // 不良事件情况
    private LocalDate eventOccurrenceDate;
    private LocalDate eventOccurrenceDateEn;
    private LocalDate knowledgeDate;
    private LocalDate knowledgeDateEn;
    private String injuryType;
    private String injuryTypeEn;
    private String injury;
    private String injuryEn;
    private String deviceMalfunctionDesc;
    private String deviceMalfunctionDescEn;
    private String patientName;
    private String patientNameEn;
    private LocalDate birthDate;
    private LocalDate birthDateEn;
    private String age;
    private String ageEn;
    private String gender;
    private String genderEn;
    private String medicalRecordNo;
    private String medicalRecordNoEn;
    private String medicalHistory;
    private String medicalHistoryEn;

    // 使用情况
    private String diseaseIntended;
    private String diseaseIntendedEn;
    private LocalDate usageDate;
    private LocalDate usageDateEn;
    private String usageSite;
    private String usageSiteEn;
    private String institutionName;
    private String institutionNameEn;
    private String usageProcess;
    private String usageProcessEn;
    private String drugDeviceCombDesc;
    private String drugDeviceCombDescEn;

    // 事件调查
    private String investigationFlag;
    private String investigationFlagEn;
    private String investigationDesc;
    private String investigationDescEn;

    // 评价结果
    private String relativeEvaluation;
    private String relativeEvaluationEn;
    private String eventReasonAnalysis;
    private String eventReasonAnalysisEn;
    private String needRiskAssessment;
    private String needRiskAssessmentEn;
    private LocalDate planSubmitDate;
    private LocalDate planSubmitDateEn;

    // 控制措施
    private String hasControlMeasure;
    private String hasControlMeasureEn;
    private String controlMeasureDetails;
    private String controlMeasureDetailsEn;
    private String noControlMeasureReason;
    private String noControlMeasureReasonEn;

    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 