package com.cloud.zlist.autofire;

public class SimilarDegree {

    public static final double degree = 0.8;

    /**
     * 采用动态规划的方法解决
     *
     * @param source
     * @param target
     * @return
     */
    public static int EditDistance(String source, String target) {
        char[] sources = source.toCharArray();
        char[] targets = target.toCharArray();
        int sourceLen = sources.length;
        int targetLen = targets.length;
        int[][] d = new int[sourceLen + 1][targetLen + 1];
        for (int i = 0; i <= sourceLen; i++) {
            d[i][0] = i;
        }
        for (int i = 0; i <= targetLen; i++) {
            d[0][i] = i;
        }

        for (int i = 1; i <= sourceLen; i++) {
            for (int j = 1; j <= targetLen; j++) {
                if (sources[i - 1] == targets[j - 1]) {
                    d[i][j] = d[i - 1][j - 1];
                } else {
                    //插入
                    int insert = d[i][j - 1] + 1;
                    //删除
                    int delete = d[i - 1][j] + 1;
                    //替换
                    int replace = d[i - 1][j - 1] + 1;
                    d[i][j] = Math.min(insert, delete) > Math.min(delete, replace) ? Math.min(delete, replace) :
                            Math.min(insert, delete);
                }
            }
        }
        return d[sourceLen][targetLen];
    }

    public static void main(String[] args) {
        System.out.println(EditDistance("html > body > ul > li.proiect_item:nth-child(1) > div.item_row.item_row_title > div:nth-child(1) > a",
                "html > body > ul > li.proiect_item:nth-child(2) > div.item_row.item_row_title > div:nth-child(1) > a"));
    }

}
