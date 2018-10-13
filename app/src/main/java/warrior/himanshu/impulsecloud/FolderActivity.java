package warrior.himanshu.impulsecloud;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FolderActivity extends AppCompatActivity {

    private ListView listView;
    private String cookie;
    private URL url;
    private JSONObject mainfolder;
    private JSONArray folders, files;
    private int startidx;
    private String fname, purl, newfolderurl, newfileurl;
    private Triplet[] viewArray;
    private ListAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            url = new URL(getIntent().getStringExtra("url"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        cookie = getIntent().getStringExtra("cookie");
        new AsyncTaskRunner().execute();

    }

    private class AsyncTaskRunner extends AsyncTask<Void, String, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(Void... voidsi) {
            try {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", cookie);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String jsontext = ReadAll(reader);
                mainfolder = new JSONObject(jsontext);
                fname = mainfolder.getString("name");
                purl = mainfolder.getString("parent_url");
                newfolderurl = mainfolder.getString("new_folder");
                newfileurl = mainfolder.getString("upload_file");
                folders = (JSONArray) mainfolder.get("folders");
                files = (JSONArray) mainfolder.get("files");
                startidx = folders.length();
                int totallength = folders.length()+files.length();
                viewArray = new Triplet[totallength];
                JSONObject data;
                for (int i = 0; i < startidx; i++) {
                    data = (JSONObject) folders.get(i);
                    viewArray[i] = new Triplet(data.getString("name"), true);
                }
                for (int i = startidx; i < totallength; i++) {
                    data = (JSONObject) files.get(i-startidx);
                    viewArray[i] = new Triplet(data.getString("name"), false);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            progressDialog.cancel();
            listAdapter = new ListAdapter(getApplicationContext(), R.layout.folderview, viewArray);
            listView.setAdapter(listAdapter);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(FolderActivity.this);
            progressDialog.setMessage("folder");
            progressDialog.setTitle("Fetching Details");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        private String ReadAll(BufferedReader reader) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

    }

    public class ListAdapter extends ArrayAdapter<Triplet> implements View.OnClickListener{

        private int resourceLayout;
        private Context mContext;
        private Triplet[] items;

        ListAdapter(Context context, int resource, Triplet[] items) {
            super(context, resource, items);
            this.resourceLayout = resource;
            this.mContext = context;
            this.items = items;
        }

        @Override
        public void onClick(View v){

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            final Triplet p = getItem(position);

            if (p != null) {
                ImageView itemicon = v.findViewById(R.id.itemicon);
                TextView itemname = v.findViewById(R.id.itemname);
                ImageView deleteicon = v.findViewById(R.id.deleteicon);
                if (!p.folder) itemicon.setImageResource(R.drawable.file_icon);
                itemname.setText(p.name);
                itemname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), p.name, Toast.LENGTH_SHORT).show();
                    }
                });
                deleteicon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Delete" + p.name, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            return v;
        }

        @Override
        public Triplet getItem(int position){
            if (position < items.length){
                return items[position];
            }
            return null;
        }

    }

    private class Triplet{
        public String name;
        public Boolean folder;

        public Triplet(String name, Boolean folder) {
            this.name = name;
            this.folder = folder;
        }
    }
}