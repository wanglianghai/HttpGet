package com.bignerdranch.android.networktest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.bignerdranch.android.networktest.R.id.response_text;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button mSendRequest;
    private TextView mResponseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendRequest = (Button) findViewById(R.id.send_request);
        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
     //           sendRequestWithHttpURLConnection();
                sendRequestWithOkHttp();
            }
        });
        mResponseText = (TextView) findViewById(response_text);
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://10.0.2.2:8081/LeaveMessageBorderProject/NewFile.xml";
                String contents = null;
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    contents =  response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                parseXMLWithPull(contents);
                showResponse(contents);
            }
        }).start();
    }

    private void parseXMLWithPull(String XMLString) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput( new StringReader( XMLString ) );
            int eventType = xpp.getEventType();
            String id = null;
            String name = null;
            String version = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String xppName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (xppName.equals("id")) {
                            id = xpp.nextText();
                        } else if (xppName.equals("name")) {
                            name = xpp.nextText();
                        } else if (xppName.equals("version")) {
                            version = xpp.nextText();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (xppName.equals("app")) {
                            Log.i(TAG, "parseXMLWithPull: id:" + id );
                            Log.i(TAG, "parseXMLWithPull: name:" + name );
                            Log.i(TAG, "parseXMLWithPull: version:" + version );
                        }
                        break;

                    default:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("https://www.baidu.com/");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    readStream(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void readStream(InputStream in) {
        BufferedReader reader = null;
        try {
            StringBuilder response = new StringBuilder();
            String line;
            reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            showResponse(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

  /*  private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("https://www.baidu.com/");
                    connection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(connection.getInputStream());
                    readStream(in);
                    *//*reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                   // connection.setRequestMethod("POST");
                 //   DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                //    out.writeBytes("username=admin&password=123456");
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    showResponse(response.toString());*//*
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }*/

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mResponseText.setText(response);
            }
        });
    }
}