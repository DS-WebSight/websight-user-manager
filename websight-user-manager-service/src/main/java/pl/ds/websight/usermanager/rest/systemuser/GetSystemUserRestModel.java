package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.usermanager.rest.user.GetUserRestModel;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class GetSystemUserRestModel extends GetUserRestModel {

    private static final Logger LOG = LoggerFactory.getLogger(GetUserRestModel.class);

    @Override
    public Errors validate() {
        Errors errors = super.validate();
        try {
            if (errors.isEmpty()) {
                User user = (User) getAuthorizable();
                if (!user.isSystemUser()) {
                    errors.add("id", getAuthorizableId(), "User is not a system user");
                }
            }

        } catch (RepositoryException e) {
            LOG.warn("Could not fetch user {}", getAuthorizableId(), e);
            errors.add("id", getAuthorizableId(), "Could not fetch user " + getAuthorizableId());
        }
        return errors;
    }
}
