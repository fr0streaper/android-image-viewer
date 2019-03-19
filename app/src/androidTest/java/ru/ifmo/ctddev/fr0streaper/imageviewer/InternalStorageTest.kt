package ru.ifmo.ctddev.fr0streaper.imageviewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.support.test.runner.intent.IntentStubberRegistry
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.io.FileInputStream

@RunWith(AndroidJUnit4::class)
class InternalStorageTest {

    private lateinit var context: Context
    private lateinit var testPath: String

    @Before
    fun init() {
        context = InstrumentationRegistry.getTargetContext()
        testPath = context.filesDir.parent + "/test"

        val dir = File(testPath)
        dir.mkdir()
        File("${testPath}MEZAMETAMAE WAGA ARUJI TACHI YO.mp3").delete()
    }

    @After
    fun revert() {
        val dir = File(testPath)
        dir.delete()
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("ru.ifmo.ctddev.fr0streaper.imageviewer", appContext.packageName)
    }

    @Test
    fun fromInternalStorageInvalidPath() {
        val path = "${testPath}MEZAMETAMAE WAGA ARUJI TACHI YO.mp3"
        assertFalse(InternalStorageUtilities.fromInternalStorage(path, "AYAYAYAYAYA")!!.exists())
    }

    @Test
    fun fromInternalStorageNullPath() {
        val path = null
        assertNull(InternalStorageUtilities.fromInternalStorage(path, "OH_MY_GOOOD"))
    }

    @Test
    fun fromInternalStorageEmptyPath() {
        val path = ""
        assertFalse(InternalStorageUtilities.fromInternalStorage(path, "YOU EXPECTED AN IMAGE")!!.exists())
    }

    @Test
    fun fromInternalStorageDirectoryPath() {
        val path = testPath
        assertFalse(InternalStorageUtilities.fromInternalStorage(path, "BUT IT WAS ME, DIO")!!.exists())
    }

    @Test
    fun saveToInternalStorageExists() {
        val sample = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565)
        InternalStorageUtilities.saveToInternalStorage("$testPath/SPEEDWAGON.jpg", sample)

        val file = File("$testPath/SPEEDWAGON.jpg")
        assertTrue(file.exists())
    }

    @Test
    fun saveToInternalStorageRetrieve() {
        val sample = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565)
        InternalStorageUtilities.saveToInternalStorage("$testPath/SPEEDWAGON.jpg", sample)

        val inputStream = FileInputStream(InternalStorageUtilities.fromInternalStorage(testPath, "SPEEDWAGON"))
        val retrievedSample = BitmapFactory.decodeStream(inputStream)

        for (i in 0 until sample.width) {
            for (j in 0 until sample.height) {
                assertEquals(sample.getPixel(i, j), retrievedSample.getPixel(i, j))
            }
        }
    }

}
