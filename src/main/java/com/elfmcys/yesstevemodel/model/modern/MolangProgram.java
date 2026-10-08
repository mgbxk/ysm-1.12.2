package com.elfmcys.yesstevemodel.model.modern;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.mclib.math.Constant;
import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.functions.Function;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Compiled numeric Molang scripts. ASTs contain no player or invocation state. */
public final class MolangProgram {
    public abstract static class Environment {
        private int depth, steps;
        public abstract double read(String name);
        public Object readValue(String name) { return read(name); }
        public boolean isDefined(String name) { return true; }
        public abstract void write(String name, double value);
        public abstract double function(String name, double[] arguments);
        public abstract double secondOrder(String key, double value, double frequency, double damping, double response);
        void step() { if (++steps > 10000) throw new IllegalStateException("Molang execution budget exceeded"); }
    }
    private interface Node { Object get(Frame frame); }
    private static final class Frame {
        final Environment environment;
        final double[] args;
        final Map<String, Double> temporary = new HashMap<>();
        Frame(Environment environment, double[] args) { this.environment = environment; this.args = args; }
        Object eval(Node node) { environment.step(); return node.get(this); }
        double number(Node node) { return number(eval(node)); }
        static double number(Object value) { return value instanceof Number ? ((Number) value).doubleValue() : 0; }
        Object read(String name) { return name.startsWith("temp.") ? temporary.getOrDefault(name, 0d) : environment.readValue(name); }
        boolean isDefined(String name) { return name.startsWith("temp.") ? temporary.containsKey(name) : environment.isDefined(name); }
        void write(String name, double value) {
            if (!Double.isFinite(value)) value = 0;
            if (name.startsWith("temp.")) temporary.put(name, value);
            else if (name.startsWith("variable.")) environment.write(name, value);
            else throw new IllegalArgumentException("Assignment requires a variable: " + name);
        }
    }
    private static final class Variable implements Node {
        final String name;
        Variable(String name) { this.name = canonical(name); }
        @Override public Object get(Frame frame) { return name.equals("args") ? frame.args : frame.read(name); }
    }
    private static final class Flow extends RuntimeException {
        final String kind; final double value;
        Flow(String kind, double value) { super(null, null, false, false); this.kind = kind; this.value = value; }
    }
    private final Node root;
    private MolangProgram(Node root) { this.root = root; }
    public static MolangProgram compile(String source, MolangParser parser) {
        Compiler compiler = new Compiler(source, parser);
        Node root = compiler.sequence("");
        compiler.expect("");
        return new MolangProgram(root);
    }
    public double evaluate(Environment environment, double... args) {
        if (environment.depth == 0) environment.steps = 0;
        if (++environment.depth > 32) { --environment.depth; throw new IllegalStateException("Molang call depth exceeds 32"); }
        try {
            return Frame.number(root.get(new Frame(environment, args)));
        } catch (Flow flow) {
            if (!flow.kind.equals("return")) throw new IllegalStateException(flow.kind + " outside loop");
            return flow.value;
        } finally { --environment.depth; }
    }
    public static String canonical(String name) {
        name = name.toLowerCase(Locale.ROOT);
        if (name.startsWith("q.")) return "query." + name.substring(2);
        if (name.startsWith("v.")) return "variable." + name.substring(2);
        if (name.startsWith("t.")) return "temp." + name.substring(2);
        return name;
    }
    private static boolean truth(double value) { return value != 0 && Double.isFinite(value); }

    private static final class Token {
        final String text; final Object literal;
        Token(String text, Object literal) { this.text = text; this.literal = literal; }
    }
    private static final class Compiler {
        final String source; final MolangParser parser;
        final List<Token> tokens = new ArrayList<>(); int index;
        Compiler(String source, MolangParser parser) { this.source = source; this.parser = parser; tokenize(); }
        IllegalArgumentException error(String message) { return new IllegalArgumentException(message + " at token " + index); }
        String peek() { return tokens.get(index).text; }
        boolean take(String text) { if (peek().equals(text)) { ++index; return true; } return false; }
        void expect(String text) { if (!take(text)) throw error("Expected '" + text + "', found '" + peek() + "'"); }
        void tokenize() {
            for (int i = 0; i < source.length();) {
                char c = source.charAt(i);
                if (Character.isWhitespace(c)) { i++; continue; }
                if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                    while (i < source.length() && source.charAt(i) != '\n') i++;
                    continue;
                }
                if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                    int end = source.indexOf("*/", i + 2); if (end < 0) throw error("Unterminated comment"); i = end + 2; continue;
                }
                if (c == '\'' || c == '"') {
                    char quote = c; StringBuilder value = new StringBuilder(); boolean ended = false; i++;
                    while (i < source.length()) {
                        c = source.charAt(i++);
                        if (c == quote) { ended = true; break; }
                        if (c == '\\' && i < source.length()) c = source.charAt(i++);
                        value.append(c);
                    }
                    if (!ended) throw error("Unterminated string");
                    tokens.add(new Token("string", value.toString())); continue;
                }
                if (Character.isDigit(c) || c == '.' && i + 1 < source.length() && Character.isDigit(source.charAt(i + 1))) {
                    int start = i++;
                    while (i < source.length() && (Character.isDigit(source.charAt(i)) || source.charAt(i) == '.')) i++;
                    if (i < source.length() && (source.charAt(i) == 'e' || source.charAt(i) == 'E')) {
                        i++; if (i < source.length() && (source.charAt(i) == '+' || source.charAt(i) == '-')) i++;
                        while (i < source.length() && Character.isDigit(source.charAt(i))) i++;
                    }
                    tokens.add(new Token("number", Double.parseDouble(source.substring(start, i)))); continue;
                }
                if (Character.isLetter(c) || c == '_') {
                    int start = i++;
                    while (i < source.length() && (Character.isLetterOrDigit(source.charAt(i)) || source.charAt(i) == '_' || source.charAt(i) == '.')) i++;
                    tokens.add(new Token(source.substring(start, i).toLowerCase(Locale.ROOT), null)); continue;
                }
                String two = i + 1 < source.length() ? source.substring(i, i + 2) : "";
                if (two.equals("==") || two.equals("!=") || two.equals("<=") || two.equals(">=") || two.equals("&&") || two.equals("||") || two.equals("??")) {
                    tokens.add(new Token(two, null)); i += 2; continue;
                }
                if ("+-*/%^!<>=?:,;(){}[]".indexOf(c) < 0) throw error("Unsupported character: " + c);
                tokens.add(new Token(String.valueOf(c), null)); i++;
            }
            tokens.add(new Token("", null));
        }
        Node sequence(String end) {
            List<Node> statements = new ArrayList<>();
            while (!peek().equals(end) && !peek().isEmpty()) {
                if (take(";")) continue;
                if (take("return")) { Node value = expression(); statements.add(f -> { throw new Flow("return", f.number(value)); }); }
                else if (take("break")) statements.add(f -> { throw new Flow("break", 0); });
                else if (take("continue")) statements.add(f -> { throw new Flow("continue", 0); });
                else statements.add(expression());
                take(";");
            }
            return f -> { Object value = 0d; for (Node node : statements) value = f.eval(node); return value; };
        }
        Node expression() {
            Node left = conditional();
            if (take("=")) {
                if (!(left instanceof Variable)) throw error("Assignment requires a variable");
                String name = ((Variable) left).name; Node right = expression();
                return f -> { double value = f.number(right); f.write(name, value); return value; };
            }
            return left;
        }
        Node conditional() {
            Node condition = coalesce();
            if (!take("?")) return condition;
            Node yes = expression(); Node no = take(":") ? expression() : f -> 0d;
            return f -> truth(f.number(condition)) ? f.eval(yes) : f.eval(no);
        }
        Node coalesce() {
            Node left = binary(0);
            if (!take("??")) return left;
            Node right = coalesce();
            return f -> left instanceof Variable && !f.isDefined(((Variable) left).name) ? f.eval(right) : f.eval(left);
        }
        private static final String[][] OPS = {{"||"}, {"&&"}, {"==", "!="}, {"<", "<=", ">", ">="}, {"+", "-"}, {"*", "/", "%"}, {"^"}};
        Node binary(int level) {
            if (level == OPS.length) return unary();
            Node value = binary(level + 1);
            while (java.util.Arrays.asList(OPS[level]).contains(peek())) {
                String op = peek(); index++; Node left = value, right = binary(level + (op.equals("^") ? 0 : 1));
                value = f -> {
                    Object first = f.eval(left);
                    double a = Frame.number(first);
                    if (op.equals("&&")) return truth(a) && truth(f.number(right)) ? 1d : 0d;
                    if (op.equals("||")) return truth(a) || truth(f.number(right)) ? 1d : 0d;
                    Object second = f.eval(right);
                    double b = Frame.number(second);
                    if ((op.equals("==") || op.equals("!=")) && (first instanceof String || second instanceof String)) {
                        boolean equal = first instanceof String && second instanceof String && first.equals(second);
                        return equal == op.equals("==") ? 1d : 0d;
                    }
                    switch (op) {
                        case "+": return a + b; case "-": return a - b; case "*": return a * b;
                        case "/": return b == 0 ? 0d : a / b; case "%": return b == 0 ? 0d : a % b; case "^": return Math.pow(a, b);
                        case "==": return a == b ? 1d : 0d; case "!=": return a != b ? 1d : 0d;
                        case "<": return a < b ? 1d : 0d; case "<=": return a <= b ? 1d : 0d;
                        case ">": return a > b ? 1d : 0d; case ">=": return a >= b ? 1d : 0d;
                        default: throw new IllegalStateException(op);
                    }
                };
            }
            return value;
        }
        Node unary() {
            if (take("-")) { Node value = unary(); return f -> -f.number(value); }
            if (take("+")) return unary();
            if (take("!")) { Node value = unary(); return f -> truth(f.number(value)) ? 0d : 1d; }
            return primary();
        }
        Node primary() {
            if (take("(")) { Node value = expression(); expect(")"); return value; }
            if (take("{")) { Node value = sequence("}"); expect("}"); return value; }
            Token token = tokens.get(index++);
            if (token.literal != null) return f -> token.literal;
            if (token.text.isEmpty() || !Character.isLetter(token.text.charAt(0)) && token.text.charAt(0) != '_') throw error("Expected expression");
            String name = token.text;
            if (take("(")) {
                List<Node> args = new ArrayList<>();
                if (!peek().equals(")")) do { args.add(expression()); } while (take(","));
                expect(")"); return call(name, args);
            }
            if (name.startsWith("fn.")) return call(name, new ArrayList<>());
            Node value = new Variable(name);
            if (take("[")) {
                if (!name.equals("args")) throw error("Only args can be indexed");
                Node position = expression(); expect("]");
                return f -> { double p = f.number(position); int n = (int) p; return p == n && n >= 0 && n < f.args.length ? f.args[n] : 0d; };
            }
            if (name.equals("true")) return f -> 1d;
            if (name.equals("false")) return f -> 0d;
            return value;
        }
        Node call(String name, List<Node> args) {
            name = canonical(name);
            final String functionName = name;
            if (name.equals("query.position_delta")) {
                if (args.size() != 1) throw error("position_delta requires one axis");
                return f -> { double axis = f.number(args.get(0)); return axis == (int) axis && axis >= 0 && axis <= 2 ? f.environment.read(functionName + "." + (int) axis) : 0d; };
            }
            if (name.equals("loop")) {
                if (args.size() != 2) throw error("loop requires count and body");
                return f -> { int count = Math.min(1024, Math.max(0, (int) f.number(args.get(0)))); double value = 0;
                    for (int n = 0; n < count; n++) try { value = f.number(args.get(1)); }
                    catch (Flow flow) { if (flow.kind.equals("break")) break; if (!flow.kind.equals("continue")) throw flow; }
                    return value;
                };
            }
            if (name.equals("for_each")) {
                if (args.size() != 3 || !(args.get(0) instanceof Variable)) throw error("for_each requires variable, args, body");
                return f -> { Object array = f.eval(args.get(1)); if (!(array instanceof double[])) throw new IllegalArgumentException("for_each requires args"); double value = 0;
                    for (double element : (double[]) array) { f.write(((Variable) args.get(0)).name, element);
                        try { value = f.number(args.get(2)); } catch (Flow flow) { if (flow.kind.equals("break")) break; if (!flow.kind.equals("continue")) throw flow; }
                    } return value;
                };
            }
            if (name.startsWith("fn.")) return f -> f.environment.function(functionName.substring(3), numbers(f, args));
            if (name.equals("ysm.second_order")) {
                if (args.size() != 5) throw error("second_order requires key, input, frequency, damping, response");
                return f -> f.environment.secondOrder(String.valueOf(f.eval(args.get(0))), f.number(args.get(1)), f.number(args.get(2)), f.number(args.get(3)), f.number(args.get(4)));
            }
            if (name.startsWith("ctrl.")) {
                if (args.size() != 2) throw error("ctrl query requires two arguments");
                return f -> f.environment.read(functionName + "('" + f.eval(args.get(0)) + "','" + f.eval(args.get(1)) + "')");
            }
            Class<? extends Function> type = parser.functions.get(name);
            if (type == null) throw error("Unknown function: " + name);
            final Constructor<? extends Function> constructor;
            try { constructor = type.getConstructor(IValue[].class, String.class); }
            catch (Exception e) { throw error("Invalid numeric function: " + name); }
            return f -> {
                double[] values = numbers(f, args); IValue[] inputs = new IValue[values.length];
                for (int i = 0; i < inputs.length; i++) inputs[i] = new Constant(values[i]);
                try { return constructor.newInstance(inputs, functionName).get(); }
                catch (ReflectiveOperationException e) { throw new IllegalArgumentException("Invalid function arguments: " + functionName, e); }
            };
        }
        static double[] numbers(Frame frame, List<Node> nodes) {
            double[] values = new double[nodes.size()]; for (int i = 0; i < values.length; i++) values[i] = frame.number(nodes.get(i)); return values;
        }
    }
}
