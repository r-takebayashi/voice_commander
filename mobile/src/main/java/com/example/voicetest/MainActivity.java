package com.example.voicetest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static com.example.voicetest.R.string.count_string;

public class MainActivity extends AppCompatActivity {
    private static final int MILLIS_IN_FUTURE = 11 * 500;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    private Intent intent;
    private SpeechRecognizer mRecognizer;
    private TextView textView;

    private int count = 5;
    private TextView countTextView;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        countTextView = (TextView) findViewById(R.id.countTextViwe);

        // permission チェック
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO)) {
                // 拒否した場合
            } else {
                // 許可した場合
                int MY_PERMISSIONS_RECORD_AUDIO = 1;
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
            }
        }

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(recognitionListener);

        // 音声認識スタートボタン
        Button button = (Button) findViewById(R.id.buttonView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 音声テキスト初期化
                if (!TextUtils.isEmpty(textView.getText())) {
                    textView.setText("");
                }

                // カウントダウンスタート
                countDownTimer();
                countDownTimer.start();

                // レコーディングスタート
                mRecognizer.startListening(intent);
            }
        });
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("log:: ", "準備できてます");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("log:: ", "始め！");
        }

        @Override
        public void onRmsChanged(float v) {
            Log.d("log:: ", "音声が変わった");
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            Log.d("log:: ", "新しい音声");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("log:: ", "終わりました");
        }

        @Override
        public void onError(int i) {
            switch (i) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    resetText();
                    textView.setText("ネットワークタイムエラー");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    resetText();
                    textView.setText("その外ネットワークエラー");
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    resetText();
                    textView.setText("Audio エラー");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    resetText();
                    textView.setText("サーバーエラー");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    resetText();
                    textView.setText("クライアントエラー");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    resetText();
                    textView.setText("何も聞こえてないエラー");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    resetText();
                    textView.setText("適当な結果を見つけてませんエラー");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    resetText();
                    textView.setText("RecognitionServiceが忙しいエラー");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    resetText();
                    textView.setText("RECORD AUDIOがないエラー");
                    break;
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            String key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = bundle.getStringArrayList(key);

            String[] result = new String[0];
            if (mResult != null) {
                result = new String[mResult.size()];
            }
            if (mResult != null) {
                mResult.toArray(result);
            }

            textView.setText(result[0]);

            // テキスト比較
            if (TextUtils.equals(result[0], "メリークリスマス")) {
                Toast.makeText(MainActivity.this, "あなたもね！！", Toast.LENGTH_SHORT).show();
                countDownTimer.cancel();
                countTextView.setText("");
            }
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };

    private void resetText() {
        countDownTimer.cancel();
        countTextView.setText("やり直し！");
    }

    public void countDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            count = 5;
        }

        countDownTimer = new CountDownTimer(MILLIS_IN_FUTURE, COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                @SuppressLint("StringFormatInvalid") String countString = getString(count_string, count);
                countTextView.setText(countString);
                count--;
            }

            public void onFinish() {
                countDownTimer.cancel();
                countTextView.setText(String.valueOf("やり直し？"));
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
            Log.e("EXCEPTION LOG ", "message:: " + e.getMessage());
        }
        countDownTimer = null;
    }

}
