package io.github.tkdgh816.brevlet.domain.key;

import java.util.regex.Pattern;

public final class ShortenUrlKeyRule {
    public static final int KEY_LENGTH = 8;
    public static final String BASE_56_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
    public  static final String KEY_REGEX = "^[" + BASE_56_CHARS + "]{" + KEY_LENGTH + "}$";
    public static final Pattern KEY_PATTERN = Pattern.compile(KEY_REGEX);
}
