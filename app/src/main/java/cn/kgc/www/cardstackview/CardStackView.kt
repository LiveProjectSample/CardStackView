package cn.kgc.www.cardstackview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.BaseAdapter

/**
 * Created by kgc on 2017/11/3.
 */
class CardStackView : ViewGroup {

    var lastX: Float = 0f
    var deltaX: Float = 0f
    var mTracker: VelocityTracker? = null

    var myAdapter: BaseAdapter? = null

    val childTopMargin = resources.getDimension(R.dimen.dp25)
    val childSideMargin = resources.getDimension(R.dimen.dp50)
    var topIndex = 0

    var fadeAnim: ValueAnimator? = null
    var isActionDealed = false

    constructor(context: Context):super(context){
    }
    constructor(context: Context, attrs: AttributeSet):this(context, attrs, 0){
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):super(context, attrs, defStyleAttr){
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for(childIndex in 0..childCount-1) {
            val child = getChildAt(childIndex)
            child.layout(childSideMargin.toInt(), childTopMargin.toInt(), childSideMargin.toInt()+child.measuredWidth, childTopMargin.toInt()+child.measuredHeight)
        }
    }

    //wrap_content
    //match_parent
    //绝对数值
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        var widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        widthSpecSize = (widthSpecSize - childSideMargin * 2).toInt()

        var heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        var heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        heightSpecSize = (heightSpecSize - childTopMargin).toInt()

        for(childIndex in 0..childCount-1){
            val child = getChildAt(childIndex)
            val myWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSpecSize, widthSpecMode)
            val myHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSpecSize, heightSpecMode)
            child.measure(myWidthMeasureSpec, myHeightMeasureSpec)
        }
    }

    fun setAdapter(adapter: BaseAdapter){
        myAdapter = adapter
        topIndex = 0
        //最多显示3个子view
        //存在一个数组当中的
        //index从0开始描绘
        for(index in 0..2){
            addView(myAdapter!!.getView(index, null, null),0)
        }

        val observer = viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                setAllViewsScale()
            }

        })

    }

    private fun setAllViewsScale(){
        for(childIndex in childCount - 1 downTo 0){
            val child = getChildAt(childIndex)

            child.scaleX = 1 - 0.1f*(2  - childIndex)
            child.scaleY = 1 - 0.1f*(2  - childIndex)

            val deltaY = child.height*(1-child.scaleY)/2f
            if(childIndex != childCount - 1){
                child.translationY = -deltaY - childTopMargin
            }

        }
    }

    //一个ACTION_DOWN开始，中间可能有多个ACTION_MOVE，最后以一个ACTION_UP结束
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var intercept = false
        when(ev.action){
            MotionEvent.ACTION_MOVE->{
                mTracker!!.computeCurrentVelocity(1000)
                val xVelo = mTracker!!.xVelocity
                if(Math.abs(xVelo) > 0){
                    intercept = true
                }
            }
        }
        return intercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_MOVE->{
                deltaX += event.x - lastX
                lastX = event.x
                val topView = getChildAt(childCount - 1)
                topView.translationX = deltaX

                var percent = Math.abs(deltaX/childSideMargin)
                if(percent > 1){
                    percent = 1f
                }
                scaleUnderViews(percent)
            }
            MotionEvent.ACTION_UP->{
                val topView = getChildAt(childCount - 1)

                if(Math.abs(deltaX) >= childSideMargin){
                    startFadeAnim(true)
                }else{
                    startFadeAnim(false)
//                    deltaX = 0f
//                    lastX = 0f
//                    topView.translationX = 0f
                }
            }
        }
        return true
    }

    private fun scaleUnderViews(percent: Float) {
        for(childIndex in childCount - 2 downTo 0){
            val child = getChildAt(childIndex)
            val zoom = 0.1f*percent

            child.scaleX = 1 - 0.1f*(2  - childIndex) + zoom
            child.scaleY = 1 - 0.1f*(2  - childIndex) + zoom


            if(childIndex == 1){
                val deltaY = 0.05f*(1-percent)*child.height
                child.translationY = -deltaY-childTopMargin*(1-percent)
            }else{
                val deltaY = (1-child.scaleY)/2*child.height
                child.translationY = -deltaY-childTopMargin
            }
        }
    }

    private fun startFadeAnim(isSwitch: Boolean){
        if(fadeAnim == null || !fadeAnim!!.isRunning){
            val speed = (width - childSideMargin)/500f
            val time = if(isSwitch){
                500 - Math.abs(deltaX)/speed
            }else{
                Math.abs(deltaX)/speed
            }
            fadeAnim = ValueAnimator.ofInt(0,time.toInt()).setDuration(time.toLong())
            fadeAnim!!.interpolator = LinearInterpolator()
            fadeAnim!!.addUpdateListener(object: ValueAnimator.AnimatorUpdateListener{
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    val value = animation.animatedValue.toString().toInt()
                    val animMove = value*speed
                    val frontView = getChildAt(childCount - 1)
                    when{
                        deltaX<0&&isSwitch || deltaX>0&&!isSwitch->{
                            frontView.translationX = deltaX - animMove
                        }
                        deltaX>0&&isSwitch || deltaX<0&&!isSwitch->{
                            frontView.translationX = deltaX + animMove
                        }
                    }
                    var percent = Math.abs(frontView.translationX/childSideMargin)
                    if(percent>=1f){
                        percent = 1f
                    }
                    scaleUnderViews(percent)
                }
            })
            fadeAnim!!.addListener(object: Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if(isSwitch){
                        switchToNext()
                    }else{
                        lastX = 0f
                        deltaX = 0f
                        setAllViewsScale()
                    }

                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }
        fadeAnim!!.start()
    }
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(fadeAnim != null && fadeAnim!!.isRunning)return true
        if(ev.action != MotionEvent.ACTION_DOWN &&!isActionDealed) return true

        if(mTracker == null){
            mTracker = VelocityTracker.obtain()
        }
        when(ev.action){
            MotionEvent.ACTION_DOWN->{
                mTracker!!.addMovement(ev)
                lastX = ev.x
                isActionDealed = true
            }
            MotionEvent.ACTION_MOVE->{
                mTracker!!.addMovement(ev)
            }
            MotionEvent.ACTION_UP->{
                mTracker!!.recycle()
                mTracker = null
                isActionDealed = false
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun switchToNext(){
        val index = (topIndex + 3)%myAdapter!!.count
        topIndex = (topIndex+1)%myAdapter!!.count
        val frontView = getChildAt(childCount - 1)
        removeViewAt(childCount - 1)
        addView(myAdapter!!.getView(index, frontView, null),0)
        deltaX = 0f
        lastX = 0f
        frontView.translationX = 0f
    }
}