package de.tuberlin.mcc.simra.app.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.tuberlin.mcc.simra.app.BuildConfig
import de.tuberlin.mcc.simra.app.R
import de.tuberlin.mcc.simra.app.databinding.ActivityFeedbackBinding

// ... imports
class FeedbackActivity3 : AppCompatActivity() {
    private var feedbackBinding: ActivityFeedbackBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedbackBinding = ActivityFeedbackBinding.inflate(LayoutInflater.from(this))
        setContentView(feedbackBinding!!.root)
        setupToolbar()
        setupListView()
    }

    private fun setupToolbar() {
        val toolbar = feedbackBinding!!.toolbar.toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // feedbackBinding.toolbar.backButton.setOnClickListener(this::finish); v1
        feedbackBinding!!.toolbar.backButton.setOnClickListener { _: View? -> finish() }
    }

    private fun setupListView() {
        val items = resources.getStringArray(R.array.ContactItems)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        feedbackBinding!!.listView.adapter = adapter
        feedbackBinding!!.listView.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                handleListItemClick(
                    parent,
                    view,
                    position,
                    id
                )
            }
    }

    private fun handleListItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        var intent: Intent? = null
        when (position) {
            0 -> intent = createWebIntent(getString(R.string.link_simra_Page))
            1 -> intent = createEmailIntent()
            2 -> intent = createSocialMediaIntent(getString(R.string.link_to_twitter))
            3 -> intent = createSocialMediaIntent(getString(R.string.link_to_instagram))
            else -> Toast.makeText(this, R.string.notReady, Toast.LENGTH_SHORT).show()
        }
        intent?.let { startActivity(it) }
    }

    private fun createWebIntent(url: String): Intent {
        return Intent(this, WebActivity::class.java).putExtra(INTENT_KEY_URL, url)
    }

    private fun createEmailIntent(): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = INTENT_KEY_EMAIL
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.feedbackReceiver)))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedbackHeader))
        intent.putExtra(Intent.EXTRA_TEXT, feedbackEmailBody)
        return intent
    }

    private val feedbackEmailBody: String
        get() = getString(R.string.feedbackReceiver) + System.lineSeparator() +
                "App Version: " + BuildConfig.VERSION_CODE + System.lineSeparator() +
                "Android Version: " + Build.VERSION.RELEASE

    private fun createSocialMediaIntent(link: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(link))
    }

    companion object {
        private const val INTENT_KEY_URL = "URL"
        private const val INTENT_KEY_EMAIL = "message/rfc822"
    }
}