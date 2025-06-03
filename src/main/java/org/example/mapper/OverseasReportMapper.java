package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.OverseasReport;
import java.util.List;

@Mapper
public interface OverseasReportMapper {
    
    @Insert("INSERT INTO overseas_reports (" +
            "report_no, report_no_en, report_date, report_date_en, reporter, reporter_en, " +
            "customer_name, customer_name_en, address, address_en, contact_person, contact_person_en, " +
            "tel, tel_en, occurrence_place, occurrence_place_en, " +
            "product_name, product_name_en, registration_no, registration_no_en, module, module_en, " +
            "package_, package_en, origin_country, origin_country_en, class_type, class_type_en, " +
            "product_type, product_type_en, product_lot, product_lot_en, product_no, product_no_en, " +
            "udi, udi_en, manufacturing_date, manufacturing_date_en, expiration_date, expiration_date_en, " +
            "event_occurrence_date, event_occurrence_date_en, knowledge_date, knowledge_date_en, " +
            "injury_type, injury_type_en, injury, injury_en, device_malfunction_desc, device_malfunction_desc_en, " +
            "patient_name, patient_name_en, birth_date, birth_date_en, age, age_en, gender, gender_en, " +
            "medical_record_no, medical_record_no_en, medical_history, medical_history_en, " +
            "disease_intended, disease_intended_en, usage_date, usage_date_en, usage_site, usage_site_en, " +
            "institution_name, institution_name_en, usage_process, usage_process_en, " +
            "drug_device_comb_desc, drug_device_comb_desc_en, " +
            "investigation_flag, investigation_flag_en, investigation_desc, investigation_desc_en, " +
            "relative_evaluation, relative_evaluation_en, event_reason_analysis, event_reason_analysis_en, " +
            "need_risk_assessment, need_risk_assessment_en, plan_submit_date, plan_submit_date_en, " +
            "has_control_measure, has_control_measure_en, control_measure_details, control_measure_details_en, " +
            "no_control_measure_reason, no_control_measure_reason_en" +
            ") VALUES (" +
            "#{reportNo}, #{reportNoEn}, #{reportDate}, #{reportDateEn}, #{reporter}, #{reporterEn}, " +
            "#{customerName}, #{customerNameEn}, #{address}, #{addressEn}, #{contactPerson}, #{contactPersonEn}, " +
            "#{tel}, #{telEn}, #{occurrencePlace}, #{occurrencePlaceEn}, " +
            "#{productName}, #{productNameEn}, #{registrationNo}, #{registrationNoEn}, #{module}, #{moduleEn}, " +
            "#{package_}, #{packageEn}, #{originCountry}, #{originCountryEn}, #{classType}, #{classTypeEn}, " +
            "#{productType}, #{productTypeEn}, #{productLot}, #{productLotEn}, #{productNo}, #{productNoEn}, " +
            "#{udi}, #{udiEn}, #{manufacturingDate}, #{manufacturingDateEn}, #{expirationDate}, #{expirationDateEn}, " +
            "#{eventOccurrenceDate}, #{eventOccurrenceDateEn}, #{knowledgeDate}, #{knowledgeDateEn}, " +
            "#{injuryType}, #{injuryTypeEn}, #{injury}, #{injuryEn}, #{deviceMalfunctionDesc}, #{deviceMalfunctionDescEn}, " +
            "#{patientName}, #{patientNameEn}, #{birthDate}, #{birthDateEn}, #{age}, #{ageEn}, #{gender}, #{genderEn}, " +
            "#{medicalRecordNo}, #{medicalRecordNoEn}, #{medicalHistory}, #{medicalHistoryEn}, " +
            "#{diseaseIntended}, #{diseaseIntendedEn}, #{usageDate}, #{usageDateEn}, #{usageSite}, #{usageSiteEn}, " +
            "#{institutionName}, #{institutionNameEn}, #{usageProcess}, #{usageProcessEn}, " +
            "#{drugDeviceCombDesc}, #{drugDeviceCombDescEn}, " +
            "#{investigationFlag}, #{investigationFlagEn}, #{investigationDesc}, #{investigationDescEn}, " +
            "#{relativeEvaluation}, #{relativeEvaluationEn}, #{eventReasonAnalysis}, #{eventReasonAnalysisEn}, " +
            "#{needRiskAssessment}, #{needRiskAssessmentEn}, #{planSubmitDate}, #{planSubmitDateEn}, " +
            "#{hasControlMeasure}, #{hasControlMeasureEn}, #{controlMeasureDetails}, #{controlMeasureDetailsEn}, " +
            "#{noControlMeasureReason}, #{noControlMeasureReasonEn}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OverseasReport report);

    @Select("SELECT * FROM overseas_reports WHERE report_number = #{reportNumber}")
    OverseasReport findByReportNumber(String reportNumber);

    @Select("SELECT * FROM overseas_reports")
    List<OverseasReport> findAll();

    @Delete("DELETE FROM overseas_reports WHERE report_number = #{reportNumber}")
    int deleteByReportNumber(String reportNumber);
} 