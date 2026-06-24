package com.tivanco.hymaco.appUI.mainScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.responsiveSizes

//
//@Composable
//fun SwipeableHeader(udpViewModel: UdpViewModel) {
//    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
//    var page by remember { mutableIntStateOf(0) }
//
//    LaunchedEffect(page) {
//        pagerState.animateScrollToPage(page)
//    }
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        HorizontalPager(
//            state = pagerState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(120.dp)
//        ) { page ->
//            when (page) {
//                0 -> Logos()
//                1 -> Messages(udpViewModel)
//            }
//        }
//
//        // ---------- Page Indicator ----------
//        Row(
//            modifier = Modifier
//                .padding(top = 8.dp, bottom = 4.dp),
//            horizontalArrangement = Arrangement.spacedBy(3.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            repeat(2) { index ->
//                val color = if (pagerState.currentPage == index) Color(0xFF445D73) else Color.Gray
//                Box(
//                    modifier = Modifier
//                        .clickable {
//                            page = index
//                        }
//                        .size(8.dp)
//                        .background(color, shape = CircleShape)
//                        .padding(horizontal = 4.dp)
//                )
//            }
//        }
//    }
//}
//
@Composable
fun Logos() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.logo_tivan),
            contentDescription = "Tivan",
            modifier = Modifier
                .size(responsiveSizes().imageSize),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(R.drawable.logo_hymaco),
            contentDescription = "Hymaco",
            modifier = Modifier.size(responsiveSizes().imageSize),
            contentScale = ContentScale.Fit
        )
    }
}
//
//data class TestMessage(val text: String, val type: MessageType)
//enum class MessageType { INFO, WARNING, ERROR }
//
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun Messages(udpViewModel: UdpViewModel) {
//    var deleteDialog by remember { mutableStateOf(false) }
//    var hasError by remember { mutableStateOf(false) }
//
//    val workClockValue = if (udpViewModel.workClock.isEmpty()) "-" else "${udpViewModel.workClock} ساعت"
//    val oil1Value = if (udpViewModel.oilIn.isEmpty()) "-" else "${udpViewModel.oilIn} بار"
//    val oil2Value = if (udpViewModel.oilOut.isEmpty()) "-" else "${udpViewModel.oilOut} بار"
//
//    val errorMessages by udpViewModel.errorList.collectAsState()
//
//    LazyColumn(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        // کارت بالایی
//        item {
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 10.dp, vertical = 5.dp)
//                    .fillMaxWidth()
//                    .background(Color(0xFFD6FAD6), RoundedCornerShape(16.dp))
//                    .padding(10.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Text("ساعت کارکرد: $workClockValue")
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text("فشار روغن1: $oil1Value")
//                    Text("فشار روغن2: $oil2Value")
//                }
//            }
//        }
//
//        // عنوان جدول
//        item {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
//                    .background(Color.DarkGray)
//                    .padding(8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // ستون ساعت
//                Text(
//                    text = "ساعت",
//                    modifier = Modifier.weight(1f),
//                    color = Color.White,
//                    textAlign = TextAlign.Center,
//                )
//
//                // ستون خطا
//                Row(
//                    modifier = Modifier.weight(2f),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        modifier = Modifier.weight(0.8f),
//                        text = "خطا",
//                        color = Color.White,
//                        textAlign = TextAlign.Center
//                    )
//
//                    if (errorMessages.isNotEmpty())
//                        Icon(
//                            modifier = Modifier.clickable {
//                                deleteDialog = true
//                            },
//                            painter = painterResource(R.drawable.delete),
//                            contentDescription = "Delete",
//                            tint = Color(0xFFB33A3A)
//                        )
//                }
//            }
//        }
//
//        if (errorMessages.isEmpty()) {
//            item {
//                Box(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentAlignment = Alignment.Center
//                ) { Text("هیچ خطایی وجود ندارد") }
//            }
//        } else {
//            itemsIndexed(errorMessages) { index, err ->
//                val rowColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color(0xFFC9C9C9)
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 6.dp)
//                        .background(rowColor)
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // ستون ساعت
//                    Text(
//                        text = err.time,
//                        modifier = Modifier.weight(1f).padding(start = 5.dp),
//                        color = Color.Black,
//                        textAlign = TextAlign.Center,
//                    )
//
//                    // ستون خطا
//                    Text(
//                        text = err.message,
//                        modifier = Modifier.weight(2f).padding(start = 5.dp),
//                        color = Color.Black,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//        }
//    }
//
//    // دیالوگ حذف
//    if (deleteDialog) {
//        AlertDialog(
//            onDismissRequest = { deleteDialog = false },
//            title = { Text("حذف خطا ها") },
//            text = { Text("آیا می خواهید همه خطا ها را پاک کنید؟ ") },
//            confirmButton = {
//                TextButton(onClick = {
//                    udpViewModel.deleteErrors()
//                    deleteDialog = false
//                }) {
//                    Text("تایید")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { deleteDialog = false }) {
//                    Text("لغو")
//                }
//            }
//        )
//    }
//}