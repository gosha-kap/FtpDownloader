package com.example.demo;

import org.mockftpserver.fake.filesystem.FileEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestData {

    public static String dir = "/data/";
    public static String saveFolder = System.getProperty("user.dir") + "/";
    public static String FileName1 = "1.mp3";
    public static String FileName2 = "2.mp3";
    public static String FileName3 = "3.wav";
    public static String FileName4 = "4.wav";

    public static String FileName5 = "5.mp3";
    public static String FileName6 = "6.mp3";
    public static String FileName7 = "7.mp3";
    public static String FileName8 = "8.mp3";
    public static String FileName9 = "9.wav";

    public static String rawTest1 = "slahflkasfhasdjfhlkdashfkljashdjfhldas";
    public static String rawTest2 = "l;fjl;asj;lfjsa;ldjfl;asdj;flj";
    public static String rawTest3 = "jsfjkaslfjalsdjf;lasfk";
    public static String rawTest4 = "aljsfasnmcv,m.S,JFLsdjfl";

    public static String rawTest5 = "aljsfasnmcv,m.S,JFdgLsdjfl";
    public static String rawTest6 = "aljsfassdfgsdgnmcv,m.S,JFLsdjfl";
    public static String rawTest7 = "aljsfasnmcvsddsgm.S,JFLsdjfl";
    public static String rawTest8 = "aljsfasncvbcxvbmcv,m.S,JFLsdjfl";
    public static String rawTest9 = "aljsfaxcbxcbxcbxcvbsnmcv,m.S,JFLsdjfl";


    public static List<FileEntry> listFiles = Arrays.asList(new FileEntry(dir + FileName1, rawTest1),
            new FileEntry(dir + FileName2, rawTest2),
            new FileEntry(dir + FileName3, rawTest3),
            new FileEntry(dir + FileName4, rawTest4),
            new FileEntry(dir + "folder1/" + FileName5, rawTest5),
            new FileEntry(dir + "folder1/" + FileName6, rawTest6),
            new FileEntry(dir + "folder2/" + FileName7, rawTest7),
            new FileEntry(dir + "folder2/" + FileName8, rawTest8),
            new FileEntry(dir + "folder2/" + FileName9, rawTest9));
}
