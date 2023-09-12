import java.util.List;
import java.util.Objects;

public class Expression {
    ExpressionType type;
    List<String> terms;

    Expression(List<String> terms){
        this.terms = terms;
    }
    Expression(ExpressionType expressionType) {
        this.type = expressionType;
    }

    Expression(ExpressionType type, List<String> termsAndOps) {
        this(type);
        this.terms = termsAndOps;
    }


    public String toString() {
        return "Type: %s\nExpression: %s".formatted(this.type.toString(), Objects.isNull(this.terms)? "": this.terms);
    }

    public ExpressionType getType() {
        return type;
    }

    public List<String> getTerms() {
        return terms;
    }
}