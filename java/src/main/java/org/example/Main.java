//package org.example;
//
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.time.LocalTime;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TimeZone;
//
//public class Main
//{
//
//    public static void main(String[] args) {
//        //        String host = "10.20.40.128";
//        //        String username = "dhyani";
//        //        String password = "2303";
//        //        String command = "whoami"; // Example command, replace with your own
//
//        var json1 = new JsonObject();
//        json1.put("request.type","Collect");
//        json1.put("device.type","linux");
//        json1.put("metric.name","System");
//        json1.put("object.ip","10.20.40.197");
//        json1.put("object.port",22);
//        json1.put("object.host","raj");
//        json1.put("object.password","Mind@123");
//
//        var json2 = new JsonObject();
//        json2.put("request.type","Collect");
//        json2.put("device.type","linux");
//        json2.put("metric.name","System");
//        json2.put("object.ip","10.20.40.227");
//        json2.put("object.port",22);
//        json2.put("object.host","yash");
//        json2.put("object.password","1010");
//
//        var jsonArray = new JsonArray().add(json2).add(json1);
//
//        String encodedString = Base64.getEncoder().encodeToString(jsonArray.toString().getBytes());
//        System.out.println(encodedString);
//
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder("/home/shivam/motadata-lite/motadata-lite", encodedString);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            // Read the output of the command
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//
//            var buffer = Buffer.buffer();
//
//            while ((line = reader.readLine()) != null) {
//                buffer.appendString(line);
//            }
//
//            byte[] decodedBytes = Base64.getDecoder().decode(buffer.toString());
//
//            // Convert the byte array to a string
//            String decodedString = new String(decodedBytes);
//
//            System.out.println(decodedString);
//
//            Map<String,Object> map2 = new HashMap();
//            map2.put(LocalTime.now().toString(), new JsonObject().put("test",12));
//
//
//            var out = new JsonObject(map2);
//            System.out.println(out);
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}