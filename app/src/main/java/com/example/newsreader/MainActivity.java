package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> id = new ArrayList<String>();
    ArrayList<String> title = new ArrayList<String>();
    ArrayList<String> url = new ArrayList<String>();

    ListView listView;

    public class idTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int data = inputStreamReader.read();

                while ( data != -1){
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
            return result;
        }
    }

    public class jsonTask extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int data = inputStreamReader.read();

                while ( data != -1){
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,title);
        listView.setAdapter(arrayAdapter);
        idTask idTask = new idTask();
        String ids = null;
        try {
            ids = idTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Pattern p = Pattern.compile(" (.*?),");
        Matcher m = p.matcher(ids);
        while (m.find()){
            id.add(m.group(1));
        }
        SQLiteDatabase database = this.openOrCreateDatabase("News",MODE_PRIVATE,null);

        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+"news"+"'", null);

        if (cursor == null || cursor.getCount()<=0) {
            database.execSQL("CREATE TABLE IF NOT EXISTS news (title VARCHAR, url VARCHAR)");
            try {
                for (int i = 0; i < 10; i++) {
                    jsonTask jsonTask = new jsonTask();
                    String s = jsonTask.execute("https://hacker-news.firebaseio.com/v0/item/" + id.get(i) + ".json?print=pretty").get();
                    JSONObject jsonObject = new JSONObject(s);
                    String titleS = jsonObject.getString("title");
                    title.add(titleS);
                    String urls = jsonObject.getString("url");
                    url.add(urls);
                    listView.invalidateViews();
                    database.execSQL("INSERT INTO news(title, url) VALUES ('"+titleS+"','"+urls+"')");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }try {
            Cursor c = database.rawQuery("SELECT * FROM news", null);

            int titleIndex = c.getColumnIndex("title");
            int urlIndex = c.getColumnIndex("url");
            c.moveToFirst();

            while (c != null) {
                title.add(c.getString(titleIndex));
                url.add(c.getString(urlIndex));
                c.moveToNext();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),SubActivity.class);
                intent.putExtra("url", url.get(i).toString());
                startActivity(intent);
            }
        });
    }
}