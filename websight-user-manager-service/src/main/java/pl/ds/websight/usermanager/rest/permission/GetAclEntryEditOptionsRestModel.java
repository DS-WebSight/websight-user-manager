package pl.ds.websight.usermanager.rest.permission;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;

import javax.jcr.Session;

@Model(adaptables = SlingHttpServletRequest.class)
public class GetAclEntryEditOptionsRestModel implements Validatable {

    @SlingObject
    private ResourceResolver resourceResolver;

    @RequestParameter
    private String path;

    public Session getSession() {
        return resourceResolver.adaptTo(Session.class);
    }

    public String getPath() {
        return path;
    }

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        return path != null && !path.startsWith("/") ?
                errors.add("path", path, "Path cannot be relative") :
                errors;
    }
}
