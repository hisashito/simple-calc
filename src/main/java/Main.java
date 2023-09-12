
import java.util.*;

public class Main {

    static String regNums = "-?\\d+";
    static String regVars = "[a-zA-Z]+";
    static String regAdditionOps = "[+\\-]";
    static String regProductOps = "[*/]";
    static ExpressionFactory expressionFactory = new ExpressionFactory();
    static Map<String, Integer> varMap = new TreeMap<>();

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
                    case ARITHMETIC -> processAritmeticExpression(expression.getTerms()).ifPresent(System.out::println);
                    case ASSIGNMENT -> processAssignmentExpression(expression.getTerms());
                    case EMPTY -> System.out.println();
                    case INVALID -> System.out.println("Invalid Expression");
                }
            }
        }
        System.out.println("Bye!");
    }

    public static Optional<Integer> processAritmeticExpression(List<String> termsAndOps) {
        Deque<String> operators = new ArrayDeque<>();
        Deque<String> postfix = new ArrayDeque<>();
        Deque<Integer> output = new ArrayDeque<>();
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
            if (postfix.peekFirst().matches(regNums)) {
                output.offerFirst(Integer.parseInt(postfix.pop()));
            } else {
                int operand2 = output.pop();

                int operand1 = output.peekFirst() == null ? 0 : output.pop();
                int tempRes;
                switch (postfix.pop()) {
                    case "+" -> tempRes = operand1 + operand2;
                    case "-" -> tempRes = operand1 - operand2;
                    case "/" -> tempRes = operand1 / operand2;
                    case "*" -> tempRes = operand1 * operand2;
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
        Optional<Integer> rightSide = processAritmeticExpression(termsAndOps.subList(2, termsAndOps.size()));
        if (rightSide.isPresent()) {
            int rs = rightSide.get();
            varMap.put(leftSide, rs);
        }
    }
}