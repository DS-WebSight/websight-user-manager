package pl.ds.websight.usermanager.rest.group;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class UpdateGroupRestModel extends GroupBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateGroupRestModel.class);

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        String authorizableId = getAuthorizableId();
        try {
            Authorizable authorizable = getAuthorizable();
            if (authorizable == null) {
                errors.add("id", authorizableId, "Group " + authorizableId + " does not exist");
                return errors;
            }
            if (!authorizable.isGroup()) {
                errors.add("id", authorizableId, authorizableId + " is not a group");
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not fetch group {} for update group action", authorizableId, e);
            errors.add("id", authorizableId, "Could not fetch " + authorizableId + " group");
        }
        return errors;
    }
}
