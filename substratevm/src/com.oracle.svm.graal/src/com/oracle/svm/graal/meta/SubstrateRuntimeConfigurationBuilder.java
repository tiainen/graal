/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.graal.meta;

import java.util.function.Function;

import org.graalvm.compiler.api.replacements.SnippetReflectionProvider;
import org.graalvm.compiler.bytecode.BytecodeProvider;
import org.graalvm.compiler.bytecode.ResolvedJavaMethodBytecodeProvider;
import org.graalvm.compiler.core.common.spi.ConstantFieldProvider;
import org.graalvm.compiler.nodes.spi.Replacements;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.util.Providers;

import com.oracle.graal.pointsto.meta.AnalysisUniverse;
import com.oracle.svm.core.config.ConfigurationValues;
import com.oracle.svm.core.graal.code.SubstrateBackend;
import com.oracle.svm.core.graal.meta.SubstrateReplacements;
import com.oracle.svm.hosted.SVMHost;
import com.oracle.svm.hosted.ameta.AnalysisConstantFieldProvider;
import com.oracle.svm.hosted.ameta.AnalysisConstantReflectionProvider;
import com.oracle.svm.hosted.code.SharedRuntimeConfigurationBuilder;
import com.oracle.svm.hosted.code.SubstrateGraphMakerFactory;

import jdk.vm.ci.meta.ConstantReflectionProvider;
import jdk.vm.ci.meta.MetaAccessProvider;

public class SubstrateRuntimeConfigurationBuilder extends SharedRuntimeConfigurationBuilder {

    private final AnalysisUniverse aUniverse;
    private final ConstantReflectionProvider originalReflectionProvider;

    public SubstrateRuntimeConfigurationBuilder(OptionValues options, SVMHost hostVM, AnalysisUniverse aUniverse, MetaAccessProvider metaAccess,
                    ConstantReflectionProvider originalReflectionProvider, Function<Providers, SubstrateBackend> backendProvider) {
        super(options, hostVM, metaAccess, backendProvider);
        this.aUniverse = aUniverse;
        this.originalReflectionProvider = originalReflectionProvider;
    }

    @Override
    protected ConstantReflectionProvider createConstantReflectionProvider(Providers p) {
        return new AnalysisConstantReflectionProvider(aUniverse, originalReflectionProvider);
    }

    @Override
    protected ConstantFieldProvider createConstantFieldProvider(Providers p) {
        return new AnalysisConstantFieldProvider(aUniverse, p.getMetaAccess(), (AnalysisConstantReflectionProvider) p.getConstantReflection());
    }

    @Override
    protected Replacements createReplacements(Providers p, SnippetReflectionProvider snippetReflection) {
        BytecodeProvider bytecodeProvider = new ResolvedJavaMethodBytecodeProvider();
        return new SubstrateReplacements(options, p, snippetReflection, bytecodeProvider, ConfigurationValues.getTarget(), new SubstrateGraphMakerFactory(wordTypes));
    }
}
