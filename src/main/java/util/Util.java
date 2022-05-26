package util;

import FeatureExtraction.CodeAnalysis;
import Proc.Predict;
import Proc.PredictResult;

import java.io.File;
import java.util.List;

public class Util {

    public static void deleteDir(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        for(File file:files){
            file.delete();
        }
        dir.delete();
    }

    public static List<PredictResult> run(String code_path) {

        String feature_storage_path = "code_feature\\";   //需要\\结尾
        File feature_dir = new File(feature_storage_path);
        feature_dir.mkdir();
        CodeAnalysis codeAnalysis = new CodeAnalysis(code_path);
        codeAnalysis.getFeatures(feature_storage_path);
        Predict predict = new Predict();
        System.out.println("Begin to load model\n");
        return predict.predict("E:\\MyPaper\\venv\\Scripts\\python.exe", feature_storage_path);
    }

    public static void main(String[] args) {
        run("D:\\Projects\\backend-COIN");
    }

}
