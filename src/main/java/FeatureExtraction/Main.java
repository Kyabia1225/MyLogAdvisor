package FeatureExtraction;

public class Main {
    public static void main(String[] args) {
        //CodeAnalysis codeAnalysis = new CodeAnalysis("E:\\MyPaper\\hadoop");
        String code_path = "D:\\Projects\\backend-COIN\\src";  // 不需要\\结尾
        String feature_storage_path = "D:\\tmp_feature\\";   //需要\\结尾
        CodeAnalysis codeAnalysis = new CodeAnalysis(code_path);
        codeAnalysis.getFeatures(feature_storage_path);
    }
}
