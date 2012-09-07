package in.verse.logback.nagios;

import java.io.IOException;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class NagiosSender
{
    String nscaWebUrl;
    String nscaWebUserName;
    String nscaWebPassword;
    String hostname;
    String servicename;

    AsyncHttpClient httpclient;

    NagiosResponseCallback callback;

    public NagiosSender(String nscaWebUrl, String nscaWebUserName, String nscaWebPassword, String hostname, String servicename)
    {
        super();
        this.nscaWebUrl = nscaWebUrl;
        this.nscaWebUserName = nscaWebUserName;
        this.nscaWebPassword = nscaWebPassword;
        this.hostname = hostname;
        this.servicename = servicename;

        try
        {
            AsyncHttpClientConfig.Builder cf = new AsyncHttpClientConfig.Builder();
            cf.setMaximumConnectionsPerHost(1);
            cf.setMaximumConnectionsTotal(10);
            cf.setAllowPoolingConnection(true);
            cf.setAllowSslConnectionPool(true);
            cf.setConnectionTimeoutInMs(10000);
            cf.setRequestTimeoutInMs(10000);
            cf.setUserAgent("NING [" + hostname + "/" + servicename + "]");
            httpclient = new AsyncHttpClient(cf.build());
            callback = new NagiosResponseCallback();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void send(String command)
    {
        if (httpclient != null)
        {
            BoundRequestBuilder builder = httpclient.preparePost(nscaWebUrl);
            builder.addParameter("username", nscaWebUserName);
            builder.addParameter("password", nscaWebPassword);
            builder.addParameter("input", command);
            try
            {
                builder.execute(callback);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static class NagiosResponseCallback extends AsyncCompletionHandler<Response>
    {

        @Override
        public Response onCompleted(Response response) throws Exception
        {
            return response;
        }

        @Override
        public void onThrowable(Throwable t)
        {
            // its ok to have exceptions!
            // System.out.println("Exception : " + t.getMessage());
            // t.printStackTrace();
        }
    }
}
