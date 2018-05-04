package com.sidneyjackson.textron;

/**
 * Created by sidneyjackson on 5/3/18.
 */

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class JavaScriptInterface {

    @JavascriptInterface
    public String getFileContents(String assetName){
        return readAssetsContent(MainActivity.getContext(), assetName);
    }

    //Read resources from "assets" folder in string
    public String readAssetsContent(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("error", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("error", "Error closing asset " + name);
                }
            }
        }

        return null;
    }

}