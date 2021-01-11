package pl.ds.websight.usermanager.rest.systemuser;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.QueryBuilder.Direction;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.apache.jackrabbit.api.security.user.QueryBuilder.Direction.ASCENDING;
import static org.apache.jackrabbit.api.security.user.QueryBuilder.Direction.DESCENDING;
import static org.apache.sling.models.annotations.DefaultInjectionStrategy.OPTIONAL;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = OPTIONAL)
public class FindSystemUsersRestModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    @RequestParameter
    private String filter;

    @RequestParameter
    @Default(values = "ASC")
    private String sortDirection;

    @RequestParameter
    @Default(longValues = 1L)
    private Long pageNumber;

    private UserManager userManager;

    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    public Session getSession() {
        return getResourceResolver().adaptTo(Session.class);
    }

    public UserManager getUserManager() throws RepositoryException {
        if (userManager == null) {
            userManager = AccessControlUtil.getUserManager(getSession());
        }
        return userManager;
    }

    public String getFilter() {
        return filter;
    }

    public Direction getSortDirection() {
        return StringUtils.isNotBlank(sortDirection) && "DESC".equals(sortDirection) ? DESCENDING : ASCENDING;
    }

    public Long getPageNumber() {
        return pageNumber;
    }

}
