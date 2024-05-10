package org.example.utils;

import java.time.Instant;
import java.util.Random;

public class IDFactory
{
    public long generateID(){

        long timestamp = Instant.now().toEpochMilli();

        // Generate a random number
        Random random = new Random();

        long randomNumber = random.nextLong();

        // Combine timestamp and random number to create a unique ID
        long uniqueID = timestamp + randomNumber;

        return uniqueID;
    }
}
