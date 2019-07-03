package com.example.lentachbrain

import android.annotation.TargetApi
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.DialogInterface
import android.os.Build
import android.os.StrictMode
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.solver.widgets.ConstraintWidgetContainer
import kotlinx.android.extensions.ContainerOptions
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
    }
    fun parseFeed(inputStream: InputStream): MutableList<RssFeedModel> {
        var title = ""
        var link = ""
        var description = ""
        var isItem = false
        var items: MutableList<RssFeedModel> = mutableListOf(RssFeedModel("no info", "", ""))
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
                    //xmlPullParser.nextTag()
                }
                /*} else {
                        items.add(RssFeedModel("", "", ""))*/
                when {
                    name.equals("title", true) -> {
                        title = result
                    }
                    name.equals("link", true) -> {
                        link = result
                    }
                    name.equals("description", true) -> {
                        description = result
                    }
                }
                if ((title != "") && (link != "") && (description != "")) {
                    if (isItem) {
                        items.add(RssFeedModel(title, link, description))
                    }

                    title = ""
                    link = ""
                    description = ""
                    isItem = false

                }
            }

        }
        catch(e: Exception) {
            items = listOf(RssFeedModel(e.toString(), "", "")).toMutableList()
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
            parsedList = mutableListOf(RssFeedModel(e.toString(), "", ""))
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
                    for (i in 2 until parsedList.size) {
                        var rowTitle = TextView(this)
                        var rowDescription = TextView(this)
                        var rowLink = TextView(this)
                        //if (rowLink.text.contains((regex))) {System.out.println("yooo")}
                        //var list = mutableListOf(rowTitle, rowDescription, rowLink)

                        rowTitle.text = "title"+parsedList[i].title
                        //rowTitle.fontFeatureSettings = "'color' 00FF00"
                        rowDescription.text = "desc"+parsedList[i].description
                        rowLink.text = "link"+parsedList[i].link

                        iLayout.addView(rowTitle)
                        iLayout.addView(rowDescription)
                        iLayout.addView(rowLink)
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

