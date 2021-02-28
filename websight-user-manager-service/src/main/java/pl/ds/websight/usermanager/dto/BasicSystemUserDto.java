package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;

public class BasicSystemUserDto extends AuthorizableDto {

    public BasicSystemUserDto(ResourceResolver resourceResolver, User user) throws RepositoryException {
        super(user, resourceResolver);
    }

    @Override
    public String getType() {
        return "system_user";
    }
}
