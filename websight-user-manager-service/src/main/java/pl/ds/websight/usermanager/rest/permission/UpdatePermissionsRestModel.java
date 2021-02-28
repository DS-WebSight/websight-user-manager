package pl.ds.websight.usermanager.rest.permission;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.usermanager.rest.AuthorizableBaseModel;
import pl.ds.websight.usermanager.rest.requestparameters.Action;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.validation.constraints.NotEmpty;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Model(adaptables = SlingHttpServletRequest.class)
public class UpdatePermissionsRestModel extends AuthorizableBaseModel implements Validatable {

    private static final Logger LOG = LoggerFactory.getLogger(UpdatePermissionsRestModel.class);
    private static final Splitter KEY_VALUE_SEPARATOR = Splitter.on(":").limit(2);
    private static final Splitter.MapSplitter CHANGELOG_SPLITTER = Splitter.on(',').withKeyValueSeparator(KEY_VALUE_SEPARATOR);

    @RequestParameter
    @NotEmpty(message = "Changelog must be defined for at least one path")
    private final List<String> changelog = Collections.emptyList();

    private List<UpdatePermissionsRestModel.Record> records;

    @PostConstruct
    private void init() {
        try {
            records = changelog.stream()
                    .map(CHANGELOG_SPLITTER::split)
                    .map(UpdatePermissionsRestModel.Record::new)
                    .filter(record -> StringUtils.isNotBlank(record.getPath()))
                    .collect(toList());
        } catch (IllegalArgumentException e) {
            LOG.warn("Could not parse changelog parameter {}", changelog, e);
        }
    }

    public List<UpdatePermissionsRestModel.Record> getRecords() {
        return records;
    }

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        validateAuthorizable(errors);
        if (records == null) {
            errors.add("changelog", changelog, "Changelog parameter has different syntax than [key:value,key:value]");
        } else if (changelog.size() > records.size()) {
            errors.add("changelog", changelog, "Path must be defined for every record");
        }
        return errors;
    }

    private void validateAuthorizable(Errors errors) {
        String authorizableId = getAuthorizableId();
        try {
            if (getAuthorizable() == null) {
                errors.add("authorizableId", authorizableId, "Authorizable " + authorizableId + " does not exist");
            }
        } catch (RepositoryException e) {
            LOG.warn("Could not fetch {} authorizable", authorizableId, e);
            errors.add("authorizableId", authorizableId, "Could not fetch " + authorizableId + " authorizable");
        }
    }

    public static class Record {

        private final String path;
        private final Map<Action, Boolean> actions = new EnumMap<>(Action.class);

        public Record(Map<String, String> recordKV) {
            path = recordKV.get("path");
            for (Action action : Action.values()) {
                String name = action.getName();
                if (recordKV.containsKey(name)) {
                    String updateValue = recordKV.get(name);
                    try {
                        actions.put(action, parseUpdateValue(updateValue));
                    } catch (IllegalArgumentException e) {
                        LOG.info("Skips '{}' action because it has invalid value '{}'", name, updateValue, e);
                    }
                }
            }
        }

        private static Boolean parseUpdateValue(String value) {
            return BooleanUtils.toBooleanObject(value, Boolean.TRUE.toString(), Boolean.FALSE.toString(), "null");
        }

        public String getPath() {
            return path;
        }

        public Map<Action, Boolean> getActions() {
            return actions;
        }

    }
}