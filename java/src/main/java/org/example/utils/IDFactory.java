package org.example.utils;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class IDFactory
{
    private static final AtomicLong counter = new AtomicLong(0);
    public long generateID(){
        return counter.getAndIncrement();
    }
}
