package pl.ds.websight.usermanager.auth;

import org.osgi.service.component.annotations.Component;
import pl.ds.websight.admin.auth.AnonymousAccessEnabler;

@Component(service = AnonymousAccessEnabler.class)
public class UserManagerAnonymousAccessEnabler implements AnonymousAccessEnabler {

    @Override
    public String[] getPaths() {
        return new String[] { "/apps/websight-user-manager-service" };
    }
}
