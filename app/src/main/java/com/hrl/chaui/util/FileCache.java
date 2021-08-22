package com.hrl.chaui.util;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileCache {
    private static HashMap<String, HashMap<Integer,byte[]>> fileCache=new HashMap<String, HashMap<Integer,byte[]>>();
    private static HashMap<String, Integer> fileCount=new HashMap<>();

    public static void createNewCache(String hex,HashMap<Integer,byte[]> map){
        fileCache.put(hex,map);
        fileCount.put(hex,1);
    }

    public static void add2Cache(String hex,int order,byte[] data){
        fileCache.get(hex).put(order,data);
        int count=fileCount.get(hex);
        fileCount.put(hex,count+1);
    }

    public static int getCount(String hex){
        return fileCount.get(hex);
    }

    public static File mergeToFile(String hex, int total,int length,String filePath, String fileName){
        HashMap<Integer,byte[]> fileMap=fileCache.get(hex);
        byte[] bfile= new byte[length];
        int currLen=0;
        for(int i=0;i<total;i++){
            byte[] bytes=fileMap.get(i);
            System.arraycopy(bytes,0,bfile,currLen,bytes.length);
            currLen+=bytes.length;
        }
        fileCache.remove(hex);
        fileCount.remove(hex);
        return FileUtils.bytesToFile(bfile,filePath,fileName);
    }
}
