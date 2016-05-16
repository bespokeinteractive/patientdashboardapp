<%
    ui.includeCss("allergyui", "allergies.css")
%>
${ ui.includeFragment("allergyui", "removeAllergyDialog") }

<% ui.includeJavascript("allergyui", "allergies.js") %>

${ ui.includeFragment("uicommons", "infoAndErrorMessage")}

<script>

    var removeDialog = null;

    jq( function() {
        removeDialog = emr.setupConfirmationDialog({
            selector: '#allergyui-remove-allergy-dialogs',
            actions: {
                cancel: function() {
                    removeDialog.close();
                }
            }
        });

        jq("#allergens").click(function(e) {
            e.preventDefault();
            location.href='${ ui.pageLink("allergyui", "allergy", [patientId: patient.id,returnUrl: returnUrl]) }';
            return false;
        });

    });

    function showRemoveAllergyDialogs() {
        removeDialog.show();
    }

    function allergyRemove(allergyId, patientId,action,returnUrl) {
        jq.getJSON('${ ui.actionLink("patientdashboardapp", "Allergies", "removeAllergy") }',
                {
                    allergyId: allergyId,
                    patientId: patientId,
                    action: action,
                    returnUrl:returnUrl
                });
        showRemoveAllergyDialogs();
    }


</script>

<h2>
    ${ ui.message("allergyui.allergies") }
</h2>

<table id="allergies" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("allergyui.allergen") }</th>
        <th>${ ui.message("allergyui.reaction") }</th>
        <th>${ ui.message("allergyui.severity") }</th>
        <th>${ ui.message("allergyui.comment") }</th>
        <th>${ ui.message("allergyui.lastUpdated") }</th>

        <% if (hasModifyAllergiesPrivilege) { %>
        <th>${ ui.message("coreapps.actions") }</th>
        <% } %>
    </tr>
    </thead>

    <tbody>
    <% if (allergies.size() == 0) { %>
    <tr>
        <td colspan="6" align="center" class="allergyStatus">
            <% if (allergies.allergyStatus != "No known allergies") { %>
            ${ allergies.allergyStatus }
            <% } else { %>
            <form name="deactivateForm" method="POST">
                ${ allergies.allergyStatus }
                <input type="hidden" name="patientId" value="${patient.id}"/>
                <input type="hidden" name="action" value="deactivate"/>
                <i class="icon-remove small delete-action" onclick="document.deactivateForm.submit();"/>
            </form>
            <% } %>
        </td>
    </tr>
    <% } %>

    <% allergies.each { allergy -> %>
    <tr>
        <td <% if (hasModifyAllergiesPrivilege) { %> onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'" <% } %> >
            <% if (!allergy.allergen.coded) { %>"<% } %>${ ui.format(allergy.allergen.coded ? allergy.allergen.codedAllergen : allergy.allergen) }<% if (!allergy.allergen.coded) { %>"<% } %>
        </td>

        <td <% if (hasModifyAllergiesPrivilege) { %> onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'" <% } %> >
            <% allergy.reactions.eachWithIndex { reaction, index -> %><% if (index > 0) { %>,<% } %> ${ui.format(reaction.reactionNonCoded ? reaction : reaction.reaction)}<% } %>
        </td>

        <td <% if (hasModifyAllergiesPrivilege) { %> onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'" <% } %> >
            <% if (allergy.severity) { %> ${ ui.format(allergy.severity.name) } <% } %>
        </td>

        <td <% if (hasModifyAllergiesPrivilege) { %> onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'" <% } %> >
            ${ allergy.comment }
        </td>
        <td <% if (hasModifyAllergiesPrivilege) { %> onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'" <% } %> >
            ${ ui.formatDatetimePretty(allergy.dateLastUpdated) }
        </td>

        <% if (hasModifyAllergiesPrivilege) { %>
        <td>

            <i  class="icon-pencil edit-action small" title="${ ui.message("coreapps.edit") }"
               onclick="location.href='${ ui.pageLink("allergyui", "allergy", [allergyId:allergy.id, patientId: patient.id, returnUrl: returnUrl]) }'"></i>
            <i class="icon-remove delete-action small" title="${ ui.message("coreapps.delete") }" onclick="allergyRemove(${ allergy.id},${patient.id},'removeAllergy')"></i>
        </td>
        <% } %>
    </tr>
    <% } %>
    </tbody>
</table>

<br/>

<% if (hasModifyAllergiesPrivilege) { %>
<button id="allergens" class="confirm">
    ${ ui.message("allergyui.addNewAllergy") }
</button>
<form method="POST" action="main.page" >
    <input type="hidden" name="patientId" value="${patient.id}"/>
    <input type="hidden" name="returnUrl" value="${returnUrl}"/>
    <input type="hidden" name="action" value="confirmNoKnownAllergies"/>
    <button type="submit" class="confirm right" style="<% if (allergies.allergyStatus != "Unknown") { %> display:none; <% } %>">
        ${ ui.message("allergyui.noKnownAllergy") }
    </button>
</form>
<% } %>

<div id="allergyui-remove-allergy-dialogs" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("allergyui.removeAllergy") }</h3>
    </div>
    <div class="dialog-content">
        <ul>
            <li class="info">
                <span id="removeAllergyMessage">${ ui.message("allergyui.removeAllergy.message") }</span>
            </li>
        </ul>
        <form method="POST" action="main.page">
            <input type="hidden" name="patientId" value="${patient.id}"/>
            <input type="hidden" id="allergyId" name="allergyId" value=""/>
            <input type="hidden" name="returnUrl" value="${returnUrl}"/>
            <input type="hidden" name="action" value="removeAllergy"/>
            <button class="confirm right" type="submit">${ ui.message("general.yes") }</button>
            <button class="cancel">${ ui.message("general.no") }</button>
        </form>
    </div>
</div>