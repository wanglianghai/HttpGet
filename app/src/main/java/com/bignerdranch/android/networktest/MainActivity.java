package com.bignerdranch.android.networktest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.bignerdranch.android.networktest.R.id.response_text;
import static com.bignerdranch.android.networktest.R.id.split_action_bar;

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
     //           sendRequestWithOkHttp();
     /*           HttpUtil.sendRequestWithHttpURLConnection("https://www.baidu.com/", new HttpCallbackListener() {
                    @Override
                    public void onFinish(String response) {
                        showResponse(response);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });*/
                HttpOKUtil.sendRequestWithOkHttp("http://www.baidu.com", new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        showResponse(response.toString());
                        response.close();
                    }
                });
            }
        });
        mResponseText = (TextView) findViewById(response_text);
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://api.douban.com/v2/book/1220562";
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
     //           parseXMLWithPull(contents);
     //           parseXMLWithSAX(contents);
      //          parseJSONWithJSONObject(contents);
                parseJSONWithGSON(contents);
                showResponse(contents);
            }
        }).start();
    }

    private void parseJSONWithGSON(String contents) {
        try {
            JSONObject jObject = new JSONObject(contents);
            JSONArray jArray = jObject.getJSONArray("tags");
            String jAString = jArray.toString();
            Gson gson = new Gson();

// Deserialization
            List<Tags> tags = gson.fromJson(jAString, new TypeToken<List<Tags>>(){}.getType());
            for (Tags t:
                    tags) {
                Log.i(TAG, "parseJSONWithGSON: name:" + t.getName());
                Log.i(TAG, "parseJSONWithGSON: count:" + t.getCount());
            }
// ==> ints2 is same as ints
        } catch (Exception e) {

        }

    }

    private void parseJSONWithJSONObject(String contents) {
        try {
            JSONObject jsonObject = new JSONObject(contents);
            JSONArray jsonArray = jsonObject.getJSONArray("tags");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject itemObject = jsonArray.getJSONObject(i);
                Log.i(TAG, "parseJSONWithJSONObject: count: " + itemObject.getString("count"));
                Log.i(TAG, "parseJSONWithJSONObject: name: " + itemObject.getString("name"));
                Log.i(TAG, "parseJSONWithJSONObject: title: " + itemObject.getString("title"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseXMLWithSAX(String XMLString) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();
            MyHandler handler = new MyHandler();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new StringReader(XMLString)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyHandler extends DefaultHandler{
        private String mNodeName;
        private StringBuilder mId;
        private StringBuilder mVersion;
        private StringBuilder mName;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            mId = new StringBuilder();
            mVersion = new StringBuilder();
            mName = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            mNodeName = localName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            if (mNodeName.equals("id")) {
                mId.append(ch, start, length);
            } else if (mNodeName.equals("name")) {
                mName.append(ch, start, length);
            } else if (mNodeName.equals("version")) {
                mVersion.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equals("app")) {
                Log.i(TAG, "endElement: id " + mId.toString().trim());
                Log.i(TAG, "endElement: name " + mName.toString().trim());
                Log.i(TAG, "endElement: version" + mVersion.toString().trim());
                mId.setLength(0);
                mName.setLength(0);
                mVersion.setLength(0);
            }
        }


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