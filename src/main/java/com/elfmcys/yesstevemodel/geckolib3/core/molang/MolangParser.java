package com.elfmcys.yesstevemodel.geckolib3.core.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangAssignment;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangExpression;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangMultiStatement;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.expressions.MolangValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.functions.CosDegrees;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.functions.SinDegrees;
import com.elfmcys.yesstevemodel.mclib.math.Constant;
import com.elfmcys.yesstevemodel.mclib.math.IValue;
import com.elfmcys.yesstevemodel.mclib.math.MathBuilder;
import com.elfmcys.yesstevemodel.mclib.math.Variable;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MoLang 解析器
 * <a href="https://bedrock.dev/docs/1.19.0.0/1.19.30.23/Molang#Math%20Functions">Wiki</a>
 */
public class MolangParser extends MathBuilder {
    public static final Map<String, LazyVariable> VARIABLES = new Object2ObjectOpenHashMap<>();
    public static final MolangExpression ZERO = new MolangValue(null, new Constant(0));
    public static final MolangExpression ONE = new MolangValue(null, new Constant(1));
    public static final String RETURN = "return ";
    private static final Pattern STRING_QUERY = Pattern.compile("ctrl\\.(hold|swing|use|armor|ride)\\(\\s*'[^']*'\\s*,\\s*'[^']*'\\s*\\)|(?:query|q)\\.is_item_name_any\\(\\s*'[^']*'\\s*(?:,\\s*'[^']*'\\s*)+\\)");
    private final Map<String, String> stringQueryTokens = new java.util.HashMap<>();
    private final Map<String, IValue> stringQueryValues = new java.util.HashMap<>();
    private ToDoubleFunction<String> stringQueryResolver = call -> 0;

    public void setStringQueryResolver(ToDoubleFunction<String> resolver) { stringQueryResolver = resolver; }

    public double readNumericQuery(String name) {
        IValue stringQuery = stringQueryValues.get(name);
        if (stringQuery != null) return stringQuery.get();
        if (name.startsWith("ctrl.") && name.indexOf('(') >= 0) return stringQueryResolver.applyAsDouble(name);
        return getVariable(com.elfmcys.yesstevemodel.model.modern.MolangProgram.canonical(name)).get();
    }

    public boolean isQueryDefined(String name) {
        name = com.elfmcys.yesstevemodel.model.modern.MolangProgram.canonical(name);
        if (!name.startsWith("variable.") && !name.startsWith("temp.")) return true;
        LazyVariable value = VARIABLES.get(name);
        return value != null && value.isInitialized();
    }

    public Object readQueryValue(String name) {
        // These integrations have no state provider in the 1.12 port. Their idle
        // string state is empty, so comparing it with '' must not block normal actions.
        if (java.util.Arrays.asList("ctrl.carryon_type", "ctrl.parcool_state", "ctrl.swem_state",
                "ctrl.slashblade_animation", "ctrl.iss_animation").contains(name)) return "";
        return readNumericQuery(name);
    }

    @Override
    public IValue valueFromObject(Object object) throws Exception {
        IValue value = stringQueryValues.get(object);
        if (value == null && object instanceof String && ((String) object).startsWith("-")) {
            IValue positive = stringQueryValues.get(((String) object).substring(1));
            if (positive != null) return new com.elfmcys.yesstevemodel.mclib.math.Negative(positive);
        }
        return value == null ? super.valueFromObject(object) : value;
    }

    private String prepareStringQueries(String expression) {
        Matcher matcher = STRING_QUERY.matcher(expression);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            final String call = matcher.group();
            String token = stringQueryTokens.get(call);
            if (token == null) {
                token = "__ysm_string_query_" + stringQueryTokens.size();
                stringQueryTokens.put(call, token);
                // Resolve against the player currently being rendered, rather than freezing
                // the item condition while the model's animation file is being loaded.
                stringQueryValues.put(token, () -> stringQueryResolver.applyAsDouble(call));
            }
            matcher.appendReplacement(output, token);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public MolangParser() {
        super();
        // 将函数重新映射为 MoLang 标准名
        this.doCoreRemaps();
    }

    private void doCoreRemaps() {
        // 将 sin 和 cos 改成角度参数
        this.functions.put("cos", CosDegrees.class);
        this.functions.put("sin", SinDegrees.class);

        this.remap("abs", "math.abs");
        this.remap("acos", "math.acos");
        this.remap("asin", "math.asin");
        this.remap("atan", "math.atan");
        this.remap("atan2", "math.atan2");
        this.remap("ceil", "math.ceil");
        this.remap("clamp", "math.clamp");
        this.remap("cos", "math.cos");
        this.remap("roll", "math.die_roll");
        this.remap("rolli", "math.die_roll_integer");
        this.remap("exp", "math.exp");
        this.remap("floor", "math.floor");
        this.remap("hermite", "math.hermite_blend");
        this.remap("lerp", "math.lerp");
        this.remap("lerprotate", "math.lerprotate");
        this.remap("ln", "math.ln");
        this.remap("max", "math.max");
        this.remap("min", "math.min");
        this.remap("mod", "math.mod");
        this.remap("pi", "math.pi");
        this.remap("pow", "math.pow");
        this.remap("random", "math.random");
        this.remap("randomi", "math.random_integer");
        this.remap("round", "math.round");
        this.remap("sin", "math.sin");
        this.remap("sqrt", "math.sqrt");
        this.remap("trunc", "math.trunc");
    }

    @Override
    public void register(Variable variable) {
        if (!(variable instanceof LazyVariable)) {
            variable = LazyVariable.from(variable);
        }
        VARIABLES.put(variable.getName(), (LazyVariable) variable);
    }

    /**
     * 重映射方法
     */
    public void remap(String old, String newName) {
        this.functions.put(newName, this.functions.remove(old));
    }

    @Deprecated
    public void setValue(String name, double value) {
        this.setValue(name, () -> value);
    }

    public void setValue(String name, DoubleSupplier value) {
        LazyVariable variable = this.getVariable(name);
        if (variable != null) {
            variable.set(value);
        }
    }

    @Override
    protected LazyVariable getVariable(String name) {
        return VARIABLES.computeIfAbsent(name, key -> { LazyVariable value = new LazyVariable(key, 0); value.unset(); return value; });
    }

    public LazyVariable getVariable(String name, MolangMultiStatement currentStatement) {
        LazyVariable variable;
        if (currentStatement != null) {
            variable = currentStatement.locals.get(name);
            if (variable != null) {
                return variable;
            }
        }
        return this.getVariable(name);
    }

    public MolangExpression parseJson(JsonElement element) throws MolangException {
        if (!element.isJsonPrimitive()) {
            return ZERO;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isNumber()) {
            return new MolangValue(this, new Constant(primitive.getAsDouble()));
        }
        if (primitive.isString()) {
            String string = primitive.getAsString();
            try {
                return new MolangValue(this, new Constant(Double.parseDouble(string)));
            } catch (NumberFormatException ex) {
                return this.parseExpression(string);
            }
        }
        return ZERO;
    }

    /**
     * 解析一个 MoLang 表达式
     */
    public MolangExpression parseExpression(String expression) throws MolangException {
        expression = prepareStringQueries(expression);
        if (expression.indexOf('\'') >= 0 || expression.indexOf('"') >= 0 || expression.contains("??") || expression.matches("(?is).*(?:\\bfn\\.|ysm\\.second_order|\\b(?:query|q)\\.position_delta|[{}\\[\\]]|\\bloop\\s*\\(|\\bfor_each\\s*\\(|//|/\\*).*") ) {
            final String source = expression;
            try {
                com.elfmcys.yesstevemodel.model.modern.MolangProgram program = com.elfmcys.yesstevemodel.model.modern.MolangProgram.compile(source, this);
                java.util.concurrent.atomic.AtomicBoolean warned = new java.util.concurrent.atomic.AtomicBoolean();
                return new MolangValue(this, () -> {
                    try { return program.evaluate(com.elfmcys.yesstevemodel.model.modern.MolangRuntime.environment(this)); }
                    catch (RuntimeException e) {
                        if (warned.compareAndSet(false, true)) com.elfmcys.yesstevemodel.YesSteveModel.LOGGER.warn("Cannot execute model Molang {}: {}", source, e.getMessage());
                        return 0;
                    }
                });
            } catch (RuntimeException e) { throw new MolangException("Cannot compile Molang script: " + e.getMessage()); }
        }
        MolangMultiStatement result = null;
        expression = prepareStringQueries(expression).toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\bq\\.", "query.").replaceAll("\\bv\\.", "variable.")
                .replaceAll("\\bt\\.", "temp.");
        for (String split : expression.trim().split(";")) {
            String trimmed = split.trim();
            if (!trimmed.isEmpty()) {
                if (result == null) {
                    result = new MolangMultiStatement(this);
                }
                result.expressions.add(this.parseOneLine(trimmed, result));
            }
        }
        if (result == null) {
            throw new MolangException("Molang expression cannot be blank!");
        }
        return result;
    }

    /**
     * 解析单个 MoLang 表达式
     */
    protected MolangExpression parseOneLine(String expression, MolangMultiStatement currentStatement) throws MolangException {
        if (expression.startsWith(RETURN)) {
            try {
                return new MolangValue(this, this.parse(expression.substring(RETURN.length()))).addReturn();
            } catch (Exception e) {
                throw new MolangException("Couldn't parse return '" + expression + "' expression!");
            }
        }

        try {
            // 将表达式拆分
            List<Object> symbols = this.breakdownChars(this.breakdown(expression));
            // 如果是赋值表达式
            if (symbols.size() >= 3 && (symbols.get(0) instanceof String name) && this.isVariable(symbols.get(0)) && symbols.get(1).equals("=")) {
                symbols = symbols.subList(2, symbols.size());
                LazyVariable variable;
                if (name.startsWith("variable.") || name.startsWith("temp.")) {
                    variable = this.getVariable(name);
                } else if (!VARIABLES.containsKey(name) && !currentStatement.locals.containsKey(name)) {
                    currentStatement.locals.put(name, (variable = new LazyVariable(name, 0)));
                } else {
                    variable = this.getVariable(name, currentStatement);
                }
                return new MolangAssignment(this, variable, this.parseSymbolsMolang(symbols));
            }
            // 如果是其他表达式
            return new MolangValue(this, this.parseSymbolsMolang(symbols));
        } catch (Exception e) {
            throw new MolangException("Couldn't parse '" + expression + "' expression!");
        }
    }

    /**
     * 将 parseSymbols 方法包装，并抛出 MolangException
     */
    private IValue parseSymbolsMolang(List<Object> symbols) throws MolangException {
        try {
            return this.parseSymbols(symbols);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MolangException("Couldn't parse an expression!");
        }
    }

    /**
     * 拓展此方法，从而让 {@link #breakdownChars(String[])} 能够解析等号
     * 这样就能更加轻松解析赋值表达式
     */
    @Override
    protected boolean isOperator(String s) {
        return super.isOperator(s) || s.equals("=");
    }
}
