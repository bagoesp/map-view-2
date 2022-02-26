package com.bugs.mapview2

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    // lateinit view
    private lateinit var ivMap: ImageView
    private lateinit var spinAwal: Spinner
    private lateinit var spinTujuan: Spinner
    private lateinit var btnSearch: Button

    // mapview
    private lateinit var mCanvas: Canvas
    private val mPaint = Paint()
    private lateinit var mBitmap: Bitmap
    private val mRect = RectF()

    // sel
    private lateinit var selSel: Array<Array<Sel>>
    private var selSize = 0F

    companion object {
        private const val COLS = 10
        private const val ROWS = 22
        private const val WALL = 3F
        private const val PATH = 10F
        private const val INSET = 15F
    }

    init {
        createMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init view
        ivMap = findViewById(R.id.iv_map)
        spinAwal = findViewById(R.id.spin_awal)
        spinTujuan = findViewById(R.id.spin_tujuan)
        btnSearch = findViewById(R.id.btn_search)

        // init spinner
        val lokasi = resources.getStringArray(R.array.lokasi_array)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lokasi)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinAwal.adapter = adapter
        spinTujuan.adapter = adapter

        // show map
        ivMap.post { showMap() }

        // btn search
        btnSearch.setOnClickListener {
            val awal = initSel(spinAwal.selectedItem.toString())
            val tujuan = initSel(spinTujuan.selectedItem.toString())

            if (awal.x != tujuan.x || awal.y != tujuan.y) {
                showRute(awal, tujuan)
            } else {
                Toast.makeText(this, "Lokasi sama", Toast.LENGTH_SHORT).show()
                showMap()
            }
        }
    }

    private fun showRute(awal: Sel, tujuan: Sel){
        showMap()
        refreshSel()
        drawRute(awal, tujuan)
        drawAwal(awal)
        drawTujuan(tujuan)
    }

    private fun refreshSel() {
        for (x in 0 until COLS) {
            for (y in 0 until ROWS) {
                selSel[x][y].g = 0
                selSel[x][y].h = 0
                selSel[x][y].f = 0
                selSel[x][y].parent = null
            }
        }
    }

    private fun drawRute(awal: Sel, tujuan: Sel) {
        val rute = Knn(selSel, awal, tujuan).cariRute()
        if (rute.isNotEmpty()) {
            mPaint.color = Color.GREEN
            mPaint.strokeWidth = PATH
            for (i in 0 until rute.size) {
                if (i == 0) {
                    mPaint.color = Color.BLUE
                    val current = rute[i]
                    val next = rute[i+1]
                    mCanvas.save()
                    mCanvas.translate(current.x * selSize, current.y * selSize)
                    drawDir(checkDir(next, current))
                    ivMap.invalidate()
                    mCanvas.restore()
                }
                else if (i>0 && i<rute.size-1) {
                    val previous = rute[i-1]
                    val current = rute[i]
                    val next = rute[i+1]
                    mCanvas.save()
                    mCanvas.translate(current.x * selSize, current.y * selSize)

                    // draw previous and next direction
                    // previous
                    mPaint.color = Color.CYAN
                    drawDir(checkDir(previous, current))
                    // next
                    mPaint.color = Color.GREEN
                    drawDir(checkDir(next, current))
                    ivMap.invalidate()
                    mCanvas.restore()
                }
                else {
                    val previous = rute[i-1]
                    val current = rute[i]
                    mCanvas.save()
                    mCanvas.translate(current.x*selSize, current.y*selSize)
                    mPaint.color = Color.RED
                    drawDir(checkDir(previous, current))
                    ivMap.invalidate()
                    mCanvas.restore()
                }
            }
        }
    }

    private fun drawDir(direction: Direction) {
        when(direction) {
            Direction.B -> {
                mCanvas.drawLine(0F, selSize/2, selSize/2, selSize/2, mPaint)
            }
            Direction.BL -> {
                mCanvas.drawLine(0F, 0F, selSize/2, selSize/2, mPaint)
            }
            Direction.U -> {
                mCanvas.drawLine(selSize/2, 0F, selSize/2, selSize/2, mPaint)
            }
            Direction.TL -> {
                mCanvas.drawLine(selSize, 0F, selSize/2, selSize/2, mPaint)
            }
            Direction.T -> {
                mCanvas.drawLine(selSize, selSize/2, selSize/2, selSize/2, mPaint)
            }
            Direction.TG -> {
                mCanvas.drawLine(selSize, selSize, selSize/2, selSize/2, mPaint)
            }
            Direction.S -> {
                mCanvas.drawLine(selSize/2, selSize/2, selSize/2, selSize, mPaint)
            }
            Direction.BD -> {
                mCanvas.drawLine(0F, selSize, selSize/2, selSize/2, mPaint)
            }
        }
    }

    private fun checkDir(from: Sel, next: Sel) : Direction{
        // barat
        if (from.x < next.x && from.y == next.y)
            return Direction.B
        // barat laut
        if (from.x < next.x && from.y < next.y)
            return Direction.BL
        // utara
        if (from.x == next.x && from.y < next.y)
            return Direction.U
        // timur laut
        if (from.x > next.x && from.y < next.y)
            return Direction.TL
        // timur
        if (from.x > next.x && from.y == next.y)
            return Direction.T
        // tenggara
        if (from.x > next.x && from.y > next.y)
            return Direction.TG
        // selatan
        if (from.x == next.x && from.y > next.y)
            return Direction.S
        // barat daya
        if (from.x < next.x && from.y > next.y)
            return Direction.BD

        return Direction.B
    }

    private fun drawAwal(awal:Sel){
        mCanvas.save()
        mPaint.color = Color.RED
        mCanvas.translate(awal.x * selSize, awal.y * selSize)
        mRect.set(INSET, INSET, selSize - INSET, selSize - INSET)
        mCanvas.drawRect(mRect, mPaint)
        ivMap.invalidate()
        mCanvas.restore()
    }

    private fun drawTujuan(tujuan:Sel){
        mCanvas.save()
        mPaint.color = Color.BLUE
        mCanvas.translate(tujuan.x * selSize, tujuan.y * selSize)
        mRect.set(INSET, INSET, selSize - INSET, selSize - INSET)
        mCanvas.drawRect(mRect, mPaint)
        ivMap.invalidate()
        mCanvas.restore()
    }

    private fun showMap(){
        mBitmap = Bitmap.createBitmap(ivMap.width, ivMap.height, Bitmap.Config.ARGB_8888)
        ivMap.setImageBitmap(mBitmap)
        mCanvas = Canvas(mBitmap)
        mCanvas.drawColor(Color.WHITE)
        mPaint.color = Color.BLACK
        mPaint.strokeWidth = WALL

        selSize = (ivMap.width/(COLS+5)).toFloat()
        val hMargin = (ivMap.width - (selSize * COLS)) / 2
        val vMargin = (ivMap.height - (selSize * ROWS)) / 2

        mCanvas.translate(hMargin, vMargin)

        for (x in 0 until COLS) {
            for (y in 0 until ROWS) {
                if (selSel[x][y].top)
                    mCanvas.drawLine(x*selSize, y*selSize, (x+1)*selSize, y*selSize, mPaint)
                if (selSel[x][y].left)
                    mCanvas.drawLine(x*selSize, y*selSize, x*selSize, (y+1)*selSize, mPaint)
                if (selSel[x][y].right)
                    mCanvas.drawLine((x+1)*selSize, y*selSize, (x+1)*selSize, (y+1)*selSize, mPaint)
                if (selSel[x][y].bottom)
                    mCanvas.drawLine(x*selSize, (y+1)*selSize, (x+1)*selSize, (y+1)*selSize, mPaint)
            }
        }

        ivMap.invalidate()
    }

    private fun initSel(lokasi: String) : Sel {
        var sel  = Sel()
        when(lokasi) {
            "Aula" -> { sel = selSel[4][19] }
            "Ruang 302" -> { sel = selSel[7][4]}
            "Ruang 303" -> { sel = selSel[7][8] }
            "Ruang 304" -> { sel = selSel[7][12] }
            "Ruang 305" -> { sel = selSel[7][16] }
            "Ruang 306" -> { sel = selSel[2][2] }
            "Ruang 307" -> { sel = selSel[2][6] }
            "Lobi A" -> { sel = selSel[2][9] }
            "Lobi B" -> { sel = selSel[2][12] }
            "Lobi C" -> { sel = selSel[2][16] }
            "Toilet A" -> { sel = selSel[7][0] }
            "Toilet B" -> { sel = selSel[7][1] }
        }
        return sel
    }

    private fun createMap() {
        selSel = Array(COLS) { Array(ROWS) { Sel() } }
        for (x in 0 until COLS) {
            for (y in 0 until ROWS) {
                selSel[x][y].x = x
                selSel[x][y].y = y
            }
        }

        // set the wall
        // edge of the wall
        // left wall
        for (y in 0 until ROWS) {
            val x = 0
            selSel[x][y].left = true
        }
        // top wall
        for (x in 0 until COLS) {
            val y = 0
            selSel[x][y].top = true
        }
        // right wall
        for (y in 0 until ROWS) {
            val x = COLS-1
            selSel[x][y].right = true
        }
        // bottom wall
        for (x in 0 until COLS){
            val y = ROWS-1
            selSel[x][y].bottom = true
        }

        // inner wall

        // ruang 306
        // bottom wall
        for (x in 0 until 4){
            val y = 3
            selSel[x][y].bottom = true
        }
        // right wall
        for (y in 0 until 4){
            val x = 3
            selSel[x][y].right = true
        }
        // pintu
        selSel[3][2].right = false

        // ruang 307
        // top and bottom wall
        for (x in 0 until 4) {
            val y1 = 4
            val y2 = 7
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }
        // right wall
        for (y in 4 until 8) {
            val x = 3
            selSel[x][y].right = true
        }
        // pintu
        selSel[3][6].right = false

        // lobi A
        // top and bottom wall
        for (x in 0 until 4) {
            val y1 = 8
            selSel[x][y1].top = true
            val y2 = 9
            selSel[x][y2].bottom = true
        }

        // lobi B
        // top and bottom wall
        for (x in 0 until 4) {
            val y1 = 11
            val y2 = 14
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }

        // lobi C
        // top and bottom wall
        for (x in 0 until 4) {
            val y1 = 16
            val y2 = 17
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }

        // toilet A
        // bottom wall
        for (x in 6 until 10) {
            val y = 0
            selSel[x][y].bottom = true
        }

        // toilet B
        // top and bottom wall
        for (x in 6 until 10) {
            val y = 1
            selSel[x][y].top = true
            selSel[x][y].bottom = true
        }

        // ruang 302
        // top and bottom wall
        for (x in 6 until 10) {
            val y1 = 2
            val y2 = 5
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }
        // left wall
        for (y in 2 until 6) {
            val x = 6
            selSel[x][y].left = true
        }
        // pintu
        selSel[6][4].left = false

        // ruang 303
        // top and bottom wall
        for (x in 6 until 10) {
            val y1 = 6
            val y2 = 9
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }
        // left wall
        for (y in 6 until 10){
            val x = 6
            selSel[x][y].left = true
        }
        // pintu
        selSel[6][8].left = false

        // ruang 304
        // top and bottom wall
        for (x in 6 until 10){
            val y1 = 10
            val y2 = 13
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }
        // left wall
        for (y in 10 until 14){
            val x = 6
            selSel[x][y].left = true
        }
        // pintu
        selSel[6][12].left = false

        // ruang 305
        // top and bottom wall
        for (x in 6 until 10){
            val y1 = 14
            val y2 = 17
            selSel[x][y1].top = true
            selSel[x][y2].bottom = true
        }
        // left wall
        for (y in 14 until 18) {
            val x = 6
            selSel[x][y].left = true
        }
        // pintu
        selSel[6][16].left = false

        // jalan
        // left wall
        for (y in 0 until 8) {
            val x = 4
            selSel[x][y].left = true
        }
        // pintu
        selSel[4][2].left = false
        selSel[4][6].left = false
        // tangga
        selSel[4][10].left = true
        selSel[4][15].left = true

        // right wall
        for (y in 2 until 18) {
            val x = 5
            selSel[x][y].right = true
        }
        // pintu
        selSel[5][4].right = false
        selSel[5][8].right = false
        selSel[5][12].right = false
        selSel[5][16].right = false

        // aula
        // top wall
        for (x in 0 until COLS){
            val y = 18
            selSel[x][y].top = true
        }
        // pintu
        selSel[4][18].top = false
        selSel[5][18].top = false

    }
}

enum class Direction {
    B, BL, U, TL, T, TG, S, BD
}