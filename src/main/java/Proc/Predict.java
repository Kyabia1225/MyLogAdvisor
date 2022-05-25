package Proc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Predict {
    private float threshold;

    public Predict() {
    }

    public Predict(float threshold) {
        this.threshold = threshold;
    }

    public List<PredictResult> predict(String pythonInterpreterPath, String feature_storage_path) {
        String cmd = pythonInterpreterPath + " E:\\MyPaper\\code2vec.py " + feature_storage_path;
        List<PredictResult> resultList = new ArrayList<>();
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            boolean beginFlag = false;
            String line = null;
            while ((line = in.readLine()) != null) {
                if(line.equals("next line to start.")) {
                    beginFlag = true;
                    continue;
                }
                if(beginFlag) {
                    String[] r = line.split(" ");
                    PredictResult predictResult = new PredictResult(r[0], r[1], Float.parseFloat(r[2]));
                    resultList.add(predictResult);
                }
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
            if(proc.exitValue() != 0){
                throw new InterruptedException("请仔细检查本地Python依赖");
            }
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
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
