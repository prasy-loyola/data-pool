package com.ps;

import java.io.File;

public class Config {

    public static final String RESOURCE_PATH = new File(".").getAbsolutePath() + File.separator +  System.getProperty("data.resourcePath","");
    public static final String WORKBOOK_NAME = System.getProperty("data.excel.workbookName","UserData");
    public static final String WORKSHEET_NAME = System.getProperty("data.excel.worksheetName","LoginDetails");
    public static final long USER_TIMEOUT = Long.parseLong(System.getProperty("data.timeout","60000"));
    public static final int PORT = Integer.parseInt(System.getenv("PORT")!=null ? System.getenv("PORT") : "2233");
    public static final String DEFAULT_ID_FIELD = System.getProperty("data.excel.default.id","keySet");
    public static final String DATE_FORMAT = "dd-MMM hh.mm.ss aa";

    public static final String POSTGRES_DB_CONNECTION_URL = System.getenv("JDBC_DATABASE_URL") ;
    public static final String DATABASE_URL = System.getenv("DATABASE_URL") ;

}
