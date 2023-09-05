

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static Pattern patNum = Pattern.compile("-?\\d+");
    static Pattern patOp = Pattern.compile("[\\+*\\s*\\-*\\s*]+");
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
                Optional<Integer> res = processInput2(preprocessInputStrVer2(str, patOp, patNum));
                res.ifPresentOrElse(
                        System.out::println,
                        ()->System.out.println(msgInvalidExpr));
            }
        }
        System.out.println(msgBye);
    }

    public static Optional<Integer> processInput2(List<String> expression) {
        if (Objects.isNull(expression)) return Optional.ofNullable(null);
        Stack<String> operators = new Stack<>();
        int[] res = {0};
        expression.forEach(elem -> {
            if (elem.matches("-?\\d+")) {
                if (operators.isEmpty()) {
                    res[0] += Integer.parseInt(elem);
                } else {
                    String op = operators.pop();
                    switch (op) {
                        case "+" -> res[0] += Integer.parseInt(elem);
                        case "-" -> res[0] -= Integer.parseInt(elem);
                    }
                }
            } else {
                operators.push(elem);
            }
        });
        return Optional.of(res[0]);
    }

    public static List<String> preprocessInputStrVer2(String str, Pattern patternOperators, Pattern patternNumbers) {
        List<String> groups = new ArrayList<>();
        Matcher op;
        Matcher num;
        str = str.trim();
        if (str.charAt(str.length()-1) == '+' || str.charAt(str.length()-1)== '-') {
            return null;
        }
        while (str.length() > 0) {
            int index = 0;
            String found;
            int foundlen;
            if ((num = patternNumbers.matcher(str)).find() && (index = num.start()) == 0) {
                found = num.group();
                foundlen = found.length();
            } else if ((op = patternOperators.matcher(str)).find() && (index = op.start()) == 0) {
                found = op.group();
                foundlen = found.length();
                found = found.chars().filter(c -> c == '-').count() % 2 == 0 ? String.valueOf('+') : "-";
            } else  {
                return null;
            }
            groups.add(found);
            str = str.substring(index + foundlen).trim();
        }
        return groups;
    }
}