import FeatureExtraction.CodeAnalysis;
import Proc.Predict;
import Proc.PredictResult;

import java.util.List;

public class Main {
    static String process_code_path(String code_path) {
        code_path = code_path.replaceAll("/", "\\");
        if(code_path.endsWith("\\")) {
            code_path = code_path.substring(0, code_path.length() - 2);
        }
        return code_path;
    }

    static String process_feature_storage_path(String feature_storage_path) {
        feature_storage_path = feature_storage_path.replaceAll("/", "\\");
        if(!feature_storage_path.endsWith("\\")) {
            feature_storage_path += "\\";
        }
        return feature_storage_path;
    }
    public static void main(String[] args) {
        //CodeAnalysis codeAnalysis = new CodeAnalysis("E:\\MyPaper\\hadoop");
        String code_path = "D:\\Projects\\MyLogAdvisor";  // 不需要\\结尾
        String feature_storage_path = "D:\\tmp_feature\\";   //需要\\结尾
        code_path = process_code_path(code_path);
        feature_storage_path = process_feature_storage_path(feature_storage_path);
        CodeAnalysis codeAnalysis = new CodeAnalysis(code_path);
        codeAnalysis.getFeatures(feature_storage_path);
        Predict predict = new Predict();
        List<PredictResult> resultList = predict.predict("E:\\MyPaper\\venv\\Scripts\\python.exe", feature_storage_path);
        for(PredictResult pr:resultList){
            System.out.println(pr.getMethodName() + " in " + pr.getFileName() + " is: " + pr.getProbability());
        }
    }
}
