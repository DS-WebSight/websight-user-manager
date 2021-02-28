package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;

import static java.util.Objects.requireNonNull;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.FIRST_NAME_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.LAST_NAME_PROPERTY_NAME;
import static pl.ds.websight.usermanager.rest.user.UserBaseModel.PROFILE_NODE_NAME;

public class BasicUserDto extends AuthorizableDto {

    protected String firstName;

    protected String lastName;

    protected final boolean enabled;

    public BasicUserDto(ResourceResolver resourceResolver, User user) throws RepositoryException {
        super(user, resourceResolver);
        enabled = !user.isDisabled();

        Resource profileResource = resourceResolver.getResource(user.getPath() + '/' + PROFILE_NODE_NAME);
        if (profileResource != null) {
            ValueMap profileMap = requireNonNull(profileResource.adaptTo(ValueMap.class), "Could not adapt profile resource to value map");
            firstName = profileMap.get(FIRST_NAME_PROPERTY_NAME, String.class);
            lastName = profileMap.get(LAST_NAME_PROPERTY_NAME, String.class);
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getType() {
        return "user";
    }
}
