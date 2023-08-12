package com.example.serviceandroid

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.serviceandroid.presenter.CreateSong.CreateSongInterface
import com.example.serviceandroid.presenter.CreateSong.CreateSongPresenter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton

class CreateSongActivity : AppCompatActivity(), CreateSongInterface {

    private val metadataRetriever = MediaMetadataRetriever()
    private val REQUEST_CODE_PICK_MP3: Int = 123
    lateinit var spinnerCategory: Spinner
    lateinit var createSongPresenter: CreateSongPresenter
    var categoryList: List<String> = ArrayList<String>()
    lateinit var edtNameSingle: EditText
    lateinit var edtNameSong: EditText
    lateinit var btnChooseImage: MaterialButton
    lateinit var btnChooseMp3: ImageButton
    lateinit var titleMp3: TextView
    lateinit var imageSong: ImageView
    lateinit var uriSong: Uri
    lateinit var uriMp3: Uri
    lateinit var btnSave: MaterialButton
    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_song)
        createSongPresenter = CreateSongPresenter(this, this)
        initUI()
        createSongPresenter.getDataListCategory()

        btnChooseImage.setOnClickListener {
            selectImage();
        }

        btnChooseMp3.setOnClickListener {
            selectFileMp3()
        }

        btnSave.setOnClickListener {
            saveSong();
        }
    }

    private fun saveSong() {
        showAlertDialog()
        createSongPresenter.saveSong(uriMp3, uriSong, edtNameSong.text.toString(), edtNameSingle.text.toString(), spinnerCategory.selectedItem.toString())
    }

    private fun selectFileMp3() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("audio/*")
        startActivityForResult(intent, REQUEST_CODE_PICK_MP3)
    }

    private fun selectImage() {
        ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_MP3 && resultCode == Activity.RESULT_OK){
            uriMp3 = data!!.data!!
            metadataRetriever.setDataSource(this, uriMp3)
            titleMp3.setText(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "." + metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE))
            val duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        }else if (resultCode == Activity.RESULT_OK) {
            uriSong = data?.data!!
            imageSong.setImageURI(uriSong)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initUI() {
        spinnerCategory = findViewById(R.id.spinnerCategory)
        edtNameSong = findViewById(R.id.edtNameSong)
        edtNameSingle = findViewById(R.id.edtNameSingle)
        btnChooseImage = findViewById(R.id.btnChooseImage)
        btnChooseMp3 = findViewById(R.id.btnChooseMp3)
        titleMp3 = findViewById(R.id.titleFile)
        imageSong = findViewById(R.id.imageSong)
        btnSave = findViewById(R.id.btnSaveSong)
    }

    override fun getDataListCategorySuccess(list: List<String>) {
        categoryList = list
        val spinnerAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_dropdown_item,
            categoryList
        )
        spinnerCategory.adapter = spinnerAdapter
    }

    public fun showAlertDialog(){
        val builder = AlertDialog.Builder(this)
//        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val inflate = LayoutInflater.from(this).inflate(R.layout.layout_loading, null);
        builder.setView(inflate)
        dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun saveSongSuccess() {
        dialog.dismiss()
        Toast.makeText(this@CreateSongActivity, "Thành Công", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun saveSongError() {
        TODO("Not yet implemented")
        dialog.dismiss()
        Toast.makeText(this@CreateSongActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
    }
}