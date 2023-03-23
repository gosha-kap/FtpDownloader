package com.example.demo.clients;

import com.example.demo.clients.isapi.IsapiRestClient;
import com.example.demo.clients.isapi.Model;
import com.example.demo.settings.Credention;
import com.example.demo.settings.HiWatchSettings;
import com.example.demo.settings.Settings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HiWatchClient implements MyClient {

    private HiWatchSettings settings;
    private String saveFolder;
    private IsapiRestClient restClient;

    private String host;

    private final long ADD_HEADER = 40L;

    Logger logger = LoggerFactory.getLogger(HiWatchClient.class);

    public HiWatchClient(Credention credention, Settings settings) {

        this.settings = (HiWatchSettings) settings;
        this.saveFolder = settings.getSaveFolder();
        this.restClient = new IsapiRestClient(credention, this.settings);
        this.host = credention.getServer();
    }

    @Override
    public void connect() throws IOException {
        //Using login check//
    }

    @Override
    public List<String> getFilesFromRoot() {

        List<Model.SearchMatchItem> videos;
        try {

            videos = restClient.searchMedia(settings.getFrom(), settings.getTo(), settings.getChannel());
            logger.info("Get List of record from:"+host+". Found "+ videos.size()+" record(s).");
        } catch (InterruptedException e) {
            throw new RuntimeException("Error getting data from host");
        } catch (IOException e) {
            throw new RuntimeException("Connection error to host");
        }
        if (videos.isEmpty()) {
            throw new RuntimeException("No videos within that time/date range found");
        }
        XmlMapper xmlMapper = new XmlMapper();
        List<String> list = videos.stream().map(
                x -> {
                    try {
                        return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                                Model.downloadRequest.builder().playbackURI(x.getMediaSegmentDescriptor().getPlaybackURI()
                                ).build());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());

        return list;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void downLoad() throws IOException {
        Pattern pattern = Pattern.compile("starttime=(\\d{8})T(\\d{6})Z.*name=(.*)&.*size=(\\d*)");
        List<String> xmlListOfRecords = getFilesFromRoot();
        for (String xmlRequest : xmlListOfRecords) {
            Matcher matcher = pattern.matcher(xmlRequest);
            if (matcher.find()) {
                String date = matcher.group(1);
                 Long size = Long.parseLong(matcher.group(4))+ ADD_HEADER;
                String folderName = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
                String fileName = matcher.group(3);
                File localFolder = new File(saveFolder.concat("/").concat(folderName));
                File localFile = new File(saveFolder.concat("/").concat(folderName).concat("/").concat(fileName + ".mp4"));
                if (!localFolder.exists())
                    localFolder.mkdir();
                boolean exists = localFile.exists();
                long length = localFile.length() ;
                if (exists && length == size) {
                    logger.info("File: "+ fileName+" is exist, size: "+length);
                    continue;
                } else if (exists) {
                    localFile.delete();
                } else {
                    if (!localFile.createNewFile())
                        throw new RuntimeException("Can't create file");
                }
                try {
                    logger.info("Get file:"+localFile);
                    HttpResponse<Path> result = restClient.getFile(localFile, xmlRequest);
                    if (result.statusCode() != 200) {
                        throw new RuntimeException("Error in response code:" + result.statusCode());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Error downloading file");
                } catch (ExecutionException e) {
                    throw new RuntimeException("ExecutionException: "+e.getMessage());
                } catch (TimeoutException e) {
                    throw new RuntimeException("TimeoutException: "+ e.getMessage());
                }


            } else {
                throw new IllegalArgumentException("Error parsing record meta data");
            }
        }


    }
}
