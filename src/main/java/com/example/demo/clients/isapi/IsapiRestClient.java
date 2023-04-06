// Copyright (c) 2020 Ryan Richard

package com.example.demo.clients.isapi;

import com.example.demo.entity.Credention;
import com.example.demo.entity.HiWatchSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.example.demo.clients.isapi.DateConverter.dateToApiString;

@Getter
@RequiredArgsConstructor
@Component
@Slf4j
public class IsapiRestClient {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final XmlMapper xmlMapper = new XmlMapper();

    public List<Model.SearchMatchItem> searchMedia(Credention credention,HiWatchSettings settings) throws IOException, InterruptedException {

        LocalDateTime to = (Objects.nonNull(settings.getToTime())) ? settings.getToTime() : LocalDateTime.now();
        LocalDateTime from = (Objects.nonNull(settings.getToTime())) ? settings.getFromTime() : to.minusHours(24);
        if(settings.isTimeShift()){
            to = to.minusHours(10);
            from = from.minusHours(10);
        }
        List<Model.SearchMatchItem> allResults = new LinkedList<>();
        Model.CMSearchResult searchResult;
        int searchResultPosition = settings.getSearchResultPosition();
        int maxResults = settings.getSearchMaxResult();
        do {
            searchResult = doHttpRequest(
                    POST,
                    "/ISAPI/ContentMgmt/search",
                    getSearchRequestBodyXml(from, to, settings.getChannel(), searchResultPosition, maxResults),
                    Model.CMSearchResult.class,
                    credention
            );
            List<Model.SearchMatchItem> matches = searchResult.getMatchList();
            if (matches != null) {
                allResults.addAll(matches);
            }
            searchResultPosition += maxResults;

        } while (searchResult.isResponseStatus() && searchResult.getResponseStatusStrg().equalsIgnoreCase("more"));
        return allResults;
    }

    public HttpResponse<Path> getFile(File file, String body,Credention credention) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return doHttpRequestDownload(GET,"/ISAPI/ContentMgmt/download",body,file,credention);
    }

    private String getSearchRequestBodyXml(LocalDateTime fromDate, LocalDateTime toDate, int trackId, int searchResultPosition, int maxResults) throws JsonProcessingException {

        String result = xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                Model.CMSearchDescription.builder()
                        .maxResults(maxResults)
                        .searchResultPosition(searchResultPosition)
                        .timeSpan(List.of(Model.TimeSpan.builder()
                                .startTime(dateToApiString(fromDate))
                                .endTime(dateToApiString(toDate))
                                .build()))
                        .trackID(List.of(trackId))
                        .metadataList(Model.Metadata.builder().metadataDescriptor("//recordType.meta.std-cgi.com").build())
                        .build()
        );
        return  result;
    }

    private HttpResponse<Path> doHttpRequestDownload(String requestMethod, String requestPath, String body, File file ,Credention credention) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        String authorizationHeaderValue = getaAthorizationHeaderValue(requestMethod,requestPath,body, credention);
        return doHttpRequestWithAuthHeader(requestMethod, requestPath, body, authorizationHeaderValue,file.toPath(),credention);
    }
    private <T> T doHttpRequest(String requestMethod, String requestPath, String body, Class<T> resultClass, Credention credention) throws IOException, InterruptedException {

        String authorizationHeaderValue = getaAthorizationHeaderValue(requestMethod,requestPath,body,credention);
        HttpResponse<String> response = doHttpRequestWithAuthHeader(requestMethod, requestPath, body, authorizationHeaderValue,credention);

        if (response.statusCode() == 401) {
            throw new RuntimeException("Could not authenticate. Wrong username or password?");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("Expected to get successful response but got response code " + response.statusCode());
        }

        // Avoid a jackson parsing error where it doesn't like empty lists
        String workaroundForEmptyResult = response.body().replaceAll("<matchList>\\s+</matchList>", "<matchList/>");

        // Return the parsed response
        return xmlMapper.readValue(workaroundForEmptyResult, resultClass);
    }


    private String getaAthorizationHeaderValue(String requestMethod, String requestPath, String body, Credention credention) throws IOException, InterruptedException {
        HttpResponse<String> unauthorizedResponse = doHttpRequestWithAuthHeader(requestMethod, requestPath, body, null,credention);
        if (unauthorizedResponse.statusCode() != 401) {
            throw new RuntimeException("Expected to get a 401 digest auth challenge response but didn't");
        }
        // Calculate the authorization digest value
        return  new DigestAuth(unauthorizedResponse.headers(), requestMethod, requestPath, credention.getUser(), credention.getPassword())
                .getAuthorizationHeaderValue();
    }


    private  HttpResponse<String> doHttpRequestWithAuthHeader(String requestMethod, String path, String body, String authHeaderValue,Credention credention) throws IOException, InterruptedException {

        HttpRequest request = buildRequest(requestMethod, path, body,authHeaderValue,credention);
        HttpResponse<String> response =  HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    private  HttpResponse<Path> doHttpRequestWithAuthHeader(String requestMethod, String path, String body, String authHeaderValue,Path file, Credention credention) throws IOException, InterruptedException, ExecutionException, TimeoutException {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        HttpRequest request = buildRequest(requestMethod, path, body,authHeaderValue,credention);

   /*   In java net Http client we have no data timeout, connectTimeout will not effected , when get success reply from host,
        but if data transfer interrupted, client is freeze, so use async send method to set up timeout*/
        CompletableFuture<HttpResponse<Path>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(file));
        return response.get(60, TimeUnit.MINUTES);

    }

    private HttpRequest buildRequest(String requestMethod, String path, String body, String authHeaderValue,Credention credention){
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://" + credention.getServer()+":"+credention.getPort() + path))
                .timeout(Duration.ofSeconds(2L))
                .header("Accept", "application/xml")
                .header("Content-Type","application/xml")
                .method(requestMethod,HttpRequest.BodyPublishers.ofString(body));

        if (authHeaderValue != null) {
            requestBuilder.header("Authorization", authHeaderValue);
        }
        return requestBuilder.build();
    }

}
