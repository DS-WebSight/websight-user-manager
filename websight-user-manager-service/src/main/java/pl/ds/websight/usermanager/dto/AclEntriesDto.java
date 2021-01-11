package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlEntry;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import pl.ds.websight.usermanager.rest.permission.InheritedMembershipInfo;
import pl.ds.websight.usermanager.util.JcrSecurityUtil;
import pl.ds.websight.usermanager.util.PathAccessUtil;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AclEntriesDto {

    private final Set<EntryDto> entries;
    private final Set<InheritedEntryDto> inheritedEntries;

    public AclEntriesDto(Map<String, JackrabbitAccessControlEntry[]> mainPrincipalPolicies,
            Set<InheritedMembershipInfo> inheritanceInfos) throws RepositoryException {
        this.entries = mapToEntryDto(mainPrincipalPolicies, EntryDto::new);
        this.inheritedEntries = mapToInheritedEntryDto(inheritanceInfos);
    }

    private static Set<InheritedEntryDto> mapToInheritedEntryDto(Set<InheritedMembershipInfo> inheritanceInfos)
            throws RepositoryException {
        Set<InheritedEntryDto> inheritedEntries = new LinkedHashSet<>();
        for (InheritedMembershipInfo inheritedPolicy : inheritanceInfos) {
            List<String> hierarchy = getHierarchy(inheritedPolicy);
            inheritedEntries.addAll(mapToEntryDto(inheritedPolicy.getPolicies(),
                    (ace, path, policyId) -> new InheritedEntryDto(ace, path, policyId, hierarchy)));
        }
        return inheritedEntries;
    }

    private static List<String> getHierarchy(InheritedMembershipInfo inheritedPolicies) throws RepositoryException {
        List<String> hierarchy = new ArrayList<>();
        InheritedMembershipInfo membershipProvider = inheritedPolicies.getMembershipInfoProvider();
        while (membershipProvider != null) {
            hierarchy.add(membershipProvider.getAuthorizable().getID());
            membershipProvider = membershipProvider.getMembershipInfoProvider();
        }
        return hierarchy;
    }

    private static <T extends EntryDto> Set<T> mapToEntryDto(Map<String, JackrabbitAccessControlEntry[]> policiesMap,
            EntryDtoCreator<T> creator) throws RepositoryException {
        Set<T> entries = new LinkedHashSet<>();
        for (Map.Entry<String, JackrabbitAccessControlEntry[]> aclEntriesEntry : policiesMap.entrySet()) {
            String policyId = aclEntriesEntry.getKey();
            for (JackrabbitAccessControlEntry aclEntry : aclEntriesEntry.getValue()) {
                String path = PathAccessUtil.getPath(aclEntry);
                if (path != null) {
                    entries.add(creator.create(aclEntry, path, policyId));
                }
            }
        }
        return entries;
    }

    public Set<EntryDto> getEntries() {
        return entries;
    }

    public Set<InheritedEntryDto> getInheritedEntries() {
        return inheritedEntries;
    }

    private interface EntryDtoCreator<T extends EntryDto> {
        T create(JackrabbitAccessControlEntry ace, String path, String policyId) throws RepositoryException;
    }

    private static class EntryDto {

        private final String policyId;
        private final String entryId;
        private final String authorizableId;
        private final String path;
        private final String[] privileges;
        private final Map<String, List<String>> restrictions;
        private final boolean isAllow;

        private EntryDto(JackrabbitAccessControlEntry aclEntry, String path, String policyId) throws RepositoryException {
            this.policyId = policyId;
            this.entryId = JcrSecurityUtil.getEntryId(aclEntry);
            this.authorizableId = aclEntry.getPrincipal().getName();
            this.path = path;
            this.privileges = Arrays.stream(AccessControlUtils.namesFromPrivileges(aclEntry.getPrivileges()))
                    .sorted()
                    .toArray(String[]::new);
            this.restrictions = JcrSecurityUtil.getRestrictions(aclEntry, false);
            this.isAllow = aclEntry.isAllow();
        }

        public String getPolicyId() {
            return policyId;
        }

        public String getEntryId() {
            return entryId;
        }

        public String getAuthorizableId() {
            return authorizableId;
        }

        public String getPath() {
            return path;
        }

        public String[] getPrivileges() {
            return privileges;
        }

        public Map<String, List<String>> getRestrictions() {
            return restrictions;
        }

        public boolean isAllow() {
            return isAllow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EntryDto entryDto = (EntryDto) o;
            return policyId.equals(entryDto.policyId) &&
                    entryId.equals(entryDto.entryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(policyId, entryId);
        }

    }

    private static class InheritedEntryDto extends EntryDto {

        private final List<String> membershipHierarchy;

        private InheritedEntryDto(JackrabbitAccessControlEntry aclEntry, String path, String policyId,
                List<String> membershipHierarchy) throws RepositoryException {
            super(aclEntry, path, policyId);
            this.membershipHierarchy = membershipHierarchy;
        }

        public List<String> getMembershipHierarchy() {
            return membershipHierarchy;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}
