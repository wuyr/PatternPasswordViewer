package com.demo.patternpasswordviewer;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by wuyr on 2/24/16 5:13 PM.
 */
public class MainActivity extends AppCompatActivity {

    private TextView mPassword;
    private PatternView mPatternView;
    private String mReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            initImmerseBar();
        initViews();
        initDB();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return false;
    }

    private void initViews() {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPassword = (TextView) findViewById(R.id.password);
        mPatternView = (PatternView) findViewById(R.id.pattern_view);

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pw = getPassword();
                if (pw != null) {
                    mPassword.setText(pw);
                    mPatternView.draw(pw);
                } else {
                    final Snackbar snackbar = Snackbar.make(findViewById(R.id.root),
                            mReason, Snackbar.LENGTH_INDEFINITE);
                    Snackbar.SnackbarLayout root = (Snackbar.SnackbarLayout) snackbar.getView();
                    //background
                    root.setBackgroundColor(Color.parseColor("#d03d4b"));
                    //margin
                    if (ImmerseUtil.isHasNavigationBar(MainActivity.this)) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) root.getLayoutParams();
                        lp.bottomMargin = ImmerseUtil.getNavigationBarHeight(MainActivity.this);
                        root.setLayoutParams(lp);
                    }

                    ((TextView) root.findViewById(R.id.snackbar_text)).setTextColor(Color.parseColor("#f0b568"));
                    snackbar.setAction("ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    }).setActionTextColor(Color.GREEN);
                    snackbar.show();
                }
            }
        });
    }

    /**
     * 初始化沉浸状态栏
     */
    private void initImmerseBar() {
        ImmerseUtil.setImmerseBar(this,
                getResources().getColor(R.color.colorPrimaryDark));
    }

    private String getPassword() {
        mReason = null;
        final String noPermissions = "无权访问data分区";
        String passwordPath = "/data/system/gesture.key";
        File pwFile = new File(passwordPath);
        if (!new File(passwordPath).exists() || pwFile.length() == 0) {
            mReason = "当前未设置图案锁屏";
            return null;
        }
        PrintWriter pw = null;
        final StringBuilder sha1 = new StringBuilder();
        try {
            final Process process = Runtime.getRuntime().exec("su");
            pw = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            /*pw.println("cp /data/system/gesture.key " + getFilesDir().toString() + "/");
            pw.println("chmod 777 " + getFilesDir().toString() + "/gesture.key");*/
            pw.println("cat " + passwordPath);
            pw.println("exit");
            new Thread() {
                @Override
                public void run() {
                    InputStream is = process.getInputStream();
                    byte[] tmp = new byte[64];
                    int pos;
                    try {
                        pos = is.read(tmp);
                        byte[] data = new byte[pos];
                        System.arraycopy(tmp, 0, data, 0, pos);
                        sha1.append(p2SHA1(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                        mReason = noPermissions;
                    }
                }
            }.start();

            new Thread() {
                @Override
                public void run() {
                    InputStream is = process.getErrorStream();
                    byte[] data = new byte[64];
                    int pos;
                    StringBuilder sb = new StringBuilder();
                    try {
                        while ((pos = is.read(data)) != -1)
                            sb.append(new String(data, 0, pos));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Log.e("error", sb.toString());
                        if (sb.length() > 0)
                            mReason = noPermissions;
                    }
                }
            }.start();
            int code = process.waitFor();
            process.destroy();
            if (code == 0)
                return queryPassword(sha1.toString());
            else {
                mReason = noPermissions;
                return null;
            }
        } catch (Exception e) {
            mReason = noPermissions;
            e.printStackTrace();
        } finally {
            try {
                if (pw != null)
                    pw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 密文转SHA1码
     * @param data 密文的字节数组
     * @return SHA1
     */
    private String p2SHA1(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            byte2HexString(b, sb);
        return sb.toString();
    }

    /**
     * 字节转16进制String
     * @param b 要转换的字节
     * @param sb 转换后的存放对象
     */
    private void byte2HexString(byte b, StringBuilder sb) {
        int number = b & 0x00ff;
        if (number < 16)
            sb.append(0);
        sb.append(Integer.toHexString(number));
    }

    private String queryPassword(String key) {
        if (key == null)
            return null;
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    getDatabasePath("rainbow.db").toString(),
                    null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteCantOpenDatabaseException e) {
            mReason = "程序数据异常，请重置应用数据或重新安装本应用";
        }
        if (db == null)
            return null;
        Cursor cursor = db.rawQuery("select * from map where key = ?", new String[]{key});
        String result = null;
        while (cursor.moveToNext())
            result = cursor.getString(cursor.getColumnIndex("value"));
        cursor.close();
        db.close();
        return result;
    }

    private void initDB() {
        final File db = getDatabasePath("rainbow.db");
        if (!db.exists()) {
            new File("/data/data/com.demo.patternpasswordviewer/databases/").mkdirs();
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("正在初始化彩虹表...");
            pd.setCancelable(false);
            pd.show();
            new Thread() {
                @Override
                public void run() {
                    FileOutputStream fos = null;
                    InputStream is = null;
                    try {
                        is = getResources().openRawResource(R.raw.rainbow);
                        fos = new FileOutputStream(db);
                        byte[] tmp = new byte[128];
                        int pos;
                        while ((pos = is.read(tmp)) != -1)
                            fos.write(tmp, 0, pos);
                    } catch (Exception e) {
                        Log.e("MainActivity", e.getMessage(), e);
                    } finally {
                        try {
                            if (fos != null)
                                fos.close();
                            if (is != null)
                                is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pd.dismiss();
                    }
                }
            }.start();
        }
    }
}
