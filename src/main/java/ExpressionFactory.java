import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionFactory {

    static String inAdditionOp = "[+\\-\s]+";
    static String inProdOp = "[*/]+";
    static String inNumber = "\\d+";
    static String inVar = "[a-zA-Z]+";
    static String inParent = "[()]";

    public static Expression getExpression(String inputstr) {

        List<String> termsAndOperators = prepareInputStr(inputstr, inAdditionOp, inProdOp, inNumber, inVar, inParent);

        if (Objects.isNull(termsAndOperators)) {
            return new Expression(ExpressionType.INVALID);
        } else if (termsAndOperators.isEmpty()) {
            return new Expression(ExpressionType.EMPTY);
        } else {
            if (termsAndOperators.contains("=")) {
                Expression exp = new Expression(ExpressionType.ASSIGNMENT, termsAndOperators);
                return exp;
            } else {
                Expression exp = new Expression(ExpressionType.ARITHMETIC, termsAndOperators);
                return exp;
            }
        }
    }

    public static List<String> prepareInputStr(String str, String rgxAdditionOps, String rgxProductOps, String rgxNumbers, String rgxVars, String inBrackets) {

        List<String> groups = new ArrayList<>();
        Pattern patternAddOps = Pattern.compile("^" + rgxAdditionOps);
        Pattern patternProdOps = Pattern.compile("^" + rgxProductOps);
        Pattern patternNumbers = Pattern.compile("^" + rgxNumbers + "\\b");
        Pattern patternVars = Pattern.compile("^" + rgxVars);
        Pattern patternParentheses = Pattern.compile("^" + inBrackets); //"[()]");
        Deque<String> parentheses = new ArrayDeque<>();
        Matcher op;
        Matcher num;
        Matcher var;
        Matcher parent;
        str = str.trim();

        if (str.isEmpty()) {
            return List.of();
        }

        if (Pattern.compile(rgxAdditionOps + "$").matcher(str).find()
                || Pattern.compile(rgxProductOps + "$").matcher(str).find()
                || Pattern.compile("=$").matcher(str).find()
                || str.indexOf("=") != str.lastIndexOf("=")) {
            return null;
        }

        int eqIndex = -1;
        int lastIndex = str.lastIndexOf("=");

        if ((eqIndex = str.indexOf("=")) != -1 && eqIndex == lastIndex) {
            String variable = "^" + rgxVars + "$";
            String[] assignment = str.split("=");
            assignment[0] = assignment[0].trim();
            if (!Pattern.compile(variable).matcher(assignment[0]).find() || assignment.length != 2) {
                return null;
            }

            List<String> rightSide = prepareInputStr(assignment[1], inAdditionOp, inProdOp, inNumber, rgxVars, inBrackets);
            if (Objects.isNull(rightSide) || rightSide.isEmpty()) {
                return null;
            } else {
                groups.add(assignment[0]);
                groups.add("=");
                groups.addAll(rightSide);
                return groups;
            }
        }

        while (str.length() > 0) {
            int index = 0;
            String found;
            int foundlen;
            if ((num = patternNumbers.matcher(str)).find()) {
                found = num.group();
                foundlen = found.length();
            } else if ((op = patternAddOps.matcher(str)).find()) {
                found = op.group();
                foundlen = found.length();
                found = found.chars().filter(c -> c == '-').count() % 2 == 0 ? String.valueOf('+') : "-";
            } else if ((op = patternProdOps.matcher(str)).find()) {
                found = op.group();
                foundlen = found.length();
                //if (!found.replaceAll("\\s", "").matches("(.)\\1*")) {
                if (found.replaceAll("\\s", "").length() > 1) {
                    return null;
                }
            } else if ((var = patternVars.matcher(str)).find()) {
                found = var.group();
                foundlen = found.length();
            } else if ((parent = patternParentheses.matcher(str)).find()) {
                found = parent.group();
                foundlen = found.length();
                found = found.trim();

                if (found.matches("\\(")) {
                    parentheses.offerLast(found);
                } else {
                    String lastChar = parentheses.peekLast();
                    if (Objects.isNull(lastChar)) {
                        return null;
                    }
                    parentheses.pollLast();
                }
            } else {
                return null;
            }
            groups.add(found);
            str = str.substring(index + foundlen).trim();
        }
        if (!parentheses.isEmpty()) return null;
        // System.out.println(groups);
        return groups;
    }


    /*public static void main(String[] args) {
        ExpressionFactory fact = new ExpressionFactory();
        List<String> tests = List.of("5+9", " 5 ++++9", "5****6", "5*6", "5 *6", "5* 6", "m=52", "m=2+6/3", "m=-32", "m = 25 + var / 6 ");
        tests.forEach(input -> System.out.println(input + ": \n" + getExpression(input) + "\n"));

    }*/
}
