package pl.ds.websight.usermanager.dto;

import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.security.Privilege;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toCollection;

public class AclEntryEditOptionsDto {

    private static final Logger LOG = LoggerFactory.getLogger(AclEntryEditOptionsDto.class);

    private final Set<String> restrictions;

    private final Set<String> privileges;

    public AclEntryEditOptionsDto(String path, JackrabbitAccessControlManager acm) throws RepositoryException {
        String restrictionPath = path != null ? path : "/";
        JackrabbitAccessControlList acl = AccessControlUtils.getAccessControlList(acm, restrictionPath);
        this.restrictions = new LinkedHashSet<>();
        for (String restrictionName : acl.getRestrictionNames()) {
            if (acl.getRestrictionType(restrictionName) == PropertyType.UNDEFINED) {
                LOG.warn("Could not recognize a type of restriction: {}", restrictionName);
            } else {
                restrictions.add(restrictionName);
            }
        }
        this.privileges = Arrays.stream(acm.getSupportedPrivileges(path))
                .map(Privilege::getName)
                .sorted()
                .collect(toCollection(TreeSet::new));
    }

    public Set<String> getRestrictions() {
        return restrictions;
    }

    public Set<String> getPrivileges() {
        return privileges;
    }

}