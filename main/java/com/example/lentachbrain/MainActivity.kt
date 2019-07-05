package com.example.lentachbrain

import android.annotation.TargetApi
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.Image
import android.os.Build
import android.os.StrictMode
import android.text.util.Linkify
import android.text.util.Linkify.WEB_URLS
import android.util.Xml
import android.view.LayoutInflater
import android.widget.*
import com.squareup.picasso.Picasso
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.net.URL


class MainActivity : AppCompatActivity() {

    class RssFeedModel(title: String, link: String, description: String, image: String) {
        var title = title
        var link = link
        var description = description
        var image = image
    }
    fun parseFeed(inputStream: InputStream): MutableList<RssFeedModel> {
        var title = ""
        var link = ""
        var description = ""
        var image = ""
        var isItem = false
        var items: MutableList<RssFeedModel> = mutableListOf(RssFeedModel("no info", "", "", ""))
        try {
            var xmlPullParser: XmlPullParser = Xml.newPullParser()
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            xmlPullParser.setInput(inputStream, null)
            xmlPullParser.nextTag()
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                var eventType = xmlPullParser.eventType
                var name: String
                name = xmlPullParser.name ?: continue
                if (eventType == XmlPullParser.END_TAG) {
                    if (name != "") {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = false
                        }
                    }
                }
                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equals("item", ignoreCase = true)) {
                        isItem = true
                    }
                }
                var result = ""
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.text
                }
                if (name.equals("title", true)) {
                    title = result
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                    }
                }
                if (name.equals("description", true)) {
                    description = result
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                    }
                }
                if (name.equals("link", true)) {
                    link = result
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                    }
                }
                if ((name.equals("image", true)) || (name.equals("img", true))) {

                    image = result
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                    }
                }
                if ((title != "") && (description != "")) {
                    if (isItem) {
                        items.add(RssFeedModel(title, link, description, image))

                        isItem = false
                    }

                    title = ""
                    link = ""
                    description = ""

                }
            }

        }
        catch(e: Exception) {
            items.add(RssFeedModel(e.toString(), "", "", ""))
        }
        finally {
            inputStream.close()
            return items
        }
    }

    fun rssView(finalText: TextView): List<RssFeedModel> {
        var rssUrl: URL
        var parsedList: MutableList<RssFeedModel>
        try {
            rssUrl = URL(finalText.text.toString())
            var inputSource: InputStream = rssUrl.openConnection().getInputStream()
            parsedList = parseFeed(inputSource)

        } catch (e: Exception) {
            parsedList = mutableListOf(RssFeedModel("", "", "", ""),RssFeedModel(e.toString(), "", "", ""))
        }
        return parsedList
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val button: Button = findViewById(R.id.addRSSButton)

        //var feedModelList: List<RssFeedModel>




        button.setOnClickListener {
            val li: LayoutInflater = LayoutInflater.from(this)
            var promptView = li.inflate(R.layout.prompt, null)
            var iLayout: LinearLayout = findViewById(R.id.rssScrollLayout)
            var mBuilder = AlertDialog.Builder(this)
            mBuilder.setView(promptView)
            var userUrlInput = promptView.findViewById<EditText>(R.id.urlRSS)

            val positiveButtonClick = { dialog: DialogInterface,
                                        _: Int ->
                dialog.cancel()
                try{

                    var parsedList = rssView(finalText = userUrlInput)
                    for (i in 1 until parsedList.size) {
                        var rowTitle = TextView(this)
                        var rowDescription = TextView(this)
                        var rowLink = TextView(this)
                        var imageLink = parsedList[i].image
                        var rowImage = ImageView(this)
                        //var imageVal = BitmapFactory.decodeStream(imageLink.openConnection().getInputStream())
                        //var rowImage: ImageView = ImageView.
                        rowTitle.text = parsedList[i].title
                        rowTitle.typeface = Typeface.DEFAULT_BOLD
                        rowDescription.text =  parsedList[i].description
                        rowLink.text = parsedList[i].link


                        if (rowTitle.text != "") {
                            iLayout.addView(rowTitle)
                        }
                        if (rowDescription.text != "") {
                            iLayout.addView(rowDescription)
                        }
                        if (rowLink.text != "") {
                            iLayout.addView(rowLink)
                        }
                        try {
                            if (imageLink != "") {
                                Linkify.addLinks(rowLink, WEB_URLS)
                                Picasso.with(this).load(imageLink).into(rowImage)
                                iLayout.addView(rowImage)
                            }
                        } catch (e: Exception) {}
                }
                }catch(e: Exception){
                    var exceptionText = TextView(this)
                    exceptionText.text = e.toString()
                    iLayout.addView(exceptionText)}
            }

            mBuilder.setCancelable(true)
            mBuilder.setPositiveButton("ok", DialogInterface.OnClickListener(function = positiveButtonClick))
            val mAlertDialog: AlertDialog = mBuilder.create()
            mAlertDialog.show()
        }
    }
}

