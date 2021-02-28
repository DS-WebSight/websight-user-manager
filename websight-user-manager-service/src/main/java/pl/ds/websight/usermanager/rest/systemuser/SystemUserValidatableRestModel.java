package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class SystemUserValidatableRestModel extends AuthorizableBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(SystemUserValidatableRestModel.class);

    @Override
    public Errors validate() {
        try {
            if (getAuthorizable() != null) {
                return Errors.of("id", getAuthorizableId(), getAuthorizableExistsMessage());
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not check system user validity for id: {}", getAuthorizableId(), e);
            return Errors.of("authorizableId", getAuthorizableId(), "Error while create system user model validation");
        }
        return Errors.createErrors();
    }
}
