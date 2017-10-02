package testframework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SutHttpClient {
    private URIBuilder urlBuilder;
    private static Logger logger = LoggerFactory.getLogger(SutHttpClient.class);

    SutHttpClient(String url) throws URISyntaxException {
        this.urlBuilder = new URIBuilder(url);
    }

    private ResponseResult postJsonRequest(String path, JSONObject body) throws IOException {
        String requestBody = body.toJSONString();

        String url =  this.urlBuilder.setPath(path).toString();
        HttpPost request = new HttpPost(url);
        logger.debug("POST request url: {}, body: {}", url, requestBody);

        StringEntity params = new StringEntity(requestBody);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpResponse response = httpClient.execute(request);

        InputStream inputStream = response.getEntity().getContent();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        httpClient.close();
        ResponseResult responseResult =
                new ResponseResult(Integer.toString(response.getStatusLine().getStatusCode()),
                result.toString("UTF-8"));

        logger.debug("POST response status code: {}, body: {}", responseResult.getStatusCode(),
                responseResult.getResponseBody());

        return responseResult;
    }

    ResponseResult runTest(JSONObject body) throws IOException {
        return postJsonRequest("test", body);
    }
}

class ResponseResult {
    private String statusCode;
    private String responseBody;

    ResponseResult(String statusCode, String responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    String getStatusCode() {
        return statusCode;
    }

    String getResponseBody() {
        return responseBody;
    }
}
