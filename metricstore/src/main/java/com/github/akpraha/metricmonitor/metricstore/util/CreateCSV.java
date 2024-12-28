package com.github.akpraha.metricmonitor.metricstore.util;

import java.util.Random;

/**
 * @author Andy Key
 * @created 12/28/2024, Sat
 */
public class CreateCSV {
    public static void main(String[] args) {
        Random r = new Random();
        String lineFmt = "\"%s\",%s,%s,%s,%s,%s\n";
        String[] d = new String[5];
        for (int i = 0; i < 500; i++) {
            int num = 3 + r.nextInt(3);
            String name = "metric-" + i;
            for (int j = 0; j < d.length; j++) {
                if (j < num) {
                    d[j] = "\"dimension-" + r.nextInt(10000) + "\"";
                } else {
                    d[j] = "";
                }
            }
            System.out.printf(lineFmt, name, d[0], d[1], d[2], d[3], d[4]);
        }
    }
}
