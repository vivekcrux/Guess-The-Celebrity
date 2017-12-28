package com.example.vivek.guessthecelebrity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebs = new ArrayList<String>();
    ArrayList<String> names = new ArrayList<String>();
    ImageView imageView;
    int chosenCeleb = 0;
    int correctPosition = 0;
    String[] answers = new String[4];
    Button button0;
    Button button1;
    Button button2;
    Button button3;
    ProgressBar progressBar;

    public void checkAnswer(View view){
        if(view.getTag().toString().equals(Integer.toString(correctPosition))) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! Correct is " + names.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        while (!generateQuestion());

    }

    public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try{

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();
                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (Exception e) {

                e.printStackTrace();

            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... urls) {

            String finalResult = "";
            for (int i = 0; i < urls.length; i++) {
                try {

                    String result = "";
                    URL url = new URL(urls[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    InputStream in = connection.getInputStream();

                    InputStreamReader reader = new InputStreamReader(in);

                    int data = reader.read();

                    while (data != -1) {
                        result += (char) data;
                        data = reader.read();
                    }

                    String contents[] = result.split("<!-- Begin comScore Tag -->");
                    result = contents[0];
                    finalResult += result;

                    publishProgress(i);

                    //Log.i("result",result);

                } catch (Exception e) {

                    e.printStackTrace();

                }
            }

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(finalResult);

            while(m.find()){
                celebs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(finalResult);

            while(m.find()){
                names.add(m.group(1));
            }

            return finalResult;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            while (!generateQuestion());
            ((TextView)findViewById(R.id.waitTextView)).setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.GONE);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        button0 = (Button)findViewById(R.id.button0);
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(5);

        //DownloadTask webContent = new DownloadTask();

        String urls[] = new String[5];
        for(int i = 1; i < 6; i++ ) {

            urls[i-1] = "http://www.santabanta.com/images/gallery/?page=" + Integer.toString(i);

        }

        DownloadTask webContent = new DownloadTask();
        webContent.execute(urls);


    }
    public boolean generateQuestion(){

        //Question
        Random random = new Random();
        chosenCeleb = random.nextInt(celebs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap imageDownloaded = null;
        try {
            imageDownloaded = imageTask.execute(celebs.get(chosenCeleb)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(imageDownloaded);

        System.out.println(imageDownloaded);
        if(imageDownloaded == null){
            return false;
        }

        correctPosition = random.nextInt(4);
        int wrongAnswer;

        for(int i =0; i < 4; i++){
            if(i == correctPosition){
                answers[i] = names.get(chosenCeleb);
            } else {

                wrongAnswer = random.nextInt(celebs.size());

                while(wrongAnswer == chosenCeleb){
                    wrongAnswer = random.nextInt(celebs.size());
                }
                answers[i] = names.get(wrongAnswer);
            }
        }

        button0.setText(answers[0]);
        button1.setText(answers[1]);
        button2.setText(answers[2]);
        button3.setText(answers[3]);

        return true;

    }
}
