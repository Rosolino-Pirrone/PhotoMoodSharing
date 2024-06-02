package com.example.photomoodsharing

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.photomoodsharing.ui.theme.PhotoMoodSharingTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {loadInterstitial(this)
            PhotoMoodSharingTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    

                    Greeting()
                }
            }
        }
    }
    override fun onDestroy() {
        removeInterstitial()
        super.onDestroy()
    }
}



var imageList_2 : List<Pair<Int, FloatOffset>> = mutableListOf()
var textList : List<Pair<String, FloatOffset>> = mutableListOf()

var coloreSfondo = Color.Black
var coloreTestoList : List<Color> = mutableListOf()
//var imageList2 : MutableMap<Int, FloatOffset> = mutableMapOf()
//var immaginiList : MutableList<Int> = mutableListOf()
//var imageList2 by remember { mutableStateOf(mutableMapOf<Int, FloatOffset>()) }

/*
val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
    // Callback is invoked after the user selects a media item or closes the
    // photo picker.
    if (uri != null) {
        Log.d("PhotoPicker", "Selected URI: $uri")
    } else {
        Log.d("PhotoPicker", "No media selected")
    }
}
public fun scegliFoto(){pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
}
*/


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalFoundationApi::class
)




@Composable
fun Greeting() {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableStateOf(1f) }
    var angle by remember { mutableStateOf(0f) }

    val sheetState = rememberModalBottomSheetState()
    //val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()

    // Remember a launcher for activity result
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use {
                imageBitmap = it.readBytes().toBitmap().asImageBitmap()
            }
        }
    }
    

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val graphicsLayer = rememberGraphicsLayer()
    var contenuto = "griglia"
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val writeStorageAccessState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No permissions are needed on Android 10+ to add files in the shared storage
            emptyList()
        } else {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    )

    // This logic should live in your ViewModel - trigger a side effect to invoke URI sharing.
    // checks permissions granted, and then saves the bitmap from a Picture that is already capturing content
    // and shares it with the default share sheet.
    fun shareBitmapFromComposable() {
        if (writeStorageAccessState.allPermissionsGranted) {
            coroutineScope.launch {
                val bitmap = graphicsLayer.toImageBitmap()
                val uri = bitmap.asAndroidBitmap().saveToDisk(context)
                shareBitmap(context, uri)
            }
        } else if (writeStorageAccessState.shouldShowRationale) {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "The storage permission is needed to save the image",
                    actionLabel = "Grant Access"
                )

                if (result == SnackbarResult.ActionPerformed) {
                    writeStorageAccessState.launchMultiplePermissionRequest()
                }
            }
        } else {
            writeStorageAccessState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    //Text("Top app bar")
                }
            )
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .background(Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Image(
                        modifier = Modifier
                            .size(75.dp),
                        painter = painterResource(id = R.drawable.mood_s),
                        contentDescription = null
                    )

                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                /*showInterstitial(context) {
                                    //On Ad Dismiss Lambda Function
                                    //e.g. you can show dialog to the user or redirect them etc.
                                }*/
                                contenuto = "griglia"
                                showBottomSheet = true
                            },
                        painter = painterResource(id = R.drawable.e4),
                        contentDescription = null
                    )

                    Image(modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                            offset = Offset.Zero
                            zoom = 1f
                            angle = 0f

                        },
                        painter = painterResource(id = R.drawable.photos),
                        contentDescription = null
                    )


                }
                AdmobBanner()
            }

        },

        bottomBar =  {
            BottomAppBar (modifier = Modifier
                .background(color = Color.Transparent),
                containerColor = Color.Transparent,

                actions = {
                  /*  IconButton(modifier = Modifier,
                        onClick = { showBottomSheet = true}) {
                        Icon(Icons.Filled.Add,
                            contentDescription = "description",
                        )
                    }
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "description",
                        )
                    }

*/
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .background(Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {



                        Image(modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                contenuto = "opzioni"
                                showBottomSheet = true
                            },
                            painter = painterResource(id = R.drawable.colors),
                            contentDescription = null
                        )

                        Image(modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                contenuto = "opzioniTesto"
                                showBottomSheet = true
                            },
                            painter = painterResource(id = R.drawable.f51),
                            contentDescription = null
                        )


                    }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showInterstitial(context) {
                    //On Ad Dismiss Lambda Function
                    //e.g. you can show dialog to the user or redirect them etc.
                }
                shareBitmapFromComposable()
            }) {
                Icon(Icons.Default.Share, "share")
            }
        }

        /*
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add Emoji") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    showBottomSheet = true
                }
            )
        }
        */
    ) {
        // Screen content

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                // Sheet content
                if (contenuto == "griglia") {
                    ImageGrid()
                }
                else if (contenuto == "opzioniTesto") {
                    val colors = listOf(
                        Color.Red, Color.Green, Color.Blue, Color.Yellow,
                        Color.Cyan, Color.Magenta, Color.Black, Color.Gray, Color.White
                    )

                    var selectedColor by remember { mutableStateOf(Color.White) }
                    var testo = ""
                    var text by rememberSaveable { mutableStateOf("") }
                    Column(
                        modifier = Modifier
                            //.fillMaxSize()
                            .background(coloreSfondo)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Aggiungi un testo e scegli il colore: \n $text",
                            style = TextStyle(
                                fontSize = 28.sp,
                                shadow = Shadow(
                                    blurRadius = 5f
                                )
                            ),
                            color = selectedColor,
                            fontSize = 30.sp,
                            modifier = Modifier.padding(8.dp)
                        )

                        Row (modifier = Modifier
                            .fillMaxWidth()
                            .background(coloreSfondo)
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically

                        ){

                            TextField(modifier = Modifier
                                .width(250.dp)
                                ,
                                value = text,
                                onValueChange = { text = it },
                                label = { Text("Scrivi il testo") },
                                singleLine = false
                            )

                            Button(onClick = {  val xOffset =
                                Random.nextFloat() * 200 // Valore casuale per l'offset X
                                val yOffset =
                                    Random.nextFloat() * 400 // Valore casuale per l'offset Y
                                textList =
                                    textList + listOf(Pair(text, FloatOffset(xOffset, yOffset)))
                                coloreTestoList = coloreTestoList + listOf(selectedColor )
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                            }) {
                                Text("Add text")
                            }

                        }

                        ColorPalette(
                            colors = colors,
                            onColorSelected = { selectedColor = it
                                //coloreSfondo = it

                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        AdmobBanner()
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(coloreSfondo, RoundedCornerShape(8.dp))
                        )
                    }

                }

                else{
                    ColorPickerDemo()
                }
/*
                Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            }) {
                Text("Hide bottom sheet")
            }
*/

            }
        }



/*
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
            //.padding(padding)
                .offset(x = 0.dp, y = 75.dp)
            ,
            verticalArrangement = Arrangement.Top

        ) {
*/





                Row(
                    modifier = Modifier
                        //.fillMaxSize()
                        //.fillMaxWidth()
                        //.height(600.dp)
                        .background(Color.Black)
                        //.size(300.dp)
                        //.offset(x = 0.dp, y = 50.dp)
                        ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top

                ) {
                    Box(
                        modifier = Modifier

                            .fillMaxSize()
                            //.fillMaxWidth()
                            //.height(600.dp)
                            .background(coloreSfondo)
                            //.wrapContentHeight(Alignment.Top)
                            //.wrapContentWidth(Alignment.CenterHorizontally)
                            .drawWithCache {
                                onDrawWithContent {
                                    graphicsLayer.record {
                                        this@onDrawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                }
                            }

                    ) {
                        //var offsetX by remember { mutableStateOf(0f) }
                        //var offsetY by remember { mutableStateOf(0f) }
                        fun Offset.rotateBy(angle: Float): Offset {
                            val angleInRadians = angle * PI / 180
                            return Offset(
                                (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
                                (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat()
                            )
                        }

                        var visualizza: Boolean by rememberSaveable { mutableStateOf(true) }
                        var visualizza2: Boolean by rememberSaveable { mutableStateOf(true) }
                        val haptics = LocalHapticFeedback.current

                        imageBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(coloreSfondo)
                                    .pointerInput(Unit) {

                                        detectTransformGestures(
                                            onGesture = { centroid, pan, gestureZoom, gestureRotate ->
                                                val oldScale = zoom
                                                val newScale = zoom * gestureZoom

                                                // For natural zooming and rotating, the centroid of the gesture should
                                                // be the fixed point where zooming and rotating occurs.
                                                // We compute where the centroid was (in the pre-transformed coordinate
                                                // space), and then compute where it will be after this delta.
                                                // We then compute what the new offset should be to keep the centroid
                                                // visually stationary for rotating and zooming, and also apply the pan.
                                                offset = (offset + centroid / oldScale).rotateBy(
                                                    gestureRotate
                                                ) -
                                                        (centroid / newScale + pan / oldScale)
                                                zoom = newScale
                                                angle += gestureRotate
                                            }
                                        )
                                    }


                                    .graphicsLayer {

                                            translationX = -offset.x * zoom
                                            translationY = -offset.y * zoom
                                            scaleX = zoom
                                            scaleY = zoom
                                            rotationZ = angle
                                            transformOrigin = TransformOrigin(0f, 0f)

                                    }
                                   /* .combinedClickable(
                                        onClick = { },
                                        onLongClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            visualizza2 = !visualizza2
                                        },
                                        //onLongClickLabel = stringResource(R.string.open_context_menu)
                                    )
                                    .graphicsLayer(alpha = if (visualizza2) 1f else 0.5f)*/
                                    //.height(350.dp)

                            )
                        }

                        //var imageList_3 by rememberSaveable { mutableStateOf(listOf<Pair<Int, FloatOffset>>()) }
                        //val density = LocalDensity.current
                        //val image = ImageBitmap.imageResource(id = R.drawable.a)
                        //val xOffset = Random.nextFloat() * 200 // Valore casuale per l'offset X
                        //val yOffset = Random.nextFloat() * 400 // Valore casuale per l'offset Y
                        //val image2 =  R.drawable.a
                        //imageList_2 = imageList_2 + listOf(Pair(image_2, FloatOffset(xOffset, yOffset)))


                        AddEmoticon(imageList_3 = imageList_2)
AddText(textList2 = textList, colors = coloreTestoList)

                    }
                }


       // }

    }
    //var imageList_2 by rememberSaveable { mutableStateOf(listOf<Pair<Int, FloatOffset>>()) }
}
fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, size)


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PhotoMoodSharingTheme {
        ColorPickerDemo()
    }
}

@Composable
fun Testo() {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Cyan, Color.Magenta, Color.Black, Color.Gray, Color.White
    )

    var selectedColor by remember { mutableStateOf(Color.White) }
var testo = ""
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            //.fillMaxSize()
            .background(coloreSfondo)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Aggiungi un testo e scegli il colore: \n $text",
            style = TextStyle(
                fontSize = 28.sp,
                shadow = Shadow(
                      blurRadius = 5f
                )
            ),
            color = selectedColor,
            fontSize = 30.sp,
            modifier = Modifier.padding(8.dp)
        )

Row (modifier = Modifier
    .fillMaxWidth()
    .background(coloreSfondo)
        ,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically

){

    TextField(modifier = Modifier
        .width(250.dp)
        ,
        value = text,
        onValueChange = { text = it },
        label = { Text("Scrivi il testo") },
        singleLine = false
    )

    Button(onClick = {  val xOffset =
        Random.nextFloat() * 200 // Valore casuale per l'offset X
        val yOffset =
            Random.nextFloat() * 400 // Valore casuale per l'offset Y
        textList =
            textList + listOf(Pair(text, FloatOffset(xOffset, yOffset)))
        coloreTestoList = coloreTestoList + listOf(selectedColor )
    }) {
        Text("Add text")
    }

}

        ColorPalette(
            colors = colors,
            onColorSelected = { selectedColor = it
                //coloreSfondo = it

            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        AdmobBanner()
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(coloreSfondo, RoundedCornerShape(8.dp))
        )
    }
}



@Composable
fun ColorPickerDemo() {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Cyan, Color.Magenta, Color.Black, Color.Gray, Color.White
    )

    var selectedColor by remember { mutableStateOf(Color.White) }

    Column(
        modifier = Modifier
            //.fillMaxSize()
            .background(selectedColor)
            .padding(16.dp)
    ) {
        Text(
            text = "Select a background color:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )

        ColorPalette(
            colors = colors,
            onColorSelected = { selectedColor = it
                coloreSfondo = it}
        )

        Spacer(modifier = Modifier.height(16.dp))
        AdmobBanner()
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(selectedColor, RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun ColorPalette(colors: List<Color>, onColorSelected: (Color) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { color ->
            ColorBox(color = color) {
                onColorSelected(color)
            }
        }
    }
}


@Composable
fun ColorBox(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, RoundedCornerShape(4.dp))
            .clickable { onClick() }
    )
}

@Composable
fun AdmobBanner() {
    AndroidView(
        modifier = Modifier.fillMaxWidth()
        //.offset(    x = 0.dp, y = 50.dp)
                ,
        factory = { context ->
            // on below line specifying ad view.
            AdView(context).apply {
                // on below line specifying ad size
                //adSize = AdSize.BANNER
                // on below line specifying ad unit id
                // currently added a test ad unit id.
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/9214589741"
                // calling load ad to load our ad.
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

var mInterstitialAd: InterstitialAd? = null

fun loadInterstitial(context: Context) {
    InterstitialAd.load(
        context,
        "ca-app-pub-7616959571804190/2004283909", //Change this with your own AdUnitID!
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        }
    )
}
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
fun showInterstitial(context: Context, onAdDismissed: () -> Unit) {
    val activity = context.findActivity()

    if (mInterstitialAd != null && activity != null) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                mInterstitialAd = null
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null

                loadInterstitial(context)
                onAdDismissed()
            }
        }
        mInterstitialAd?.show(activity)
    }
}

fun removeInterstitial() {
    mInterstitialAd?.fullScreenContentCallback = null
    mInterstitialAd = null
}

val imagesEmoji = listOf<Int>(
    R.drawable.e0,
    R.drawable.e1, R.drawable.e2,
    R.drawable.e3, R.drawable.e4, R.drawable.e5, R.drawable.e6, R.drawable.e7, R.drawable.e8, R.drawable.e9, R.drawable.e10, R.drawable.e11,
    R.drawable.e12, R.drawable.e13, R.drawable.e14, R.drawable.e15, R.drawable.e16, R.drawable.e17, R.drawable.e18, R.drawable.e19, R.drawable.e20,
    R.drawable.e21, R.drawable.e22, R.drawable.e23, R.drawable.e24, R.drawable.e25, R.drawable.e26, R.drawable.e27, R.drawable.e28, R.drawable.e29,
    R.drawable.e30, R.drawable.e31, R.drawable.e32, R.drawable.e33, R.drawable.e34, R.drawable.e35, R.drawable.e36, R.drawable.e37, R.drawable.e38,
    R.drawable.e39, R.drawable.e40, R.drawable.e41, R.drawable.e42, R.drawable.e43, R.drawable.e44, R.drawable.e45, R.drawable.e46, R.drawable.e47,
    R.drawable.e48, R.drawable.e49, R.drawable.e50, R.drawable.e51, R.drawable.e52, R.drawable.e53, R.drawable.e54, R.drawable.e55, R.drawable.e56,
    R.drawable.e57, R.drawable.e58, R.drawable.e59, R.drawable.e60, R.drawable.e61, R.drawable.e62, R.drawable.e63, R.drawable.e64, R.drawable.e65,
    R.drawable.e66, R.drawable.e67, R.drawable.e68, R.drawable.e69, R.drawable.e70, R.drawable.e71, R.drawable.e72, R.drawable.e73, R.drawable.e74,
    R.drawable.e75,
    )

val imagesLove = listOf<Int>(
    R.drawable.la, R.drawable.lb, R.drawable.lc, R.drawable.ld,
    R.drawable.le, R.drawable.lf, R.drawable.lg, R.drawable.lh, R.drawable.li, R.drawable.lm, R.drawable.ln, R.drawable.lo, R.drawable.lp,
    R.drawable.lq, R.drawable.lr, R.drawable.ls, R.drawable.lt, R.drawable.lu, R.drawable.lv, R.drawable.lz, R.drawable.laa, R.drawable.lbb,
    R.drawable.lcc, R.drawable.ldd, R.drawable.lee, R.drawable.lff, R.drawable.lgg, R.drawable.lhh, R.drawable.lii, R.drawable.lmm, R.drawable.lnn,
    R.drawable.loo, R.drawable.lpp, R.drawable.lqq, R.drawable.lrr, R.drawable.lss, R.drawable.ltt, R.drawable.luu, R.drawable.lvv,
)

val imagesMeteo = listOf<Int>(
    R.drawable.ma, R.drawable.mb, R.drawable.mc, R.drawable.md, R.drawable.me, R.drawable.mf, R.drawable.mg, R.drawable.mh, R.drawable.mi,
    R.drawable.ml, R.drawable.mn, R.drawable.mo, R.drawable.mp, R.drawable.mq, R.drawable.mr, R.drawable.ms, R.drawable.mt, R.drawable.mu,
    R.drawable.mv, R.drawable.mz, R.drawable.maa, R.drawable.mbb, R.drawable.mcc, R.drawable.mdd, R.drawable.mee, R.drawable.mff, R.drawable.mgg, R.drawable.mhh, R.drawable.mii, R.drawable.mll,
    R.drawable.mnn, R.drawable.moo, R.drawable.mpp, R.drawable.mqq, R.drawable.mrr, R.drawable.mss, R.drawable.mtt, R.drawable.muu,
    R.drawable.mvv, R.drawable.mzz, R.drawable.maaa, R.drawable.mbbb, R.drawable.mccc, R.drawable.mddd, R.drawable.meee,
    R.drawable.mfff, R.drawable.mggg, R.drawable.mhhh, R.drawable.miii, R.drawable.mlll, R.drawable.mnnn, R.drawable.mooo,
    R.drawable.mppp, R.drawable.mqqq, R.drawable.mrrr, R.drawable.msss, R.drawable.mttt, R.drawable.muuu, R.drawable.mvvv,

    )

val imagesFunef = listOf<Int>(
    R.drawable.f0, R.drawable.f1, R.drawable.f2, R.drawable.f3, R.drawable.f4, R.drawable.f5, R.drawable.f6, R.drawable.f7, R.drawable.f8,
    R.drawable.f9, R.drawable.f10, R.drawable.f11, R.drawable.f12, R.drawable.f13, R.drawable.f14, R.drawable.f15, R.drawable.f16, R.drawable.f17,
    R.drawable.f18, R.drawable.f19, R.drawable.f20, R.drawable.f21, R.drawable.f22, R.drawable.f23, R.drawable.f24, R.drawable.f25, R.drawable.f26,
    R.drawable.f27, R.drawable.f28, R.drawable.f29, R.drawable.f30, R.drawable.f31, R.drawable.f32, R.drawable.f33, R.drawable.f34, R.drawable.f35,
    R.drawable.f36, R.drawable.f37, R.drawable.f38, R.drawable.f39, R.drawable.f40, R.drawable.f41, R.drawable.f42, R.drawable.f43, R.drawable.f44,
    R.drawable.f45, R.drawable.f46, R.drawable.f47, R.drawable.f48, R.drawable.f49, R.drawable.f50, R.drawable.f51, R.drawable.f52, R.drawable.f53,
    R.drawable.f54, R.drawable.f55, R.drawable.f56, R.drawable.f57, R.drawable.f58, R.drawable.f59, R.drawable.f60, R.drawable.f61, R.drawable.f62,
    R.drawable.f63, R.drawable.f64, R.drawable.f65, R.drawable.f66, R.drawable.f67, R.drawable.f68, R.drawable.f69, R.drawable.f70, R.drawable.f71,
    R.drawable.f72, R.drawable.f73, R.drawable.f74, R.drawable.f75, R.drawable.f76, R.drawable.f77, R.drawable.f78, R.drawable.f79, R.drawable.f80,
    R.drawable.f81, R.drawable.f82, R.drawable.f83, R.drawable.f84, R.drawable.f85, R.drawable.f86, R.drawable.f87, R.drawable.f88, R.drawable.f89,
    R.drawable.f90, R.drawable.f91, R.drawable.f92, R.drawable.f93, R.drawable.f94, R.drawable.f95, R.drawable.f96, R.drawable.f97, R.drawable.f98, R.drawable.f99,
    R.drawable.f100, R.drawable.f101, R.drawable.f102, R.drawable.f103, R.drawable.f104, R.drawable.f105, R.drawable.f106, R.drawable.f107, R.drawable.f108,
    R.drawable.f109, R.drawable.f110, R.drawable.f111, R.drawable.f112, R.drawable.f113, R.drawable.f114, R.drawable.f115, R.drawable.f116, R.drawable.f117,
    R.drawable.f118, R.drawable.f119, R.drawable.f120, R.drawable.f121, R.drawable.f122, R.drawable.f123, R.drawable.f124, R.drawable.f125, R.drawable.f126,
    R.drawable.f127, R.drawable.f128, R.drawable.f129, R.drawable.f130, R.drawable.f131, R.drawable.f132, R.drawable.f133, R.drawable.f134, R.drawable.f135,
    R.drawable.f136, R.drawable.f137, R.drawable.f138, R.drawable.f139, R.drawable.f140, R.drawable.f141, R.drawable.f142, R.drawable.f143, R.drawable.f144,
    R.drawable.f145, R.drawable.f146, R.drawable.f147, R.drawable.f148, R.drawable.f149, R.drawable.f150, R.drawable.f151, R.drawable.f152, R.drawable.f153,
    R.drawable.f154, R.drawable.f155, R.drawable.f156, R.drawable.f157, R.drawable.f158, R.drawable.f159, R.drawable.f160, R.drawable.f161, R.drawable.f162,
    R.drawable.f163, R.drawable.f164, R.drawable.f165, R.drawable.f166, R.drawable.f167, R.drawable.f168, R.drawable.f169, R.drawable.f170, R.drawable.f171,
    R.drawable.f172, R.drawable.f173, R.drawable.f174, R.drawable.f175, R.drawable.f176, R.drawable.f177, R.drawable.f178, R.drawable.f179, R.drawable.f180,
    R.drawable.f181, R.drawable.f182, R.drawable.f183, R.drawable.f184, R.drawable.f185, R.drawable.f186, R.drawable.f187, R.drawable.f188, R.drawable.f189,
    R.drawable.f190, R.drawable.f191, R.drawable.f192, R.drawable.f193, R.drawable.f194, R.drawable.f195, R.drawable.f196, R.drawable.f197, R.drawable.f198,
    R.drawable.f199, R.drawable.f200, R.drawable.f201, R.drawable.f202, R.drawable.f203, R.drawable.f204, R.drawable.f205, R.drawable.f206, R.drawable.f207,
    R.drawable.f208, R.drawable.f209, R.drawable.f210, R.drawable.f211, R.drawable.f212, R.drawable.f213, R.drawable.f214, R.drawable.f215, R.drawable.f216,
    R.drawable.f217, R.drawable.f218, R.drawable.f219, R.drawable.f220, R.drawable.f221, R.drawable.f222, R.drawable.f223, R.drawable.f224, R.drawable.f225,
    R.drawable.f226, R.drawable.f227, R.drawable.f228, R.drawable.f229, R.drawable.f230, R.drawable.f231, R.drawable.f232, R.drawable.f233, R.drawable.f234,
    R.drawable.f235, R.drawable.f236, R.drawable.f237, R.drawable.f238, R.drawable.f239,

)

data class FloatOffset(val x: Float, val y: Float)

@JvmOverloads
@Composable
fun ImageGrid( modifier: Modifier = Modifier) {
    //var imageList by remember { mutableStateOf(listOf<Pair<ImageBitmap, FloatOffset>>()) }
    //var immagine_0 = R.drawable.a
    //var immagine = ImageBitmap.imageResource(id = immagine_0)
    //var list  = imagesLove
    var list by rememberSaveable{ mutableStateOf(imagesEmoji) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        verticalArrangement = Arrangement.Top,

        ) {

        //val density = LocalDensity.current
        //val image = ImageBitmap.imageResource(id = R.drawable.a)
        //val image2 =  R.drawable.a


        Row (modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){


            Image(modifier = Modifier
                .size(50.dp)
                .clickable { list = imagesEmoji }
                ,
                painter = painterResource(id = R.drawable.e4),
                contentDescription = null
            )

            Image(modifier = Modifier
                .size(50.dp)
                .clickable { list = imagesLove }
                ,
                painter = painterResource(id = R.drawable.lg),
                contentDescription = null
            )

            Image(modifier = Modifier
                .size(60.dp)
                .clickable { list = imagesMeteo }
                ,
                painter = painterResource(id = R.drawable.me),
                contentDescription = null
            )

            Image(modifier = Modifier
                .size(50.dp)
                .clickable { list = imagesFunef }
                ,
                painter = painterResource(id = R.drawable.f1),
                contentDescription = null
            )


        }
        AdmobBanner()
        //AddEmoticon(imageList_3 = imageList_2)

        LazyVerticalGrid(modifier = modifier
            .background(Color.Magenta),
            verticalArrangement = Arrangement.Top,
            columns = GridCells.Adaptive(minSize = 128.dp)

        )  {


            items(list, key = { it }) { image ->

                Column(
                    modifier = modifier
                    //.background(Color.Magenta)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(

                        painter = painterResource(image),
                        contentDescription = null,

                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clickable {
                                //immagine_0 = image
                                val xOffset =
                                    Random.nextFloat() * 200 // Valore casuale per l'offset X
                                val yOffset =
                                    Random.nextFloat() * 400 // Valore casuale per l'offset Y
                                imageList_2 =
                                    imageList_2 + listOf(Pair(image, FloatOffset(xOffset, yOffset)))

                                //println("This list has ${imageList2.count()} items")
                                //.clip(CircleShape)

                            }
                    )
                }
            }

        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddEmoticon(imageList_3 : List<Pair<Int, FloatOffset>> = mutableListOf()){
    val density = LocalDensity.current

    imageList_3.forEach { (image_2, offset) ->

        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        var visualizza: Boolean by rememberSaveable { mutableStateOf(true) }
        val haptics = LocalHapticFeedback.current

        Image(
            modifier = Modifier
                .offset(
                    x = with(density) { offset.x.dp },
                    y = with(density) { offset.y.dp }
                )
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                //.background(Color.Blue)
                .size(100.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                    }
                }
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        visualizza = !visualizza
                    },
                    //onLongClickLabel = stringResource(R.string.open_context_menu)
                )
                .graphicsLayer(alpha = if (visualizza) 1f else 0f)
            ,
            painter = painterResource(id = image_2),
            contentDescription = null,

            )

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddText(textList2: List<Pair<String, FloatOffset>> = mutableListOf(), colors: List<Color>){
    val density = LocalDensity.current
    var index = 0
    textList2.forEach { (testo, offset) ->

        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }
        var visualizza: Boolean by rememberSaveable { mutableStateOf(true) }
        val haptics = LocalHapticFeedback.current

        Text(
            text = testo,
            //style = MaterialTheme.typography.bodyLarge,
            style = TextStyle(
                fontSize =30.sp,
                shadow = Shadow(
                    color = colors[index],  blurRadius = 5f
                )
            )
            //color = colors[index]
                    ,
            modifier = Modifier
                .padding(8.dp)
                .width(300.dp)
                .offset(x = 100.dp, y = 150.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                    }
                }
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        visualizza = !visualizza
                    },
                    //onLongClickLabel = stringResource(R.string.open_context_menu)
                )
                .graphicsLayer(alpha = if (visualizza) 1f else 0f)
        )

        index += 1
    }
}




private suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.png"
    )

    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

/**
 * We call [MediaScannerConnection] to index the newly created image inside MediaStore to be visible
 * for other apps, as well as returning its [MediaStore] Uri
 */
private suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

private fun shareBitmap(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ContextCompat.startActivity(context, Intent.createChooser(intent, "Share your image"), null)
}

