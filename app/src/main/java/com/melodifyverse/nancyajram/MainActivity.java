package com.melodifyverse.nancyajram;



import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.melodifyverse.nancyajram.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final List<songsItem> filteredList = new ArrayList<>();
    private final int[] colors = {
            Color.parseColor("#BFFFFFFF"), // White
            Color.parseColor("#BF808080"), // Gray
            Color.parseColor("#BFFCDC62"), // Soft Yellow
            Color.parseColor("#BFFFCC00"), // Yellow
            Color.parseColor("#BF2FC317"), // Green
            Color.parseColor("#BF5226FE"), // Purple Blue
            Color.parseColor("#BF8B21FF")  // Purple
    };
    public String[] audioAssets = {"m1.ogg", "m2.ogg", "m3.ogg", "m4.ogg", "m5.ogg", "m6.ogg", "m7.ogg",
            "m8.ogg", "m9.ogg", "m10.ogg", "m11.ogg", "m12.ogg", "m13.ogg", "m14.ogg", "m15.ogg", "m16.ogg",
            "m17.ogg", "m18.ogg", "m19.ogg", "m20.ogg", "m21.ogg", "m22.ogg", "m23.ogg", "m24.ogg", "m25.ogg",
            "m26.ogg", "m27.ogg", "m28.ogg", "m29.ogg", "m30.ogg", "m31.ogg", "m32.ogg", "m33.ogg", "m34.ogg",
            "m35.ogg", "m36.ogg", "m37.ogg", "m38.ogg", "m39.ogg", "m40.ogg", "m41.ogg", "m42.ogg", "m43.ogg",
            "m44.ogg", "m45.ogg", "m46.ogg", "m47.ogg", "m48.ogg", "m49.ogg", "m50.ogg", "m51.ogg", "m52.ogg",
            "m53.ogg", "m54.ogg", "m55.ogg", "m56.ogg", "m57.ogg", "m58.ogg"
    };
    public String currentSong = audioAssets[0];
    ActivityMainBinding binding;
    ImageView btnMiniPlay;
    TextView txtSongTitle;
    RelativeLayout nowPlayingContainer;
    RecyclerView songsRecyclerview;
    songsAdapter msongsAdapter;
    List<songsItem> mData;
    SharedPreferences countSettings;
    private CountDownTimer sleepTimer;
    private long remainingTime = 0;
    private TextView tvTimerCountdown;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private int count = 0;
    int play_list_type = 0;
    ArrayList<songsItem> templist = new ArrayList<>();
    List<String> list_songs = new ArrayList<>();
    SQLiteDatabase mydatabase;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You will not receive notifications.", Toast.LENGTH_SHORT).show();
        }
    });

    private final BroadcastReceiver playPauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ExampleService.ACTION_PLAY.equals(intent.getAction())) {
                if (PlayerManager.isPlaying()) {
                    btnMiniPlay.setImageResource(R.drawable.pause33);
                } else {
                    btnMiniPlay.setImageResource(R.drawable.play__1_);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (songsRecyclerview != null) {
            LinearLayoutManager lm = (LinearLayoutManager) songsRecyclerview.getLayoutManager();
            if (lm != null) {
                getSharedPreferences("player_state", MODE_PRIVATE)
                        .edit()
                        .putInt("scroll_pos", lm.findFirstVisibleItemPosition())
                        .apply();
            }
        }
        unregisterReceiver(playPauseReceiver);
    }
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("player_state", MODE_PRIVATE);
        int scrollPos = prefs.getInt("scroll_pos", 0);

        if (songsRecyclerview != null) {
            songsRecyclerview.scrollToPosition(scrollPos);
        }

        int playingPos = PlayerManager.getCurrentPos();

        if (msongsAdapter != null) {
            msongsAdapter.setPlayingPosition(playingPos);
        }

        if (PlayerManager.isPlaying() && playingPos != -1 && mData != null && playingPos < mData.size()) {
            showNowPlaying(mData.get(playingPos).getTitle());
        } else {
            hideNowPlaying();
        }

        if (PlayerManager.hasCompleted()) {
            int completedPos = PlayerManager.getCurrentPos();

            if (completedPos != -1 && mData != null && completedPos < mData.size()) {
                showAds(completedPos, mData.get(completedPos).songID);
            }

            PlayerManager.setHasCompleted(false);
        }

        IntentFilter filter = new IntentFilter(ExampleService.ACTION_PLAY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(playPauseReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(playPauseReceiver, filter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent_view), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });

        askNotificationPermission();

        ImageView favBtn = findViewById(R.id.btn_favorite);
        favBtn.setOnClickListener(v -> GotoFavourite());

        ImageView btnSearch = findViewById(R.id.btn_search);
        EditText searchEditText = findViewById(R.id.search_edit_text);

        ImageView btn_timer = findViewById(R.id.btn_timer);
        btn_timer.setOnClickListener(v -> showSleepTimerDialog());

        ImageView btnAward = findViewById(R.id.btn_award);
        btnAward.setOnClickListener(v -> showRewardedAd());

        nowPlayingContainer = findViewById(R.id.now_playing_container);
        txtSongTitle = findViewById(R.id.txt_song_title);
        btnMiniPlay = findViewById(R.id.btn_mini_play);

        hideNowPlaying();
        setupMiniPlayerControls();

        nowPlayingContainer.setOnClickListener(v -> {
            int pos = PlayerManager.getCurrentPos();
            if (pos != -1 && mData != null && pos < mData.size()) {
                launchPlayActivity(pos, mData.get(pos).songID);
            }
        });

        btnSearch.setOnClickListener(v -> {
            if (searchEditText.getVisibility() == View.GONE) {
                searchEditText.setVisibility(View.VISIBLE);
                searchEditText.setTranslationY(-20f);
                searchEditText.animate().alpha(1f).translationY(0f).setDuration(250).start();
                btnSearch.setImageResource(R.drawable.baseline_search_off_24);
            } else {
                searchEditText.animate().alpha(0f).translationY(-20f).setDuration(200)
                        .withEndAction(() -> searchEditText.setVisibility(View.GONE)).start();
                btnSearch.setImageResource(R.drawable.baseline_search_24);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton backButton = findViewById(R.id.backbutton2);
        backButton.setOnClickListener(v -> showPremiumDialog());

        countSettings = getSharedPreferences("count", 0);
        count = countSettings.getInt("counts", 0);

        MobileAds.initialize(this, initializationStatus -> {});
        loadInterstitial();
        loadRewardedAd();

        mydatabase = openOrCreateDatabase("mohammed24", MODE_PRIVATE, null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Favourites(SongId INT);");

        setupRecyclerView();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void setupRecyclerView() {
        songsRecyclerview = findViewById(R.id.news_rv);
        mData = new ArrayList<>();
        // Populate mData with your songsItem objects
        populateSongList();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            play_list_type = bundle.getInt("play_list_type", 0);
        }

        loadFavorites();

        if (play_list_type == 1) {
            filterFavorites();
        }

        msongsAdapter = new songsAdapter(this, mData, new songsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(songsItem item, int pos) {
                if (item.getColorIndex() == -1) {
                    item.setColorIndex(new Random().nextInt(colors.length));
                }

                getSharedPreferences("player_state", MODE_PRIVATE).edit()
                        .putInt("playing_pos", pos)
                        .putBoolean("is_playing", true)
                        .apply();

                msongsAdapter.setPlayingPosition(pos);
                showNowPlaying(item.getTitle());
                launchPlayActivity(pos, item.songID);
                 startService(new Intent(MainActivity.this, ExampleService.class));
            }

            @Override
            public void OnImageClick(songsItem item, ImageView v, int pos) {
                handleFavoriteClick(item, v, pos);
            }
        });

        songsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        songsRecyclerview.setAdapter(msongsAdapter);
    }

    private void handleFavoriteClick(songsItem item, ImageView v, int pos) {
        Toast add_to = Toast.makeText(getApplicationContext(), "تم إضافة هذه الأغنية إلى قائمتك المفضلة", Toast.LENGTH_SHORT);
        Toast remove_from = Toast.makeText(getApplicationContext(), "تم حذف هذه الأغنية من قائمتك المفضلة", Toast.LENGTH_SHORT);

        if (play_list_type == 0) {
            if (item.getFavourite() == 0) {
                mydatabase.execSQL("INSERT INTO Favourites VALUES(" + item.songID + ");");
                item.setFavourite(1);
                v.setImageResource(R.drawable.favorite_active);
                add_to.show();
            } else {
                mydatabase.execSQL("DELETE FROM Favourites WHERE SongId = " + item.songID);
                item.setFavourite(0);
                v.setImageResource(R.drawable.favorite);
                remove_from.show();
            }
            msongsAdapter.notifyItemChanged(pos);
        } else {
            mydatabase.execSQL("DELETE FROM Favourites WHERE SongId = " + item.songID);
            mData.remove(pos);
            list_songs.remove(audioAssets[pos]);
            audioAssets = list_songs.toArray(new String[0]);
            msongsAdapter.notifyItemRemoved(pos);
            remove_from.show();
        }
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

    private void loadFavorites() {
        try (Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Favourites", null)) {
            while (resultSet.moveToNext()) {
                int songId = resultSet.getInt(0);
                if (songId >= 0 && songId < mData.size()) {
                    mData.get(songId).setFavourite(play_list_type == 1 ? 2 : 1);
                }
            }
        }
    }

    private void filterFavorites() {
        templist = new ArrayList<>();
        list_songs = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getFavourite() == 2) {
                templist.add(mData.get(i));
                list_songs.add(audioAssets[i]);
            }
        }
        audioAssets = list_songs.toArray(new String[0]);
        if (audioAssets.length > 0) {
            currentSong = audioAssets[0];
        }
        mData = templist;
    }

    private void showNowPlaying(String title) {
        if (nowPlayingContainer == null) return;
        nowPlayingContainer.setVisibility(View.VISIBLE);
        nowPlayingContainer.setClickable(true);
        txtSongTitle.setText(title);

        if (PlayerManager.isPlaying()) {
            btnMiniPlay.setImageResource(R.drawable.pause33);
        } else {
            btnMiniPlay.setImageResource(R.drawable.play__1_);
        }
    }

    private void hideNowPlaying() {
        if (nowPlayingContainer != null) {
            nowPlayingContainer.setVisibility(View.GONE);
            nowPlayingContainer.setClickable(false);
        }
        stopService(new Intent(this, ExampleService.class));
    }

    private void setupMiniPlayerControls() {
        btnMiniPlay.setOnClickListener(v -> {
            PlayerManager.toggle();
            if (PlayerManager.isPlaying()) {
                btnMiniPlay.setImageResource(R.drawable.pause33);
            } else {
                btnMiniPlay.setImageResource(R.drawable.play__1_);
            }
            if (msongsAdapter != null) {
                msongsAdapter.setPlayingPosition(PlayerManager.getCurrentPos());
            }
             Intent intent = new Intent(this, ExampleService.class);
            intent.setAction(ExampleService.ACTION_PLAY);
            startService(intent);
        });

        nowPlayingContainer.setOnClickListener(v -> {
            Intent intent = new Intent(this, Play.class);
            intent.putExtra("song", currentSong);
            intent.putExtra("fromMiniPlayer", true);
            startActivity(intent);
        });
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
                stopMusicFromTimer();
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
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void stopMusicFromTimer() {
        PlayerManager.stop();
        hideNowPlaying();
        if (msongsAdapter != null) {
            msongsAdapter.setPlayingPosition(-1);
        }
        if (btnMiniPlay != null) {
            btnMiniPlay.setImageResource(R.drawable.play__1_);
        }
        Toast.makeText(this, "Sleep timer finished", Toast.LENGTH_SHORT).show();
    }

    private void cancelSleepTimer() {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
        }
    }

    private void filterData(String query) {
        filteredList.clear();
        if (mData != null) {
            for (songsItem item : mData) {
                if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        if (msongsAdapter != null) {
            msongsAdapter.updateList(filteredList);
        }
    }

    public void GotoFavourite() {
        int intentvalue = 0;
        try (Cursor cur = mydatabase.rawQuery("SELECT COUNT(*) FROM Favourites", null)) {
            if (cur != null && cur.moveToFirst()) {
                intentvalue = cur.getInt(0) > 0 ? 1 : 0;
            }
        }

        Intent intent = getIntent();
        intent.putExtra("play_list_type", intentvalue);
        recreate(); // Activity is recreated to show either the full list or favorites
    }

    private void showAds(int pos, int songID) {
        SharedPreferences prefs = getSharedPreferences("ad_prefs", MODE_PRIVATE);
        long adFreeExpiry = prefs.getLong("ad_free_expiry", 0);

        if (System.currentTimeMillis() < adFreeExpiry) {
            Log.d("Admob", "Ad-free period is active. Skipping ad.");
            return;
        }

        if (count > 3) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
                loadInterstitial(); // Pre-load the next ad
            }
            count = 0;
        } else {
            count++;
        }

        final SharedPreferences.Editor edit = countSettings.edit();
        edit.putInt("counts", count);
        edit.apply();
    }

    private void launchPlayActivity(int pos, int songID) {
        Intent intent = new Intent(MainActivity.this, Play.class);
        String activityType = (play_list_type == 0) ? "main" : "favourite";
        int itemIndex = (play_list_type == 0) ? pos : songID;
        int songIdentifier = (play_list_type == 0) ? songID : pos;

        if (mData != null && songIdentifier >= 0 && songIdentifier < mData.size()){
            intent.putExtra("i", itemIndex);
            intent.putExtra("id", songIdentifier);
            intent.putExtra("favourite", mData.get(songIdentifier).getFavourite());
            intent.putExtra("activity_type", activityType);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error playing song.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, getResources().getString(R.string.admob_interstitial), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        Log.i("Admob", "Interstitial ad loaded.");
                        MainActivity.this.mInterstitialAd = interstitialAd;

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                Log.d("Admob", "Ad dismiss.");
                                loadInterstitial(); // Pre-load the next ad after dismiss
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Log.d("Admob", "Ad failed to show: " + adError);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                                Log.d("Admob", "Ad displayed.");
                                MainActivity.this.mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i("Admob", "onAdFailedToLoad: " + loadAdError);
                        mInterstitialAd = null;
                    }
                });
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.admob_rewarded), adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                Log.d("Admob", "Rewarded ad loaded.");
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        Log.d("Admob", "Rewarded ad dismissed.");
                        loadRewardedAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        Log.d("Admob", "Rewarded ad failed to show: " + adError);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        Log.d("Admob", "Rewarded ad displayed.");
                        mRewardedAd = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedAd = null;
                Log.d("Admob", "Rewarded ad failed to load: " + loadAdError.getMessage());
            }
        });
    }

    private void showRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.show(MainActivity.this, rewardItem -> {
                SharedPreferences.Editor editor = getSharedPreferences("ad_prefs", MODE_PRIVATE).edit();
                editor.putLong("ad_free_expiry", System.currentTimeMillis() + (20 * 60 * 1000));
                editor.apply();
                Toast.makeText(MainActivity.this, "استمتع بـ 30 دقيقة بدون إعلانات!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(MainActivity.this, "الإعلان غير جاهز حالياً. يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show();
            loadRewardedAd();
        }
    }

    private void showPremiumDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.premum);

        dialog.findViewById(R.id.rewardedAdLayout).setOnClickListener(view -> {
            showRewardedAd();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.relativeLayout3).setOnClickListener(view -> {
            dialog.dismiss();
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        });

        dialog.findViewById(R.id.relativeLayout2).setOnClickListener(view -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.findViewById(R.id.relativeLayout4).setOnClickListener(view -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, Menu.class));
        });

        dialog.findViewById(R.id.relativeLayout9).setOnClickListener(view -> {
            dialog.dismiss();
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shareTxt));
                String sAux = "تطبيق " + getString(R.string.shareTxt) + "_انقر هنا:   ";
                sAux = sAux + "http://play.google.com/store/apps/details?id=" + getPackageName();
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch (Exception e) {
                Log.e("MainActivity", "Error sharing app", e);
            }
        });

        dialog.findViewById(R.id.relativeLayout13).setOnClickListener(view -> {
            dialog.dismiss();
            try {
                Uri uri1 = Uri.parse(getString(R.string.getanotherapp));
                Intent getMarket = new Intent(Intent.ACTION_VIEW, uri1);
                startActivity(getMarket);
            } catch (ActivityNotFoundException e) {
                Log.e("MainActivity", "Error opening market", e);
            }
        });

        dialog.show();
    }
}
