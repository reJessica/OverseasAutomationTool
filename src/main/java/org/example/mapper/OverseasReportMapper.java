package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.OverseasReport;

import java.util.List;
import java.util.Map;

@Mapper
public interface OverseasReportMapper {
    
    @Insert("INSERT INTO overseas_reports (" +
            "report_path, status, created_at, updated_at, " +
            "report_no, report_no_en, PM_no, report_date, report_date_en, " +
            "reporter, reporter_en, customer_name, customer_name_en, " +
            "address, address_en, contact_person, contact_person_en, " +
            "tel, tel_en, occurrence_place, occurrence_place_en, " +
            "product_name, product_name_en, registration_no, registration_no_en, " +
            "module, module_en, product_package, product_package_en, origin_country, origin_country_en, " +
            "class_type, class_type_en, product_type, product_type_en, " +
            "product_lot, product_lot_en, product_no, product_no_en, " +
            "udi, manufacturing_date, manufacturing_date_en, expiration_date, expiration_date_en, " +
            "event_occurrence_date, event_occurrence_date_en, knowledge_date, knowledge_date_en, " +
            "injury_type, injury_type_en, injury, injury_en, " +
            "device_malfunction_desc, device_malfunction_desc_en, " +
            "patient_name, patient_name_en, birth_date, birth_date_en, " +
            "age, age_en, gender, gender_en, medical_record_no, medical_record_no_en, " +
            "medical_history, medical_history_en, " +
            "disease_intended, disease_intended_en, usage_date, usage_date_en, " +
            "usage_site, usage_site_en, institution_name, institution_name_en, " +
            "usage_process, usage_process_en, drug_device_comb_desc, drug_device_comb_desc_en, " +
            "investigation_flag, investigation_flag_en, investigation_desc, investigation_desc_en, " +
            "relative_evaluation, relative_evaluation_en, event_reason_analysis, event_reason_analysis_en, " +
            "need_risk_assessment, need_risk_assessment_en, plan_submit_date, plan_submit_date_en, " +
            "has_control_measure, has_control_measure_en, control_measure_details, control_measure_details_en, " +
            "no_control_measure_reason, no_control_measure_reason_en" +
            ") VALUES (" +
            "#{reportPath}, #{status}, #{createdAt}, #{updatedAt}, " +
            "#{reportNo}, #{reportNoEn}, #{PMNo}, #{reportDate}, #{reportDateEn}, " +
            "#{reporter}, #{reporterEn}, #{customerName}, #{customerNameEn}, " +
            "#{address}, #{addressEn}, #{contactPerson}, #{contactPersonEn}, " +
            "#{tel}, #{telEn}, #{occurrencePlace}, #{occurrencePlaceEn}, " +
            "#{productName}, #{productNameEn}, #{registrationNo}, #{registrationNoEn}, " +
            "#{module}, #{moduleEn}, #{productPackage}, #{productPackageEn}, #{originCountry}, #{originCountryEn}, " +
            "#{classType}, #{classTypeEn}, #{productType}, #{productTypeEn}, " +
            "#{productLot}, #{productLotEn}, #{productNo}, #{productNoEn}, " +
            "#{udi}, #{manufacturingDate}, #{manufacturingDateEn}, #{expirationDate}, #{expirationDateEn}, " +
            "#{eventOccurrenceDate}, #{eventOccurrenceDateEn}, #{knowledgeDate}, #{knowledgeDateEn}, " +
            "#{injuryType}, #{injuryTypeEn}, #{injury}, #{injuryEn}, " +
            "#{deviceMalfunctionDesc}, #{deviceMalfunctionDescEn}, " +
            "#{patientName}, #{patientNameEn}, #{birthDate}, #{birthDateEn}, " +
            "#{age}, #{ageEn}, #{gender}, #{genderEn}, #{medicalRecordNo}, #{medicalRecordNoEn}, " +
            "#{medicalHistory}, #{medicalHistoryEn}, " +
            "#{diseaseIntended}, #{diseaseIntendedEn}, #{usageDate}, #{usageDateEn}, " +
            "#{usageSite}, #{usageSiteEn}, #{institutionName}, #{institutionNameEn}, " +
            "#{usageProcess}, #{usageProcessEn}, #{drugDeviceCombDesc}, #{drugDeviceCombDescEn}, " +
            "#{investigationFlag}, #{investigationFlagEn}, #{investigationDesc}, #{investigationDescEn}, " +
            "#{relativeEvaluation}, #{relativeEvaluationEn}, #{eventReasonAnalysis}, #{eventReasonAnalysisEn}, " +
            "#{needRiskAssessment}, #{needRiskAssessmentEn}, #{planSubmitDate}, #{planSubmitDateEn}, " +
            "#{hasControlMeasure}, #{hasControlMeasureEn}, #{controlMeasureDetails}, #{controlMeasureDetailsEn}, " +
            "#{noControlMeasureReason}, #{noControlMeasureReasonEn}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OverseasReport report);

    @Select("SELECT * FROM overseas_reports WHERE id = #{id}")
    OverseasReport findById(Long id);

    @Select("SELECT * FROM overseas_reports WHERE report_no = #{reportNumber}")
    OverseasReport findByReportNumber(String reportNumber);

    @Select("SELECT * FROM overseas_reports ORDER BY status = 'PENDING' DESC, created_at DESC LIMIT #{offset}, #{pageSize}")
    List<OverseasReport> findAll(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select({
        "<script>",
        "SELECT * FROM overseas_reports",
        "<where>",
        "<if test='search != null and search != \"\"'>",
        "<choose>",
        "<when test='exactMatch'>",
        "AND (report_no = #{search}",
        "OR PM_no = #{search}",
        "OR product_name = #{search}",
        "OR product_name_en = #{search})",
        "</when>",
        "<otherwise>",
        "AND (report_no LIKE CONCAT('%', #{search}, '%')",
        "OR PM_no LIKE CONCAT('%', #{search}, '%')",
        "OR product_name LIKE CONCAT('%', #{search}, '%')",
        "OR product_name_en LIKE CONCAT('%', #{search}, '%'))",
        "</otherwise>",
        "</choose>",
        "</if>",
        "</where>",
        "ORDER BY status = 'PENDING' DESC, created_at DESC",
        "LIMIT #{offset}, #{pageSize}",
        "</script>"
    })
    List<OverseasReport> searchReports(@Param("search") String search, @Param("exactMatch") boolean exactMatch, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM overseas_reports",
        "<where>",
        "<if test='search != null and search != \"\"'>",
        "<choose>",
        "<when test='exactMatch'>",
        "AND (report_no = #{search}",
        "OR PM_no = #{search}",
        "OR product_name = #{search}",
        "OR product_name_en = #{search})",
        "</when>",
        "<otherwise>",
        "AND (report_no LIKE CONCAT('%', #{search}, '%')",
        "OR PM_no LIKE CONCAT('%', #{search}, '%')",
        "OR product_name LIKE CONCAT('%', #{search}, '%')",
        "OR product_name_en LIKE CONCAT('%', #{search}, '%'))",
        "</otherwise>",
        "</choose>",
        "</if>",
        "</where>",
        "</script>"
    })
    long countSearchResults(@Param("search") String search, @Param("exactMatch") boolean exactMatch);

    @Select("SELECT COUNT(*) FROM overseas_reports")
    long count();

    @Update("UPDATE overseas_reports SET " +
            "report_path = #{reportPath}, status = #{status}, " +
            "updated_at = #{updatedAt}, report_no = #{reportNo}, report_no_en = #{reportNoEn}, " +
            "PM_no = #{PMNo}, " +
            "report_date = #{reportDate}, report_date_en = #{reportDateEn}, " +
            "reporter = #{reporter}, reporter_en = #{reporterEn}, " +
            "customer_name = #{customerName}, customer_name_en = #{customerNameEn}, " +
            "address = #{address}, address_en = #{addressEn}, " +
            "contact_person = #{contactPerson}, contact_person_en = #{contactPersonEn}, " +
            "tel = #{tel}, tel_en = #{telEn}, " +
            "occurrence_place = #{occurrencePlace}, occurrence_place_en = #{occurrencePlaceEn}, " +
            "product_name = #{productName}, product_name_en = #{productNameEn}, " +
            "registration_no = #{registrationNo}, registration_no_en = #{registrationNoEn}, " +
            "module = #{module}, module_en = #{moduleEn}, " +
            "product_package = #{productPackage}, product_package_en = #{productPackageEn}, " +
            "origin_country = #{originCountry}, origin_country_en = #{originCountryEn}, " +
            "class_type = #{classType}, class_type_en = #{classTypeEn}, " +
            "product_type = #{productType}, product_type_en = #{productTypeEn}, " +
            "product_lot = #{productLot}, product_lot_en = #{productLotEn}, " +
            "product_no = #{productNo}, product_no_en = #{productNoEn}, " +
            "udi = #{udi}, manufacturing_date = #{manufacturingDate}, " +
            "manufacturing_date_en = #{manufacturingDateEn}, " +
            "expiration_date = #{expirationDate}, expiration_date_en = #{expirationDateEn}, " +
            "event_occurrence_date = #{eventOccurrenceDate}, " +
            "event_occurrence_date_en = #{eventOccurrenceDateEn}, " +
            "knowledge_date = #{knowledgeDate}, knowledge_date_en = #{knowledgeDateEn}, " +
            "injury_type = #{injuryType}, injury_type_en = #{injuryTypeEn}, " +
            "injury = #{injury}, injury_en = #{injuryEn}, " +
            "device_malfunction_desc = #{deviceMalfunctionDesc}, " +
            "device_malfunction_desc_en = #{deviceMalfunctionDescEn}, " +
            "patient_name = #{patientName}, patient_name_en = #{patientNameEn}, " +
            "birth_date = #{birthDate}, birth_date_en = #{birthDateEn}, " +
            "age = #{age}, age_en = #{ageEn}, " +
            "gender = #{gender}, gender_en = #{genderEn}, " +
            "medical_record_no = #{medicalRecordNo}, medical_record_no_en = #{medicalRecordNoEn}, " +
            "medical_history = #{medicalHistory}, medical_history_en = #{medicalHistoryEn}, " +
            "disease_intended = #{diseaseIntended}, disease_intended_en = #{diseaseIntendedEn}, " +
            "usage_date = #{usageDate}, usage_date_en = #{usageDateEn}, " +
            "usage_site = #{usageSite}, usage_site_en = #{usageSiteEn}, " +
            "institution_name = #{institutionName}, institution_name_en = #{institutionNameEn}, " +
            "usage_process = #{usageProcess}, usage_process_en = #{usageProcessEn}, " +
            "drug_device_comb_desc = #{drugDeviceCombDesc}, drug_device_comb_desc_en = #{drugDeviceCombDescEn}, " +
            "investigation_flag = #{investigationFlag}, investigation_flag_en = #{investigationFlagEn}, " +
            "investigation_desc = #{investigationDesc}, investigation_desc_en = #{investigationDescEn}, " +
            "relative_evaluation = #{relativeEvaluation}, relative_evaluation_en = #{relativeEvaluationEn}, " +
            "event_reason_analysis = #{eventReasonAnalysis}, event_reason_analysis_en = #{eventReasonAnalysisEn}, " +
            "need_risk_assessment = #{needRiskAssessment}, need_risk_assessment_en = #{needRiskAssessmentEn}, " +
            "plan_submit_date = #{planSubmitDate}, plan_submit_date_en = #{planSubmitDateEn}, " +
            "has_control_measure = #{hasControlMeasure}, has_control_measure_en = #{hasControlMeasureEn}, " +
            "control_measure_details = #{controlMeasureDetails}, control_measure_details_en = #{controlMeasureDetailsEn}, " +
            "no_control_measure_reason = #{noControlMeasureReason}, no_control_measure_reason_en = #{noControlMeasureReasonEn} " +
            "WHERE id = #{id}")
    void update(OverseasReport report);

    @Update("UPDATE overseas_reports SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Delete("DELETE FROM overseas_reports WHERE id = #{id}")
    void deleteById(Long id);

    @Select("SELECT " +
        "SFDALicense_ProductCNname as product_name, " +
        "SFDALicense_Product_EN_name as product_name_en, " +
        "SFDALicense as registration_no, " +
        "SFDALicense as registration_no_en, " +
        // "manufactures_Address as origin_country, " +
        // "manufactures_Address as origin_country_en, " +
        "SFDALicense_产品类别_设备_耗材 as product_type, " +
        "SFDALicense_产品类别_设备_耗材 as product_type_en, " +
        "EAN_UPC_DI as udi, " +
        "EAN_UPC_DI as udi_en, " +
        "SMN as smn " +
        "FROM material_list " +
        "WHERE `SMN` = #{productNo}")
    Map<String, Object> getProductInfoByNo(String productNo);

    @Select("SELECT `Class` as class_type " +
        "FROM certificate_list " +
        "WHERE `SMN` = #{smn}")
    Map<String, Object> getCertificateInfoBySMN(String smn);

    @Select({
        "<script>",
        "SELECT * FROM overseas_reports WHERE id IN",
        "<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'>",
        "#{item}",
        "</foreach>",
        "</script>"
    })
    List<OverseasReport> findByIds(List<Long> ids);
} 