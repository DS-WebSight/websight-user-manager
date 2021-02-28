package pl.ds.websight.usermanager.rest.user;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;
import pl.ds.websight.usermanager.util.AuthorizableUtil;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Model(adaptables = SlingHttpServletRequest.class)
public class DeleteUserRestModel extends AuthorizableBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserRestModel.class);

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        String authorizableId = getAuthorizableId();
        try {
            Authorizable authorizable = getAuthorizable();
            if (authorizable == null) {
                errors.add("id", authorizableId, "Cannot remove non existing user " + authorizableId);
                return errors;
            }
            if (authorizable.isGroup()) {
                errors.add("id", authorizableId, "Cannot remove group " + authorizableId + " with delete user action");
            }
            Session session = getSession();
            if (authorizableId.equals(session.getUserID())) {
                errors.add("id", authorizableId, "Cannot remove current user " + authorizableId);
            }
            if (AuthorizableUtil.isProtected(authorizableId)) {
                errors.add("id", authorizableId, "Cannot remove protected user " + authorizableId);
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not fetch user {}", getAuthorizableId(), e);
            errors.add("id", authorizableId, "Could not fetch user " + authorizableId);
        }
        return errors;
    }
}
