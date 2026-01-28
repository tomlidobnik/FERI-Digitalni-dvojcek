package si.um.feri.copycats.utils;

public enum MarkerType {
    FUN,
    SPORTS,
    EDUCATION,
    DEFAULT;

    public static MarkerType fromTag(String tag) {
        if (tag == null) return DEFAULT;

        switch (tag.toLowerCase()) {
            case "fun":
                return FUN;
            case "sports":
                return SPORTS;
            case "education":
                return EDUCATION;
            default:
                return DEFAULT;
        }
    }
}
