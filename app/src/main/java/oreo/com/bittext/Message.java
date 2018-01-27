package oreo.com.bittext;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;

/**
 * Created by rkark on 1/27/2018.
 */


public class Message extends Activity implements View.OnClickListener{
    private ChatArrayAdapter chatArrayAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView recep;
    ListView messages;
    EditText message;
    Button send;
    int counter = 1;
    String prevHash = "0";
    List<String> list = new ArrayList<>();
    PublicKey pubKey;
    long lastTime = System.currentTimeMillis();
    int times = 1;
    String privateKeyText = "MIICWwIBAAKBgQCBqExVB1Lc3RZ0nBS0YZNLs4dkZOmoTIlXGtFSiyDF93/IctJA5CPUNExAFGZ+X1j" +
            "WDO4gMMRe9n1hYMXK2UdSy5Yn5On+y38JQQqeWira6MBwoFUD8O0J27lwpA6H64WHyx0Qev1dSScTRcfB0svv2qBcN5K0L+cPY2SP" +
            "mNsgAQIDAQABAoGAbXPBXFYeYUxR1IaeA9gLG/Cl7t4xUV2q4tlEs0W3mObh/DjRvO3aQR0U8VNHYo7i029+5bXFT9UM6tNipWbeiZGmdH" +
            "f6gR3BcRe2U6shGrrNbdtsZV0ldFbaaxiBjC1FMBc2SoXwqdPZWiGHam6isCKHUo1fDBcWbWitAlsFWUECQQDaUODPY8PoCCA1hswhcBeEkcv8" +
            "Ln5vF+TJRvMN/ts3AooDzcn64fuATpod1BCZPFqDATdGujiVlVqTVFUGNlbNAkEAmAm0l2hjYqXDJsNY8WRHqHrvTZwbQe0NP9ZgGyhTXfAm52428" +
            "6BVkO7qazBBH7xFJ697na04XiJgSR7bAo0mBQJAKC/7MjILPgtA4pmNuhoIOj8cba+hVpHva53D4C+p6tZs0YZwR96RjWzqsdL3uz2A69ytcDeXcvGr" +
            "rOLczlNgQJAUWb44wwIElwwFYQIDg6j80gG8mW/jleufVx5TnET2broCYdQMrbIuXXB2ywV4jj6SdpB9FU3VedP6DZ2CnKqRQJAfUpVbbxW3Tq7Rh26YDD7" +
            "PAI4T8DQPrSGn7SzkU4e59IHmIMtDBk3NQY/aw7c/Nqq2RJuUO6LzJXBgeVI5s0XEw==";
    String publicKeyText = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBqExVB1Lc3RZ0nBS0YZNLs4dkZOmoTIlXGtFSiyDF93/IctJA5CPUNExAFGZ+X" +
            "1jWDO4gMMRe9n1hYMXK2UdSy5Yn5On+y38JQQqeWira6MBwoFUD8O0J27lwpA6H64WHyx0Qev1dSScTRcfB0svv2qBcN5K0L+cPY2SPmNsgAQIDAQAB";
    private boolean side = true;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);
        String name = getIntent().getStringExtra("name");
        recep = (TextView)findViewById(R.id.recep);
        messages = (ListView)findViewById(R.id.messages);
        message = (EditText)findViewById(R.id.message);
        send = (Button)findViewById(R.id.send);
        findViewById(R.id.send).setOnClickListener(this);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        messages.setAdapter(chatArrayAdapter);
        recep.setText(name);
        messages.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messages.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                messages.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        db.collection("blockchain")
                .whereEqualTo("test", "test")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<String> cities = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            if (doc.get("message") != null) {
                                if(Long.parseLong(doc.get("timeStamp").toString()) > lastTime) {
                                    if (doc.get("from").equals(recep.getText().toString())) {
                                        chatArrayAdapter.add(new ChatMessage(side, message.getText().toString()));
                                        message.setText("");
                                        side = false;
                                    } else {
                                        chatArrayAdapter.add(new ChatMessage(side, message.getText().toString()));
                                        message.setText("");
                                        side = true;
                                    }
                                    lastTime = doc.getLong("timeStamp");
                                }
                                cities.add(doc.getString("message"));
                            }
                        }
                        Log.d(TAG, "Current cites in CA: " + cities);
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send:
               addToBlock();

        }
    }
    private boolean addToBlock() {
        String receipient = recep.getText().toString();
        String from = "";
        if(receipient.startsWith("R")){
            from = "Skyler Zheng";
        }else{
            from = "Rohith Reddy Karkala";
        }
        long timeStamp = System.currentTimeMillis();
        String text = message.getText().toString();
        getPrevHash();
        Block block = new Block(prevHash,text, receipient, from);
        System.out.println("Block #"+counter+++" has been generated");
        block.mineHash((int)Math.random()*100+1);
        Map<String, Object> addBlock = new HashMap<>();
        addBlock.put("curHash", block.getCurrentHash());
        addBlock.put("from", from);
        addBlock.put("message", text);
        addBlock.put("prevHash", prevHash);
        addBlock.put("to", receipient);
        addBlock.put("test", "test");
        addBlock.put("timeStamp", System.currentTimeMillis());
        System.out.println(times);
        String name = "block"+System.currentTimeMillis();
        db.collection("blockchain").document(name).set(addBlock);
        //side = !side;
        return true;
    }
    public void getPrevHash(){
        db.collection("blockchain").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                List<DocumentSnapshot> vals = documentSnapshots.getDocuments();
                times = vals.size();
                for (DocumentSnapshot a : vals){
                    try {
                        prevHash = a.get("curHash").toString();
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
    }
}
