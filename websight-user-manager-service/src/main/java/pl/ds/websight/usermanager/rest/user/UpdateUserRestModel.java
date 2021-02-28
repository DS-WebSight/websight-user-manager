package pl.ds.websight.usermanager.rest.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.util.AuthorizableUtil;

import javax.jcr.RepositoryException;
import java.io.IOException;

import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = OPTIONAL)
public class UpdateUserRestModel extends UserBaseModel implements Validatable {

    @RequestParameter
    private Boolean changePassword;

    @RequestParameter
    private String password;

    @RequestParameter
    private String confirmedPassword;

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        try {
            Authorizable authorizable = getAuthorizable();
            if (authorizable == null) {
                errors.add("id", getAuthorizableId(), "User " + getAuthorizableId() + " does not exist");
            } else if (authorizable.isGroup()) {
                errors.add("id", getAuthorizableId(), "Cannot update group with user update");
            }
            if (isChangingStatusOfProtectedUser(authorizable)) {
                errors.add("enabled", isEnabled(), "Cannot update status of protected user");
            }
            validatePassword(errors);
        } catch (RepositoryException | IOException e) {
            throw new IllegalStateException("Error while update user model validation", e);
        }
        return errors;
    }

    private void validatePassword(Errors errors) throws IOException {
        if (!isChangingPassword()) {
            return;
        }
        if (StringUtils.isBlank(password)) {
            errors.add("password", null, "Password cannot be empty");
            return;
        }
        String passwordConstraint = getPasswordConstraint();
        if (StringUtils.isNotBlank(passwordConstraint) && !password.matches(passwordConstraint)) {
            errors.add("password", null, "Password must match regex " + passwordConstraint);
        }
        if (!password.equals(confirmedPassword)) {
            errors.add("confirmedPassword", null, "Passwords must be equal");
        }
    }

    private boolean isChangingStatusOfProtectedUser(Authorizable authorizable) throws RepositoryException {
        if (authorizable == null || authorizable.isGroup()) {
            return false;
        }
        User user = (User) authorizable;
        Boolean enabled = isEnabled();
        return AuthorizableUtil.isProtected(getAuthorizableId()) && enabled != null && user.isDisabled() == enabled;
    }

    public String getPassword() {
        return password;
    }

    public boolean isChangingPassword() {
        return Boolean.TRUE.equals(changePassword);
    }
}
