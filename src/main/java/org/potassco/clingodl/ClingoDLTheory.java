package org.potassco.clingodl;

import java.util.NoSuchElementException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.Ast;
import org.potassco.clingo.ast.AstCallback;
import org.potassco.clingo.ast.ProgramBuilder;
import org.potassco.clingo.control.Control;
import org.potassco.clingo.internal.NativeSize;
import org.potassco.clingo.internal.NativeSizeByReference;
import org.potassco.clingo.solving.Model;
import org.potassco.clingo.solving.SolveEventCallback;
import org.potassco.clingo.symbol.Symbol;
import org.potassco.clingo.theory.Theory;

public class ClingoDLTheory extends Theory {

	private final Pointer theory;
	private Control control;

	public ClingoDLTheory() {
		PointerByReference theory = new PointerByReference();
		ClingoDL.check(ClingoDL.INSTANCE.clingodl_create(theory));
		this.theory = theory.getValue();
	}

	/**
	 * Configure theory manually (without using clingo's options facility).
	 * <p>
	 * Note that the theory has to be configured before registering it and cannot
	 * be reconfigured.
	 *
	 * @param key   the key of the option
	 * @param value the value of the option
	 */
	public void configure(String key, String value) {
		if (this.control != null)
			throw new IllegalStateException("you have to configure the theory before registering it");
		ClingoDL.check(ClingoDL.INSTANCE.clingodl_configure(this.theory, key, value));
	}

	/**
	 * Register the theory with a control object.
	 *
	 * @param control the control object to register the theory
	 */
	public void register(Control control) {
		this.control = control;
		ClingoDL.check(ClingoDL.INSTANCE.clingodl_register(this.theory, control.getPointer()));
	}

	/**
	 * Rewrite asts before adding them via the given program builder.
	 *
	 * @param builder the program builder to add
	 * @return callback to rewrite an ast
	 */
	public AstCallback rewriteAst(ProgramBuilder builder) {
		return (Ast ast) ->
				ClingoDL.check(ClingoDL.INSTANCE.clingodl_rewrite_ast(theory, ast.getPointer(), builder::add, control.getPointer()));
	}

	/**
	 * Callback to inform the theory on found models
	 *
	 * @return the clingcon on model callback
	 */
	public SolveEventCallback onModel() {
		return new SolveEventCallback() {
			@Override
			public void onModel(Model model) {
				ClingoDL.check(ClingoDL.INSTANCE.clingodl_on_model(theory, model.getPointer()));
			}
		};
	}

	/**
	 * @param model the last model
	 * @return the current assignment
	 */
	public Assignment getAssignment(Model model) {
		return new Assignment(theory, model.getThreadId());
	}

	/**
	 * Prepare the theory between grounding and solving
	 */
	public void prepare() {
		if (this.control == null)
			throw new IllegalStateException("you have to register the theory first");
		ClingoDL.check(ClingoDL.INSTANCE.clingodl_prepare(this.theory, this.control.getPointer()));
	}

	/**
	 * Destroy the theory. Currently, no way to unregister a theory.
	 */
	public void destroy() {
		ClingoDL.check(ClingoDL.INSTANCE.clingodl_destroy(this.theory));
	}

	/**
	 * Obtain a symbol
	 *
	 * @param symbol the symbol to lookup
	 * @return the found symbol
	 */
	public Symbol getSymbol(Symbol symbol) {
		NativeSizeByReference nativeSizeByRef = new NativeSizeByReference();
		byte exists = ClingoDL.INSTANCE.clingodl_lookup_symbol(theory, symbol.getLong(), nativeSizeByRef);
		if (exists == 0)
			throw new NoSuchElementException("symbol does not exist");
		NativeSize index = new NativeSize(nativeSizeByRef.getValue());
		long symbolId = ClingoDL.INSTANCE.clingodl_get_symbol(theory, index);
		return Symbol.fromLong(symbolId);
	}

}
