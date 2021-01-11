package pl.ds.websight.usermanager.rest.group;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import java.util.Collections;
import java.util.List;

import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = OPTIONAL)
public class GroupBaseModel extends AuthorizableBaseModel {

    public static final String ID_PROPERTY_NAME = "id";
    public static final String DISPLAY_NAME_PROPERTY = "displayName";
    public static final String DESCRIPTION_PROPERTY = "description";

    @RequestParameter
    private String displayName;

    @RequestParameter
    private String description;

    @RequestParameter
    private List<String> parentGroups = Collections.emptyList();

    @RequestParameter
    private List<String> members = Collections.emptyList();

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getParentGroups() {
        return parentGroups;
    }

    public List<String> getMembers() {
        return members;
    }
}
