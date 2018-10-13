package warrior.himanshu.impulsecloud;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername, mPassword;
    private Button mLogin;
    private String username, password, cookie;
    private String url =  "http://10.42.0.1:8000/";
    private String Post_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                Post_data = "username="+username+"&password="+password;
                new BackgroundLogin().execute();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class BackgroundLogin extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected Boolean doInBackground(Void... x) {
            JSONObject data;
            Boolean a;
            try {
                a = sendPost();
            } catch (IOException e) {
                a = false;
//                Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                a = false;
            }
            return a;
        }
        @Override
        protected void onPostExecute(Boolean x){
            progressDialog.cancel();
            String xy = "Incorrect Credentials";
            if(x){
                xy = "Correct Credentials";
            }
            Toast.makeText(getApplicationContext(), xy, Toast.LENGTH_SHORT).show();
//            arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,result);
//            listView.setAdapter(arrayAdapter);
//            mMessageClickedHandler = new AdapterView.OnItemClickListener() {
//                public void onItemClick(AdapterView parent, View v, int position, long id) {
            if(x){
                    Intent intent = new Intent(getApplicationContext(),FolderActivity.class);
                    intent.putExtra("url", url+"api/");
                    intent.putExtra("cookie", cookie);
                    startActivity(intent);
                }
//            };
//            listView.setOnItemClickListener(mMessageClickedHandler);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Searching",
                    "Wait for few seconds");
        }

        private boolean sendPost() throws IOException, JSONException {
            URL obj = new URL(url+"api/rest-auth/login/");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
//            con.setRequestProperty("User-Agent", USER_AGENT);

            // For POST only - START
            con.setDoOutput(true);
            OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream());
            os.write(Post_data);
            os.flush();
            os.close();
            // For POST only - END
            cookie = con.getHeaderField("Set-Cookie");
//            System.out.println("POST Response Code :: " + responseCode);
            StringBuffer response;

                con.connect();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
//                System.out.println(response.toString());
//            URL obj1 = new URL(url+"api/");
//            HttpURLConnection con1 = (HttpURLConnection) obj1.openConnection();
//            con1.setRequestMethod("GET");
//            con1.setRequestProperty("Cookie", cookie);
//            con1.connect();
//
//            in = new BufferedReader(new InputStreamReader(
//                    con1.getInputStream()));
//            response = new StringBuffer();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
            JSONObject ob = new JSONObject(response.toString());
            if (ob.has("key")) return true;
            else return false;

        }
    }

        private String ReadAll(BufferedReader reader) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while((cp = reader.read()) != -1){
                sb.append((char) cp);
            }
            return sb.toString();
        }

}
