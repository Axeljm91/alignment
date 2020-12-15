package com.iba.ialign.filters;

import org.apache.log4j.spi.LoggingEvent;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLoggingFilter extends org.apache.log4j.spi.Filter implements Filter {
    public boolean isLoggable(LogRecord record) {
        if (  !record.getLevel().equals(Level.FINEST)
                && record.getMessage().contains("origin=BCREU-PROXY")){
            return false;
        }
        return true;
    }

    @Override
    public int decide(LoggingEvent event) {
        return 0;
    }
}