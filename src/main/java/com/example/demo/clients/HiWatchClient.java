package com.example.demo.clients;

import com.example.demo.Utils.Utils;
import com.example.demo.clients.isapi.IsapiRestClient;
import com.example.demo.clients.isapi.Model;
import com.example.demo.dto.CheckResponse;
import com.example.demo.dto.CheckStatus;
import com.example.demo.entity.ClientType;
import com.example.demo.entity.Credention;
import com.example.demo.entity.HiWatchSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HiWatchClient implements MyClient {

    private IsapiRestClient restClient;

    private final long ADD_HEADER = 40L;

    @Autowired
    public HiWatchClient(IsapiRestClient restClient) {
      this.restClient = restClient;
    }


    public List<String> getFilesFromRoot(Credention credention, HiWatchSettings settings) {

        List<Model.SearchMatchItem> videos;
        try {
            videos = restClient.searchMedia(credention,settings);
            log.info("Get List of record from:"+credention.getServer()+":"+credention.getPort()+". Found "+ videos.size()+" record(s).");
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
    public  void downLoad(Credention credention, String saveFolder ,Object settings) throws IOException {
        HiWatchSettings hiWatchSettings = null;
        try{
            hiWatchSettings = (HiWatchSettings)settings;
        }
        catch (ClassCastException e){
            log.error("Can't get Hiwatch Settings");
            throw  new RuntimeException("Can't get Hiwatch Settings");
        }
        List<String> xmlListOfRecords = getFilesFromRoot(credention,hiWatchSettings);
        Pattern pattern = Pattern.compile("starttime=(\\d{8})T(\\d{6})Z.*name=(.*)&.*size=(\\d*)");
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
                    log.info("File: "+ fileName+" is exist, size: "+length);
                    continue;
                } else if (exists) {
                    localFile.delete();
                } else {
                    if (!localFile.createNewFile())
                        throw new RuntimeException("Can't create file");
                }
                try {
                    log.info("Get file:"+localFile);
                    HttpResponse<Path> result = restClient.getFile(localFile, xmlRequest,credention);
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

    @Override
    public ClientType getType() {
        return ClientType.HiWatch;
    }

    @Override
    public CheckResponse check(Credention credention , Object settings) {
        CheckResponse checkResponse = new CheckResponse();
        HiWatchSettings hiWatchSettings = null;
        try{
            hiWatchSettings = (HiWatchSettings)settings;
        }
        catch (ClassCastException e){
            log.error("Can't get Hiwatch Settings");
            checkResponse.setCheckStatus(CheckStatus.ERROR);
            checkResponse.setMessadge("Can't get Hiwatch Settings");
            return checkResponse;

        }
        List<String> xmlListOfRecords = null;
        try{
            xmlListOfRecords = getFilesFromRoot(credention,hiWatchSettings);
            checkResponse.setRecords(Utils.convertXml(xmlListOfRecords));
            checkResponse.setCheckStatus(CheckStatus.OK);
            return checkResponse;

        }
        catch(Exception e) {
            log.error("Error to parse xml list to string");
            checkResponse.setMessadge(e.getMessage());
            checkResponse.setCheckStatus(CheckStatus.ERROR);
            return checkResponse;
        }



    }


}
