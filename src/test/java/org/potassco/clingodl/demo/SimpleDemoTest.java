package org.potassco.clingodl.demo;

import org.junit.Test;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.ProgramBuilder;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.control.LoggerCallback;
import org.potassco.clingo.solving.Model;
import org.potassco.clingo.solving.SolveHandle;
import org.potassco.clingo.solving.SolveMode;
import org.potassco.clingodl.Assignment;
import org.potassco.clingodl.ClingoDLTheory;

import java.util.Collections;

public class SimpleDemoTest {

	@Test
	public void testDemo() {
		String program = "&diff{ x } >= 1. &diff{ y } >= 3.";

		LoggerCallback logger = (code, message) -> System.out.printf("[%s] %s", code.name(), message);

		ClingoDLTheory theory = new ClingoDLTheory();
		Control control = new Control(logger, 10000, "0");
		theory.register(control);
		try (ProgramBuilder builder = new ProgramBuilder(control)) {
			Ast.parseString(program, theory.rewriteAst(builder), logger, 10000);
		}
		control.ground();
		theory.prepare();

		try (SolveHandle handle = control.solve(Collections.emptyList(), theory.onModel(), SolveMode.YIELD)) {
			while (handle.hasNext()) {
				Model model = handle.next();
				System.out.println("Model " + model.getNumber() + ": " + model);
				for (Assignment.Tuple tuple : theory.getAssignment(model)) {
					System.out.println("Assignment: " + tuple);
				}
			}
		}
	}
}
