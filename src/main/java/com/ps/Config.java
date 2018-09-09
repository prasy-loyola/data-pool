package com.ps;

import java.io.File;

public class Config {

    public static final String RESOURCE_PATH = new File(".").getAbsolutePath() + File.separator +  System.getProperty("data.resourcePath","");
    public static final String WORKBOOK_NAME = System.getProperty("data.excel.workbookName","UserData");
    public static final String WORKSHEET_NAME = System.getProperty("data.excel.worksheetName","LoginDetails");
    public static final long USER_TIMEOUT = Long.parseLong(System.getProperty("data.timeout","60000"));
    public static final int PORT = Integer.parseInt(System.getenv("PORT"));
    public static final String DEFAULT_ID_FIELD = System.getProperty("data.excel.default.id","keySet");
    public static final String DATE_FORMAT = "dd-MMM hh.mm.ss aa";
}
