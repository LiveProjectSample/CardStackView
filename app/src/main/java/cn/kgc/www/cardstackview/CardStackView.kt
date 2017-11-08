package cn.kgc.www.cardstackview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewGroup
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
        //最多显示3个子view
        //存在一个数组当中的
        //index从0开始描绘
        for(index in 0..2){
            addView(myAdapter!!.getView(index, null, null),0)
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
            }
            MotionEvent.ACTION_UP->{
                deltaX = 0f
                lastX = 0f
                val topView = getChildAt(childCount - 1)
                topView.translationX = deltaX
            }
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if(mTracker == null){
            mTracker = VelocityTracker.obtain()
        }
        when(ev.action){
            MotionEvent.ACTION_DOWN->{
                mTracker!!.addMovement(ev)
                lastX = ev.x
            }
            MotionEvent.ACTION_MOVE->{
                mTracker!!.addMovement(ev)
            }
            MotionEvent.ACTION_UP->{
                mTracker!!.recycle()
                mTracker = null
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}