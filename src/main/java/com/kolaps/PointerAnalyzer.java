package com.kolaps;

import boomerang.Boomerang;
import boomerang.ForwardQuery;
import boomerang.Query;
import boomerang.scope.*;
import boomerang.scope.sootup.SootUpFrameworkScope;
import qilin.CoreConfig;
import qilin.core.PTA;
import qilin.driver.PTAFactory;
import qilin.driver.PTAPattern;
import qilin.pta.PTAConfig;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PointerAnalyzer {

    public static PTAPattern ptaPattern;
    public static PTA pta;



    public static void startAnalysis(JavaView view, MethodSignature signature, JavaSootMethod mainMethod)
    {
        DataFlowScope dat = DataFlowScope.EXCLUDE_PHANTOM_CLASSES;
        CallGraphAlgorithm rta = new RapidTypeAnalysisAlgorithm(view);

        CallGraph cg = rta.initialize(Collections.singletonList(signature));

        FrameworkScope soot = new SootUpFrameworkScope(view,cg, Collections.singletonList(mainMethod),dat);
        Boomerang boom = new Boomerang(soot);
        boomerang.scope.CallGraph boomGraph =boom.getCallGraph();
        AnalysisScope scope = new AnalysisScope(boomGraph) {
            @Override
            protected Collection<? extends Query> generate(ControlFlowGraph.Edge seed) {
                Statement statement = seed.getTarget();
                return List.of();
            }
        };
        Collection<Query> seeds = scope.computeSeeds();
        System.out.println("Boomerang: " + boom);
        /*PTAConfig.v();
        ptaPattern = new PTAPattern("insens");
        PTA pta = PTAFactory.createPTA(ptaPattern, view, entrypoint);*/
    }
}
