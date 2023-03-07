package org.potassco.clingodl;

import org.junit.Assert;
import org.potassco.clingo.theory.Value;
import org.potassco.clingo.theory.ValueType;
import org.potassco.clingo.theory.ValueUnion;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.ProgramBuilder;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.control.LoggerCallback;
import org.potassco.clingo.solving.Model;
import org.potassco.clingo.solving.SolveHandle;
import org.potassco.clingo.solving.SolveMode;
import org.potassco.clingo.symbol.Function;
import org.potassco.clingo.symbol.Symbol;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AbstractClingoDLTest {

    protected Set<Result> solve(String program, Integer min, Integer max, String... args) {
        LoggerCallback logger = (code, message) -> System.out.printf("[%s] %s", code.name(), message);

        ClingoDLTheory theory = new ClingoDLTheory();
        if (min != null) {
            theory.configure("min-int", min.toString());
        }
        if (max != null) {
            theory.configure("max-int", max.toString());
        }

        Control control = new Control(logger, 10000, args);
        theory.register(control);
        try (ProgramBuilder builder = new ProgramBuilder(control)) {
            Ast.parseString(program, theory.rewriteAst(builder), logger, 10000);
        }
        control.ground();
        theory.prepare();

        Set<Result> results = new HashSet<>();
        try (SolveHandle handle = control.solve(Collections.emptyList(), theory.onModel(), SolveMode.YIELD)) {
            while (handle.hasNext()) {
                Model model = handle.next();
                System.out.println("Model " + model.getNumber() + ": " + model);
                Set<Symbol> symbols = Set.of(model.getSymbols());
                Set<Assignment.Tuple> assignments = new HashSet<>();
                for (Assignment.Tuple tuple : theory.getAssignment(model)) {
                    System.out.println("Assignment: " + tuple);
                    assignments.add(tuple);
                }
                Result result = new Result(symbols, assignments);
                results.add(result);
            }
        }
        return results;
    }
    protected Set<Result> solve(String program, String... args) {
        return solve(program, null, null, args);
    }

    protected String getProgram(String fileName) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        Path file = Paths.get(url.getPath());
        return Files.readString(file);
    }

    protected void assertEquals(String expectedString, Set<Result> results) {
        Set expected = new HashSet();
        String[] resultParts = expectedString.split(" ; ");
        for (String resultPart : resultParts) {
            String[] parts = resultPart.split(" - ");
            Assert.assertTrue(parts.length == 1 || parts.length == 2);
            Set<Symbol> model;
            Set<Assignment.Tuple> tuples;
            if (parts.length == 1) {
                model = Collections.emptySet();
                tuples = parseTuples(parts[0]);
            } else if (parts.length == 2) {
                model = parseModel(parts[0]);
                tuples = parseTuples(parts[1]);
            } else {
                Assert.fail("Invalid expected string '" + expected + "'");
                return;
            }
            Result result = new Result(model, tuples);
            expected.add(result.toStringSets());
        }
        Set actual = new HashSet<>();
        for (Result result : results) {
            actual.add(result.toStringSets());
        }

        Assert.assertEquals(expected, actual);
    }

    private Set<Symbol> parseModel(String text) {
        Set<Symbol> symbols = new HashSet<>();
        for (String part : text.split(" ")) {
            Symbol symbol = Symbol.fromString(part);
            symbols.add(symbol);
        }
        return symbols;
    }

    private Set<Assignment.Tuple> parseTuples(String text) {
        Set<Assignment.Tuple> tuples = new HashSet<>();
        for (String assignment : text.split(" ")) {
            String[] tuple = assignment.split("=");
            if (tuple.length != 2) {
                Assert.fail("Invalid expected tuple '" + text + "'");
            }
            Symbol symbol = Function.fromString(tuple[0]);
            int raw = Integer.parseInt(tuple[1]);
            ValueUnion union = new ValueUnion(raw);
            Value value = new Value(ValueType.INT.getValue(), union);
            tuples.add(new Assignment.Tuple(symbol, value));
        }
        return tuples;
    }

    static final class Result {
        Set<Symbol> model;
        Set<Assignment.Tuple> assignments;

        Result(Set<Symbol> model, Set<Assignment.Tuple> assignments) {
            this.model = model;
            this.assignments = assignments;
        }

        Result(Set<Assignment.Tuple> assignments) {
            this.model = Collections.emptySet();
            this.assignments = assignments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return model.equals(result.model) && assignments.equals(result.assignments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, assignments);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (Symbol symbol : model) {
                builder.append(symbol.toString());
                if (i++ < model.size() - 1) {
                    builder.append(" ");
                }
            }
            if (!model.isEmpty()) {
                builder.append(" ");
            }
            i = 0;
            for (Assignment.Tuple tuple : assignments) {
                builder.append(tuple.toString());
                if (i++ < assignments.size() - 1) {
                    builder.append(" ");
                }
            }
            return builder.toString();
        }

        public List<Set> toStringSets() {
            Set<String> symbolStrings = new HashSet<>();
            for (Symbol symbol : model) {
                symbolStrings.add(symbol.toString());
            }
            Set<String> assignmentStrings = new HashSet<>();
            for (Assignment.Tuple tuple : assignments) {
                assignmentStrings.add(tuple.toString());
            }
            return List.of(
                    symbolStrings,
                    assignmentStrings
            );
        }
    }
}
