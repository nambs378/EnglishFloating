package namnn.englishfloating.Service

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import namnn.englishfloating.R
import namnn.englishfloating.database.AppDatabase
import namnn.englishfloating.database.dao.VocabularyDAO
import namnn.englishfloating.database.entity.Vocabulary


class FloatingViewService : Service() {
    //    var size: Point = Point()
    val path = Path()
    lateinit var size: WindowMetrics
    private lateinit var floatViewParams: WindowManager.LayoutParams
    private lateinit var imageCloseParams: WindowManager.LayoutParams
    private lateinit var backgroundCloseViewParams: WindowManager.LayoutParams

    private lateinit var mWindowManager: WindowManager

    private lateinit var mFloatingView: View
    private lateinit var collapsedView: View
    private lateinit var expandedView: View
    private lateinit var viewPager2: ViewPager2
    private lateinit var imageClose: ImageView
    private lateinit var backgroundGradientClose: View

    private var imageCloseWidth = 0
    private var imageCloseHeight = 0

    private var imgCloseWidthZoneStart = 0
    private var imgCloseWidthZoneEnd = 0
    private var imgCloseWidthZone = 100

    private var imgCloseHeightZone = 0
    private var imgCloseHeightZoneStart = 0
    private var imgCloseHeightZoneEnd = 0

    private var backgroundGradientWidth = 0
    private var backgroundGradientHeight = 0

    var mWidthScreen = 0
    var mHeightScreen = 0
    var mCenterWidth = 0

    var mWidthWidget = 0
    var mHeightWidget = 0

    private var currentVocabulary: Vocabulary? = null
    private lateinit var textSelected: String
    private lateinit var answerButtonList: List<TextView>
    private lateinit var vocabularyDAO: VocabularyDAO

    // View
    private lateinit var englishTv: TextView
    private lateinit var ivDot: ImageView
    private lateinit var testCountTv: TextView

    private var isFloatingViewInImageClose = false

    private lateinit var animImageCloseZoomIn: Animation
    private lateinit var animImageCloseZoomOut: Animation

    private lateinit var floatingStartSound: MediaPlayer
    private lateinit var successSound: MediaPlayer

    private var animRunning = false
    private var isExpanded = false

    private var maxTestCount = 10
    private var currentTestCount = 0
    private var isCheckQuestion = false

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)


            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Vocabulary test")
                .setSmallIcon(R.drawable.ic_island_forest)
                .setContentText("Time to take the vocabulary test").build()
            notificationManager.notify(1, notification)
            startForeground(1, notification)
        }


        successSound = MediaPlayer.create(this, R.raw.sound_success)
        floatingStartSound = MediaPlayer.create(this, R.raw.sound_start)
        floatingStartSound.start()

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        initLayoutParams()

        //Specify the view position
        floatViewParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        floatViewParams.x = 0
        floatViewParams.y = 100

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        //Add background gradient
        initBackground()
        //Add image close
        initImageClose()
        //Add the view to the window
        mWindowManager.addView(mFloatingView, floatViewParams)

        //Get physical screen size
//        this.display!!.getRealSize(size)
        var size = mWindowManager.currentWindowMetrics
//        suze.bounds.width()
        mWidthScreen = size.bounds.width()
        mHeightScreen = size.bounds.height()

        animImageCloseZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        animImageCloseZoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);


        collapsedView = mFloatingView.findViewById(R.id.collapse_view)
        expandedView = mFloatingView.findViewById(R.id.expanded_container)

        //Set the close button
//        val closeButtonCollapsed: ImageView = mFloatingView.findViewById<View>(R.id.close_btn) as ImageView
//        closeButtonCollapsed.setOnClickListener(View.OnClickListener { //close the service and remove the from from the window
//            stopSelf()
//        })


        /*  //Set the view while floating view is expanded.
          //Set the play button.
          val playButton = mFloatingView!!.findViewById<View>(R.id.play_btn) as ImageView
          playButton.setOnClickListener {
              Toast.makeText(this@FloatingViewService, "Playing the song.", Toast.LENGTH_LONG)
                  .show()
          }

          //Set the next button.

          //Set the next button.
          val nextButton = mFloatingView!!.findViewById<View>(R.id.next_btn) as ImageView
          nextButton.setOnClickListener {
              Toast.makeText(this@FloatingViewService, "Playing next song.", Toast.LENGTH_LONG)
                  .show()
          }


          //Set the pause button.


          //Set the pause button.
          val prevButton = mFloatingView!!.findViewById<View>(R.id.prev_btn) as ImageView
          prevButton.setOnClickListener {
              Toast.makeText(
                  this@FloatingViewService,
                  "Playing previous song.",
                  Toast.LENGTH_LONG
              ).show()
          }


          //Set the close button


          //Set the close button
          val closeButton = mFloatingView!!.findViewById<View>(R.id.close_button) as ImageView
          closeButton.setOnClickListener {
              collapsedView.visibility = View.VISIBLE
              expandedView.visibility = View.GONE
          }


          //Open the application on thi button click
          val openButton = mFloatingView!!.findViewById<View>(R.id.open_button) as ImageView
          openButton.setOnClickListener { //Open the application  click.
              val intent = Intent(this@FloatingViewService, MainActivity::class.java)
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
              startActivity(intent)


              //close the service and remove view from the view hierarchy
              stopSelf()
          }*/

        val homeButton = mFloatingView.findViewById<View>(R.id.btn_home) as Button
        homeButton.setOnClickListener {
            val intent = Intent("android.intent.category.LAUNCHER")
            intent.setClassName("namnn.englishfloating", "namnn.englishfloating.MainActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //Set the close button
        val closeButton = mFloatingView.findViewById<View>(R.id.btn_close) as Button
        closeButton.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            collapsedView.visibility = View.VISIBLE
            expandedView.visibility = View.GONE
            isExpanded = false
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "english_db",
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
        vocabularyDAO = db.languageDao()

        englishTv = mFloatingView.findViewById<View>(R.id.floating_tv_english) as TextView
        testCountTv = mFloatingView.findViewById<View>(R.id.floating_tv_test_count) as TextView
        ivDot = mFloatingView.findViewById<View>(R.id.iv_dot) as ImageView

        ivDot.visibility = View.VISIBLE

        val refreshButton = mFloatingView.findViewById<View>(R.id.refresh_button) as Button
        val answerA = mFloatingView.findViewById<View>(R.id.answer_a) as TextView
        val answerB = mFloatingView.findViewById<View>(R.id.answer_b) as TextView
        val answerC = mFloatingView.findViewById<View>(R.id.answer_c) as TextView
        val answerD = mFloatingView.findViewById<View>(R.id.answer_d) as TextView

        answerButtonList = listOf(answerA, answerB, answerC, answerD)

        refreshButton.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            getQuestion(englishTv, vocabularyDAO)
        }

        answerA.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            setStateBtnAnswer(answerA, ButtonState.SELECTED)
            textSelected = answerA.text.toString()
            checkQuestion()
        }

        answerB.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            setStateBtnAnswer(answerB, ButtonState.SELECTED)
            textSelected = answerB.text.toString()
            checkQuestion()
        }

        answerC.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            setStateBtnAnswer(answerC, ButtonState.SELECTED)
            textSelected = answerC.text.toString()
            checkQuestion()
        }

        answerD.setOnClickListener {
            if (isCheckQuestion) return@setOnClickListener
            setStateBtnAnswer(answerD, ButtonState.SELECTED)
            textSelected = answerD.text.toString()
            checkQuestion()
        }

        val layout = mFloatingView.findViewById<View>(R.id.layoutParent)
        val vto = layout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = layout.measuredWidth
                mWidthScreen = size.bounds.width() - width
                mCenterWidth = size.bounds.width() / 2
                mWidthWidget = width
                mHeightWidget = layout.measuredHeight
                Log.d(
                    "TAGG",
                    "width $mWidthScreen screen size ${size.bounds.width()} height ${size.bounds.height()} "
                )

                path.moveTo(
                    ((mCenterWidth - imageCloseWidth / 2).toFloat()),
                    (mHeightScreen + 300).toFloat()
                );
                path.lineTo(
                    ((mCenterWidth - imageCloseWidth / 2).toFloat()),
                    (mHeightScreen - (mHeightScreen - backgroundGradientHeight + 300)).toFloat()
                )
            }
        })


        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById<View>(R.id.root_container)
            .setOnTouchListener(object : OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            //remember the initial position.
                            initialX = floatViewParams.x
                            initialY = floatViewParams.y

                            val mAnimator: ObjectAnimator =
                                ObjectAnimator.ofFloat(imageClose, View.X, View.Y, path)
                            mAnimator.duration = 500
                            mAnimator.start()

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY

                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
//                            if (isExpanded) return false
                            //Calculate the X and Y coordinates of the view.
                            floatViewParams.y = initialY + (event.rawY - initialTouchY).toInt()

                            if (!isExpanded) {
                                floatViewParams.x = initialX + (event.rawX - initialTouchX).toInt()
                                backgroundGradientClose.visibility = View.VISIBLE
                                imageClose.x =
                                    ((((floatViewParams.x * imgCloseWidthZone) / mWidthScreen) + imgCloseWidthZoneStart) - (imageCloseWidth / 2)).toFloat()
                                imageClose.y =
                                    ((((floatViewParams.y * imgCloseHeightZone) / mHeightScreen) + imgCloseHeightZoneStart) - (imageCloseHeight / 2)).toFloat()

                                if ((floatViewParams.x + mWidthWidget / 2) > (imgCloseWidthZoneStart - 250) && (floatViewParams.x + mWidthWidget / 2) < (imgCloseWidthZoneEnd + 250)
                                    && (floatViewParams.y + mHeightWidget / 2) > (imgCloseHeightZoneStart - 250) && (floatViewParams.y + mHeightWidget / 2) < (imgCloseHeightZoneEnd + 250)
                                ) {
                                    if (!isExpanded) {
                                        floatViewParams.x =
                                            (imageClose.x + (imageCloseWidth * 1.2) / 2 - mWidthWidget / 2).toInt()
                                    }
                                    floatViewParams.y =
                                        (imageClose.y + (imageCloseHeight * 1.2) / 2 - mHeightWidget / 2).toInt()
                                    if (!animRunning) {
                                        zoomImageClose(true, imageClose)
                                        animRunning = true
                                    }
                                    isFloatingViewInImageClose = true
                                } else {
                                    if (isFloatingViewInImageClose) {
                                        isFloatingViewInImageClose = false
                                    }
                                    if (animRunning) {
                                        zoomImageClose(false, imageClose)
                                        animRunning = false
                                    }
                                }
                            }

                            //Update the layout with new X & Y coordinate floating view
                            mWindowManager.updateViewLayout(mFloatingView, floatViewParams)

                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val Xdiff = (event.rawX - initialTouchX).toInt()
                            val Ydiff = (event.rawY - initialTouchY).toInt()
                            var x = 0
                            backgroundGradientClose.visibility = View.INVISIBLE

                            val outSideView = collapsedView.width / 4

                            if (isFloatingViewInImageClose) {
                                stopSelf()
                            } else {
                                if (((initialX + (event.rawX - initialTouchX).toInt()) + (mWidthWidget / 2)) < mCenterWidth) {
                                    if (floatViewParams.x > 0) {
                                        val mHandler = Handler(Looper.getMainLooper())
                                        val mAction: Runnable = object : Runnable {
                                            override fun run() {
                                                if (floatViewParams.x > 0) {
                                                    x += 60
                                                    floatViewParams.x =
                                                        (initialX + (event.rawX - initialTouchX).toInt()) - x
                                                    mWindowManager.updateViewLayout(
                                                        mFloatingView,
                                                        floatViewParams
                                                    )
                                                    mHandler.postDelayed(this, 5)
                                                } else {
                                                    mHandler.removeCallbacks(this)
                                                }
                                            }
                                        }
                                        mHandler.post(mAction)
                                    }
                                } else {
                                    if (floatViewParams.x < mWidthScreen) {
                                        val mHandler = Handler(Looper.getMainLooper())
                                        val mAction: Runnable = object : Runnable {
                                            override fun run() {
                                                if (floatViewParams.x < mWidthScreen) {
                                                    x += 60
                                                    floatViewParams.x =
                                                        (initialX + (event.rawX - initialTouchX).toInt()) + x
                                                    mWindowManager.updateViewLayout(
                                                        mFloatingView,
                                                        floatViewParams
                                                    )
                                                    mHandler.postDelayed(this, 5)
                                                } else {
                                                    mHandler.removeCallbacks(this)
                                                }
                                            }
                                        }
                                        mHandler.post(mAction)
                                    }
                                }
                            }

                            Log.d(
                                "TAGG",
                                "ACTION_MOVE  Xdiff ${Xdiff}  Ydiff ${Ydiff} + outSideView $outSideView"
                            )
                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 5 && Ydiff < 5 && Xdiff > -5 && Ydiff > -5) {
                                if (isViewCollapsed()) {
                                    //When user clicks on the image view of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view will become visible.
                                    collapsedView.visibility = View.GONE
                                    expandedView.visibility = View.VISIBLE
                                    if (!isExpanded) {
                                        Log.d("AAAAAAAAAAAAAAA", isExpanded.toString())
                                        getQuestion(englishTv, vocabularyDAO)
                                        isExpanded = true
                                        ivDot.visibility = View.GONE
                                    }
                                }
                            }
                            return true
                        }

                    }
                    return false
                }
            })

    }

    private fun initImageClose() {
        imageClose = backgroundGradientClose.findViewById<View>(R.id.img_close) as ImageView
        imageClose.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                imageClose.viewTreeObserver.removeOnGlobalLayoutListener(this)
                imageCloseWidth = imageClose.measuredWidth
                imageCloseHeight = imageClose.measuredHeight

                imgCloseHeightZone =
                    ((mHeightScreen * imgCloseWidthZone) / mWidthScreen) - imgCloseWidthZone / 2
                imgCloseHeightZoneEnd = (mHeightScreen - (mHeightScreen * 0.2)).toInt()
                imgCloseHeightZoneStart = imgCloseHeightZoneEnd - imgCloseHeightZone

                imgCloseWidthZoneStart = (mWidthScreen - imgCloseWidthZone) / 2
                imgCloseWidthZoneEnd = imgCloseWidthZoneStart + imgCloseWidthZone

                imageClose.x = ((mWidthScreen - imageCloseWidth) / 2).toFloat()
                imageClose.y = (mHeightScreen + 200).toFloat()
            }
        })
    }

    private fun initBackground() {
        backgroundGradientClose =
            LayoutInflater.from(this).inflate(R.layout.layout_background_gradient_close, null);
        backgroundCloseViewParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        backgroundGradientClose.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                backgroundGradientClose.viewTreeObserver.removeOnGlobalLayoutListener(this)
                backgroundGradientWidth = backgroundGradientClose.measuredWidth
                backgroundGradientHeight = backgroundGradientClose.measuredHeight
                Log.d(
                    "TAGG",
                    "Background width ${backgroundGradientClose.measuredWidth} , height ${backgroundGradientClose.measuredHeight}"
                )
            }

        })
        backgroundGradientClose.visibility = View.INVISIBLE
        mWindowManager.addView(backgroundGradientClose, backgroundCloseViewParams)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private fun isViewCollapsed(): Boolean {
        return mFloatingView.findViewById<View>(R.id.collapse_view)
            .visibility == View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mWindowManager.removeView(mFloatingView)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun zoomImageClose(b: Boolean, imageClose: ImageView) {
        if (b) {
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                (imageCloseWidth * 1.2).toInt(),
                (imageCloseHeight * 1.2).toInt()
            )
            imageClose.layoutParams = layoutParams
        } else {
            val layoutParams: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(imageCloseWidth, imageCloseHeight)
            imageClose.layoutParams = layoutParams
        }
    }

    private fun getQuestion(tv: TextView, vocabularyDAO: VocabularyDAO) {
        currentVocabulary = if (currentVocabulary == null) {
            vocabularyDAO.getRandomEnglish()
        } else {
            vocabularyDAO.getRandomEnglishAvoidId(currentVocabulary!!.id!!)
        }
        currentVocabulary = vocabularyDAO.getRandomEnglish()
//        Handler(Looper.getMainLooper()).post { tv.text = currentLanguage.english }
        tv.text = currentVocabulary!!.english
        val wrongLanguage =
            vocabularyDAO.getRandomEnglishWithoutCurrentEnglish(currentVocabulary!!.id!!)
        wrongLanguage.add(currentVocabulary!!.vietnamese!!)
        wrongLanguage.shuffle()
        for (i in wrongLanguage.indices) {
            answerButtonList[i].text = wrongLanguage[i]
        }
    }

    private fun checkQuestion() {
        isCheckQuestion = true
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            for (item in answerButtonList) {
                val answerText = item.text.toString()
                if (currentVocabulary!!.vietnamese.equals(answerText)) {
                    setStateBtnAnswer(item, ButtonState.CORRECT)
                    if (textSelected == answerText) {
                        // select correct
                        successSound.start()
                        currentTestCount++
                        testCountTv.text =  "${currentTestCount}/${maxTestCount}"
                        if (maxTestCount == currentTestCount) {
                            // end test
                            stopSelf()
                            currentTestCount = 0
                        }
                        val newWrongCount : Int = if (currentVocabulary!!.wrongCount > 0) {
                            currentVocabulary!!.wrongCount - 1
                        } else {
                            0
                        }
                        vocabularyDAO.updateWrongCountById(currentVocabulary!!.id!!, newWrongCount)
                    } else {
                        val newWrongCount = currentVocabulary!!.wrongCount + 1
                        vocabularyDAO.updateWrongCountById(currentVocabulary!!.id!!, newWrongCount)
                    }
                }
                setAllBtnDefault()
            }
        }, 500)
    }

    private fun setAllBtnDefault() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            for (item in answerButtonList) {
                setStateBtnAnswer(item, ButtonState.DEFAULT)
                getQuestion(englishTv, vocabularyDAO)
            }
            isCheckQuestion = false
        }, 1500)
    }

    private fun setStateBtnAnswer(btn: TextView, state: ButtonState) {
        when (state) {
            ButtonState.DEFAULT -> {
                btn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_unselected, null)
            }
            ButtonState.SELECTED -> {
                btn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_selected, null)
            }
            ButtonState.WRONG -> {
                btn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_wrong, null)
            }
            ButtonState.CORRECT -> {
                btn.background =
                    ResourcesCompat.getDrawable(resources, R.drawable.bg_btn_correct, null)
            }
        }
    }

    private fun initLayoutParams() {
        //Add the view to the window.
        floatViewParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        imageCloseParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                resources.getDimension(R.dimen.image_close_width).toInt(),
                resources.getDimension(R.dimen.image_close_height).toInt(),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                resources.getDimension(R.dimen.image_close_width).toInt(),
                resources.getDimension(R.dimen.image_close_height).toInt(),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        backgroundCloseViewParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }
    }
}

enum class ButtonState {
    DEFAULT,
    SELECTED,
    WRONG,
    CORRECT
}