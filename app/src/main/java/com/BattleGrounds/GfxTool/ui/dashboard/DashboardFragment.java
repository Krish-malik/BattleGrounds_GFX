package com.BattleGrounds.GfxTool.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.BattleGrounds.GfxTool.MainActivity2;
import com.BattleGrounds.GfxTool.R;
import com.BattleGrounds.GfxTool.databinding.FragmentDashboardBinding;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private String selectedGamePackage = "com.pubg.imobile"; // Default game package
    private String selectedGameName = "BGMI"; // Default game name
    private int selectedGameImage = R.drawable.bgmi; // Default game image
    private int currentGameIndex = 0; // Track current game index for swipe

    // Card references for selection states
    private CardView selectedCard = null;
    private CardView bgmiCard, pubgCard, koreaCard, taiwanCard;

    // Gesture detector for swipe functionality
    private GestureDetector gestureDetector;

    // Game data arrays for easy navigation
    private final String[] gameNames = {"BGMI", "PUBG Global", "PUBG Korea", "PUBG Taiwan"};
    private final String[] gamePackages = {"com.pubg.imobile", "com.tencent.ig", "com.pubg.krmobile", "com.rekoo.pubgm"};
    private final int[] gameImages = {R.drawable.bgmi, R.drawable.pubg, R.drawable.korea, R.drawable.taiwan};
    private CardView[] gameCards;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Set up swipe functionality
        setupSwipeGestures();

        // Set default selection
        setDefaultSelection();

        // Setup continue button
        setupContinueButton();

        return root;
    }

    private void initializeViews() {
        // Find card views
        LinearLayout imageSelectorLayout = binding.horizontalScrollView.findViewById(R.id.imageSelectorLayout);
        bgmiCard = imageSelectorLayout.findViewById(R.id.bgmiCard);
        pubgCard = imageSelectorLayout.findViewById(R.id.pubgCard);
        koreaCard = imageSelectorLayout.findViewById(R.id.koreaCard);
        taiwanCard = imageSelectorLayout.findViewById(R.id.taiwanCard);

        // Initialize game cards array
        gameCards = new CardView[]{bgmiCard, pubgCard, koreaCard, taiwanCard};
    }

    private void setupSwipeGestures() {
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();

                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Handle single tap (original functionality)
                onMainGameImageClick(binding.mainGameImage);
                return true;
            }
        });

        // Set touch listener on main game image container
        binding.imageContainer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // Also set on the image itself for better touch coverage
        binding.mainGameImage.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    private void onSwipeLeft() {
        // Swipe left - next game
        currentGameIndex = (currentGameIndex + 1) % gameNames.length;
        animateSwipeTransition(true);
        updateGameSelectionByIndex(currentGameIndex);

        // Add haptic feedback
        binding.getRoot().performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void onSwipeRight() {
        // Swipe right - previous game
        currentGameIndex = (currentGameIndex - 1 + gameNames.length) % gameNames.length;
        animateSwipeTransition(false);
        updateGameSelectionByIndex(currentGameIndex);

        // Add haptic feedback
        binding.getRoot().performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void animateSwipeTransition(boolean isLeftSwipe) {
        View imageContainer = binding.imageContainer;

        // Create slide animation based on swipe direction
        float startX = isLeftSwipe ? 0f : 0f;
        float midX = isLeftSwipe ? -50f : 50f;
        float endX = 0f;

        // First phase: slide and scale down
        imageContainer.animate()
                .translationX(midX)
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .withEndAction(() -> {
                    // Second phase: slide back and scale up
                    imageContainer.animate()
                            .translationX(endX)
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(200)
                            .setInterpolator(new OvershootInterpolator(1.2f))
                            .withEndAction(() -> {
                                // Final phase: settle to normal size
                                imageContainer.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100);
                            });
                });

        // Add rotation effect to the image
        binding.mainGameImage.animate()
                .rotation(isLeftSwipe ? -5f : 5f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.mainGameImage.animate()
                            .rotation(0f)
                            .setDuration(200);
                });
    }

    private void updateGameSelectionByIndex(int index) {
        // Reset previous card selection
        if (selectedCard != null) {
            resetCardSelection(selectedCard);
        }

        // Update game selection
        CardView newCard = gameCards[index];
        updateGameSelection(newCard, gameImages[index], gameNames[index], gamePackages[index]);

        // Animate new card selection
        animateCardSelection(newCard);

        // Scroll to show selected card
        scrollToSelectedCard(newCard);
    }

    private void scrollToSelectedCard(CardView card) {
        // Smooth scroll to show the selected card
        binding.horizontalScrollView.post(() -> {
            int cardPosition = 0;
            for (int i = 0; i < gameCards.length; i++) {
                if (gameCards[i] == card) {
                    cardPosition = i * 180; // Approximate card width + margin
                    break;
                }
            }
            binding.horizontalScrollView.smoothScrollTo(cardPosition, 0);
        });
    }

    private void setupClickListeners() {
        LinearLayout imageSelectorLayout = binding.horizontalScrollView.findViewById(R.id.imageSelectorLayout);

        // Find image views within cards
        CircleImageView bgmiImage = imageSelectorLayout.findViewById(R.id.bgmi);
        CircleImageView pubgImage = imageSelectorLayout.findViewById(R.id.pubg);
        CircleImageView koreaImage = imageSelectorLayout.findViewById(R.id.korea);
        CircleImageView taiwanImage = imageSelectorLayout.findViewById(R.id.taiwan);

        // Set click listeners for cards (better touch target)
        setCardClickListener(bgmiCard, bgmiImage, R.drawable.bgmi, "BGMI", "com.pubg.imobile", 0);
        setCardClickListener(pubgCard, pubgImage, R.drawable.pubg, "PUBG Global", "com.tencent.ig", 1);
        setCardClickListener(koreaCard, koreaImage, R.drawable.korea, "PUBG Korea", "com.pubg.krmobile", 2);
        setCardClickListener(taiwanCard, taiwanImage, R.drawable.taiwan, "PUBG Taiwan", "com.rekoo.pubgm", 3);
    }

    private void setDefaultSelection() {
        // Set BGMI as default selection
        currentGameIndex = 0;
        updateGameSelection(bgmiCard, R.drawable.bgmi, "BGMI", "com.pubg.imobile");
    }

    private void setupContinueButton() {
        MaterialButton continueButton = binding.getRoot().findViewById(R.id.continueButton);
        if (continueButton != null) {
            continueButton.setOnClickListener(view -> {
                // Add button press animation
                view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .withEndAction(() -> {
                            view.setScaleX(1f);
                            view.setScaleY(1f);
                            launchGfxTool();
                        });
            });
        }
    }

    private void setCardClickListener(CardView card, CircleImageView imageView, int drawableRes,
                                      String gameName, String packageName, int gameIndex) {

        View.OnClickListener clickListener = view -> {
            // Update current game index
            currentGameIndex = gameIndex;

            // Animate card selection
            animateCardSelection(card);

            // Update game selection
            updateGameSelection(card, drawableRes, gameName, packageName);
        };

        // Set click listener on both card and image
        card.setOnClickListener(clickListener);
        imageView.setOnClickListener(clickListener);
    }

    private void animateCardSelection(CardView clickedCard) {
        // Reset previous selection
        if (selectedCard != null && selectedCard != clickedCard) {
            resetCardSelection(selectedCard);
        }

        // Animate new selection
        ScaleAnimation scaleUp = new ScaleAnimation(
                1f, 1.05f, 1f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(200);
        scaleUp.setFillAfter(true);

        clickedCard.startAnimation(scaleUp);
        clickedCard.setCardElevation(12f); // Increase elevation
        clickedCard.setSelected(true);

        selectedCard = clickedCard;
    }

    private void resetCardSelection(CardView card) {
        ScaleAnimation scaleDown = new ScaleAnimation(
                1.05f, 1f, 1.05f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(200);
        scaleDown.setFillAfter(true);

        card.startAnimation(scaleDown);
        card.setCardElevation(6f); // Reset elevation
        card.setSelected(false);
    }

    private void updateGameSelection(CardView card, int drawableRes, String gameName, String packageName) {
        // Update main image with fade animation
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.mainGameImage.setImageResource(drawableRes);

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(250);
                binding.mainGameImage.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        binding.mainGameImage.startAnimation(fadeOut);

        // Update game version text with slide animation
        Animation slideOut = new TranslateAnimation(0, -300, 0, 0);
        slideOut.setDuration(250);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.gameversion.setText(gameName);

                Animation slideIn = new TranslateAnimation(300, 0, 0, 0);
                slideIn.setDuration(250);
                binding.gameversion.startAnimation(slideIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        binding.gameversion.startAnimation(slideOut);

        // Update selection variables
        selectedGamePackage = packageName;
        selectedGameName = gameName;
        selectedGameImage = drawableRes;

        selectedCard = card;
    }

    private void onMainGameImageClick(View view) {
        // Enhanced bounce animation
        view.animate()
                .setInterpolator(new BounceInterpolator())
                .setDuration(400)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .withEndAction(() -> {
                    view.animate()
                            .setDuration(200)
                            .scaleX(1f)
                            .scaleY(1f)
                            .withEndAction(this::launchGfxTool);
                });
    }

    private void launchGfxTool() {
        if (isAppInstalled(requireContext(), selectedGamePackage)) {
            Intent intent = new Intent(requireContext(), MainActivity2.class);
            intent.putExtra("GAME_NAME", selectedGameName);
            intent.putExtra("GAME_VERSION", binding.gameversion.getText().toString());
            intent.putExtra("GAME_IMAGE", selectedGameImage);
            intent.putExtra("GAME_PACKAGE", selectedGamePackage);

            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            showGameNotInstalledDialog();
        }
    }

    private void showGameNotInstalledDialog() {
        // Enhanced error feedback with animation
        View rootView = binding.getRoot();
        rootView.animate()
                .translationX(20f)
                .setDuration(50)
                .withEndAction(() -> {
                    rootView.animate()
                            .translationX(-20f)
                            .setDuration(50)
                            .withEndAction(() -> {
                                rootView.animate()
                                        .translationX(0f)
                                        .setDuration(50);
                            });
                });

        Toast.makeText(requireContext(),
                selectedGameName + " is not installed on your device!",
                Toast.LENGTH_LONG).show();
    }

    // Function to Check if an App is Installed
    private boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        selectedCard = null;
        gestureDetector = null;
        gameCards = null;
    }

    // Additional helper method for programmatic selection (if needed)
    public void selectGame(String packageName) {
        int index = -1;
        for (int i = 0; i < gamePackages.length; i++) {
            if (gamePackages[i].equals(packageName)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            currentGameIndex = index;
            updateGameSelectionByIndex(index);
        }
    }

    // New method to get current game info (useful for external access)
    public String getCurrentGamePackage() {
        return selectedGamePackage;
    }

    public String getCurrentGameName() {
        return selectedGameName;
    }

    public int getCurrentGameIndex() {
        return currentGameIndex;
    }
}