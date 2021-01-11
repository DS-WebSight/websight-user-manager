package pl.ds.websight.usermanager.rest.permission;


import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class PrincipalValidatableRestModel extends AuthorizableBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(PrincipalValidatableRestModel.class);

    @Override
    public Errors validate() {
        try {
            if (getAuthorizable() == null) {
                return Errors.of("authorizableId", getAuthorizableId(), "Could not find authorizable");
            }
            if (getAuthorizable().getPrincipal() == null) {
                return Errors.of("authorizableId", getAuthorizableId(), "Could not find principal");
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not check principal validity for id: {}", getAuthorizableId(), e);
            return Errors.of("authorizableId", getAuthorizableId(), "Could not find authorizable");
        }
        return Errors.createErrors();
    }
}
