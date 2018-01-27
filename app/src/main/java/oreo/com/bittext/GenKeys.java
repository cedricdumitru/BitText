package oreo.com.bittext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;

/**
 * Created by rkark on 1/27/2018.
 */

public class GenKeys {
    public PrivateKey privateKey;
    public PublicKey publicKey;
    public void Gen(){
            generateKeyPair();


    }
    public void generateKeyPair() {
        try {
          KeyPair vals =KeyPairGenerator.getInstance("RSA").generateKeyPair();
          privateKey = vals.getPrivate();
          publicKey = vals.getPublic();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getPublicKey(){
        return publicKey.toString();
    }
    public String getPrivateKey(){
        return privateKey.toString();
    }
}
