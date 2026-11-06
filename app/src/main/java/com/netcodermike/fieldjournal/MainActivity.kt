package com.netcodermike.fieldjournal

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var title: EditText
    private lateinit var notes: EditText
    private lateinit var status: TextView
    private lateinit var entries: LinearLayout
    private var photo: String? = null
    private var marker: String? = null
    private val store get() = File(filesDir, "journal.json")
    private var journal = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { if (store.exists()) journal = JSONArray(store.readText()) }
        catch (e: Exception) { AlertDialog.Builder(this).setMessage("Journal could not be read. The original file is preserved; restart after recovering it.").setPositiveButton("Close") { _, _ -> finish() }.show(); return }
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        scroll.addView(layout); setContentView(scroll)
        layout.setOnApplyWindowInsetsListener { v, insets ->
            val bars = if (android.os.Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars()) else null
            if (bars != null) v.setPadding(32, bars.top + 24, 32, bars.bottom + 24)
            insets
        }
        layout.addView(TextView(this).apply { text = "FIELD JOURNAL\nYour observations, offline."; textSize = 28f })
        title = EditText(this).apply { hint = "Entry title" }; layout.addView(title)
        notes = EditText(this).apply { hint = "What did you discover?"; minLines = 3 }; layout.addView(notes)
        status = TextView(this); layout.addView(status)
        fun button(label: String, action: () -> Unit) { layout.addView(Button(this).apply { text = label; setOnClickListener { action() } }) }
        button("Attach a photo") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }, 10)
        }
        button("Add location marker") { locate() }
        button("Save observation") { save() }
        button("Export journal JSON") { startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply { type = "application/json"; putExtra(Intent.EXTRA_TITLE, "field-journal.json") }, 11) }
        entries = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; layout.addView(entries)
        drawEntries()
    }
    private fun locate() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 20); return
        }
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            val best = manager.getProviders(true).mapNotNull { manager.getLastKnownLocation(it) }.maxByOrNull { it.time }
            if (best == null) { status.text = "No cached fix. Get a GPS fix with another location app, then try again."; return }
            marker = "${best.latitude},${best.longitude} (cached ${DateFormat.getDateTimeInstance().format(Date(best.time))})"
            status.text = "Marker: $marker"
        } catch (e: SecurityException) { status.text = "Location permission unavailable." }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 20 && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) locate()
        else status.text = "You can save entries without location."
    }
    @Deprecated("Legacy Activity result API for dependency-free sample")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            if (requestCode == 10) {
                val name = "photo-${UUID.randomUUID()}.img"
                contentResolver.openInputStream(uri)?.use { input ->
                    File(filesDir, name).outputStream().use { out ->
                        val buffer = ByteArray(8192); var total = 0
                        while (true) { val n = input.read(buffer); if (n < 0) break; total += n; require(total <= 20 * 1024 * 1024) { "Photo exceeds 20 MiB" }; out.write(buffer, 0, n) }
                    }
                } ?: error("Cannot open image")
                photo?.let { File(filesDir, it).delete() }; photo = name; status.text = "Photo attached."
            } else if (requestCode == 11) contentResolver.openOutputStream(uri)?.use { it.write(journal.toString(2).toByteArray()) }
        } catch (e: Exception) { status.text = "Could not process file: ${e.message}" }
    }
    private fun save() {
        if (title.text.isBlank()) { title.error = "Give this observation a title"; return }
        val entry = JSONObject().put("id", UUID.randomUUID().toString()).put("title", title.text.toString()).put("notes", notes.text.toString()).put("time", System.currentTimeMillis()).put("location", marker ?: "").put("photo", photo ?: "")
        val next = JSONArray(journal.toString()).put(entry)
        try { val temp = File(filesDir, "journal.tmp"); temp.writeText(next.toString(2)); check(temp.renameTo(store)) { "Atomic save failed" }; journal = next }
        catch (e: Exception) { status.text = "Save failed: ${e.message}"; return }
        title.setText(""); notes.setText(""); marker = null; photo = null; status.text = "Saved on this device."; drawEntries()
    }
    private fun drawEntries() {
        entries.removeAllViews()
        for (i in journal.length() - 1 downTo 0) {
            val item = journal.getJSONObject(i)
            entries.addView(TextView(this).apply { text = "\n${item.getString("title")}\n${DateFormat.getDateTimeInstance().format(Date(item.getLong("time")))}\n${item.getString("notes")}\n${item.optString("location")}"; textSize = 18f; setTextIsSelectable(true) })
            val name = item.optString("photo")
            if (name.isNotEmpty()) entries.addView(Button(this).apply { text = "View attached photo"; setOnClickListener {
                val bitmap = android.graphics.BitmapFactory.decodeFile(File(filesDir, name).path, android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 })
                val image = ImageView(this@MainActivity).apply { setImageBitmap(bitmap); adjustViewBounds = true }
                AlertDialog.Builder(this@MainActivity).setView(image).setPositiveButton("Close", null).show()
            } })
        }
    }
}
