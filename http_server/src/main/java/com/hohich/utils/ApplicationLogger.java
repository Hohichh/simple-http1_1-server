package com.hohich.utils;

import java.io.IOException;
import java.util.logging.*;


public class ApplicationLogger {
    private static final Logger logger = Logger.getLogger("GlobalLogger");


    private static void loggerConfigure(){
        try{
            logger.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());

            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setLevel(Level.FINEST);
            fileHandler.setFormatter(new SimpleFormatter());

            logger.setUseParentHandlers(false);

            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);
        } catch(IOException e){
            logger.log(Level.SEVERE, "Error initializing logger", e);
        }
    }

    public static Logger getLogger(){
        loggerConfigure();
        return logger;
    }
}
