package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import javax.jcr.RepositoryException;

import static java.util.Objects.requireNonNull;

public abstract class AuthorizableDto {

    private final String id;

    private final String path;

    private String uuid;

    public AuthorizableDto(Authorizable authorizable, ResourceResolver resolver) throws RepositoryException {
        this.id = authorizable.getID();
        this.path = authorizable.getPath();

        Resource authorizableResource = resolver.getResource(path);
        if (authorizableResource != null) {
            ValueMap resourceMap = requireNonNull(authorizableResource.adaptTo(ValueMap.class), "Could not adapt authorizable resource to" +
                    " value map");
            uuid = resourceMap.get(JcrConstants.JCR_UUID, String.class);
        }
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getUuid() {
        return uuid;
    }

    public abstract String getType();
}
