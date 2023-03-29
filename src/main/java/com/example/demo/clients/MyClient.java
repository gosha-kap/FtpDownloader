package com.example.demo.clients;

import java.io.IOException;
import java.util.List;


public interface MyClient<T> {

    void connect() throws IOException;

    List<T> getFilesFromRoot();

    void close() throws IOException;
    void downLoad() throws IOException;
    List<String> check() throws IOException;
}
