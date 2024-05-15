//package com.motadata;
//
//import com.motadata.utils.Constants;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.json.JsonObject;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.Base64;
//
//public class Main
//{
//    public static void main(String[] args)
//    {
//        try
//        {
//
//            var processBuilder = new ProcessBuilder("fping", "10.20.40.239", "-c3", "-q");
//
//            var process = processBuilder.start();
//
//            // Read the output of the command
//            var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//
//            var buffer = Buffer.buffer();
//
//            while((line = reader.readLine()) != null)
//            {
//                buffer.appendString(line);
//            }
//
//            System.out.println(buffer.toString());
//
//        } catch(Exception exception)
//        {
//        }
//
//    }
//}
//
