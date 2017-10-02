package testFramework;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Properties;

class TestRunner {
    private static Logger logger = LoggerFactory.getLogger(TestRunner.class);
    private SutHttpClient sutHttpClient;
    private Properties prop = new Properties();
    private InputStream input = null;

    TestRunner() {
        try {
            String filename = "config.properties";
            input = TestRunner.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                logger.error("Sorry, unable to find {} properties", filename);
                logger.warn("Using default values: sut_address=\"http://localhost:8787/\", " +
                        "server_key=\"26rvr596rgq0mpac\"");
                prop.setProperty("sut_address", "http://localhost:8787/");
                prop.setProperty("server_key", "26rvr596rgq0mpac");
                return;
            }
            prop.load(input);

            logger.debug("Using {} address for SUT", prop.getProperty("sut_address"));
            logger.debug("Using {} server key", prop.getProperty("server_key"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void runAllTests(String testDirectoryName, String resultFileName) {
        logger.debug("Run tests for: {}", testDirectoryName);
        logger.debug("Put result to: {}", resultFileName);
        try {
            this.sutHttpClient = new SutHttpClient(prop.getProperty("sut_address"));
        } catch (URISyntaxException e) {
            logger.error("{} is not valid address", prop.getProperty("sut_address"));
            e.printStackTrace();
            return;
        }

        FileFilter jsonFileList = new JsonFiles();
        FileFilter allFileList = new AllFiles();
        RecursiveFileLister recursiveFileLister = null;
        try {
            recursiveFileLister = new RecursiveFileLister(testDirectoryName, allFileList);
        } catch (FileNotFoundException e) {
            logger.error("{} is not found", testDirectoryName);
            e.printStackTrace();
            return;
        }
        PrintWriter resultFile = null;
        try {
            resultFile = new PrintWriter(resultFileName);
        } catch (FileNotFoundException ignored) {
        }

        for (File f : recursiveFileLister.getFilesList()) {
            resultFile.write(runSingleTest(f));
        }
        resultFile.close();
    }

    private String runSingleTest(File testFile) {
        JSONObject requestBody = null;
        try {
            requestBody = readJsonFromFile(testFile);
        } catch (ParseException e) {
            logger.error("Unable to parse {}", testFile.getAbsoluteFile());
            return "Error";
        } catch (IOException e) {
            logger.error("File {} not found or parsing error", testFile.getAbsoluteFile());
            e.printStackTrace();
            return "Error";
        }

        if (requestBody.containsKey("server_key")) {
            requestBody.put("server_key", prop.getProperty("server_key"));
        }

        ResponseResult singleResult = null;
        try {
            singleResult = sutHttpClient.runTest(requestBody);
        } catch (IOException e) {
            logger.error("SUT Connection issues");
            e.printStackTrace();
            return "Error";
        }
        String absFilePath = testFile.getAbsoluteFile().toString();
        String requestStatusCode = singleResult.getStatusCode();
        String requestStatus = getResponseStatus(singleResult.getResponseBody());

        String resultString = String.format("%s::%s::%s\n", absFilePath, requestStatusCode, requestStatus);
        logger.debug("{} test completed with the result: {}", absFilePath, resultString);

        return resultString;
    }

    private JSONObject readJsonFromFile(File file) throws IOException, ParseException {
        logger.debug("Parsing {} file", file.getAbsoluteFile());
        JSONParser parser = new JSONParser();
        Reader jsonReader = new FileReader(file);
        Object obj = parser.parse(jsonReader);

        return (JSONObject) obj;
    }

    private String getResponseStatus(String responseBody) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(responseBody);
        } catch (ParseException e) {
            logger.error("Unable to parse server response: {}", responseBody);
            e.printStackTrace();
        }
        JSONObject jsonObject =(JSONObject) obj;

        return jsonObject.get("status").toString();
    }
}
