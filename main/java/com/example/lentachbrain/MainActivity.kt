package com.example.lentachbrain

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.DialogInterface
import android.os.StrictMode
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.xml.sax.SAXException
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.xml.parsers.ParserConfigurationException


class MainActivity : AppCompatActivity() {

    /*class RSSHandler : DefaultHandler() {
        var rssResult = ""
        var rssTitle = ""
        var isFirstTitle = true
        var rssArray: Array<String> = arrayOf(rssTitle, rssResult)
        override fun startElement(uri: String, localName: String, qname: String, attributes: Attributes) {
            if ((localName == "title") && (isFirstTitle)) {
                 rssTitle = qname
                isFirstTitle = false
            }
            else if (localName == "title") {

            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            rssResult = ""
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            var cdata = String(ch, start, length)
                rssResult += cdata.trim().replace("\\s+", " ")+"\t"
        }
    }*/

    class RssFeedModel(title: String, link: String, description: String) {
        var title = title
        var link = link
        var description = description
        fun get(): List<String> {
            var result: List<String> = listOf(title, link, description)
            return result
        }
    }
    fun parseFeed(inputStream: InputStream): List<RssFeedModel> {
        var title = ""
        var link = ""
        var description = ""
        var isItem = false
        var items: List<RssFeedModel> = ArrayList()
        try {
            var xmlPullParser: XmlPullParser = Xml.newPullParser()
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            xmlPullParser.setInput(inputStream, null)

            xmlPullParser.nextTag()
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                var eventType = xmlPullParser.eventType
                var name: String? = xmlPullParser.name ?: continue
                if (eventType == XmlPullParser.END_TAG) {
                    if (name != null) {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = false
                        }
                    }
                    continue
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("item", ignoreCase = true)) {
                        isItem = true
                    }
                    continue
                }
                var result: String = ""
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.text
                    xmlPullParser.nextTag()
                }
                if (name.equals("title", true)) {
                    title = result
                } else if (name.equals("link", true)) {
                    link = result
                } else if (name.equals("description", true)) {
                    description = result
                }
                if ((title != "") && (link != "") && (description != "")) {
                    if (isItem) {
                        var item = RssFeedModel(title, link, description)
                        items += item
                    } else {
                        feedTitle.text = title
                        feedLink.text = link
                        feedDescription.text = description
                    }

                    title = ""
                    link = ""
                    description = ""
                    isItem = false

                }
            }
            return items
        } finally {
            inputStream.close()
        }
    }

    fun rssView(finalText: TextView): List<RssFeedModel> {
        var rssUrl: URL
        var result: String
        var parsedList: List<RssFeedModel>
        try {
            rssUrl = URL(finalText.text.toString())
            var inputSource: InputStream = rssUrl.openConnection().getInputStream()
            parsedList = parseFeed(inputSource)

        } catch (e: IOException) {
            parsedList = listOf(RssFeedModel(e.message.toString(), "", ""))
        } catch (e: SAXException) {
            parsedList = listOf(RssFeedModel(e.message.toString(), "", ""))
        } catch (e: ParserConfigurationException) {
            parsedList = listOf(RssFeedModel(e.message.toString(), "", ""))
        }
        return parsedList
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mFeedTitle: TextView = findViewById(R.id.feedTitle)
        var mFeedLink: TextView = findViewById(R.id.feedLink)
        var mFeedDescription: TextView = findViewById(R.id.feedDescription)
        var policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var button: Button = findViewById(R.id.addRSSButton)

        var feedModelList: List<RssFeedModel>




        button.setOnClickListener {
            val li: LayoutInflater = LayoutInflater.from(this)
            var promptView = li.inflate(R.layout.prompt, null)
            var iLayout: LinearLayout = findViewById(R.id.linear)
            var mBuilder = AlertDialog.Builder(this)
            mBuilder.setView(promptView)
            var userUrlInput = promptView.findViewById<EditText>(R.id.urlRSS)

            val positiveButtonClick = { dialog: DialogInterface,
                                        _: Int ->
                dialog.cancel()
                var parsedList = rssView(finalText = userUrlInput)
                for (i in 0..parsedList.size) {
                    var rowTitle: TextView = TextView(this)
                    var rowDescription: TextView = TextView(this)
                    var rowLink: TextView = TextView(this)
                    rowTitle.text = parsedList[i].title
                    rowDescription.text = parsedList[i].description
                    rowLink.text = parsedList[i].link
                    iLayout.addView(rowTitle)
                    iLayout.addView(rowDescription)
                    iLayout.addView(rowLink)

                }
            }

            mBuilder.setCancelable(true)
            mBuilder.setPositiveButton("ok", DialogInterface.OnClickListener(function = positiveButtonClick))
            val mAlertDialog: AlertDialog = mBuilder.create()
            mAlertDialog.show()
        }
    }
}
    /*private inner class FetchFeedTask : AsyncTask<Void, Void, Boolean>() {

        private var urlLink: String? = null

        override fun onPreExecute() {
            mSwipeLayout.setRefreshing(true)
            urlLink = mEditText.getText().toString()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            if (TextUtils.isEmpty(urlLink))
                return false

            try {
                if (!urlLink!!.startsWith("http://") && !urlLink!!.startsWith("https://"))
                    urlLink = "http://" + urlLink!!

                val url = URL(urlLink!!)
                val inputStream = url.openConnection().getInputStream()
                mFeedModelList = parseFeed(inputStream)
                return true
            } catch (e: IOException) {
                Log.e(FragmentActivity.TAG, "Error", e)
            } catch (e: XmlPullParserException) {
                Log.e(FragmentActivity.TAG, "Error", e)
            }

            return false
        }

        override fun onPostExecute(success: Boolean?) {
            mSwipeLayout.setRefreshing(false)

            if (success!!) {
                mFeedTitleTextView.setText("Feed Title: $mFeedTitle")
                mFeedDescriptionTextView.setText("Feed Description: $mFeedDescription")
                mFeedLinkTextView.setText("Feed Link: $mFeedLink")
                // Fill RecyclerView
                mRecyclerView.setAdapter(RssFeedListAdapter(mFeedModelList))
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Enter a valid Rss feed url",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }*/

