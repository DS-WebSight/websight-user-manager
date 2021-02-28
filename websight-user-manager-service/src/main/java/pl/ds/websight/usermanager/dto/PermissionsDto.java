package pl.ds.websight.usermanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionsDto {

    private final String name;
    private final String path;
    private final Map<String, Boolean> actions;
    private final Map<String, Rules> declaredActions;
    private final boolean hasChildren;

    private List<PermissionsDto> children;

    public PermissionsDto(String name, String path, boolean hasChildren, Map<String, Boolean> actions, Map<String, Rules> declaredActions) {
        this.name = name;
        this.path = path;
        this.hasChildren = hasChildren;
        this.declaredActions = declaredActions;
        this.actions = new HashMap<>();
        actions.entrySet().forEach(entry -> this.actions.put(entry.getKey(), mapUndeclaredFalse(entry)));
    }

    private Boolean mapUndeclaredFalse(Map.Entry<String, Boolean> entry) {
        boolean value = entry.getValue();
        if (!value && !isDeclared(entry.getKey())) {
            return null; // NOSONAR
        }
        return value;
    }

    private boolean isDeclared(String actionName) {
        return declaredActions.containsKey(actionName) && !declaredActions.get(actionName).getEffective().isEmpty();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    @JsonInclude
    public Map<String, Boolean> getActions() {
        return actions;
    }

    public List<PermissionsDto> getChildren() {
        return children;
    }

    public void addChild(PermissionsDto child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public Map<String, Rules> getDeclaredActions() {
        return declaredActions;
    }

    public static class Rules {

        private final Set<Rule> effective;
        private final Set<Rule> ineffective;

        public Rules() {
            effective = new LinkedHashSet<>();
            ineffective = new LinkedHashSet<>();
        }

        public Set<Rule> getEffective() {
            return effective;
        }

        public Set<Rule> getIneffective() {
            return ineffective;
        }

    }

    public static class Rule {

        private final String authorizableId;
        private final Map<String, List<String>> restrictions;
        private final boolean isAllow;
        private final boolean isGroup;

        public Rule(String authorizableId, boolean isAllow, boolean isGroup, Map<String, List<String>> restrictions) {
            this.authorizableId = authorizableId;
            this.isAllow = isAllow;
            this.restrictions = restrictions;
            this.isGroup = isGroup;
        }

        public String getAuthorizableId() {
            return authorizableId;
        }

        public boolean isAllow() {
            return isAllow;
        }

        public boolean isGroup() {
            return isGroup;
        }

        public Map<String, List<String>> getRestrictions() {
            return restrictions;
        }

    }

}
