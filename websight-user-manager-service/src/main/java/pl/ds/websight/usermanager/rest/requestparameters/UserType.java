package pl.ds.websight.usermanager.rest.requestparameters;

public enum UserType {

    ALL("all"), REGULAR("regular"), SYSTEM("system");

    private final String type;

    UserType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
