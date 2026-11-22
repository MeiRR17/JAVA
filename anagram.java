import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Anagram comparison and offline grouping exercise, Java 17+.
 *
 * Usage:
 *   java anagram                  (interactive, two prompts)
 *   java anagram --compare "rail safety" "fairy tales"
 *   java anagram --groups words.txt
 *
 * Unicode letters and digits are compared case-insensitively after NFKC
 * normalization. Whitespace and punctuation are ignored; accents are kept.
 */
public final class anagram {
    private static final int MAX_LINE_CODEPOINTS = 512;
    private static final int MAX_ROWS = 50_000;
    private static final long MAX_FILE_BYTES = 8L * 1024 * 1024;

    private anagram() {}

    /** Returns the canonical signature of a word or phrase, or "" when empty. */
    public static String signature(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
        int[] letters = normalized.codePoints()
                .filter(Character::isLetterOrDigit)
                .toArray();
        if (letters.length > MAX_LINE_CODEPOINTS) {
            throw new IllegalArgumentException("Text exceeds 512 letters or digits");
        }
        Arrays.sort(letters);
        StringBuilder result = new StringBuilder(letters.length);
        for (int letter : letters) {
            result.appendCodePoint(letter);
        }
        return result.toString();
    }

    /** Compatible with the original exercise's method name. */
    public static boolean checkIfStringsHaveSameLetters(String first, String second) {
        String a = signature(first);
        String b = signature(second);
        return !a.isEmpty() && a.equals(b);
    }

    private static void compare(String first, String second) {
        if (checkIfStringsHaveSameLetters(first, second)) {
            System.out.println("The strings are anagrams.");
        } else {
            System.out.println("The strings are not anagrams.");
        }
    }

    private static void interactive() {
        Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
        System.out.print("Enter the first string: ");
        if (!input.hasNextLine()) {
            throw new IllegalArgumentException("No first string provided");
        }
        String first = input.nextLine();
        System.out.print("Enter the second string: ");
        if (!input.hasNextLine()) {
            throw new IllegalArgumentException("No second string provided");
        }
        compare(first, input.nextLine());
    }

    /** Groups distinct entries without modifying the original UTF-8 file. */
    public static List<List<String>> findGroups(Path path) throws IOException {
        if (Files.isSymbolicLink(path)
                || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                || Files.size(path) > MAX_FILE_BYTES) {
            throw new IllegalArgumentException(
                    "Input must be a regular UTF-8 file of at most 8 MiB");
        }

        Map<String, Set<String>> groups = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lines = 0;
            while ((line = reader.readLine()) != null) {
                if (++lines > MAX_ROWS) {
                    throw new IllegalArgumentException("Input exceeds 50,000 lines");
                }
                String value = line.strip();
                if (value.isEmpty()) {
                    continue;
                }
                if (value.codePointCount(0, value.length()) > MAX_LINE_CODEPOINTS) {
                    throw new IllegalArgumentException(
                            "Line " + lines + " exceeds 512 Unicode characters");
                }
                String key = signature(value);
                if (!key.isEmpty()) {
                    groups.computeIfAbsent(key, ignored -> new LinkedHashSet<>()).add(value);
                }
            }
        }

        List<Map.Entry<String, Set<String>>> collected = new ArrayList<>(groups.entrySet());
        collected.removeIf(entry -> entry.getValue().size() < 2);
        collected.sort(
                Comparator.<Map.Entry<String, Set<String>>>comparingInt(
                        entry -> entry.getValue().size()).reversed()
                        .thenComparing(Map.Entry::getKey)
        );
        List<List<String>> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : collected) {
            result.add(List.copyOf(entry.getValue()));
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                interactive();
            } else if (args.length == 3 && "--compare".equals(args[0])) {
                compare(args[1], args[2]);
            } else if (args.length == 2 && "--groups".equals(args[0])) {
                List<List<String>> groups = findGroups(Path.of(args[1]));
                System.out.println("Anagram groups: " + groups.size());
                for (int index = 0; index < groups.size(); index++) {
                    System.out.println((index + 1) + ". " + String.join(" | ", groups.get(index)));
                }
            } else {
                System.err.println("Usage: java anagram [--compare <first> <second>"
                        + " | --groups <UTF-8-file>]");
                System.exit(2);
            }
        } catch (IllegalArgumentException | IOException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.exit(1);
        }
    }
}
