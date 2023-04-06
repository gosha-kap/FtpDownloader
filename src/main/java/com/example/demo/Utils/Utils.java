package com.example.demo.Utils;

import org.apache.commons.net.ftp.FTPFile;
import org.quartz.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import static org.quartz.CronScheduleBuilder.cronSchedule;

public class Utils {


    public static void printFileDetails(FTPFile[] files) {
        long totalSize = 0;
        int count = 0;
        for (FTPFile file : files) {
            count++;
            totalSize += file.getSize();
        }
        double resultSize = totalSize / 1024 / 1024;
        String result = String.format("%.2f", resultSize);
        double averangeSize = resultSize / count;
        String result2 = String.format("%.2f", averangeSize);
        String out = ("Total " + count + " file(s)" + ", total size:" + result + "Mb, avarange size:" + result2 + "Mb.");
    }


    public static Map<String, Long> getListFiles(List<FTPFile> files, String postFix) {
        Map<String, Long> downloadedFiles = new HashMap<>();
        for (FTPFile file : files) {
            if (file.getName().endsWith(postFix)) {
                downloadedFiles.put(file.getName(), file.getSize());
            }
        }
        return downloadedFiles;
    }

    public static boolean isDirExist(String path, List<File> dirs) {
        return dirs.stream().anyMatch(file -> file.getName().equals(path));
    }

    public static boolean createDir(String path, File rootDir) {
        File dir = new File(rootDir.getAbsolutePath() + "/" + path);
        return dir.mkdir();
    }

    public static Map<String, Long> getDownlodedFiles(String folderName) {

        File folder = new File(folderName);
        Map<String, Long> local = new HashMap<>();
        if (!folder.exists() || !folder.isDirectory()) return null;
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            try {
                local.put(fileEntry.getName(), Files.size(fileEntry.toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return local;

    }

    public static List<String> convertXml(List<String> xmlListOfRecords) throws IOException, ParserConfigurationException, SAXException, URISyntaxException {
        StringBuilder stringBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        stringBuilder.append("<root>");
        for (String xmlitem : xmlListOfRecords)
            stringBuilder.append(xmlitem);
        stringBuilder.append("</root>");
        List<String> result = new ArrayList<>();
        ByteArrayInputStream input = new ByteArrayInputStream(
                stringBuilder.toString().getBytes("utf-8"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(input);
        NodeList nodeList = document.getElementsByTagName("playbackURI");
        for (int i = 0; i < nodeList.getLength(); i++) {
            URI uri = new URI(nodeList.item(i).getTextContent());
            String query = uri.getRawQuery();
            Map<String, String> map = Arrays.stream(query.split("&"))
                    .collect(Collectors.toMap(x -> x.split("=")[0], x -> x.split("=")[1]));
            stringBuilder = new StringBuilder();
            LocalDateTime from = LocalDateTime.parse(map.get("starttime"), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            stringBuilder.append(from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("---");
            LocalDateTime to = LocalDateTime.parse(map.get("endtime"), DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            stringBuilder.append(to.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append(", ");
            stringBuilder.append("name = ").append(map.get("name")).append(", size = ").append(map.get("size")).append(" kByte.");
            result.add(stringBuilder.toString());
        }
        return result;
    }
}
