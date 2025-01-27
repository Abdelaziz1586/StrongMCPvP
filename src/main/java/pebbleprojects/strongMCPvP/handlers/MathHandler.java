package pebbleprojects.strongMCPvP.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MathHandler {

    public static MathHandler INSTANCE;
    private final Pattern pattern, parenthesesPattern;

    public MathHandler() {
        INSTANCE = this;

        parenthesesPattern = Pattern.compile("\\((([^()]+))\\)");
        pattern = Pattern.compile("\\d+\\.?\\d*\\s*[*/+-]\\s*\\d+\\.?\\d*");
    }

    public String processString(String input) {
        try {
            while (input.contains("(")) {
                final Matcher parenthesesMatcher = parenthesesPattern.matcher(input);

                if (parenthesesMatcher.find()) {
                    final String innerExpression = parenthesesMatcher.group(1);
                    input = input.substring(0, parenthesesMatcher.start()) +
                            evaluateSimpleExpression(innerExpression) +
                            input.substring(parenthesesMatcher.end());
                    continue;
                }

                break;
            }

            Matcher matcher = pattern.matcher(input);

            while (matcher.find()) {
                final String expression = matcher.group();
                input = input.substring(0, matcher.start()) +
                        evaluateSimpleExpression(expression) +
                        input.substring(matcher.end());
                matcher = pattern.matcher(input);
            }

            return input;
        } catch (final Exception e) {
            return "Error processing expression: " + e.getMessage();
        }
    }

    private double evaluateSimpleExpression(String expression) {
        expression = expression.trim();

        final String[] parts;
        final double result;

        if (expression.contains("+")) {
            parts = expression.split("\\+");
            result = Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
        } else if (expression.contains("-")) {
            parts = expression.split("-");
            result = Double.parseDouble(parts[0].trim()) - Double.parseDouble(parts[1].trim());
        } else if (expression.contains("*")) {
            parts = expression.split("\\*");
            result = Double.parseDouble(parts[0].trim()) * Double.parseDouble(parts[1].trim());
        } else if (expression.contains("/")) {
            parts = expression.split("/");
            if (Double.parseDouble(parts[1].trim()) == 0)
                throw new ArithmeticException("Division by zero");

            result = Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        } else {
            result = Double.parseDouble(expression);
        }

        return result;
    }
}
