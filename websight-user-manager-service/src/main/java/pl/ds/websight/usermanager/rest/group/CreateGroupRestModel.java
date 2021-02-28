package pl.ds.websight.usermanager.rest.group;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;

import javax.jcr.RepositoryException;

@Model(adaptables = SlingHttpServletRequest.class)
public class CreateGroupRestModel extends GroupBaseModel implements Validatable {

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        try {
            if (getAuthorizable() != null) {
                errors.add("id", getAuthorizableId(), getAuthorizableExistsMessage());
            }
        } catch (RepositoryException e) {
            throw new IllegalStateException("Error while create group model validation", e);
        }
        return errors;
    }
}
