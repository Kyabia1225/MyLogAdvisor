package FeatureExtraction;

public class Main {
    public static void main(String[] args) {
        CodeAnalysis codeAnalysis = new CodeAnalysis("E:\\MyPaper\\hadoop");
        codeAnalysis.tryToParse();
        codeAnalysis.outputToFile();
    }
}
