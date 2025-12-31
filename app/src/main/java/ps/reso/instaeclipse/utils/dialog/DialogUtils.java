package ps.reso.instaeclipse.utils.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;
import ps.reso.instaeclipse.mods.devops.config.ConfigManager;
import ps.reso.instaeclipse.mods.ghost.ui.GhostEmojiManager;
import ps.reso.instaeclipse.mods.ui.UIHookManager;
import ps.reso.instaeclipse.utils.core.SettingsManager;
import ps.reso.instaeclipse.utils.feature.FeatureFlags;
import ps.reso.instaeclipse.utils.ghost.GhostModeUtils;

public class DialogUtils {

    private static AlertDialog currentDialog;

    // --- Glassmorphism Constants ---
    private static final int GLASS_BACKGROUND_COLOR = Color.parseColor("#E6121212"); // 90% Dark
    private static final int GLASS_BORDER_COLOR = Color.parseColor("#33FFFFFF"); // Subtle white border
    private static final int GLASS_CORNER_RADIUS = 45;
    private static final int ACCENT_COLOR = Color.parseColor("#448AFF"); // Blue Accent

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void showEclipseOptionsDialog(Context context) {
        SettingsManager.init(context);
        Context themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Dialog_Alert);

        LinearLayout mainLayout = buildMainMenuLayout(themedContext);
        ScrollView scrollView = new ScrollView(themedContext);
        scrollView.setFillViewport(true);
        scrollView.addView(mainLayout);

        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
        builder.setView(scrollView);
        builder.setCancelable(true);

        currentDialog = builder.create();

        // Make the dialog window transparent so our Glass layout shows correctly
        Window window = currentDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f); // Darker dim behind the glass
            
            // Optional: Blur behind (Android 12+) - Requires specific layout params
            // window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
        }

        currentDialog.show();
    }

    public static void showSimpleDialog(Context context, String title, String message) {
        try {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Exception e) {
            // handle UI crash fallback
        }
    }

    @SuppressLint("SetTextI18n")
    private static LinearLayout buildMainMenuLayout(Context context) {
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(50, 60, 50, 50);

        // Apply Glass Background
        mainLayout.setBackground(createGlassDrawable());

        // Title with Icon
        TextView title = new TextView(context);
        title.setText("InstaEclipse");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 30);
        
        // Subtitle/Icon
        TextView subTitle = new TextView(context);
        subTitle.setText("🌘 Control Center");
        subTitle.setTextColor(Color.parseColor("#AAAAAA"));
        subTitle.setTextSize(14);
        subTitle.setGravity(Gravity.CENTER);
        subTitle.setPadding(0, 0, 0, 40);

        mainLayout.addView(title);
        mainLayout.addView(subTitle);
        mainLayout.addView(createGlassDivider(context));

        // Menu Items - Spaced out nicely
        mainLayout.addView(createClickableSection(context, "🎛  Developer Options", () -> showDevOptions(context)));
        mainLayout.addView(createSpacer(context, 20));
        
        mainLayout.addView(createClickableSection(context, "👻  Ghost Mode Settings", () -> showGhostOptions(context)));
        mainLayout.addView(createSpacer(context, 20));

        mainLayout.addView(createClickableSection(context, "🛡  Ad & Analytics Block", () -> showAdOptions(context)));
        mainLayout.addView(createSpacer(context, 20));

        mainLayout.addView(createClickableSection(context, "🧘  Distraction-Free", () -> showDistractionOptions(context)));
        mainLayout.addView(createSpacer(context, 20));

        mainLayout.addView(createClickableSection(context, "⚙  Misc Features", () -> showMiscOptions(context)));
        mainLayout.addView(createSpacer(context, 20));
        
        mainLayout.addView(createClickableSection(context, "ℹ️  About", () -> showAboutDialog(context)));
        mainLayout.addView(createSpacer(context, 20));

        mainLayout.addView(createClickableSection(context, "🔁  Restart App", () -> showRestartSection(context)));

        mainLayout.addView(createSpacer(context, 40));
        mainLayout.addView(createGlassDivider(context));

        // Footer Credit
        TextView footer = new TextView(context);
        footer.setText("Developed by @reso7200");
        footer.setTextColor(Color.GRAY);
        footer.setTextSize(12);
        footer.setPadding(0, 30, 0, 10);
        footer.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.addView(footer);

        // Close Button (Glassy Red/Transparent)
        TextView closeButton = new TextView(context);
        closeButton.setText("Close");
        closeButton.setTextColor(Color.parseColor("#FF6B6B")); // Soft Red
        closeButton.setTextSize(16);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setPadding(40, 30, 40, 30);
        closeButton.setGravity(Gravity.CENTER);
        closeButton.setBackground(createRippleDrawable(Color.TRANSPARENT, 99));

        closeButton.setOnClickListener(v -> {
            if (currentDialog != null) currentDialog.dismiss();
        });

        mainLayout.addView(closeButton);

        SettingsManager.saveAllFlags();

        Activity activity = UIHookManager.getCurrentActivity();
        if (activity != null) {
            GhostEmojiManager.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
        }

        return mainLayout;
    }

    // --- Helpers for Glass UI ---

    private static GradientDrawable createGlassDrawable() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(GLASS_BACKGROUND_COLOR);
        gd.setCornerRadius(GLASS_CORNER_RADIUS);
        gd.setStroke(3, GLASS_BORDER_COLOR); // 3px white-ish border for glass effect
        return gd;
    }
    
    // Create a Ripple Effect (Click feedback)
    private static android.graphics.drawable.Drawable createRippleDrawable(int normalColor, int cornerRadius) {
        ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"));
        GradientDrawable content = new GradientDrawable();
        content.setColor(normalColor);
        content.setCornerRadius(cornerRadius);
        
        return new RippleDrawable(rippleColor, content, null);
    }
    
    private static View createSpacer(Context context, int height) {
        View view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return view;
    }

    private static View createGlassDivider(Context context) {
        View divider = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        params.setMargins(20, 10, 20, 10);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.parseColor("#33FFFFFF")); // Semi-transparent white
        return divider;
    }

    private static View createClickableSection(Context context, String label, Runnable onClick) {
        TextView section = new TextView(context);
        section.setText(label);
        section.setTextSize(17);
        section.setTextColor(Color.parseColor("#EEEEEE"));
        section.setPadding(40, 35, 40, 35);
        
        // Glassy button background
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#1AFFFFFF")); // Very subtle light fill
        background.setCornerRadius(30);
        
        // Wrap in Ripple for touch effect
        ColorStateList rippleColor = ColorStateList.valueOf(Color.parseColor("#40FFFFFF"));
        RippleDrawable ripple = new RippleDrawable(rippleColor, background, null);
        
        section.setBackground(ripple);
        section.setOnClickListener(v -> onClick.run());
        
        // Add subtle shadow margin
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        section.setLayoutParams(params);
        
        return section;
    }

    // ... (Keep existing Logic for Switches/Features below) ...
    // The logic below is identical to your code but wrapped in the new UI helpers

    private static void showGhostQuickToggleOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);
        // ... (Logic remains same, but uses new layout style) ...
        Switch[] toggleSwitches = new Switch[]{
            createSwitch(context, "Include Hide Seen", FeatureFlags.quickToggleSeen),
            createSwitch(context, "Include Hide Typing", FeatureFlags.quickToggleTyping),
            createSwitch(context, "Include Disable Screenshot", FeatureFlags.quickToggleScreenshot),
            createSwitch(context, "Include Hide View Once", FeatureFlags.quickToggleViewOnce),
            createSwitch(context, "Include Hide Story Seen", FeatureFlags.quickToggleStory),
            createSwitch(context, "Include Hide Live Seen", FeatureFlags.quickToggleLive)
        };

        @SuppressLint("UseSwitchCompatOrMaterialCode") 
        Switch enableAllSwitch = createSwitch(context, "Enable/Disable All", areAllEnabled(toggleSwitches));
        
        // Master listener setup (Same as original)
        setupMasterSwitch(enableAllSwitch, toggleSwitches, (index, isChecked) -> {
             switch (index) {
                case 0: FeatureFlags.quickToggleSeen = isChecked; break;
                case 1: FeatureFlags.quickToggleTyping = isChecked; break;
                case 2: FeatureFlags.quickToggleScreenshot = isChecked; break;
                case 3: FeatureFlags.quickToggleViewOnce = isChecked; break;
                case 4: FeatureFlags.quickToggleStory = isChecked; break;
                case 5: FeatureFlags.quickToggleLive = isChecked; break;
            }
            SettingsManager.saveAllFlags();
        });

        layout.addView(createGlassDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAllSwitch));
        layout.addView(createGlassDivider(context));
        for (Switch s : toggleSwitches) layout.addView(s);

        showSectionDialog(context, "Quick Toggles", layout, () -> {});
    }

    // Helper to reduce code duplication in switch logic
    private interface SwitchListener { void onChange(int index, boolean isChecked); }
    
    private static void setupMasterSwitch(Switch master, Switch[] children, SwitchListener listener) {
        master.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (Switch s : children) s.setChecked(isChecked);
        });

        for (int i = 0; i < children.length; i++) {
            final int index = i;
            children[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                master.setOnCheckedChangeListener(null);
                master.setChecked(areAllEnabled(children));
                master.setOnCheckedChangeListener((mv, mc) -> {
                     for (Switch s : children) s.setChecked(mc);
                });
                listener.onChange(index, isChecked);
                
                // Update emoji if needed
                Activity activity = UIHookManager.getCurrentActivity();
                if (activity != null) GhostEmojiManager.addGhostEmojiNextToInbox(activity, GhostModeUtils.isGhostModeActive());
            });
        }
    }

    // ... Restart logic (Same as original) ...
    private static void restartApp(Context context) {
         try {
            String packageName = context.getPackageName();
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                clearAppCache(context);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Runtime.getRuntime().exit(0);
            }
        } catch (Exception e) {
             Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void clearAppCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) deleteRecursive(cacheDir);
        } catch (Exception ignored) {}
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        fileOrDirectory.delete();
    }

    // ==== SECTIONS (Using new Glass UI) ====

    private static void showDevOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);
        Switch devModeSwitch = createSwitch(context, "Enable Developer Mode", FeatureFlags.isDevEnabled);
        devModeSwitch.setOnCheckedChangeListener((v, c) -> {
            FeatureFlags.isDevEnabled = c;
            SettingsManager.saveAllFlags();
        });

        layout.addView(devModeSwitch);
        layout.addView(createSpacer(context, 20));

        layout.addView(createStyledButton(context, "📥 Import Config", v -> {
             // Import logic (same as original)
             Activity act = UIHookManager.getCurrentActivity();
             if (act != null) {
                 FeatureFlags.isImportingConfig = true;
                 Intent i = new Intent();
                 i.setComponent(new ComponentName("ps.reso.instaeclipse", "ps.reso.instaeclipse.mods.devops.config.JsonImportActivity"));
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 try { act.startActivity(i); } catch(Exception e) { showSimpleDialog(context, "Error", "Failed"); }
             }
        }));
        
        layout.addView(createSpacer(context, 20));

        layout.addView(createStyledButton(context, "📤 Export Config", v -> {
             // Export logic
             Activity act = UIHookManager.getCurrentActivity();
             if (act != null) {
                 FeatureFlags.isExportingConfig = true;
                 ConfigManager.exportCurrentDevConfig(act);
                 Intent i = new Intent();
                 i.setComponent(new ComponentName("ps.reso.instaeclipse", "ps.reso.instaeclipse.mods.devops.config.JsonExportActivity"));
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 try { act.startActivity(i); } catch(Exception e) { showSimpleDialog(context, "Error", "Failed"); }
             }
        }));

        showSectionDialog(context, "Developer Options", layout, SettingsManager::saveAllFlags);
    }
    
    // New styled button for actions
    private static Button createStyledButton(Context context, String text, View.OnClickListener listener) {
        Button btn = new Button(context);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(createRippleDrawable(Color.parseColor("#30FFFFFF"), 20)); // Glassy button
        btn.setOnClickListener(listener);
        return btn;
    }

    private static void showGhostOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);
        Switch[] switches = new Switch[]{
            createSwitch(context, "Hide Seen", FeatureFlags.isGhostSeen),
            createSwitch(context, "Hide Typing", FeatureFlags.isGhostTyping),
            createSwitch(context, "Disable Screenshot", FeatureFlags.isGhostScreenshot),
            createSwitch(context, "Hide View Once", FeatureFlags.isGhostViewOnce),
            createSwitch(context, "Hide Story Seen", FeatureFlags.isGhostStory),
            createSwitch(context, "Hide Live Seen", FeatureFlags.isGhostLive)
        };

        layout.addView(createClickableSection(context, "🛠 Customize Quick Toggle", () -> showGhostQuickToggleOptions(context)));
        layout.addView(createSpacer(context, 20));
        
        Switch enableAll = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));
        
        setupMasterSwitch(enableAll, switches, (i, c) -> {
            switch(i) {
                case 0: FeatureFlags.isGhostSeen = c; break;
                case 1: FeatureFlags.isGhostTyping = c; break;
                case 2: FeatureFlags.isGhostScreenshot = c; break;
                case 3: FeatureFlags.isGhostViewOnce = c; break;
                case 4: FeatureFlags.isGhostStory = c; break;
                case 5: FeatureFlags.isGhostLive = c; break;
            }
            SettingsManager.saveAllFlags();
        });

        layout.addView(createGlassDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAll));
        layout.addView(createGlassDivider(context));
        for(Switch s : switches) layout.addView(s);

        showSectionDialog(context, "Ghost Mode", layout, () -> {});
    }

    private static void showAdOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);
        Switch[] switches = new Switch[]{
            createSwitch(context, "Block Ads", FeatureFlags.isAdBlockEnabled),
            createSwitch(context, "Block Analytics", FeatureFlags.isAnalyticsBlocked),
            createSwitch(context, "Disable Tracking Links", FeatureFlags.disableTrackingLinks)
        };

        Switch enableAll = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));
        
        setupMasterSwitch(enableAll, switches, (i, c) -> {
            if(i==0) FeatureFlags.isAdBlockEnabled = c;
            if(i==1) FeatureFlags.isAnalyticsBlocked = c;
            if(i==2) FeatureFlags.disableTrackingLinks = c;
            SettingsManager.saveAllFlags();
        });

        layout.addView(createGlassDivider(context));
        layout.addView(createEnableAllSwitch(context, enableAll));
        layout.addView(createGlassDivider(context));
        for(Switch s : switches) layout.addView(s);

        showSectionDialog(context, "Ad Block", layout, () -> {});
    }

    private static void showDistractionOptions(Context context) {
        // Logic identical to original, just UI wrapped
        LinearLayout layout = createSwitchLayout(context);
        Switch extreme = createSwitch(context, "Extreme Mode 🔒", FeatureFlags.isExtremeMode);
        Switch s1 = createSwitch(context, "Disable Stories", FeatureFlags.disableStories);
        Switch s2 = createSwitch(context, "Disable Feed", FeatureFlags.disableFeed);
        Switch s3 = createSwitch(context, "Disable Reels", FeatureFlags.disableReels);
        Switch s4 = createSwitch(context, "Disable Reels Except DM", FeatureFlags.disableReelsExceptDM);
        Switch s5 = createSwitch(context, "Disable Explore", FeatureFlags.disableExplore);
        Switch s6 = createSwitch(context, "Disable Comments", FeatureFlags.disableComments);
        
        Switch[] all = new Switch[]{s1, s2, s3, s4, s5, s6};
        Switch master = createSwitch(context, "Enable/Disable All", areAllEnabled(all));

        // Re-implementing original complex logic for Extreme mode/DM logic
        if (FeatureFlags.isExtremeMode) {
             for(Switch s: all) s.setEnabled(false);
             s4.setEnabled(s4.isChecked()); // Exception from original code
             master.setEnabled(false);
             extreme.setChecked(true);
             extreme.setEnabled(false);
        }

        extreme.setOnCheckedChangeListener((v, c) -> {
            if(c) {
                new AlertDialog.Builder(context).setTitle("Extreme Mode").setMessage("Cannot disable until reinstall. Sure?")
                .setPositiveButton("Yes", (d, w) -> {
                    FeatureFlags.isExtremeMode = true;
                    FeatureFlags.isDistractionFree = true;
                    FeatureFlags.disableStories = s1.isChecked();
                    FeatureFlags.disableFeed = s2.isChecked();
                    FeatureFlags.disableReels = s3.isChecked();
                    FeatureFlags.disableReelsExceptDM = s4.isChecked();
                    FeatureFlags.disableExplore = s5.isChecked();
                    FeatureFlags.disableComments = s6.isChecked();
                    SettingsManager.saveAllFlags();
                    for(Switch s: all) s.setEnabled(!s.isChecked());
                    s4.setEnabled(s4.isChecked());
                    master.setEnabled(false);
                    extreme.setEnabled(false);
                }).setNegativeButton("Cancel", (d,w) -> extreme.setChecked(false)).show();
            }
        });

        // Reels logic
        s3.setOnCheckedChangeListener((v, c) -> {
            s4.setEnabled(c);
            if(!c) { s4.setChecked(false); s4.setEnabled(false); }
            SettingsManager.saveAllFlags();
        });
        
        master.setOnCheckedChangeListener((v, c) -> {
            for(Switch s : all) { s.setChecked(c); s.setEnabled(true); }
            if(!c) { s4.setChecked(false); s4.setEnabled(false); }
        });

        layout.addView(extreme);
        layout.addView(createGlassDivider(context));
        layout.addView(createEnableAllSwitch(context, master));
        layout.addView(createGlassDivider(context));
        for(Switch s : all) layout.addView(s);

        showSectionDialog(context, "Distraction Free", layout, () -> {
            FeatureFlags.disableStories = s1.isChecked();
            FeatureFlags.disableFeed = s2.isChecked();
            FeatureFlags.disableReels = s3.isChecked();
            FeatureFlags.disableReelsExceptDM = s4.isChecked();
            FeatureFlags.disableExplore = s5.isChecked();
            FeatureFlags.disableComments = s6.isChecked();
        });
    }

    private static void showMiscOptions(Context context) {
        LinearLayout layout = createSwitchLayout(context);
        Switch[] switches = new Switch[]{
            createSwitch(context, "Disable Story Auto-Swipe", FeatureFlags.disableStoryFlipping),
            createSwitch(context, "Disable Video Autoplay", FeatureFlags.disableVideoAutoPlay),
            createSwitch(context, "Show Follower Toast", FeatureFlags.showFollowerToast),
            createSwitch(context, "Show Feature Toasts", FeatureFlags.showFeatureToasts)
        };
        Switch master = createSwitch(context, "Enable/Disable All", areAllEnabled(switches));
        
        setupMasterSwitch(master, switches, (i, c) -> {
            if(i==0) FeatureFlags.disableStoryFlipping = c;
            if(i==1) FeatureFlags.disableVideoAutoPlay = c;
            if(i==2) FeatureFlags.showFollowerToast = c;
            if(i==3) FeatureFlags.showFeatureToasts = c;
            SettingsManager.saveAllFlags();
        });

        layout.addView(createGlassDivider(context));
        layout.addView(createEnableAllSwitch(context, master));
        layout.addView(createGlassDivider(context));
        for(Switch s : switches) layout.addView(s);

        showSectionDialog(context, "Misc Features", layout, () -> {});
    }

    private static void showAboutDialog(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(context);
        title.setText("InstaEclipse 🌘");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24f);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView creator = new TextView(context);
        creator.setText("Created by @reso7200");
        creator.setTextColor(Color.LTGRAY);
        creator.setGravity(Gravity.CENTER);
        creator.setPadding(0, 20, 0, 40);

        Button githubButton = createStyledButton(context, "🌐 GitHub Repo", v -> {
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/ReSo7200/InstaEclipse"));
             browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             context.startActivity(browserIntent);
        });

        layout.addView(title);
        layout.addView(creator);
        layout.addView(githubButton);

        showSectionDialog(context, "About", layout, () -> {});
    }

    private static void showRestartSection(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 40);
        layout.setGravity(Gravity.CENTER);

        TextView message = new TextView(context);
        message.setText("Clear app cache and restart?");
        message.setTextColor(Color.WHITE);
        message.setTextSize(18f);
        message.setPadding(0, 0, 0, 40);

        Button restartButton = createStyledButton(context, "🔁 Restart Now", v -> restartApp(context));
        restartButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B71C1C"))); // Red tint

        layout.addView(message);
        layout.addView(restartButton);

        showSectionDialog(context, "Restart App", layout, () -> {});
    }

    // ==== CORE UI BUILDERS ====

    private static void showSectionDialog(Context context, String title, LinearLayout contentLayout, Runnable onSave) {
        if (currentDialog != null) currentDialog.dismiss();

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 50, 40, 40);
        container.setBackground(createGlassDrawable());

        // Header
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(22);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, 30);
        container.addView(titleView);

        container.addView(createGlassDivider(context));
        container.addView(contentLayout);
        container.addView(createGlassDivider(context));

        // Back Button
        TextView backBtn = new TextView(context);
        backBtn.setText("← Back");
        backBtn.setTextColor(Color.WHITE);
        backBtn.setTextSize(16);
        backBtn.setGravity(Gravity.CENTER);
        backBtn.setPadding(0, 30, 0, 10);
        backBtn.setBackground(createRippleDrawable(Color.TRANSPARENT, 99));
        
        backBtn.setOnClickListener(v -> {
            onSave.run();
            SettingsManager.saveAllFlags();
            showEclipseOptionsDialog(context);
        });

        container.addView(backBtn);

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.addView(container);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(scrollView);
        currentDialog = builder.create();
        
        // Apply Glass Window
        Window window = currentDialog.getWindow();
        if(window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0.7f);
        }

        currentDialog.show();
    }

    private static LinearLayout createSwitchLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 20, 10, 20);
        return layout;
    }

    private static Switch createSwitch(Context context, String label, boolean defaultState) {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch toggle = new Switch(context);
        toggle.setText(label);
        toggle.setChecked(defaultState);
        toggle.setPadding(20, 25, 20, 25);
        toggle.setTextColor(Color.parseColor("#F0F0F0"));
        toggle.setTextSize(16);
        
        // Custom colors for Glass look
        toggle.setThumbTintList(new ColorStateList(
            new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}},
            new int[]{ACCENT_COLOR, Color.LTGRAY}
        ));
        toggle.setTrackTintList(new ColorStateList(
            new int[][]{new int[]{android.R.attr.state_checked}, new int[]{-android.R.attr.state_checked}},
            new int[]{Color.parseColor("#1C4C78"), Color.DKGRAY}
        ));
        
        return toggle;
    }

    private static LinearLayout createEnableAllSwitch(Context context, Switch enableAllSwitch) {
        enableAllSwitch.setTextSize(18f);
        enableAllSwitch.setTextColor(Color.WHITE);
        enableAllSwitch.setTypeface(null, Typeface.BOLD);
        enableAllSwitch.setPadding(30, 30, 30, 30);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(10, 10, 10, 10);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#33000000")); // Slightly darker background for this special switch
        bg.setCornerRadius(25);
        container.setBackground(bg);

        container.addView(enableAllSwitch);
        return container;
    }

    private static boolean areAllEnabled(Switch[] switches) {
        for (Switch s : switches) if (!s.isChecked()) return false;
        return true;
    }
}
