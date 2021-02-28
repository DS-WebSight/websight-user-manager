package pl.ds.websight.usermanager.rest.permission;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.api.security.user.Authorizable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class InheritedMembershipInfo {

    private final Authorizable authorizable;
    private final InheritedMembershipInfo membershipInfoProvider;
    private Map<String, JackrabbitAccessControlEntry[]> policies;

    private InheritedMembershipInfo(Authorizable authorizable, InheritedMembershipInfo membershipInfoProvider,
            Map<String, JackrabbitAccessControlEntry[]> policies) {
        this.authorizable = authorizable;
        this.membershipInfoProvider = membershipInfoProvider;
        this.policies = policies;
    }

    public void setPolicies(Map<String, JackrabbitAccessControlEntry[]> policies) {
        this.policies = policies;
    }

    public Authorizable getAuthorizable() {
        return authorizable;
    }

    public InheritedMembershipInfo getMembershipInfoProvider() {
        return membershipInfoProvider;
    }

    public Map<String, JackrabbitAccessControlEntry[]> getPolicies() {
        return Collections.unmodifiableMap(policies);
    }

    public static InheritedMembershipInfo wrap(Authorizable authorizable, InheritedMembershipInfo parent,
            Map<String, JackrabbitAccessControlEntry[]> policiesMap) {
        return new InheritedMembershipInfo(authorizable, parent, policiesMap);
    }

    public static InheritedMembershipInfo wrapEveryoneGroup(Map<String, JackrabbitAccessControlEntry[]> everyonePolicies) {
        return new InheritedMembershipInfo(null, null, everyonePolicies);
    }

    public static InheritedMembershipInfo wrapRoot(Authorizable authorizable) {
        return new InheritedMembershipInfo(authorizable, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InheritedMembershipInfo that = (InheritedMembershipInfo) o;
        return authorizable.equals(that.authorizable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizable);
    }
}
