package org.potassco.clingodl;

import org.junit.Test;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.internal.Clingo;

public class TheoryTest {

	@Test
	public void testGetVersion() {
		System.out.println("Clingo Version: " + Clingo.getVersion());
		System.out.println("ClingCon Version: " + ClingoDL.getVersion());
	}

	@Test
	public void testCreateTheory() {
		ClingoDLTheory theory = new ClingoDLTheory();
		theory.destroy();
	}

	@Test
	public void testRegisterTheory() {
		Control control = new Control();
		control.add("{a}.");
		ClingoDLTheory theory = new ClingoDLTheory();
		theory.register(control);
		theory.destroy();
	}

	@Test
	public void testPrepare() {
		Control control = new Control();
		control.add("{a}.");
		ClingoDLTheory theory = new ClingoDLTheory();
		theory.register(control);
		theory.prepare();
		theory.destroy();
	}
}
