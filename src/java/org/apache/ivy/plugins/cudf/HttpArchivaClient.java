package org.apache.ivy.plugins.cudf;

import com.zenika.cudf.model.CUDFDescriptor;
import com.zenika.cudf.parser.DefaultDeserializer;
import com.zenika.cudf.parser.ParsingException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Antoine Rouaze <antoine.rouaze@zenika.com>
 */
public class HttpArchivaClient implements ArchivaClient {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 9091;

    private static final String CUDF_WEB_SERVICE_PATH = "archiva/archivaServices/cudfService/";

    private String host;
    private int port;

    private String proxyHost;
    private int proxyPort;

    public HttpArchivaClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public HttpArchivaClient(String host) {
        this(host, DEFAULT_PORT);
    }

    public HttpArchivaClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public CUDFDescriptor resolve(CUDFDescriptor descriptor) {
        HttpClient client = getHttpClient();
        PostMethod postMethod = new PostMethod(CUDF_WEB_SERVICE_PATH + "resolve");
        RequestEntity requestEntity = new CUDFRequestEntity(descriptor);
        postMethod.setRequestEntity(requestEntity);
        try {
            int statusCode = client.executeMethod(postMethod);
            checkStatusCode(statusCode);
            InputStream stream = postMethod.getResponseBodyAsStream();
            DefaultDeserializer deserializer = new DefaultDeserializer(new InputStreamReader(stream));
            return deserializer.deserialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParsingException e) {
            throw new RuntimeException("Unable to parse CUDF response.", e);
        } finally {
            postMethod.releaseConnection();
        }
    }

    private void checkStatusCode(int statusCode) {
        if (statusCode == 404) {
            throw new RuntimeException("Unable to find Archiva server. Verify your configuration");
        } else if (statusCode >= 500 && statusCode < 600) {
            throw new RuntimeException("Internal server error. Please contact your Archiva administrator");
        }
    }

    private HttpClient getHttpClient() {
        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost(host, port);
        if (isProxyConfigured()) {
            hostConfiguration.setProxy(proxyHost, proxyPort);
        }
        HttpClient httpClient = new HttpClient();
        httpClient.setHostConfiguration(hostConfiguration);
        return httpClient;
    }

    public boolean isProxyConfigured() {
        return (proxyHost != null && !proxyHost.isEmpty()) && proxyPort != 0;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
