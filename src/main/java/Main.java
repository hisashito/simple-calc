

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static String regNums = "\\d+";
    static String regVars = "^[a-zA-Z]+"; // \\d*";
    static String regOps = "[\\+*\\s*\\-*\\s*]+";
    static String regWrongEq = "^=|([^=?]+=|=){2,}";
    static Map<String, Integer> varMap = new TreeMap<>();
    static String cmdHelp = "/help";
    static String cmdExit = "/exit";
    static String msgHelp = "The program calculates the sum of numbers";
    static String msgUnkCmd = "Unknown command";
    static String msgInvalidExpr = "Invalid expression";
    static String msgBye = "Bye!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str;
        while (!(str = scanner.nextLine()).matches(cmdExit)) {
            if(str.trim().isEmpty()){
                continue;
            }
            if (str.charAt(0)== '/'){
                if (str.matches(cmdHelp)) {
                    System.out.println(msgHelp);
                } else {
                    System.out.println(msgUnkCmd);
                }
            } else {
                Expression expression = preprocessInputStrVer2(str, regOps, regNums, regVars, regWrongEq);
                switch (expression.getType()) {
                    case ARITMETIC ->
                            processAritmeticExpression(expression.getTermsAndOperators()).ifPresent(System.out::println);
                    case ASSIGNMENT ->
                            processAssignmentExpression(expression.getTermsAndOperators());
                    case EMPTY ->
                            System.out.println();
                    case INVALID ->
                            System.out.println("Invalid Expression");
                }
            }
        }
        System.out.println(msgBye);
    }

    public static void processAssignmentExpression(List<String> termsAndOps) {
        String leftSide = termsAndOps.get(0);
        Optional<Integer> rightSide = processAritmeticExpression(termsAndOps.subList(2, termsAndOps.size()));
        if (rightSide.isPresent()) {
            varMap.put(leftSide, rightSide.get());
        }
    }

    public static Optional<Integer> processAritmeticExpression(List<String> termsAndOps) {
        Stack<String> operators = new Stack<>();
        int[] res = {0};
        if (termsAndOps
                .stream()
                .filter(elem -> elem.matches(regVars) && !varMap.containsKey(elem))
                .findAny()
                .isPresent()) {
            System.out.println("Unknown variable");
            return Optional.ofNullable(null);
        }
        termsAndOps.forEach(elem -> {
            if (elem.matches(regNums)) {
                if (operators.isEmpty()) {
                    res[0] += Integer.parseInt(elem);
                } else {
                    String op = operators.pop();
                    switch (op) {
                        case "+" -> res[0] += Integer.parseInt(elem);
                        case "-" -> res[0] -= Integer.parseInt(elem);
                    }
                }
            } else if (elem.matches(regOps)) {
                operators.push(elem);
            } else if (elem.matches(regVars)) {
                // System.out.println("var to be retreived:" + elem);
                int val = varMap.get(elem);
                if (operators.isEmpty()) {
                    res[0] += val;
                } else {
                    String op = operators.pop();
                    switch (op) {
                        case "+" -> res[0] += val;
                        case "-" -> res[0] -= val;
                    }
                }
            }
        });
        return Optional.of(res[0]);
    }

    public static Expression preprocessInputStrVer2(String str, String rgxOperators, String rgxNumbers, String rgxVars, String regWrongEq) {

        List<String> groups = new ArrayList<>();
        Pattern patternOperators = Pattern.compile("^" + rgxOperators);
        Pattern patternNumbers = Pattern.compile("^" + rgxNumbers + "\\b");
        Pattern patternVars = Pattern.compile("^" + rgxVars);
        Matcher op;
        Matcher num;
        Matcher var;
        str = str.trim();

        if (str.isEmpty()) {
            return new Expression(groups);
        }

        if (Pattern.compile(rgxOperators + "$").matcher(str).find() || Pattern.compile(regWrongEq).matcher(str).find()) {
            return new Expression();
        }

        int eqIndex = -1;
        int lastIndex = str.lastIndexOf("=");

        if ((eqIndex = str.indexOf("=")) != -1 && eqIndex == lastIndex) {
            String variable = "^" + rgxVars + "$";
            String[] assignment = str.split("=");
            assignment[0] = assignment[0].trim();
            if (!Pattern.compile(variable).matcher(assignment[0]).find() || assignment.length != 2) {
                return new Expression();
            }

            Expression rightSide = preprocessInputStrVer2(assignment[1], rgxOperators, rgxNumbers, rgxVars, regWrongEq);
            if (rightSide.getType().equals(Expression.TypeOfExpression.EMPTY) || rightSide.getType().equals(Expression.TypeOfExpression.INVALID)) {
                return new Expression();
            } else {
                groups.add(assignment[0]);
                groups.add("=");
                groups.addAll(rightSide.getTermsAndOperators());
                return new Expression(groups);
            }
        }

        while (str.length() > 0) {
            int index = 0;
            String found;
            int foundlen;
            if ((num = patternNumbers.matcher(str)).find()) {
                found = num.group();
                foundlen = found.length();
            } else if ((op = patternOperators.matcher(str)).find()) {
                found = op.group();
                foundlen = found.length();
                found = found.chars().filter(c -> c == '-').count() % 2 == 0 ? String.valueOf('+') : "-";
            } else if ((var = patternVars.matcher(str)).find()) {
                found = var.group();
                foundlen = found.length();
            } else {
                return new Expression();
            }
            groups.add(found);
            str = str.substring(index + foundlen).trim();
        }
        return new Expression(groups);
    }

    static class Expression {
        enum TypeOfExpression {
            ARITMETIC, EMPTY, ASSIGNMENT, INVALID
        }

        List<String> termsAndOperators;
        TypeOfExpression type;

        public Expression() {
            this.termsAndOperators = List.of();
            this.type = TypeOfExpression.INVALID;
        }

        public Expression(List<String> termsAndOperators) {
            this.termsAndOperators = termsAndOperators;
            if (Objects.isNull(termsAndOperators)) {
                this.termsAndOperators = List.of();
                this.type = TypeOfExpression.INVALID;
            } else if (termsAndOperators.isEmpty()) {
                this.type = TypeOfExpression.EMPTY;
            } else {
                this.type = termsAndOperators.contains("=") ? TypeOfExpression.ASSIGNMENT : TypeOfExpression.ARITMETIC;
            }
        }

        public List<String> getTermsAndOperators() {
            return termsAndOperators;
        }

        public TypeOfExpression getType() {
            return type;
        }
    }

}