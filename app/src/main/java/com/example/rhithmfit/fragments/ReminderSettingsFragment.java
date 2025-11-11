package com.example.rhithmfit.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rhithmfit.databinding.FragmentReminderSettingsBinding;

public class ReminderSettingsFragment extends Fragment {

    // SharedPreferences keys
    public static final String PREFS_NAME = "reminder_prefs";
    public static final String KEY_ENABLED = "reminder_enabled";
    public static final String KEY_HOUR = "reminder_hour";
    public static final String KEY_MINUTE = "reminder_minute";
    public static final String KEY_LAST_SHOWN_DAY = "reminder_last_shown_day";

    private FragmentReminderSettingsBinding binding;

    public ReminderSettingsFragment() { }

    public static ReminderSettingsFragment newInstance() {
        return new ReminderSettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReminderSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved state
        boolean enabled = prefs.getBoolean(KEY_ENABLED, false);
        int hour = prefs.getInt(KEY_HOUR, 8);    // default 8:00 AM
        int minute = prefs.getInt(KEY_MINUTE, 0);

        // Fill UI with saved values
        binding.switchEnableReminder.setChecked(enabled);

        binding.timePickerReminder.setIs24HourView(false);
        if (Build.VERSION.SDK_INT >= 23) {
            binding.timePickerReminder.setHour(hour);
            binding.timePickerReminder.setMinute(minute);
        } else {
            binding.timePickerReminder.setCurrentHour(hour);
            binding.timePickerReminder.setCurrentMinute(minute);
        }

        // Save settings button
        binding.buttonSaveReminder.setOnClickListener(v -> {
            boolean newEnabled = binding.switchEnableReminder.isChecked();

            int newHour;
            int newMinute;
            if (Build.VERSION.SDK_INT >= 23) {
                newHour = binding.timePickerReminder.getHour();
                newMinute = binding.timePickerReminder.getMinute();
            } else {
                newHour = binding.timePickerReminder.getCurrentHour();
                newMinute = binding.timePickerReminder.getCurrentMinute();
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ENABLED, newEnabled);
            editor.putInt(KEY_HOUR, newHour);
            editor.putInt(KEY_MINUTE, newMinute);

            // Reset last_shown_day when enabling reminders so we'll show again today if needed
            if (newEnabled) {
                editor.putInt(KEY_LAST_SHOWN_DAY, -1);
            }

            editor.apply();

            if (newEnabled) {
                Toast.makeText(
                        requireContext(),
                        "Workout reminder set for " + formatTime(newHour, newMinute),
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        requireContext(),
                        "Workout reminders turned off",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Back to home button
        binding.buttonBackHome.setOnClickListener(v -> {
            // just close this fragment and go back
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }

    private String formatTime(int hour24, int minute) {
        int h = hour24 % 12;
        if (h == 0) h = 12;
        String ampm = (hour24 < 12) ? "AM" : "PM";
        String mm = (minute < 10) ? ("0" + minute) : ("" + minute);
        return h + ":" + mm + " " + ampm;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
