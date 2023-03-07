package org.potassco.clingodl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.sun.jna.Pointer;
import org.potassco.clingo.internal.NativeSize;
import org.potassco.clingo.internal.NativeSizeByReference;
import org.potassco.clingo.symbol.Symbol;
import org.potassco.clingo.theory.Value;

public class Assignment implements Iterable<Assignment.Tuple> {

	private final Pointer theory;
	private final int threadId;

	public Assignment(Pointer theory, int threadId) {
		this.theory = theory;
		this.threadId = threadId;
	}


	@Override
	public Iterator<Tuple> iterator() {
		NativeSizeByReference nativeSizeByReference = new NativeSizeByReference();
		ClingoDL.INSTANCE.clingodl_assignment_begin(theory, threadId, nativeSizeByReference);

		return new Iterator<>() {
			private final NativeSizeByReference index = nativeSizeByReference;
			private boolean continueIteration = true;
			private boolean hasNext = true;

			@Override
			public boolean hasNext() {
				if (continueIteration) {
					hasNext = ClingoDL.INSTANCE.clingodl_assignment_next(theory, threadId, index) > 0;
					continueIteration = false;
				}
				return hasNext;
			}

			@Override
			public Tuple next() {
				if (hasNext()) {
					continueIteration = true;
					NativeSize index = new NativeSize(this.index.getValue());
					Value valueByReference = new Value();
					long symbolLong = ClingoDL.INSTANCE.clingodl_get_symbol(theory, index);
					Symbol symbol = Symbol.fromLong(symbolLong);

					ClingoDL.INSTANCE.clingodl_assignment_get_value(theory, threadId, index, valueByReference);

					return new Tuple(symbol, valueByReference);
				}
				throw new NoSuchElementException();
			}
		};
	}

	public static final class Tuple {
		private final Symbol symbol;
		private final Value value;

		public Tuple(Symbol symbol, Value value) {
			this.symbol = symbol;
			this.value = value;
		}

		public Symbol getSymbol() {
			return symbol;
		}

		public Value getValue() {
			return value;
		}

		@Override
		public String toString() {
			return symbol.toString() + "=" + value.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Tuple tuple = (Tuple) o;
			return symbol.equals(tuple.symbol) && value.equals(tuple.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(symbol, value);
		}
	}

}
