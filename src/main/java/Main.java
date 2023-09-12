
import java.util.*;
import java.math.BigInteger;

public class Main {

    static String regNums = "-?\\d+";
    static String regVars = "[a-zA-Z]+";
    static String regAdditionOps = "[+\\-]";
    static String regProductOps = "[*/]";
    static ExpressionFactory expressionFactory = new ExpressionFactory();
    // static Map<String, Integer> varMap = new TreeMap<>();
    static Map<String, BigInteger> varMap = new TreeMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str;
        while (!(str = scanner.nextLine()).matches("/exit")) {
            if (str.trim().isEmpty()) {
                continue;
            }
            if (str.charAt(0) == '/') {
                if (str.matches("/help")) {
                    System.out.println("The program calculates the sum of numbers");
                } else if (str.matches("/mem")) {
                    System.out.println("Variables stored: \n\t" + varMap);
                } else {
                    System.out.println("Unknown command");
                }
            } else {

                Expression expression = expressionFactory.getExpression(str);
                switch (expression.getType()) {
                    case ARITHMETIC -> processArithmeticExpression(expression.getTerms()).ifPresent(System.out::println);
                    case ASSIGNMENT -> processAssignmentExpression(expression.getTerms());
                    case EMPTY -> System.out.println();
                    case INVALID -> System.out.println("Invalid Expression");
                }
            }
        }
        System.out.println("Bye!");
    }

    // public static Optional<Integer> processAritmeticExpression(List<String> termsAndOps) {
    public static Optional<BigInteger> processArithmeticExpression(List<String> termsAndOps) {
        Deque<String> operators = new ArrayDeque<>();
        Deque<String> postfix = new ArrayDeque<>();
        // Deque<Integer> output = new ArrayDeque<>();
        Deque<BigInteger> output = new ArrayDeque<>();
        if (termsAndOps
                .stream()
                .filter(elem -> elem.matches(regVars) && !varMap.containsKey(elem))
                .findAny()
                .isPresent()) {
            System.out.println("Unknown variable");
            return Optional.ofNullable(null);
        }

        termsAndOps.forEach(elem -> {
            if (elem.matches(regNums) || elem.matches(regVars)) {
                String val = elem.matches(regVars) ? String.valueOf(varMap.get(elem)) : elem;
                postfix.offerLast(val);
            } else if (elem.matches(regAdditionOps)) {
                while (!operators.isEmpty() && operators.peekLast().matches(regProductOps)) { //"[*/]"
                    postfix.offerLast(operators.pollLast());
                }
                if (!operators.isEmpty() && operators.peekLast().matches(regAdditionOps)) {
                    postfix.offerLast(operators.pollLast());
                }
                operators.offerLast(elem);
            } else if (elem.matches(regProductOps)) { //"[*/]"
                if (!operators.isEmpty() && operators.peekLast().matches(regProductOps)) {
                    postfix.offerLast(operators.pollLast());
                }
                operators.offerLast(elem);
            } else if (elem.matches("\\)")) {
                while (!operators.isEmpty() && !operators.peekLast().matches("\\(")) {
                    postfix.offerLast(operators.pollLast());
                }
                if (!operators.isEmpty() && operators.peekLast().matches("\\(")) {
                    operators.pollLast();
                }
            } else if (elem.matches("\\(")) {
                operators.offerLast(elem);
            }
        });

        while (!operators.isEmpty()) {
            postfix.offerLast(operators.pollLast());
        }

        while (!postfix.isEmpty()) {
            // System.out.println("POSTFIX: "+ postfix);

            /**
             * the conditionals could be more specific in case there is an invalid symbol
             * that could not be detected before.
             */

            if (postfix.peekFirst().matches(regNums)) {
                // output.offerFirst(Integer.parseInt(postfix.pop()));
                output.offerFirst(new BigInteger(postfix.pop()));
            } else {
                //int operand2 = output.pop();
                // int operand1 = output.peekFirst() == null ? 0 : output.pop();
                // int tempRes;
                BigInteger operand2 = output.pop();
                BigInteger operand1 = output.peekFirst() == null ? BigInteger.ZERO : output.pop();
                BigInteger tempRes;
                switch (postfix.pop()) {
                    case "+" -> tempRes = operand1.add(operand2); // operand1 + operand2
                    case "-" -> tempRes = operand1.subtract(operand2); // operand1 - operand2
                    case "/" -> {
                        if (!BigInteger.ZERO.equals(operand2)) {
                            tempRes = operand1.divide(operand2); // operand1 / operand2
                        } else {
                            System.out.println("Division by Zero!");
                            return Optional.ofNullable(null);
                        }
                    }
                    case "*" -> tempRes = operand1.multiply(operand2); // operand1 * operand2
                    default -> {
                        System.out.println("Invalid expression");
                        return Optional.ofNullable(null);
                    }
                }
                output.push(tempRes);
                // System.out.println("OUTPUT: "+output);
            }

        }
        return Optional.of(output.getFirst());
    }

    public static void processAssignmentExpression(List<String> termsAndOps) {
        String leftSide = termsAndOps.get(0);
        // Optional<BigInteger> rightSide = processAritmeticExpression(termsAndOps.subList(2, termsAndOps.size()));
        Optional<BigInteger> rightSide = processArithmeticExpression(termsAndOps.subList(2, termsAndOps.size()));
        if (rightSide.isPresent()) {
            // int rs = rightSide.get();
            BigInteger rs = rightSide.get();
            varMap.put(leftSide, rs);
        }
    }
}