package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import pl.ds.websight.usermanager.util.AuthorizableUtil;
import pl.ds.websight.usermanager.util.DateFormatter;

import javax.jcr.RepositoryException;
import java.util.Date;

import static java.util.Objects.requireNonNull;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.EMAIL_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LAST_LOGGED_IN_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LOGIN_COUNT_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.META_INFO_NODE_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.PROFILE_NODE_NAME;

public class UserDto extends BasicUserDto {

    private String email;

    private final boolean admin;

    private final boolean protectedUser;

    private Long loginCount;

    private String lastLoggedIn;

    private String lastLoggedInRelative;

    public UserDto(ResourceResolver resourceResolver, User user) throws RepositoryException {
        super(resourceResolver, user);
        admin = user.isAdmin();
        protectedUser = AuthorizableUtil.isProtected(user.getID());

        Resource profileResource = resourceResolver.getResource(user.getPath() + '/' + PROFILE_NODE_NAME);
        if (profileResource != null) {
            ValueMap profileMap = requireNonNull(profileResource.adaptTo(ValueMap.class), "User profile value map does not exist");
            email = profileMap.get(EMAIL_PROPERTY_NAME, String.class);
        }

        Resource metaInfoResource = resourceResolver.getResource(user.getPath() + '/' + META_INFO_NODE_NAME);
        if (metaInfoResource != null) {
            ValueMap metaInfoMap = requireNonNull(metaInfoResource.adaptTo(ValueMap.class), "Meta info value map does not exist");
            loginCount = metaInfoMap.get(LOGIN_COUNT_PROPERTY_NAME, Long.class);
            Date lastLoggedInDate = metaInfoMap.get(LAST_LOGGED_IN_PROPERTY_NAME, Date.class);
            lastLoggedIn = DateFormatter.formatDate(lastLoggedInDate);
            lastLoggedInRelative = DateFormatter.toRelative(lastLoggedInDate);
        }
    }

    public String getEmail() {
        return email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isProtectedUser() {
        return protectedUser;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public String getLastLoggedInRelative() {
        return lastLoggedInRelative;
    }

    public String getLastLoggedIn() {
        return lastLoggedIn;
    }
}
