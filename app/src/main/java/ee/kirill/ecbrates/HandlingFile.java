package ee.kirill.ecbrates;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class HandlingFile {

    private static final String TAG = "HandlingFile";
    Context fileContext;

    public HandlingFile(Context fileContext) {
        this.fileContext = fileContext;
    }

    public void writeToFile(String data, String fileName) {
        try {
            FileOutputStream outputStreamWriter = fileContext.getApplicationContext().
                    openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStreamWriter.write(data.getBytes());
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }
}
