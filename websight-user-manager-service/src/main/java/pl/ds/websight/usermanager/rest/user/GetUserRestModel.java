package pl.ds.websight.usermanager.rest.user;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class GetUserRestModel extends AuthorizableBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(GetUserRestModel.class);

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        try {
            Authorizable authorizable = getAuthorizable();
            if (authorizable == null) {
                errors.add("id", getAuthorizableId(), "User does not exist");
                return errors;
            }
            if (authorizable.isGroup()) {
                errors.add("id", getAuthorizableId(), getAuthorizableId() + " is not a user");
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not fetch user {}", getAuthorizableId(), e);
            errors.add("id", getAuthorizableId(), "Could not fetch user " + getAuthorizableId());
        }
        return errors;
    }
}
