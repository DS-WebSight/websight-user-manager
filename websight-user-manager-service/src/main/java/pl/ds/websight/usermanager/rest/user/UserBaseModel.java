package pl.ds.websight.usermanager.rest.user;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = OPTIONAL)
public class UserBaseModel extends AuthorizableBaseModel {

    public static final String PROFILE_NODE_NAME = "profile";
    public static final String META_INFO_NODE_NAME = "metainfo";
    public static final String ID_PROPERTY_NAME = "id";
    public static final String FIRST_NAME_PROPERTY_NAME = "firstName";
    public static final String LAST_NAME_PROPERTY_NAME = "lastName";
    public static final String EMAIL_PROPERTY_NAME = "email";
    public static final String LOGIN_COUNT_PROPERTY_NAME = "loginCount";
    public static final String LAST_LOGGED_IN_PROPERTY_NAME = "lastLoggedIn";

    @OSGiService
    private ConfigurationAdmin configurationAdmin;

    @RequestParameter
    @Size(max = MAXIMUM_FIRST_NAME_CHARACTERS)
    private String firstName;

    @RequestParameter
    @Size(max = MAXIMUM_LAST_NAME_CHARACTERS)
    private String lastName;

    @RequestParameter
    @Email
    private String email;

    @RequestParameter
    private Boolean enabled;

    @RequestParameter
    private List<String> groups = Collections.emptyList();

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public List<String> getGroups() {
        return groups;
    }

    protected Map<String, Object> getUserProfileProperties() {
        Map<String, Object> properties = new HashMap<>();
        if (firstName != null) {
            properties.put(FIRST_NAME_PROPERTY_NAME, firstName);
        }
        if (lastName != null) {
            properties.put(LAST_NAME_PROPERTY_NAME, lastName);
        }
        if (email != null) {
            properties.put(EMAIL_PROPERTY_NAME, email);
        }
        return properties;
    }

    protected String getPasswordConstraint() throws IOException {
        Configuration configuration = configurationAdmin
                .getConfiguration("org.apache.jackrabbit.oak.spi.security.user.action.DefaultAuthorizableActionProvider", null);
        Object constraint = configuration.getProperties().get("constraint");
        if (constraint instanceof String) {
            return (String) constraint;
        }
        return null;
    }
}
