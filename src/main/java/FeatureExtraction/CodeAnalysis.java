package FeatureExtraction;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class CodeAnalysis {
    private final String path;
    private List<Feature> features;
    private final Logger logger = Logger.getLogger("CodeAnalysis");

    public CodeAnalysis(String path) {
        this.path = path;
    }

    public boolean outputToFile() {
        if(this.features == null) {
            logger.warning("Nothing in features. Please try to parse.\n");
        }
        ObjectMapper mapper = new ObjectMapper();
        try{
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.features);
            BufferedWriter out = new BufferedWriter(new FileWriter("features.txt"));
            out.write(json);
            out.close();
            logger.info("output to features.txt successfully\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean tryToParse() {
        logger.info("parsing, path: " + this.path + "\n");
        Date before = new Date();
        try {
            this.features = getFeatures();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Date after = new Date();
        int parseTime = after.compareTo(before);
        logger.info("parsing completed, it took " + parseTime + " s.\n");
        return true;
    }

    public List<Feature> getFeatures() {
        if(this.features != null) {
            return this.features;
        }
        List<Feature> res = new ArrayList<>();
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(Paths.get(path));
        List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        for(SourceRoot sourceRoot:sourceRoots) {
            try {
                sourceRoot.tryToParse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<CompilationUnit> compilationUnits = sourceRoot.getCompilationUnits();
            if(!compilationUnits.isEmpty()) {
                for(CompilationUnit cu:compilationUnits) {
                    List<Feature> features = parseCompilationUnit(cu);
                    res.addAll(features);
                }
            }
        }
       return res;
    }
    private List<Feature> parseCompilationUnit(CompilationUnit cu) {

        //features is used for result storage
        List<Feature> features = new ArrayList<>();
        ArrayList<String> classNameList = new ArrayList<>();

        //read all className
        cu.accept(new VoidVisitorAdapter<List<String>>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration cORid, List<String> classNameList) {
                classNameList.add(cORid.getNameAsString());
                super.visit(cORid, classNameList);
            }
        }, classNameList);

        //split code block
        for(String className:classNameList) {
            Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(className);
            if(clazz.isPresent()) {
                //get all methods
                List<MethodDeclaration> methods = clazz.get().getMethods();
                for(MethodDeclaration method:methods) {
                    Feature feature = new Feature();
                    if(cu.getStorage().isPresent()) {
                        feature.setDirectory(cu.getStorage().get().getDirectory().toString());
                    }
                    feature.setMethodName(method.getNameAsString());
                    String methodCodeText = "";
                    if(method.getBody().isPresent()) {
                        methodCodeText = method.getDeclarationAsString(false, true, true)
                                + "\n" + method.getBody().get() + "\n";
                    } else {
                        methodCodeText = method.getDeclarationAsString(false, true, true);
                    }
                    feature.setSourceCodeText(methodCodeText);
                    Counter tryCatchBlockNum = new Counter(0);

                    //visit try stmt
                    method.accept(new ModifierVisitor<Counter>() {
                        @Override
                        public Visitable visit(TryStmt stmt, Counter tryCatchBlockNum) {
                            tryCatchBlockNum.count();
                            NodeList<CatchClause> catchClauses = stmt.getCatchClauses();
                            for(CatchClause catchClause:catchClauses) {
                                feature.addErrType(catchClause.getParameter().getTypeAsString());
                            }
                            return super.visit(stmt, tryCatchBlockNum);
                        }
                    }, tryCatchBlockNum);
                    feature.setTryCatchBlockNum(tryCatchBlockNum.getTimes());

                    //visit whether there is an assignment stmt with an assigned special value
                    method.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(AssignExpr expr, Void arg) {
                            String value = expr.getValue().toString();
                            if(containsSpecialValue(value)) {
                                feature.setSettingFlag(true);
                            }
                        }
                    }, null);
                    feature.setHasThrow(!method.getThrownExceptions().isEmpty());
                    method.accept(new ModifierVisitor<Void>() {
                        @Override
                        public Visitable visit(ReturnStmt stmt, Void arg) {
                            if(stmt.getExpression().isPresent()) {
                                Expression expression = stmt.getExpression().get();
                                if(containsSpecialValue(expression.toString())) {
                                    feature.setReturnSpecialValue(true);
                                }
                            } else {
                                //optional expression is not present, which means it returns null here
                                feature.setReturnSpecialValue(true);
                            }
                            return super.visit(stmt, arg);
                        }
                    }, null);
                    feature.setLogged(feature.getSourceCodeText().toLowerCase(Locale.ROOT).contains("log."));
                    //count callee methods
                    Counter containingMethodsCounter = new Counter(0);
                    method.accept(new VoidVisitorAdapter<Counter>() {
                        @Override
                        public void visit(MethodCallExpr methodCallExpr, Counter counter) {
                            counter.count();
                            //System.out.println(methodCallExpr.getName());
                            //todo: containing methods' name
                        }
                    }, containingMethodsCounter);
                    feature.setContainingMethodsNum(containingMethodsCounter.getTimes());
                    features.add(feature);
                }
            }
        }
        return features;
    }

    private boolean containsIgnoreCase(String cmp, String[] val) {
        cmp = cmp.toLowerCase(Locale.ROOT);
        for(String c:val){
            if(cmp.contains(c.toLowerCase(Locale.ROOT))){
                return true;
            }
        }
        return false;
    }

    private boolean containsSpecialValue(String cmp) {
        String[] specialValues = {"null", "empty", "fail", "exit", "warn", "-1"};
        return containsIgnoreCase(cmp, specialValues);
    }

}
