package com.example.sortinggame;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import android.view.DragEvent;
import android.view.MotionEvent;

public class GameActivity extends AppCompatActivity {

    private TextView textScore;
    private ImageView heart1, heart2, heart3;
    private RelativeLayout gameLayout;
    private Button btnPause, btnBack;

    private int score = 0;
    private int lives = 3;
    private boolean gameRunning = true;
    private boolean gamePaused = false;

    private Handler gameHandler = new Handler();
    private Random random = new Random();
    private List<GarbageItem> garbageTypes;

    // Game timing
    private static final int SPAWN_DELAY = 2000; // 2 seconds
    private int fallSpeed = 5; // pixels per update
    private static final int UPDATE_INTERVAL = 100; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initializeViews();
        initializeGarbageTypes();
        setupDragListeners();
        setupButtons();
        startGame();
    }

    private void initializeViews() {
        textScore = findViewById(R.id.textScore);
        heart1 = findViewById(R.id.heart1);
        heart2 = findViewById(R.id.heart2);
        heart3 = findViewById(R.id.heart3);
        gameLayout = findViewById(R.id.gameLayout);
        btnPause = findViewById(R.id.btnPause);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initializeGarbageTypes() {
        garbageTypes = new ArrayList<>();
        // You'll need to add actual drawable resources
        garbageTypes.add(new GarbageItem(GarbageItem.Type.PLASTIC, R.drawable.plasticbottle));
        garbageTypes.add(new GarbageItem(GarbageItem.Type.PLASTIC, R.drawable.plasticbag));
        garbageTypes.add(new GarbageItem(GarbageItem.Type.PAPER, R.drawable.news));
        garbageTypes.add(new GarbageItem(GarbageItem.Type.PAPER, R.drawable.box));
        garbageTypes.add(new GarbageItem(GarbageItem.Type.GLASS, R.drawable.glassbottle));
        garbageTypes.add(new GarbageItem(GarbageItem.Type.GLASS, R.drawable.jar));
    }

    private void setupDragListeners() {
        ImageView binPlastic = findViewById(R.id.binPlastic);
        ImageView binPaper = findViewById(R.id.binPaper);
        ImageView binGlass = findViewById(R.id.binGlass);

        binPlastic.setOnDragListener(new DragTouchListener(GarbageItem.Type.PLASTIC, this));
        binPaper.setOnDragListener(new DragTouchListener(GarbageItem.Type.PAPER, this));
        binGlass.setOnDragListener(new DragTouchListener(GarbageItem.Type.GLASS, this));
    }

    private void setupButtons() {
        btnPause.setOnClickListener(v -> togglePause());
        btnBack.setOnClickListener(v -> {
            gameRunning = false;
            finish();
        });
    }

    private void startGame() {
        gameHandler.post(gameLoop);
        gameHandler.postDelayed(spawnGarbage, SPAWN_DELAY);
    }

    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (gameRunning && !gamePaused) {
                updateFallingItems();
                checkCollisions();

                if (lives <= 0) {
                    endGame();
                    return;
                }
            }

            if (gameRunning) {
                gameHandler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private Runnable spawnGarbage = new Runnable() {
        @Override
        public void run() {
            if (gameRunning && !gamePaused) {
                createFallingGarbage();
            }

            if (gameRunning) {
                // Gradually increase spawn rate
                int nextDelay = Math.max(1200, SPAWN_DELAY - (score / 20) * 100);
                gameHandler.postDelayed(this, nextDelay);
            }
        }
    };

    private void createFallingGarbage() {
        GarbageItem garbageItem = garbageTypes.get(random.nextInt(garbageTypes.size()));

        ImageView garbageView = new ImageView(this);
        garbageView.setImageResource(garbageItem.getDrawableId());
        garbageView.setTag(garbageItem);

        // Make objects bigger
        int size = 230;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);

        int maxX = gameLayout.getWidth() - size;
        if (maxX <= 0) maxX = 100; // fallback if layout not ready
        int x = random.nextInt(maxX);
        params.leftMargin = x;
        params.topMargin = 0;

        garbageView.setLayoutParams(params);

        // Track dragging state
        garbageView.setTag(R.id.dragging, false);

        // Set up touch listener for drag initiation
        garbageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (gamePaused) return false; //  Prevent drag on pause

                    v.setTag(R.id.dragging, true);
                    ClipData data = ClipData.newPlainText("garbage", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        v.startDragAndDrop(data, shadowBuilder, v, 0);
                    } else {
                        v.startDrag(data, shadowBuilder, v, 0);
                    }

                    return true;
                }
                return false;
            }
        });

        garbageView.setOnLongClickListener(v -> {
            if (gamePaused) return false; //  Prevent long-click drag on pause

            v.setTag(R.id.dragging, true);
            ClipData data = ClipData.newPlainText("garbage", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                v.startDragAndDrop(data, shadowBuilder, v, 0);
            } else {
                v.startDrag(data, shadowBuilder, v, 0);
            }

            return true;
        });

        // Set up drag listener to handle drag end
        garbageView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Reset dragging state when drag ends
                        v.setTag(R.id.dragging, false);
                        // If drag ended without successful drop, make item visible again
                        if (!event.getResult()) {
                            v.setVisibility(View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });

        gameLayout.addView(garbageView);
    }

    private void updateFallingItems() {
        for (int i = gameLayout.getChildCount() - 1; i >= 0; i--) {
            View child = gameLayout.getChildAt(i);

            if (child.getTag() instanceof GarbageItem) {
                // Check if dragging
                Object draggingTag = child.getTag(R.id.dragging);
                if (draggingTag != null && (boolean) draggingTag) {
                    continue; // skip this item while it's being dragged
                }

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) child.getLayoutParams();
                params.topMargin += fallSpeed;

                if (params.topMargin > gameLayout.getHeight()) {
                    gameLayout.removeView(child);
                    loseLife();
                } else {
                    child.setLayoutParams(params);
                }
            }
        }
    }

    private void checkCollisions() {
        // Additional collision detection if needed
    }

    public void updateScore(int delta) {
        score += delta;
        textScore.setText("Score: " + score);

        // Increase fall speed every 100 points (up to a reasonable cap)
        if (score % 100 == 0 && fallSpeed < 100) {
            fallSpeed += 3;
        }
    }

    public void loseLife() {
        lives--;
        updateLivesDisplay();

        if (lives <= 0) {
            endGame();
        }
    }

    private void updateLivesDisplay() {
        switch (lives) {
            case 2:
                heart3.setVisibility(View.GONE);
                break;
            case 1:
                heart2.setVisibility(View.GONE);
                break;
            case 0:
                heart1.setVisibility(View.GONE);
                break;
        }
    }

    public void removeGarbageItem(View garbageView) {
        gameLayout.removeView(garbageView);
    }

    private void togglePause() {
        gamePaused = !gamePaused;
        btnPause.setText(gamePaused ? "Resume" : "Pause");
    }

    private void endGame() {
        gameRunning = false;

        // Save high score
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        if (score > highScore) {
            prefs.edit().putInt("highScore", score).apply();
        }

        // Show game over dialog
        new AlertDialog.Builder(this)
                .setTitle("Game Over!")
                .setMessage("Final Score: " + score + "\nHigh Score: " + Math.max(score, highScore))
                .setPositiveButton("Play Again", (dialog, which) -> {
                    Intent intent = new Intent(GameActivity.this, GameActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameRunning = false;
        gameHandler.removeCallbacksAndMessages(null);
    }
}