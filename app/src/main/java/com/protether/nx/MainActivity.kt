package com.protether.nx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.protether.nx.ptp.CameraExifItem
import com.protether.nx.ptp.PtpUsbController
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private lateinit var ptpController: PtpUsbController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ptpController = PtpUsbController(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                ProTetherAppScreen(ptpController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ptpController.close()
    }
}

@Composable
fun ProTetherAppScreen(ptpController: PtpUsbController) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableIntStateOf(0) }
    var autoTetherEnabled by remember { mutableStateOf(true) }
    var statusLog by remember { mutableStateOf("Siap • Menunggu Kamera USB OTG / PTP-IP") }

    // Parameter Exposure Manual
    val shutterValues = listOf("1/60", "1/125", "1/200", "1/400", "1/800", "1/1600")
    val apertureValues = listOf("f/1.8", "f/2.8", "f/4.0", "f/5.6", "f/8.0")
    val isoValues = listOf("100", "200", "400", "800", "1600", "3200")
    val wbValues = listOf("3200K", "4500K", "5200K", "5600K", "6500K")

    var shutterIdx by remember { mutableIntStateOf(2) }
    var apertureIdx by remember { mutableIntStateOf(1) }
    var isoIdx by remember { mutableIntStateOf(2) }
    var wbIdx by remember { mutableIntStateOf(2) }

    // Koordinat Fokus Sentuh (Touch AF)
    var afOffset by remember { mutableStateOf(Offset(420f, 360f)) }

    // Daftar File Galeri SD Card & Auto-Tether
    var mediaItems by remember {
        mutableStateOf(
            listOf(
                CameraExifItem(101, "DSC_4821.NEF", true, "1/200s", "f/2.8", "ISO 400", "50mm", 29.4),
                CameraExifItem(102, "DSC_4822.JPG", false, "1/200s", "f/2.8", "ISO 400", "50mm", 9.1),
                CameraExifItem(103, "DSC_4823.NEF", true, "1/160s", "f/4.0", "ISO 200", "35mm", 30.2),
                CameraExifItem(104, "DSC_4824.NEF", true, "1/250s", "f/2.8", "ISO 400", "70mm", 28.9)
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E14))
            .padding(14.dp)
    ) {
        // Baris Status Kamera & Toggle Auto-Tether
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ProTether NX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(statusLog, color = Color(0xFF34D399), fontSize = 11.sp)
            }
            FilterChip(
                selected = autoTetherEnabled,
                onClick = { autoTetherEnabled = !autoTetherEnabled },
                label = { Text(if (autoTetherEnabled) "Auto-Tether: ON" else "Auto-Tether: OFF") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Konten Utama berdasarkan Tab
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> {
                    // TAB 1: LIVE REMOTE & TOUCH AF + EXPOSURE MANUAL
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF161B26))
                                .border(1.dp, Color(0xFF263042), RoundedCornerShape(16.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        afOffset = tapOffset
                                        scope.launch {
                                            ptpController.setTouchAfCoordinate(
                                                tapOffset.x / size.width.toFloat(),
                                                tapOffset.y / size.height.toFloat()
                                            )
                                            statusLog = "Touch AF Terkunci: (${tapOffset.x.roundToInt()}, ${tapOffset.y.roundToInt()})"
                                        }
                                    }
                                }
                        ) {
                            Text(
                                "LIVE VIEW STREAM (PTP MJPEG)
Ketuk layar untuk memindahkan titik fokus (Touch AF)",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            // Kotak Indikator Fokus Sentuh
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(afOffset.x.roundToInt() - 36, afOffset.y.roundToInt() - 36) }
                                    .size(72.dp)
                                    .border(2.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Panel Kontrol Exposure Manual
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExposureButton("SHUTTER", shutterValues[shutterIdx], Modifier.weight(1f)) {
                                shutterIdx = (shutterIdx + 1) % shutterValues.size
                            }
                            ExposureButton("APERTURE", apertureValues[apertureIdx], Modifier.weight(1f)) {
                                apertureIdx = (apertureIdx + 1) % apertureValues.size
                            }
                            ExposureButton("ISO", isoValues[isoIdx], Modifier.weight(1f)) {
                                isoIdx = (isoIdx + 1) % isoValues.size
                            }
                            ExposureButton("WB", wbValues[wbIdx], Modifier.weight(1f)) {
                                wbIdx = (wbIdx + 1) % wbValues.size
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tombol Shutter Jarak Jauh
                        Button(
                            onClick = {
                                scope.launch {
                                    ptpController.triggerRemoteShutter()
                                    val nextNum = 4821 + mediaItems.size
                                    val newItem = CameraExifItem(
                                        objectHandle = 100 + mediaItems.size + 1,
                                        fileName = "DSC_${nextNum}.NEF",
                                        isRawNef = true,
                                        shutterSpeed = shutterValues[shutterIdx],
                                        aperture = apertureValues[apertureIdx],
                                        iso = "ISO ${isoValues[isoIdx]}",
                                        focalLength = "50mm",
                                        fileSizeMb = 29.8
                                    )
                                    mediaItems = listOf(newItem) + mediaItems
                                    statusLog = if (autoTetherEnabled) {
                                        "Auto-Tether: ${newItem.fileName} langsung tampil di HP"
                                    } else {
                                        "Jepret sukses: ${newItem.fileName} tersimpan di SD Card"
                                    }
                                    if (autoTetherEnabled) activeTab = 1
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("JEPRET KAMERA (REMOTE SHUTTER)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                1 -> {
                    // TAB 2: AUTO-TRANSFER TETHERING PREVIEW
                    val latest = mediaItems.first()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF161B26))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PRATINJAU KLIEN INSTAN (TETHERING)", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(latest.fileName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("${latest.aperture} • ${latest.shutterSpeed} • ${latest.iso} • ${latest.focalLength} (${latest.fileSizeMb} MB)", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pratinjau Resolusi Tinggi
${latest.fileName}", color = Color.White)
                        }
                        Button(
                            onClick = { statusLog = "${latest.fileName} berhasil disimpan ke memori internal HP" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simpan File RAW (.NEF) + JPG ke Penyimpanan HP")
                        }
                    }
                }

                2 -> {
                    // TAB 3: GALERI SD CARD & PEMBACA EXIF
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(mediaItems) { item ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF161B26))
                                    .border(1.dp, Color(0xFF263042), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.fileName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        if (item.isRawNef) "RAW .NEF" else "JPG",
                                        color = if (item.isRawNef) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${item.aperture} • ${item.shutterSpeed} • ${item.iso}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                Text("Ukuran: ${item.fileSizeMb} MB", color = Color(0xFF64748B), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { statusLog = "Mengunduh ${item.fileName} (${item.fileSizeMb} MB) ke HP..." },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Unduh Cepat", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigasi Bawah
        NavigationBar(containerColor = Color(0xFF161B26)) {
            NavigationBarItem(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                icon = { Box(Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape)) },
                label = { Text("Live Remote") }
            )
            NavigationBarItem(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                icon = { Box(Modifier.size(8.dp).background(Color(0xFF34D399), CircleShape)) },
                label = { Text("Tethering") }
            )
            NavigationBarItem(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                icon = { Box(Modifier.size(8.dp).background(Color(0xFF38BDF8), CircleShape)) },
                label = { Text("Galeri & EXIF") }
            )
        }
    }
}

@Composable
fun ExposureButton(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161B26))
            .border(1.dp, Color(0xFF263042), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
