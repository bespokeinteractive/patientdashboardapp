package org.openmrs.module.patientdashboardapp.page.controller;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.allergyapi.Allergies;
import org.openmrs.module.allergyapi.AllergyConstants;
import org.openmrs.module.allergyapi.api.PatientService;
import org.openmrs.module.allergyui.extension.html.AllergyComparator;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.hospitalcore.PatientQueueService;
import org.openmrs.module.hospitalcore.model.OpdPatientQueue;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.util.ConceptAnswerComparator;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.referenceapplication.ReferenceApplicationWebConstants;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.openmrs.ui.util.ByFormattedObjectComparator;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

public class MainPageController {

    public void get(UiSessionContext sessionContext,
                    PageModel model,
                    PageRequest pageRequest,
                    UiUtils ui,
                    @RequestParam("patientId") Integer patientId,
                    @RequestParam("opdId") Integer opdId,
                    @RequestParam(value = "queueId", required = false) Integer queueId,
                    @RequestParam(value = "opdLogId", required = false) Integer opdLogId,
                    @RequestParam(value = "visitStatus", required = false) String visitStatus,
                    @SpringBean("allergyService") PatientService patientService
                    ) {
        pageRequest.getSession().setAttribute(ReferenceApplicationWebConstants.SESSION_ATTRIBUTE_REDIRECT_URL,ui.thisUrl());
        sessionContext.requireAuthentication();
        Patient patient = Context.getPatientService().getPatient(patientId);
        HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
        PatientQueueService patientQueueService = Context.getService(PatientQueueService.class);

        Allergies allergies = patientService.getAllergies(patient);
        Comparator comparator = new AllergyComparator(new ByFormattedObjectComparator(ui));
        Collections.sort(allergies, comparator);

        model.addAttribute("allergies", allergies);

        model.addAttribute("patient", patient);
        model.addAttribute("hasModifyAllergiesPrivilege", Context.getAuthenticatedUser().hasPrivilege(AllergyConstants.PRIVILEGE_MODIFY_ALLERGIES));
        Map<String, String> attributes = PatientUtils.getAttributes(patient);
        Concept category = Context.getConceptService().getConceptByName("Patient Category");
        List<ConceptAnswer> categoryList = (category != null ? new ArrayList<ConceptAnswer>(category.getAnswers()) : null);
        if (CollectionUtils.isNotEmpty(categoryList)) {
            Collections.sort(categoryList, new ConceptAnswerComparator());
        }

        if (patient.getGender().equals("M")){
            model.addAttribute("gender", "MALE");
        }
        else{
            model.addAttribute("gender", "FEMALE");
        }

        model.addAttribute("patientId", patientId);
        model.addAttribute("opdId", opdId);
        model.addAttribute("queueId", queueId);
        model.addAttribute("opdLogId", opdLogId);
        model.addAttribute("patientIdentifier",patient.getPatientIdentifier());
        model.addAttribute("category",patient.getAttribute(14));
        model.addAttribute("address",patient.getPersonAddress());
        model.addAttribute("visitStatus",visitStatus);
        model.addAttribute("returnUrl",ui.thisUrl());

        Encounter lastEncounter = patientQueueService.getLastOPDEncounter(patient);
        Date lastVisitDate = null;
        if(lastEncounter!=null) {
            lastVisitDate = lastEncounter.getEncounterDatetime();
        }
        model.addAttribute("previousVisit", lastVisitDate);
        model.addAttribute("date", new Date());

        String status = null;
        if (queueId != null) {
            OpdPatientQueue opdPatientQueue = Context.getService(PatientQueueService.class).getOpdPatientQueueById(queueId);
            if (opdPatientQueue != null) {
                opdPatientQueue.setStatus("Dr. "+Context.getAuthenticatedUser().getGivenName());
                Context.getService(PatientQueueService.class).saveOpdPatientQueue(opdPatientQueue);
                status = opdPatientQueue.getVisitStatus();
            }
            if (status!= null){
                model.addAttribute("patientStatus",status);
            }else{
                model.addAttribute("patientStatus" ,"Unknown");
            }
        }
    }

    public String post(@RequestParam("patientId") Patient patient,
                       @RequestParam(value = "action", required = false) String action,
                       @RequestParam(value = "allergyId", required = false) Integer allergyId,
                       @RequestParam(value = "returnUrl", required = false) String returnUrl,
                       PageModel model,UiUtils ui,
                       HttpSession session, @SpringBean("allergyService") PatientService patientService) {

        if (StringUtils.isNotBlank(action)) {
            try {
                Allergies allergies = null;
                if ("confirmNoKnownAllergies".equals(action)) {
                    allergies = new Allergies();
                    allergies.confirmNoKnownAllergies();
                }
                else if ("deactivate".equals(action)) {
                    allergies = new Allergies();
                }
                else if ("removeAllergy".equals(action)) {
                    allergies = patientService.getAllergies(patient);
                    allergies.remove(allergies.getAllergy(allergyId));
                }

                patientService.setAllergies(patient, allergies);

                InfoErrorMessageUtil.flashInfoMessage(session, "allergyui.message.success");

                return "redirect:allergyui/allergies.page?patientId=" + patient.getPatientId() + "&returnUrl=" + ui.urlEncode(returnUrl);
            }
            catch (Exception e) {
                session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "allergyui.message.fail");
            }
        }

        model.addAttribute("allergies", patientService.getAllergies(patient));
        model.addAttribute("returnUrl", returnUrl);

        return null;
    }
}