package warrior.himanshu.impulsecloud;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import pub.devrel.easypermissions.EasyPermissions;


public class FolderActivity extends AppCompatActivity {

    private ListView listView;
    private String cookie;
    private String url1;
    private int startidx;
    private String mainfname, purl, newfolderurl, newfileurl, dialog_message, dialog_title;
    private Triplet[] viewArray;
    private ListAdapter listAdapter;
    private Boolean isOpen = false;
    private static final String TAG = FolderActivity.class.getSimpleName();
    private static final int WRITE_REQUEST_CODE = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final FloatingActionButton add_folder = findViewById(R.id.add_folder);
        final FloatingActionButton add_file = findViewById(R.id.add_file);
        final ConstraintLayout folder_layout = findViewById(R.id.add_folder_layout);
        final EditText new_folder_name = findViewById(R.id.newFolderText);
        final Button btn_add_folder = findViewById(R.id.btn_addfolder);
        final Button btn_cancel_folder = findViewById(R.id.btn_cancelfolder);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (isOpen) {
                    add_folder.setVisibility(View.INVISIBLE);
                    add_file.setVisibility(View.INVISIBLE);
                    isOpen = false;
                }
                else{
                    add_folder.setVisibility(View.VISIBLE);
                    add_file.setVisibility(View.VISIBLE);
                    isOpen = true;
                }
            }
        });
        add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folder_layout.setVisibility(View.VISIBLE);
            }
        });
        btn_cancel_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                folder_layout.setVisibility(View.INVISIBLE);
            }
        });
        btn_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String x = new_folder_name.getText().toString();
                if (x.length()!=0){
                    NewFolderTask newFolderTask = new NewFolderTask(x);
                    newFolderTask.execute(newfolderurl);
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listview);
        url1 = getIntent().getStringExtra("url");
        cookie = getIntent().getStringExtra("cookie");
        dialog_title = getIntent().getStringExtra("title");
        dialog_message = getIntent().getStringExtra("msg");
        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
        try {
            asyncTaskRunner.execute(new URL(url1));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<URL, String, Void> {
        ProgressDialog progressDialog;

        @Override
        protected Void doInBackground(URL... urls) {
            try {
                URL uuu = new URL(url1);
                HttpURLConnection conn = (HttpURLConnection) uuu.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", cookie);
                conn.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String json_text = ReadAll(reader);
                JSONObject main_folder = new JSONObject(json_text);
                mainfname = main_folder.getString("name");
                purl = main_folder.getString("parent_url");
                newfolderurl = main_folder.getString("new_folder");
                newfileurl = main_folder.getString("upload_file");
                JSONArray folders = (JSONArray) main_folder.get("folders");
                JSONArray files = (JSONArray) main_folder.get("files");
                startidx = folders.length();
                int total_length = folders.length()+files.length();
                viewArray = new Triplet[total_length];
                JSONObject data;
                for (int i = 0; i < startidx; i++) {
                    data = (JSONObject) folders.get(i);
                    viewArray[i] = new Triplet(
                            data.getString("name"),
                            data.getString("folder_url"),
                            data.getString("delete_url"),
                            true);
                }
                for (int i = startidx; i < total_length; i++) {
                    data = (JSONObject) files.get(i-startidx);
                    viewArray[i] = new Triplet(
                            data.getString("name"),
                            data.getString("download_url"),
                            data.getString("delete_url"),
                            false);
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
            progressDialog.setMessage(dialog_message);
            progressDialog.setTitle(dialog_title);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        String fname;

        public DeleteTask(String fname) {
            this.fname = fname;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                URL delete_url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) delete_url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", cookie);
                conn.connect();
                conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            progressDialog.cancel();
            Toast.makeText(getApplicationContext(), "Deleted "+fname, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
            intent.putExtra("url", url1);
            intent.putExtra("cookie", cookie);
            intent.putExtra("title", mainfname);
            intent.putExtra("msg", "Refreshing");
            finish();
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(FolderActivity.this);
            progressDialog.setMessage("Deleting");
            progressDialog.setTitle(fname);
            progressDialog.show();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class NewFolderTask extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog;
        String fname;

        public NewFolderTask(String fname) {
            this.fname = fname;
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                URL folder_url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) folder_url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie", cookie);
                conn.setDoOutput(true);
                OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
                os.write("name="+fname);
                os.flush();
                os.close();
                conn.connect();
                conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            progressDialog.cancel();
            Toast.makeText(getApplicationContext(), "Successfully added "+fname, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
            intent.putExtra("url", url1);
            intent.putExtra("cookie", cookie);
            intent.putExtra("title", mainfname);
            intent.putExtra("msg", "Refreshing");
            finish();
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(FolderActivity.this);
            progressDialog.setMessage("Adding Folder");
            progressDialog.setTitle(fname);
            progressDialog.show();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(FolderActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                @SuppressLint("SimpleDateFormat") String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1, f_url[0].length());

                //Append timestamp to file name
//                fileName = timestamp + "_" + fileName;

                //External directory path to save file
                folder = Environment.getExternalStorageDirectory() + File.separator + "ImpulseCloud/";

                //Create androiddeft folder if it does not exist
                File directory = new File(folder);

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }

    public class ListAdapter extends ArrayAdapter<Triplet>{

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
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(mContext);
                v = vi.inflate(resourceLayout, null);
            }

            final Triplet p = getItem(position);

            if (p != null) {
                ImageView item_icon = v.findViewById(R.id.itemicon);
                TextView item_name = v.findViewById(R.id.itemname);
                ImageView delete_icon = v.findViewById(R.id.deleteicon);
                if (p.folder) item_icon.setImageResource(R.drawable.folder_icon);
                else item_icon.setImageResource(R.drawable.file_icon);
                item_name.setText(p.name);
                delete_icon.setImageResource(R.drawable.delete_icon);
                item_name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (p.folder) folder_click(p);
                        else file_click(p.open_url);
                    }
                });
                delete_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delete_item(p);
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

        public void folder_click(Triplet data){
            Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
            intent.putExtra("url", data.open_url);
            intent.putExtra("cookie", cookie);
            intent.putExtra("title", data.name);
            intent.putExtra("msg", "Opening Folder");
            startActivity(intent);
        }

        @SuppressLint("InlinedApi")
        public void file_click(String download_url){
//            Toast.makeText(getApplicationContext(), "Download", Toast.LENGTH_SHORT).show();
            if (CheckForSDCard.isSDCardPresent()) {

                //check if app has permission to write to the external storage.
                if (EasyPermissions.hasPermissions(FolderActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //Get the URL entered
                    new DownloadFile().execute(download_url);

                } else {
                    //If permission is not present request for the same.
                    EasyPermissions.requestPermissions(FolderActivity.this, getString(R.string.write_file), WRITE_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }


            } else {
                Toast.makeText(getApplicationContext(),
                        "SD Card not found", Toast.LENGTH_LONG).show();

            }
        }

        public void delete_item(Triplet data){
            DeleteTask deleteTask = new DeleteTask(data.name);
            deleteTask.execute(data.delete_url);
        }

    }

    private class Triplet{
        public String name, open_url, delete_url;
        public Boolean folder;

        public Triplet(String name, String open_url, String delete_url, Boolean folder) {
            this.name = name;
            this.open_url = open_url;
            this.delete_url = delete_url;
            this.folder = folder;
        }

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