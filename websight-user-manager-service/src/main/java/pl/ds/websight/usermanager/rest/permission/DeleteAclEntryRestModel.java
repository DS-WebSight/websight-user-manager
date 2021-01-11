package pl.ds.websight.usermanager.rest.permission;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;

import javax.validation.constraints.NotBlank;

@Model(adaptables = SlingHttpServletRequest.class)
public class DeleteAclEntryRestModel extends PrincipalValidatableRestModel {

    @RequestParameter
    @NotBlank(message = "Entry ID cannot be blank")
    private String entryId;

    @RequestParameter
    @NotBlank(message = "Policy ID cannot be blank")
    private String policyId;

    public String getEntryId() {
        return entryId;
    }

    public String getPolicyId() {
        return policyId;
    }
}
