package com.matburt.mobileorg.Capture;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.matburt.mobileorg.MobileOrgApplication;
import com.matburt.mobileorg.Parsing.EditNode;
import com.matburt.mobileorg.service.DataController;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateEditNote
{
    private SharedPreferences appSettings;
    private DataController controller;
    private Activity appactivity;
    public static final String LT = "MobileOrg";

    CreateEditNote(Activity parentActivity, DataController controller) {
        this.appactivity = parentActivity;
        this.appSettings = PreferenceManager.getDefaultSharedPreferences(
                                    parentActivity.getBaseContext());
        this.controller = controller;
    }

    public void writeNewNote(String message) {
        this.writeNote(this.transformCreateBuffer(message));
    }

    public void editNote(String edittype,
                    String nodeId,
                    String nodeTitle,
                    String oldValue,
                    String newValue) {
        this.writeNote(this.transformEditBuffer(edittype, nodeId,
                                                nodeTitle, oldValue, newValue));
     // Store it in the in-memory edit list, too
        MobileOrgApplication appInst = (MobileOrgApplication) this.appactivity.getApplication();
        appInst.edits.add(new EditNode(edittype, nodeId, nodeTitle, oldValue, newValue));
    }

    public boolean writeNote(String message) {
        String storageMode = this.appSettings.getString("storageMode", "");
        BufferedWriter writer = new BufferedWriter(new StringWriter());
        
        if (storageMode.equals("internal") || storageMode == null) {
            FileOutputStream fs;
            try {
                fs = this.appactivity.openFileOutput("mobileorg.org", Context.MODE_APPEND);
                writer = new BufferedWriter(new OutputStreamWriter(fs));
            }
            catch (java.io.FileNotFoundException e) {
                Log.e(LT, "Caught FNFE trying to open mobileorg.org file");
            }
            catch (java.io.IOException e) {
                Log.e(LT, "IO Exception initializing writer on file mobileorg.org");
            }
        }
        else if (storageMode.equals("sdcard")) {
            try {
                File root = Environment.getExternalStorageDirectory();
                File morgDir = new File(root, "mobileorg");
                morgDir.mkdir();
                if (morgDir.canWrite()){
                    File orgFileCard = new File(morgDir, "mobileorg.org");
                    FileWriter orgFWriter = new FileWriter(orgFileCard, true);
                    writer = new BufferedWriter(orgFWriter);
                }
                else {
                    Log.e(LT, "Write permission denied");
                    return false;
                }
            } catch (java.io.IOException e) {
                Log.e(LT, "IO Exception initializing writer on sdcard file");
                return false;
            }
        }
        else {
            Log.e(LT, "Unknown storage mechanism " + storageMode);
            return false;
        }

        try {
            writer.write(message);
            controller.addOrUpdateFile("mobileorg.org", "New Notes","");
            writer.flush();
            writer.close();
        }
        catch (java.io.IOException e) {
            Log.e(LT, "IO Exception trying to write file mobileorg.org");
            return false;
        }
        return true;
    }

    // edittype = heading || body || tags || todo || priority
    //
    // * F(edit:edittype) [[id:yyy][Title of node]
    // ** Old value
    // Old value goes here
    // ** New value
    // New value goes here
    // ** End of edit
    public String transformEditBuffer(String edittype,
                                      String nodeId,
                                      String nodeTitle,
                                      String oldValue,
                                      String newValue) {
        if (nodeId.indexOf("olp:") != 0)
            nodeId = "id:" + nodeId;
        String editbuffer = "* F(edit:" + edittype + ") [[" + nodeId +
            "][" + nodeTitle.trim() + "]]\n";
        editbuffer += "** Old value\n" + oldValue.trim() + "\n";
        editbuffer += "** New value\n" + newValue.trim() + "\n";
        editbuffer += "** End of edit" + "\n";
        return editbuffer;
    }

    // * first line of the note
    //   [2009-09-09 Wed 09:25]
    //   Rest of the note
    public String transformCreateBuffer(String givenText) {
        String xformed = "";
        String[] bufferLines = givenText.split("\\n");
        for (int idx = 0; idx < bufferLines.length; idx++) {
            if (idx == 0) {
                if (bufferLines[idx].length() > 0 &&
                    !bufferLines[idx].substring(0,1).equals("*")) {
                    Date date = new Date();
                    String DATE_FORMAT = "[yyyy-MM-dd EEE HH:mm]";
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                    xformed += "* " + bufferLines[idx] + "\n";
                    xformed += "  " + sdf.format(date) + "\n";
                }
                else {
                    xformed = givenText;
                    break;
                }
            }
            else {
                xformed += "  " + bufferLines[idx] + "\n";
            }
        }
        return xformed + "\n\n";
    }
}