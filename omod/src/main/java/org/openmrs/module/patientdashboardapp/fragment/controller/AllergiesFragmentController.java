package org.openmrs.module.patientdashboardapp.fragment.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.module.allergyapi.Allergies;
import org.openmrs.module.allergyapi.api.PatientService;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by USER on 5/16/2016.
 */
public class AllergiesFragmentController {

    public void controller() {
    }
    public void removeAllergy(@RequestParam("patientId") Patient patient,
                       @RequestParam(value = "action", required = false) String action,
                       @RequestParam(value = "allergyId", required = false) Integer allergyId,
                       @RequestParam(value = "returnUrll", required = false) String returnUrl,
                       @RequestParam(value = "opdId") Integer opdId,
                       @RequestParam(value = "queueId")Integer queueId,
                       FragmentModel model,HttpSession session, @SpringBean("allergyService") PatientService patientService, UiUtils ui) {
        if (StringUtils.isNotBlank(action)) {
            try {
                Allergies allergies = null;

                 if ("removeAllergy".equals(action)) {
                    allergies = patientService.getAllergies(patient);
                    allergies.remove(allergies.getAllergy(allergyId));
                }

                patientService.setAllergies(patient, allergies);

                InfoErrorMessageUtil.flashInfoMessage(session, "allergyui.message.success");

            }
            catch (Exception e) {
                session.setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE, "allergyui.message.fail");
            }
        }


        if (StringUtils.isBlank(returnUrl)) {
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("patientId",patient.getId());
            map.put("opdId",opdId);
            map.put("queueId",queueId);
            returnUrl = ui.pageLink("patientdashboardapp", "main", map);
        }

        model.addAttribute("allergies", patientService.getAllergies(patient));
        model.addAttribute("returnUrll",returnUrl);
    }

}
