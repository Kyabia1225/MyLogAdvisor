package Proc;

import util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Predict {
    private Float threshold;

    public Predict() {
    }

    public Predict(Float threshold) {
        this.threshold = threshold;
    }

    public List<PredictResult> predict(String pythonInterpreterPath, String feature_storage_path) {
        String cmd = pythonInterpreterPath + " E:\\MyPaper\\code2vec.py " + feature_storage_path;
        List<PredictResult> resultList = new ArrayList<>();
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            boolean beginFlag = false;
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if(line.equals("MethodName FileName Probability")) {
                    beginFlag = true;
                    continue;
                }
                if(beginFlag) {
                    String[] r = line.split(" ");
                    PredictResult predictResult = new PredictResult(r[0], r[1], Float.parseFloat(r[2]));
                    resultList.add(predictResult);
                }
            }
            in.close();
            proc.waitFor();
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
//        if(proc!=null && proc.exitValue()!=0) {
//            System.out.println("请仔细检查本地Python依赖");
//        }
        Util.deleteDir(feature_storage_path);
        return resultList;
    }

    public void getReport(List<PredictResult> resultList) {
        List<PredictResult> needLogging = new ArrayList<>();
        List<PredictResult> notNeedLogging = new ArrayList<>();

        for(PredictResult res : resultList) {
            if(res.getProbability() >= this.threshold) {
                needLogging.add(res);
            } else {
                notNeedLogging.add(res);
            }
        }
        // process two lists.
    }


}
