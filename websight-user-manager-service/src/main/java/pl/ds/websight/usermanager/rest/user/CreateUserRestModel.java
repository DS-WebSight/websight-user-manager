package pl.ds.websight.usermanager.rest.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;

import javax.jcr.RepositoryException;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@Model(adaptables = SlingHttpServletRequest.class)
public class CreateUserRestModel extends UserBaseModel implements Validatable {

    @RequestParameter
    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        try {
            if (getAuthorizable() != null) {
                errors.add("id", getAuthorizableId(), getAuthorizableExistsMessage());
            }
            String passwordConstraint = getPasswordConstraint();
            if (StringUtils.isNotBlank(passwordConstraint) && !password.matches(passwordConstraint)) {
                errors.add("password", null, "Password must match regex " + passwordConstraint);
            }
        } catch (RepositoryException | IOException e) {
            throw new IllegalStateException("Error while create user model validation", e);
        }
        return errors;
    }

    public String getPassword() {
        return password;
    }
}
