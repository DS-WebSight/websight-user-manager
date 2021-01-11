package pl.ds.websight.usermanager.rest.requestparameters;

import javax.jcr.security.Privilege;
import java.util.Arrays;
import java.util.List;

public enum Action {

    ADMIN("admin", Arrays.asList(Privilege.JCR_ALL)),
    READ("read", Arrays.asList(Privilege.JCR_READ)),
    MODIFY("modify", Arrays.asList(Privilege.JCR_MODIFY_PROPERTIES, Privilege.JCR_LOCK_MANAGEMENT, Privilege.JCR_VERSION_MANAGEMENT)),
    CREATE("create", Arrays.asList(Privilege.JCR_ADD_CHILD_NODES, Privilege.JCR_NODE_TYPE_MANAGEMENT)),
    DELETE("delete", Arrays.asList(Privilege.JCR_REMOVE_NODE, Privilege.JCR_REMOVE_CHILD_NODES));

    private final String name;
    private final List<String> requiredPrivileges;

    Action(String name, List<String> requiredPrivileges) {
        this.name = name;
        this.requiredPrivileges = requiredPrivileges;
    }

    public String getName() {
        return name;
    }

    public List<String> getRequiredPrivileges() {
        return requiredPrivileges;
    }

}
