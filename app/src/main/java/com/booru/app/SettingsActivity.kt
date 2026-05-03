package com.booru.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.booru.app.databinding.ActivitySettingsBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // Gelbooru fields
        val etGelbooruUserId = findViewById<TextInputEditText>(R.id.etGelbooruUserId)
        val etGelbooruApiKey = findViewById<TextInputEditText>(R.id.etGelbooruApiKey)

        // Rule34 fields
        val etRule34UserId = findViewById<TextInputEditText>(R.id.etRule34UserId)
        val etRule34ApiKey = findViewById<TextInputEditText>(R.id.etRule34ApiKey)

        // Load current values
        etGelbooruUserId.setText(com.booru.app.util.Prefs.getGelbooruUserId(this))
        etGelbooruApiKey.setText(com.booru.app.util.Prefs.getGelbooruApiKey(this))
        etRule34UserId.setText(com.booru.app.util.Prefs.getRule34UserId(this))
        etRule34ApiKey.setText(com.booru.app.util.Prefs.getRule34ApiKey(this))

        // Tag format info
        findViewById<android.widget.TextView>(R.id.tvTagInfo).text =
            "Tag Format:\n" +
            "• Regular: Pokemon blue_hair\n" +
            "• OR groups: 1girl~2girls~3girls\n" +
            "• Combined: Pokemon 1girl~2girls blue_hair\n\n" +
            "The app auto-converts to each site's format:\n" +
            "• Gelbooru: {1girl ~ 2girls}\n" +
            "• Rule34: (1girl ~ 2girls)"

        // Save button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSave).setOnClickListener {
            val gelbooruUserId = etGelbooruUserId.text?.toString()?.trim() ?: ""
            val gelbooruApiKey = etGelbooruApiKey.text?.toString()?.trim() ?: ""
            val rule34UserId = etRule34UserId.text?.toString()?.trim() ?: ""
            val rule34ApiKey = etRule34ApiKey.text?.toString()?.trim() ?: ""

            com.booru.app.util.Prefs.setGelbooruUserId(this, gelbooruUserId)
            com.booru.app.util.Prefs.setGelbooruApiKey(this, gelbooruApiKey)
            com.booru.app.util.Prefs.setRule34UserId(this, rule34UserId)
            com.booru.app.util.Prefs.setRule34ApiKey(this, rule34ApiKey)

            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // How to get API key
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnHelp).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("How to Get API Keys")
                .setMessage(
                    "Gelbooru:\n" +
                    "1. Go to gelbooru.com and log in\n" +
                    "2. Go to your Profile → Options → API\n" +
                    "3. Copy your User ID and API Key\n\n" +
                    "Rule34.xxx:\n" +
                    "1. Go to rule34.xxx and log in\n" +
                    "2. Go to your Account Settings\n" +
                    "3. Find your API Key and User ID\n\n" +
                    "Note: You need an account on each site to use their API."
                )
                .setPositiveButton("OK", null)
                .show()
        }

        // Clear button
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClear).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear All Settings?")
                .setMessage("This will remove all saved API credentials.")
                .setPositiveButton("Clear") { _, _ ->
                    com.booru.app.util.Prefs.setGelbooruUserId(this, "")
                    com.booru.app.util.Prefs.setGelbooruApiKey(this, "")
                    com.booru.app.util.Prefs.setRule34UserId(this, "")
                    com.booru.app.util.Prefs.setRule34ApiKey(this, "")
                    etGelbooruUserId.setText("")
                    etGelbooruApiKey.setText("")
                    etRule34UserId.setText("")
                    etRule34ApiKey.setText("")
                    Toast.makeText(this, "Settings cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
