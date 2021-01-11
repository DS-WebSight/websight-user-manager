package pl.ds.websight.usermanager.rest.permission;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotEmpty;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Model(adaptables = SlingHttpServletRequest.class)
public class GetPermissionsRestModel extends AuthorizableBaseModel {

    private static final Logger LOG = LoggerFactory.getLogger(GetPermissionsRestModel.class);

    @RequestParameter(name = "path")
    @NotEmpty(message = "Path cannot be empty")
    private List<String> paths;

    private final Set<Principal> principals = new HashSet<>();

    @PostConstruct
    private void init() {
        try {
            Authorizable authorizable = getAuthorizable();
            if (authorizable != null) {
                Principal currentPrincipal = authorizable.getPrincipal();
                principals.add(authorizable.getPrincipal());

                JackrabbitSession jackrabbitSession = (JackrabbitSession) getSession();
                PrincipalIterator groupMembershipIterator = jackrabbitSession.getPrincipalManager().getGroupMembership(currentPrincipal);
                while (groupMembershipIterator.hasNext()) {
                    principals.add(groupMembershipIterator.nextPrincipal());
                }
            }
        } catch (RepositoryException e) {
            LOG.error("Could not initialize principals set", e);
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public Set<Principal> getPrincipals() {
        return principals;
    }

}
