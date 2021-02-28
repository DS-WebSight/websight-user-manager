package pl.ds.websight.usermanager.rest;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.validation.constraints.NotEmpty;

@Model(adaptables = SlingHttpServletRequest.class)
public class AuthorizableBaseModel {

    protected static final int MAXIMUM_FIRST_NAME_CHARACTERS = 100;
    protected static final int MAXIMUM_LAST_NAME_CHARACTERS = 100;

    @SlingObject
    private ResourceResolver resourceResolver;

    @RequestParameter
    @NotEmpty(message = "Name cannot be empty")
    private String authorizableId;

    private Authorizable authorizable;

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public Session getSession() {
        return getResourceResolver().adaptTo(Session.class);
    }

    public UserManager getUserManager() throws RepositoryException {
        return AccessControlUtil.getUserManager(getSession());
    }

    public String getAuthorizableId() {
        return authorizableId;
    }

    public Authorizable getAuthorizable() throws RepositoryException {
        if (authorizable == null) {
            authorizable = getUserManager().getAuthorizable(authorizableId);
        }
        return authorizable;
    }

    protected String getAuthorizableExistsMessage() {
        String authorizableType = authorizable.isGroup() ? "Group " : ((User) authorizable).isSystemUser() ? "System User " : "User ";
        return authorizableType + getAuthorizableId() + " already exists";
    }
}
