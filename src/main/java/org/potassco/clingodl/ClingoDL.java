/*
 * Copyright (C) 2021 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.potassco.clingodl;

import java.util.Collections;

import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.potassco.clingo.ast.AstCallback;
import org.potassco.clingo.internal.NativeSize;
import org.potassco.clingo.internal.NativeSizeByReference;
import org.potassco.clingo.theory.Value;
import org.potassco.clingo.theory.ValueType;
import org.potassco.clingo.theory.ValueTypeConverter;

/**
 * This interface is used by JNA to access the native methods of ClingoDL.
 * It is not meant to be used by 3rd party libraries, refer to {@link ClingoDLTheory} instead.
 */
interface ClingoDL extends Library {
    ClingoDL INSTANCE = initLibrary();

    static ClingoDL initLibrary() {
        DefaultTypeMapper mapper = new DefaultTypeMapper();
        ValueTypeConverter converter = new ValueTypeConverter();
        mapper.addTypeConverter(ValueType.class, converter);
        System.setProperty("jna.encoding", "UTF-8");
        return Native.load("clingo-dl", ClingoDL.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, mapper));
    }

    static void check(byte success) {
        if (success == 0)
            throw new IllegalStateException("there was an internal error");
    }

    /**
     * Return the version of the theory.
     */
    static String getVersion() {
        IntByReference major = new IntByReference();
        IntByReference minor = new IntByReference();
        IntByReference revision = new IntByReference();
        INSTANCE.clingodl_version(major, minor, revision);
        return String.format("%d.%d.%d", major.getValue(), minor.getValue(), revision.getValue());
    }

    /**
     * Return the version of the theory.
     */
    void clingodl_version(IntByReference major, IntByReference minor, IntByReference patch);

    /**
     * creates the theory
     */
    byte clingodl_create(PointerByReference theory);

    /**
     * Registers the theory with the control
     */
    byte clingodl_register(Pointer theory, Pointer control);

    /**
     * Rewrite asts before adding them via the given callback.
     */
    byte clingodl_rewrite_ast(Pointer theory, Pointer ast, AstCallback add, Pointer data);

    /**
     * Prepare the theory between grounding and solving
     */
    byte clingodl_prepare(Pointer theory, Pointer control);

    /**
     * Destroys the theory, currently no way to unregister a theory
     */
    byte clingodl_destroy(Pointer theory);

    /**
     * Configure theory manually (without using clingo's options facility).
     * Note that the theory has to be configured before registering it and cannot be reconfigured.
     */
    byte clingodl_configure(Pointer theory, String key, String value);

    /**
     * Add options for your theory
     */
    byte clingodl_register_options(Pointer theory, Pointer options);

    /**
     * Validate options for your theory
     */
    byte clingodl_validate_options(Pointer theory);

    /**
     * Callback on every model
     */
    byte clingodl_on_model(Pointer theory, Pointer model);

    /**
     * Obtain a symbol index which can be used to get the value of a symbol.
     * Returns whether the symbol exists. Does not throw.
     */
    byte clingodl_lookup_symbol(Pointer theory, long symbol, NativeSizeByReference index);

    /**
     * Obtain the symbol at the given index. Does not throw.
     */
    long clingodl_get_symbol(Pointer theory, NativeSize index);

    /**
     * Initialize index so that it can be used with {@link #clingodl_assignment_next}.
     * Does not throw.
     */
    void clingodl_assignment_begin(Pointer theory, int thread_id, NativeSizeByReference index);

    /**
     * Move to the next index that has a value.
     * Returns whether the updated index is valid. Does not throw.
     */
    byte clingodl_assignment_next(Pointer theory, int thread_id, NativeSizeByReference index);

    /**
     * Check if the symbol at the given index has a value. Does not throw.
     */
    byte clingodl_assignment_has_value(Pointer theory, int thread_id, NativeSize index);

    /**
     * Get the symbol and its value at the given index. Does not throw.
     */
    void clingodl_assignment_get_value(Pointer theory, int thread_id, NativeSize index, Value value);

    /**
     * Callback on statistic updates. Please add a subkey with the name of your theory.
     */
    byte clingodl_on_statistics(Pointer theory, Pointer step, Pointer accu);


}
