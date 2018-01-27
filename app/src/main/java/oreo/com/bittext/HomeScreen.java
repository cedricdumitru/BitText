package oreo.com.bittext;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class HomeScreen extends Activity{


    TextView header;
    ListView chats;
    FloatingActionButton add;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String name;
    boolean has;
    String fileName = "blockchain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        GenKeys gk = new GenKeys();
        gk.generateKeyPair();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        String publicKey = new String(Base64.encode(gk.publicKey.getEncoded(), 0));
        String privateKey = new String(Base64.encode(gk.privateKey.getEncoded(), 0));
      //  if(!(preferences.getString("public", "") !=null)) {
            editor.putString("public", publicKey);
            editor.putString("private", privateKey);
            editor.apply();
       // }
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        name = acct.getDisplayName();
        header = (TextView)findViewById(R.id.header);
        header.setText(name);
        chats = (ListView)findViewById(R.id.chats);
        add = (FloatingActionButton)findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I = new Intent(getBaseContext(), CreateChat.class);
                I.putExtra("name", name);
                startActivity(I);
            }
        });
        final List<String> chatNames = new ArrayList<>();
        chatNames.add("Rohith K");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, chatNames);
        chats.setAdapter(arrayAdapter);
        addUsers(name);
        chatNames.add("Hello");

        chats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent I = new Intent(getBaseContext(), Message.class);
                I.putExtra("name", chatNames.get(i));
                startActivity(I);
            }
        });

    }
    public void addUsers(final String name){
        has = true;
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                List<DocumentSnapshot> vals = documentSnapshots.getDocuments();
                for (DocumentSnapshot a : vals){
                    try {
                        String temp = a.get("username").toString();
                        System.out.println(temp);
                        if (temp.equals(name)) {
                            has = false;
                            break;
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error Getting Users", Toast.LENGTH_LONG).show();
            }
        });
        if(!has) return;
        Map<String, Object> user = new HashMap<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        user.put("username", name);
        user.put("public", preferences.getString("public" , ""));
        db.collection("users").document(""+name)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }


}
