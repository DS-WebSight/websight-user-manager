package pl.ds.websight.usermanager.rest.permission;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.oak.spi.security.authorization.accesscontrol.AccessControlConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

@Model(adaptables = SlingHttpServletRequest.class)
public class AclEntryRestModel extends PrincipalValidatableRestModel {

    private static final Logger LOG = LoggerFactory.getLogger(AclEntryRestModel.class);

    private static final CollectionType PRIVILEGES_TYPE = TypeFactory.defaultInstance().constructCollectionType(Set.class, String.class);
    private static final ObjectReader PRIVILEGES_READER = new ObjectMapper().readerFor(PRIVILEGES_TYPE);

    private static final CollectionType RESTRICTIONS_VALUES_TYPE = TypeFactory.defaultInstance().constructCollectionType(List.class,
            String.class);
    private static final MapType RESTRICTIONS_MAP_TYPE = TypeFactory.defaultInstance().constructMapType(Map.class,
            TypeFactory.defaultInstance().constructType(String.class), RESTRICTIONS_VALUES_TYPE);
    private static final ObjectReader RESTRICTIONS_READER = new ObjectMapper().enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .readerFor(RESTRICTIONS_MAP_TYPE);

    private static final String RESTRICTIONS_PARAM_NAME = "restrictions";
    private static final String PRIVILEGES_PARAM_NAME = "privileges";

    @RequestParameter
    @NotBlank(message = "Path cannot be blank")
    private String path;

    @RequestParameter(name = "allow")
    @Default(booleanValues = false)
    private boolean isAllow;

    @RequestParameter(name = RESTRICTIONS_PARAM_NAME)
    private String restrictionsJson;

    @RequestParameter(name = PRIVILEGES_PARAM_NAME)
    private String privilegesJson;

    private String[] privilegesNames;

    private Map<String, List<String>> restrictions;

    @PostConstruct
    protected void init() {
        this.privilegesNames = StringUtils.isNotBlank(privilegesJson) ?
                readPrivileges(privilegesJson).toArray(EMPTY_STRING_ARRAY) :
                EMPTY_STRING_ARRAY;
        this.restrictions = StringUtils.isNotBlank(restrictionsJson) ? readRestrictions(restrictionsJson) : Collections.emptyMap();
    }

    private static Set<String> readPrivileges(String privilegesJson) {
        try {
            return PRIVILEGES_READER.readValue(privilegesJson);
        } catch (IOException e) {
            LOG.warn("Could not read updated privileges", e);
        }
        return Collections.emptySet();
    }

    private static Map<String, List<String>> readRestrictions(String restrictionsJson) {
        try {
            return RESTRICTIONS_READER.readValue(restrictionsJson);
        } catch (IOException e) {
            LOG.warn("Could not read updated restrictions", e);
        }
        return Collections.emptyMap();
    }

    public String getPath() {
        return path;
    }

    public boolean isAllow() {
        return isAllow;
    }

    public String[] getPrivilegesNames() {
        return privilegesNames;
    }

    public Map<String, List<String>> getRestrictions() {
        return restrictions;
    }

    @Override
    public Errors validate() {
        Errors errors = super.validate();
        if (privilegesNames.length == 0) {
            errors.add(PRIVILEGES_PARAM_NAME, privilegesJson, "ACL Entry should contain at least one privilege");
        }
        if (!path.startsWith("/")) {
            errors.add("path", path, "Path for ACE cannot be relative");
        }
        validateRestrictions(errors);
        return errors;
    }

    private void validateRestrictions(Errors errors) {
        if (restrictions == null) {
            return;
        }
        for (Map.Entry<String, List<String>> restrictionEntry : restrictions.entrySet()) {
            String restriction = restrictionEntry.getKey();
            List<String> restrictionValues = restrictionEntry.getValue();
            if (AccessControlConstants.REP_GLOB.equals(restriction)) {
                if (restrictionValues == null || restrictionValues.size() > 1) {
                    errors.add(RESTRICTIONS_PARAM_NAME, restrictionValues,
                            AccessControlConstants.REP_GLOB + " should have exactly one value");
                }
            } else if (restrictionValues == null || restrictionValues.isEmpty() ||
                    restrictionValues.stream().anyMatch(StringUtils::isBlank)) {
                errors.add(RESTRICTIONS_PARAM_NAME, restrictionValues, restriction + " cannot have blank values");
            }
        }
    }

    public static String getRuleType(boolean isAllow) {
        return isAllow ? "ALLOW" : "DENY";
    }
}
