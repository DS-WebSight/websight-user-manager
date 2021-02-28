package pl.ds.websight.usermanager.rest.group;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.util.PropertiesUtil;

import javax.jcr.RepositoryException;

import static java.util.Objects.requireNonNull;
import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.DESCRIPTION_PROPERTY;
import static pl.ds.websight.usermanager.rest.group.GroupBaseModel.DISPLAY_NAME_PROPERTY;

public abstract class GroupBaseAction<T, R> extends AbstractRestAction<T, R> {

    protected void updateGroupProperties(GroupBaseModel model, Group group) throws RepositoryException {
        ResourceResolver resourceResolver = model.getResourceResolver();
        Resource groupResource = requireNonNull(resourceResolver.getResource(group.getPath()),
                "Group resource cannot be null " + group.getPath());
        ModifiableValueMap modifiableValueMap = requireNonNull(groupResource.adaptTo(ModifiableValueMap.class),
                "Value map for user is null " + groupResource.getPath());
        PropertiesUtil.putIfChangedIgnoreNull(modifiableValueMap, DISPLAY_NAME_PROPERTY, model.getDisplayName());
        PropertiesUtil.putIfChangedIgnoreNull(modifiableValueMap, DESCRIPTION_PROPERTY, model.getDescription());
    }
}
