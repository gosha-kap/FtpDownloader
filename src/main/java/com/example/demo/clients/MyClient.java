package com.example.demo.clients;

import com.example.demo.dto.CheckResponse;
import com.example.demo.entity.ClientType;
import com.example.demo.entity.Credention;

import java.io.IOException;


public interface MyClient {
     void downLoad(Credention credention,String saveFolder, Object settings ) throws IOException;
     ClientType getType();

     CheckResponse check(Credention credention ,Object settings);

}
