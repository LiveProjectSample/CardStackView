package cn.kgc.www.cardstackview

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_cardview.view.*

class MainActivity : AppCompatActivity() {


    class MyAdapter(val context: Context) : BaseAdapter() {
        val dataList = List<String>(10,{it->"item is $it"})
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view: View
            if(convertView == null){
                view = LayoutInflater.from(context).inflate(R.layout.item_cardview, null)
            }else{
                view= convertView
            }
            view.textView.setText(dataList.get(position))
            view.button.setText(dataList.get(position))
            view.button.setOnClickListener {
                Toast.makeText(context, dataList.get(position), Toast.LENGTH_LONG).show()
            }
            return view
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return dataList.size
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cardstackview.setAdapter(MyAdapter(this))
    }
}
