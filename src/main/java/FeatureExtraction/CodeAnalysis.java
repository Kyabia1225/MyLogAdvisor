package FeatureExtraction;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
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

import java.nio.file.Paths;
import java.util.*;

public class CodeAnalysis {
    private final String path;
    private final Logger logger = Logger.getLogger("CodeAnalysis");

    public CodeAnalysis(String path) {
        this.path = path;
    }

    public CodeAnalysis() {
        this.path = null;
    }

    public boolean outputToFile(List<Feature> features, String storagePath, String fileName) {
        //String storagePath = "features/" + fileName;
        File file = new File(storagePath + fileName);
        if(file.exists()) {
            logger.info(fileName + " exists.\n");
        }
        if(features == null) {
            logger.warning("Nothing in features. Please try to parse.\n");
        }
        ObjectMapper mapper = new ObjectMapper();
        try{
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(features);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            out.write(json);
            out.close();
            //logger.info("output to " + storagePath + " successfully\n");
            logger.info("parse " + fileName + " successfully\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void getFeature(String storagePath) {
        logger.info("Start parsing current page.\n");
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = null;
        try {
            parseResult = parser.parse(new File(path));
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        if(parseResult == null || !parseResult.isSuccessful()) {
            logger.info("Parse failure");
            return;
        }
        if(parseResult.getResult().isPresent()) {
            CompilationUnit compilationUnit = parseResult.getResult().get();
            List<Feature> features = parseCompilationUnit(compilationUnit);
            outputToFile(features, storagePath, compilationUnit.getStorage().get().getFileName().split("\\.")[0]);
            logger.info("Parsing completed.\n");
        }

    }
    public void getFeatures(String storagePath) {
        logger.info("Start parsing, path: " + this.path + "\n");
        //List<Feature> res = new ArrayList<>();
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setStoreTokens(false);
        parserConfiguration.setAttributeComments(false);
        ProjectRoot projectRoot = new ParserCollectionStrategy(parserConfiguration).collect(Paths.get(path));
        List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        for (int i = 0;i< sourceRoots.size();i++) {
            sourceRoots.get(i).tryToParseParallelized();
            List<CompilationUnit> compilationUnits = sourceRoots.get(i).getCompilationUnits();
            if (!compilationUnits.isEmpty()) {
                for (CompilationUnit compilationUnit : compilationUnits) {
                    List<Feature> features = parseCompilationUnit(compilationUnit);
                    outputToFile(features, storagePath, compilationUnit.getStorage().get().getFileName().split("\\.")[0]);
                    features.clear();
                }
            }
            compilationUnits.clear();
            sourceRoots.set(i, null);
            System.gc();
        }
        logger.info("Parsing completed.\n");
       //return res;
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
