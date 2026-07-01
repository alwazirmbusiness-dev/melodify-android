package com.melodifyverse.nancyajram;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Play extends AppCompatActivity {

    private AppCompatSeekBar seek_song_progressbar;
    private ImageView btn_play;
    private TextView tv_song_current_duration, tv_song_total_duration;
    private TextView tvTimerCountdown;
    private CountDownTimer sleepTimer;
    private long remainingTime = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private MusicUtils utils;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    public int i = 0;

    SQLiteDatabase mydatabase;

    public String[] audioAssets = {"m1.ogg", "m2.ogg", "m3.ogg", "m4.ogg", "m5.ogg", "m6.ogg", "m7.ogg",
            "m8.ogg", "m9.ogg", "m10.ogg", "m11.ogg", "m12.ogg", "m13.ogg", "m14.ogg", "m15.ogg", "m16.ogg",
            "m17.ogg", "m18.ogg", "m19.ogg", "m20.ogg", "m21.ogg", "m22.ogg", "m23.ogg", "m24.ogg", "m25.ogg",
            "m26.ogg", "m27.ogg", "m28.ogg", "m29.ogg", "m30.ogg", "m31.ogg", "m32.ogg", "m33.ogg", "m34.ogg",
            "m35.ogg", "m36.ogg", "m37.ogg", "m38.ogg", "m39.ogg", "m40.ogg", "m41.ogg", "m42.ogg", "m43.ogg",
            "m44.ogg", "m45.ogg", "m46.ogg", "m47.ogg", "m48.ogg", "m49.ogg", "m50.ogg", "m51.ogg", "m52.ogg",
            "m53.ogg", "m54.ogg", "m55.ogg", "m56.ogg", "m57.ogg", "m58.ogg"
    };

    public String currentSong;
    List<songsItem> mData;
    TextView second;
    String activity_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent_view), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left, insets.getInsets(WindowInsetsCompat.Type.systemBars()).top, insets.getInsets(WindowInsetsCompat.Type.systemBars()).right, insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        second = findViewById(R.id.second);

        ImageView btn_timerr = findViewById(R.id.btn_timerr);
        btn_timerr.setOnClickListener(v -> showSleepTimerDialog());

        mData = new ArrayList<>();
        populateSongList();

        initMusicPlayer();

        Intent intent = getIntent();
        boolean fromMiniPlayer = intent.getBooleanExtra("fromMiniPlayer", false);
        i = intent.getIntExtra("i", 0);
        currentSong = intent.getStringExtra("song");

        if (currentSong == null) {
            if (i >= 0 && i < audioAssets.length) {
                currentSong = audioAssets[i];
            } else {
                // Handle invalid index
                Toast.makeText(this, "Invalid song index.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (!fromMiniPlayer) {
            playSong(currentSong);

        }

        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> onBackPressed());

        mydatabase = openOrCreateDatabase("mohammed24", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Favourites(SongId INT);");

        setupFavoriteButton();

        syncUIWithPlayer();
        updateSongTitles();

        controlImageAnimation(PlayerManager.isPlaying());
        startMusicService();
    }

    private void initMusicPlayer() {
        View parent_view = findViewById(R.id.parent_view);
        seek_song_progressbar = findViewById(R.id.seek_song_progressbar);
        btn_play = findViewById(R.id.btn_play);

        seek_song_progressbar.setProgress(0);
        seek_song_progressbar.setMax(MusicUtils.MAX_PROGRESS);

        tv_song_current_duration = findViewById(R.id.tv_song_current_duration);
        tv_song_total_duration = findViewById(R.id.total_duration);

        utils = new MusicUtils();

        PlayerManager.setOnCompletionListener(() -> {
            if (isRepeat) {
                playSong(currentSong);
            } else if (isShuffle) {
                i = new Random().nextInt(audioAssets.length);
                currentSong = audioAssets[i];
                playSong(currentSong);
                updateSongTitles();
            } else {
                if (i < audioAssets.length - 1) {
                    i++;
                } else {
                    i = 0;
                }
                currentSong = audioAssets[i];
                playSong(currentSong);
                updateSongTitles();
            }
        });

        seek_song_progressbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int total = PlayerManager.getDuration();
                int pos = utils.progressToTimer(seekBar.getProgress(), total);
                PlayerManager.seekTo(pos);
                mHandler.post(mUpdateTimeTask);
            }
        });

        btn_play.setOnClickListener(v -> {
            PlayerManager.toggle();
            syncUIWithPlayer();
            Intent intent = new Intent(ExampleService.ACTION_PLAY);
            sendBroadcast(intent);
        });
    }

    private void populateSongList() {
        mData.add(new songsItem(0, "Abu_Ward_Nerkab_Hal_Syara", 0, R.drawable.aloud));
        mData.add(new songsItem(0, "دايمًاهيك الأخرس واينيز", 1, R.drawable.aloud));
        mData.add(new songsItem(0, "الاخرس سكابا", 2, R.drawable.aloud));
        mData.add(new songsItem(0, "الشامي بتهون", 3, R.drawable.aloud));
        mData.add(new songsItem(0, "الشامي صبراً", 4, R.drawable.aloud));
        mData.add(new songsItem(0, "الشامي وببج", 5, R.drawable.aloud));
        mData.add(new songsItem(0, "الاعور يا-حبيبة", 6, R.drawable.aloud));
        mData.add(new songsItem(0, " أمجد جمعةالسبع", 7, R.drawable.aloud));
        mData.add(new songsItem(0, "_خاوةأمجد جمعة مع نو", 8, R.drawable.aloud));
        mData.add(new songsItem(0, "Balti-Rassi-El-Foug", 9, R.drawable.aloud));
        mData.add(new songsItem(0, "Bessan_Ismail_Al_Harbein", 10, R.drawable.aloud));
        mData.add(new songsItem(0, "بيسان_إسماع", 11, R.drawable.aloud));
        mData.add(new songsItem(0, " بيسان_اسماعيل_نجمة", 12, R.drawable.aloud));
        mData.add(new songsItem(0, "Bessan_Ismail_يا_خلي", 13, R.drawable.aloud));
        mData.add(new songsItem(0, "Didine-Canon-16-SMAHT-OU-MCHIT", 14, R.drawable.aloud));
        mData.add(new songsItem(0, "Fouad JnedxBessanxAmjad Jomaa 3lash", 15, R.drawable.aloud));
        mData.add(new songsItem(0, "Georges Wassouf Raksa Espani", 16, R.drawable.aloud));
        mData.add(new songsItem(0, "هيثم يوسف حبيب الروح", 17, R.drawable.aloud));
        mData.add(new songsItem(0, "حماقي-ادرينالين", 18, R.drawable.aloud));
        mData.add(new songsItem(0, "حماقي-تخسرني", 19, R.drawable.aloud));
        mData.add(new songsItem(0, " حماقي-واكلة-الجو", 20, R.drawable.aloud));
        mData.add(new songsItem(0, "هند_زيادي_بوم_بوم", 21, R.drawable.aloud));
        mData.add(new songsItem(0, "هما دول_سعدن", 22, R.drawable.aloud));
        mData.add(new songsItem(0, "Lazaro-MAHBOUL-ANA ", 23, R.drawable.aloud));
        mData.add(new songsItem(0, "MARWAN_PABLO_FREE", 24, R.drawable.aloud));
        mData.add(new songsItem(0, "Nancy_Ajram_Toul_Omri_Negma", 25, R.drawable.aloud));
        mData.add(new songsItem(0, "ناصيف زيتون ورحمة رياض - ما في ليل", 26, R.drawable.aloud));
        mData.add(new songsItem(0, "بلا-بيك Nordo", 27, R.drawable.aloud));
        mData.add(new songsItem(0, "لاباس Nordo ", 28, R.drawable.aloud));
        mData.add(new songsItem(0, "Guli Mata - Saad Lamjarred", 29, R.drawable.aloud));
        mData.add(new songsItem(0, "رحمة_رياض_تفارك", 30, R.drawable.aloud));
        mData.add(new songsItem(0, " سيف_نبيل_حبك", 31, R.drawable.aloud));
        mData.add(new songsItem(0, "سيف_نبيل_طاير", 32, R.drawable.aloud));
        mData.add(new songsItem(0, "سيف_نبيل_يما", 33, R.drawable.aloud));
        mData.add(new songsItem(0, "Samara-Automatique", 34, R.drawable.aloud));
        mData.add(new songsItem(0, "Samara-ft-Baya-Feu-Rouge", 35, R.drawable.aloud));
        mData.add(new songsItem(0, "SSamara-Maktoub", 36, R.drawable.aloud));
        mData.add(new songsItem(0, "Samara-Souk", 37, R.drawable.aloud));
        mData.add(new songsItem(0, "Samara-Wink ", 38, R.drawable.aloud));
        mData.add(new songsItem(0, " عطشان ", 39, R.drawable.aloud));
        mData.add(new songsItem(0, "الفي Siilawy-Elfy ", 40, R.drawable.aloud));
        mData.add(new songsItem(0, "Siilawy-عشانك", 41, R.drawable.aloud));
        mData.add(new songsItem(0, "يا_نهار_ابيض_تامر_حسني", 42, R.drawable.aloud));
        mData.add(new songsItem(0, "Zouhair_Bahaoui_Hiya_Hakda", 43, R.drawable.aloud));
        mData.add(new songsItem(0, " Zendaya", 44, R.drawable.aloud));
        mData.add(new songsItem(0, "Makareb", 45, R.drawable.aloud1));
        mData.add(new songsItem(0, "معلمين_إعلان_وي_صيف", 46, R.drawable.aloud1));
        mData.add(new songsItem(0, "حسام_الرسام_كيمر_عرب", 47, R.drawable.aloud1));
        mData.add(new songsItem(0, "حسين-الجسمي-دلع-دلع ", 48, R.drawable.aloud1));
        mData.add(new songsItem(0, "زيد_الحبيب_بدت_تمطر", 49, R.drawable.aloud1));
        mData.add(new songsItem(0, "سليم_سالم_ياحته_ددا", 50, R.drawable.aloud1));
        mData.add(new songsItem(0, "علي_صابر_دعوة_أمي_البوم_يراقبني ", 51, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمد_السالم_اخذني_العالمة", 52, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمد_السالم_نظرة_عالمية ", 53, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمد_رمضان_قطتي_مبتخربش", 54, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمود-التركي-جمالك-قاتل", 55, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمود-التركي-حلم-چفي ", 56, R.drawable.aloud1));
        mData.add(new songsItem(0, "محمود-التركي-خليك-وياي", 57, R.drawable.aloud1));
    }

    private void setupFavoriteButton() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) return;

        i = bundle.getInt("i");
        // Use int[] to allow mutation inside the lambda (effectively-final workaround)
        int[] favourite = {bundle.getInt("favourite")};
        activity_type = bundle.getString("activity_type");

        ImageView favImageView = findViewById(R.id.img_fav_1);
        favImageView.setImageResource(favourite[0] == 0 ? R.drawable.favorite : R.drawable.favorite_active);

        favImageView.setOnClickListener(v -> {
            if (favourite[0] == 0) {
                mydatabase.execSQL("INSERT INTO Favourites VALUES(" + i + ");");
                favImageView.setImageResource(R.drawable.favorite_active);
                showToast("تم إضافة هذه الأغنية إلى قائمتك المفضلة ", true);
                favourite[0] = 1;
            } else {
                mydatabase.execSQL("DELETE FROM Favourites WHERE SongId = " + i + ";");
                favImageView.setImageResource(R.drawable.favorite);
                showToast("تم حذف هذه الأغنية من قائمتك المفضلة", false);
                favourite[0] = 0;
            }
        });
    }

    private void showToast(String message, boolean isGreen) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        if (Build.VERSION.SDK_INT <= 29) {
            View view = toast.getView();
            view.setBackgroundResource(isGreen ? R.drawable.bg_toast_green : R.drawable.bg_toast_red);
            TextView textView = view.findViewById(android.R.id.message);
            textView.setTextColor(Color.WHITE);
            textView.setGravity(17);
        }
        toast.show();
    }

    private void updateSongTitles() {
        if (i >= 0 && i < mData.size()) {
            second.setText(mData.get(i).Title);
        }
    }

    private void controlImageAnimation(boolean isPlaying) {
        ImageView image = findViewById(R.id.artist_pic);
        Animation animation = image.getAnimation();
        if (isPlaying) {
            if (animation == null) {
                ScaleAnimation heartbeat = new ScaleAnimation(1.0f, 1.05f, 1.0f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                heartbeat.setRepeatCount(Animation.INFINITE);
                heartbeat.setDuration(1400);
                heartbeat.setInterpolator(new LinearInterpolator());
                heartbeat.setRepeatMode(Animation.REVERSE);
                image.startAnimation(heartbeat);
            }
        } else {
            if (animation != null) {
                animation.cancel();
                image.clearAnimation();
            }
        }
    }

    private void showSleepTimerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_sleep_timer);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        tvTimerCountdown = dialog.findViewById(R.id.tv_timer_countdown);
        Button btn2 = dialog.findViewById(R.id.btn_2);
        Button btn5 = dialog.findViewById(R.id.btn_5);
        Button btn10 = dialog.findViewById(R.id.btn_10);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_timer);

        updateTimerText();

        btn2.setOnClickListener(v -> startSleepTimer(remainingTime + 2 * 60 * 1000));
        btn5.setOnClickListener(v -> startSleepTimer(remainingTime + 5 * 60 * 1000));
        btn10.setOnClickListener(v -> startSleepTimer(remainingTime + 10 * 60 * 1000));
        btnCancel.setOnClickListener(v -> {
            cancelSleepTimer();
            remainingTime = 0;
            updateTimerText();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void startSleepTimer(long millis) {
        cancelSleepTimer();
        remainingTime = millis;
        sleepTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                remainingTime = 0;
                updateTimerText();
                PlayerManager.stop();
                Toast.makeText(Play.this, "Sleep timer finished", Toast.LENGTH_SHORT).show();
            }
        }.start();
        Toast.makeText(this, "Sleep timer started", Toast.LENGTH_SHORT).show();
    }

    private void updateTimerText() {
        if (tvTimerCountdown != null) {
            tvTimerCountdown.setText(formatTime(remainingTime));
        }
    }

    private String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void cancelSleepTimer() {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
        }
    }

    private void playSong(String assetName) {
        if (PlayerManager.isSameSong(assetName)) {
            if (!PlayerManager.isPlaying()) {
                PlayerManager.resume();
            }
        } else {
            String title = "";
            if (i >= 0 && i < mData.size()) {
                title = mData.get(i).getTitle();
            }
            PlayerManager.play(this, assetName, i, title);
        }
        syncUIWithPlayer();
        updateMusicService(null);
    }

    public void controlClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_repeat) {
            isRepeat = !isRepeat;
            toggleButtonColor((ImageButton) v, isRepeat);
            if (isRepeat) {
                isShuffle = false;
                toggleButtonColor(findViewById(R.id.btn_shuffle), false);
            }
        } else if (id == R.id.btn_shuffle) {
            isShuffle = !isShuffle;
            toggleButtonColor((ImageButton) v, isShuffle);
            if (isShuffle) {
                isRepeat = false;
                toggleButtonColor(findViewById(R.id.btn_repeat), false);
            }
        } else if (id == R.id.btn_prev) {
            if (i == 0) {
                i = audioAssets.length - 1;
            } else {
                if (isShuffle) {
                    i = new Random().nextInt(audioAssets.length);
                } else {
                    i--;
                }
            }
            currentSong = audioAssets[i];
            playSong(currentSong);
            updateSongTitles();
            updateMusicService(ExampleService.ACTION_PREVIOUS);
        } else if (id == R.id.btn_next) {
            if (i == audioAssets.length - 1) {
                i = 0;
            } else {
                if (isShuffle) {
                    i = new Random().nextInt(audioAssets.length);
                } else {
                    i++;
                }
            }
            currentSong = audioAssets[i];
            playSong(currentSong);
            updateSongTitles();
            updateMusicService(ExampleService.ACTION_NEXT);
        }
    }

    private void toggleButtonColor(ImageButton bt, boolean isSelected) {
        if (isSelected) {
            bt.setColorFilter(getResources().getColor(R.color.colorYellow), PorterDuff.Mode.SRC_ATOP);
        } else {
            bt.setColorFilter(getResources().getColor(R.color.colorDarkOrange), PorterDuff.Mode.SRC_ATOP);
        }
    }

    private void syncUIWithPlayer() {
        if (PlayerManager.isPlaying()) {
            btn_play.setImageResource(R.drawable.pause33);
        } else {
            btn_play.setImageResource(R.drawable.play__1_);
        }
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.post(mUpdateTimeTask);
        controlImageAnimation(PlayerManager.isPlaying());
    }

    private final Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (PlayerManager.getMediaPlayer() == null || !PlayerManager.isPlaying()) return;

            long total = PlayerManager.getDuration();
            long current = PlayerManager.getCurrentPosition();

            tv_song_total_duration.setText(utils.milliSecondsToTimer(total));
            tv_song_current_duration.setText(utils.milliSecondsToTimer(current));

            int progress = utils.getProgressSeekBar(current, total);
            seek_song_progressbar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        // Do NOT stop the service here — music should keep playing via the mini-player
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            View parent_view = findViewById(android.R.id.content);
            Snackbar.make(parent_view, item.getTitle(), Snackbar.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onShowPop(View v) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.costumpopup_rate);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.txtclose).setOnClickListener(view -> dialog.cancel());

        dialog.findViewById(R.id.share_app).setOnClickListener(view -> {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareTxt));
                String sAux = "تطبيق " + getString(R.string.shareTxt) + "_انقر هنا:   ";
                sAux = sAux + "http://play.google.com/store/apps/details?id=" + getPackageName();
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch (Exception e) {
                // Handle exception
            }
        });

        dialog.findViewById(R.id.button_rate).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        dialog.findViewById(R.id.More_Apps).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.marketAccount))));
            } catch (Exception e) {
                // Handle exception
            }
        });

        dialog.findViewById(R.id.privacy_1).setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.provicyPolicy))));
            } catch (ActivityNotFoundException anfe) {
                // Handle exception
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if (Objects.equals(activity_type, "main")) {
            intent.putExtra("play_list_type", 0);
        } else {
            try (Cursor cur = mydatabase.rawQuery("SELECT COUNT(*) FROM Favourites", null)) {
                int intentvalue = 0;
                if (cur != null && cur.moveToFirst()) {
                    if (cur.getInt(0) > 0) intentvalue = 1;
                }
                intent.putExtra("play_list_type", intentvalue);
            }
        }
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    private void startMusicService() {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopMusicService() {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        stopService(serviceIntent);
    }

    private void updateMusicService(String action) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        if(action != null) {
            serviceIntent.setAction(action);
        }
        startService(serviceIntent);
    }
}
