package pl.ds.websight.usermanager.rest.permission;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;
import pl.ds.websight.usermanager.dto.AclEntryEditOptionsDto;
import pl.ds.websight.usermanager.rest.AbstractRestAction;
import pl.ds.websight.usermanager.rest.Messages;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@SlingAction(GET)
@Component
public class GetAclEntryEditOptionsRestAction extends AbstractRestAction<GetAclEntryEditOptionsRestModel, AclEntryEditOptionsDto>
        implements RestAction<GetAclEntryEditOptionsRestModel, AclEntryEditOptionsDto> {

    @Override
    protected RestActionResult<AclEntryEditOptionsDto> performAction(GetAclEntryEditOptionsRestModel model) throws Exception {
        JackrabbitAccessControlManager acm = (JackrabbitAccessControlManager) model.getSession().getAccessControlManager();
        String absPath = model.getPath();
        return RestActionResult.success(new AclEntryEditOptionsDto(absPath, acm));
    }

    @Override
    protected String getUnexpectedErrorMessage() {
        return Messages.GET_AVAILABLE_PRIVILEGES_ERROR;
    }
}
