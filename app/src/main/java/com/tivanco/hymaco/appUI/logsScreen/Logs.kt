package com.tivanco.hymaco.appUI.logsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tivanco.hymaco.R
import com.tivanco.hymaco.dataClass.Constants
import com.tivanco.hymaco.utils.SharedRepository
import com.tivanco.hymaco.utils.responsiveSizes
import com.tivanco.hymaco.viewModel.CraneControlViewModel

//private const val TAG = "LogsScreen"

@Composable
fun Logs(
    modifier: Modifier,
    craneControlViewModel: CraneControlViewModel
) {
    val debugMsg by SharedRepository.debugMsg.collectAsStateWithLifecycle()
    val alarms by craneControlViewModel.alarms.collectAsState()
    val workClock by craneControlViewModel.workClock.collectAsStateWithLifecycle()
    val hydroOilChangeClock by craneControlViewModel.hydroOilChangeClock.collectAsStateWithLifecycle()
    val gbOilChangeClock by craneControlViewModel.gbOilChangeClock.collectAsStateWithLifecycle()
    val oilIn by craneControlViewModel.oilIn.collectAsStateWithLifecycle()
    val oilOut by craneControlViewModel.oilOut.collectAsStateWithLifecycle()
    val tower by craneControlViewModel.tower.collectAsStateWithLifecycle()
    val gbState by craneControlViewModel.gbState.collectAsStateWithLifecycle()
    val rot1 by craneControlViewModel.rot1.collectAsStateWithLifecycle()
    val rot2 by craneControlViewModel.rot2.collectAsStateWithLifecycle()
    val hydroOilChange by craneControlViewModel.hydroOilChange.collectAsStateWithLifecycle()
    val gbOilChange by craneControlViewModel.gbOilChange.collectAsStateWithLifecycle()
    val persianTime by craneControlViewModel.persianTime.collectAsStateWithLifecycle()

    var deleteDialog by remember { mutableStateOf(false) }
    val microAlarm = Constants.MICROSWITCH
    val oilAlarm = Constants.OIL_CHANGE

    val workClockValue =
        if (workClock.isEmpty()) "-" else "$workClock ساعت"
    val hydroWorkClockValue =
        if (hydroOilChangeClock.isEmpty()) "-" else "$hydroOilChangeClock ساعت"
    val gbWorkClockValue =
        if (gbOilChangeClock.isEmpty()) "-" else "$gbOilChangeClock ساعت"
    val oilInValue = if (oilIn.isEmpty()) "-" else "$oilIn بار"
    val oilOutValue = if (oilOut.isEmpty()) "-" else "$oilOut بار"

    val towerConfig = getAlarmUIConfig(microAlarm, tower)
    val gbConfig = getAlarmUIConfig(microAlarm, gbState)
    val rotConfig1 = getAlarmUIConfig(microAlarm, rot1)
    val rotConfig2 = getAlarmUIConfig(microAlarm, rot2)
    val hydroOilConfig = getAlarmUIConfig(oilAlarm, hydroOilChange)
    val gbOilConfig = getAlarmUIConfig(oilAlarm, gbOilChange)

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        // SHOW RECEIVED MESSAGE FOR DEBUG AND TEST
        item {
            Text(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = debugMsg,
                fontSize = 10.sp
            )
        }
        // HEADER CARD
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .background(Color(0xFFD6FAD6), RoundedCornerShape(16.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("زمان آخرین اتصال:")
                        Text(persianTime)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("ساعت کارکرد جرثقیل:")
                        Text(workClockValue)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("فشار ورودی: $oilInValue")
                    Text("فشار خروجی: $oilOutValue")
                }
            }
        }

        // ALARM CARD
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .fillMaxWidth()
                    .background(Color.DarkGray, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "میکرو سوئیچ ها: ",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "برجک:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(
                            painter = towerConfig.painter,
                            contentDescription = "tower icon",
                            tint = towerConfig.tint
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "گیربکس:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(
                            painter = gbConfig.painter,
                            contentDescription = "gearbox icon",
                            tint = gbConfig.tint
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "گردان 1:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(
                            painter = rotConfig1.painter,
                            contentDescription = "rot1",
                            tint = rotConfig1.tint
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "گردان 2:",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(
                            painter = rotConfig2.painter,
                            contentDescription = "rot2",
                            tint = rotConfig2.tint
                        )
                    }
                }

                Text(
                    text = "وضعیت روغن:",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "هیدرولیک:",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Icon(
                                painter = hydroOilConfig.painter,
                                contentDescription = "hydroOilChange",
                                tint = hydroOilConfig.tint
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "گیربکس:",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Icon(
                                painter = gbOilConfig.painter,
                                contentDescription = "gbOilChange",
                                tint = gbOilConfig.tint
                            )
                        }
                    }
                }

                Text(
                    text = "ساعت تعویض روغن:",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "هیدرولیک: $hydroWorkClockValue",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "گیربکس: $gbWorkClockValue",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // TABLE HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .background(Color.DarkGray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "زمان وقوع خطا",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                // CLOCK COLUMN
                Text(
                    text = "ساعت کارکرد",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )

                // ERROR COLUMN
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(0.8f),
                        text = "خطا",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // IF THERE ARE ANY ERRORS THE ICON WILL APPEAR
                    if (alarms.isNotEmpty())
                        Icon(
                            modifier = Modifier.clickable {
                                deleteDialog = true
                            },
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Delete",
                            tint = Color(0xFFB33A3A)
                        )
                }
            }
        }

        // TABLE DATA
        if (alarms.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { Text("هیچ خطایی وجود ندارد") }
            }
        } else {
            itemsIndexed(alarms) { index, alarm ->
                val rowColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color(0xFFC9C9C9)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .background(rowColor)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alarm.persianDate,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = responsiveSizes().labelFontSize
                    )
                    // CLOCK COLUMN
                    Text(
                        text = alarm.time,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )

                    // ERROR COLUMN
                    Text(
                        text = alarm.message,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 5.dp),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // DELETE DIALOG
    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("حذف خطا ها") },
            text = { Text("آیا می خواهید همه خطا ها را پاک کنید؟ ") },
            confirmButton = {
                TextButton(
                    onClick = {
                        craneControlViewModel.deleteAlarms()
                        deleteDialog = false
                    }
                ) {
                    Text("تایید")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteDialog = false }
                ) {
                    Text("لغو")
                }
            }
        )
    }
}

// یه دیتا کلاس کوچولو برای نگه داشتن وضعیت ظاهر
data class AlarmUIConfig(
    val painter: Painter,
    val tint: Color
)

@Composable
fun getAlarmUIConfig(alarmType: String, alarmText: String): AlarmUIConfig {
    // اول وضعیت منطقی رو یک بار برای همیشه مشخص می‌کنیم
    val isOk = when (alarmType) {
        Constants.MICROSWITCH -> alarmText == "1"
        Constants.OIL_CHANGE -> alarmText == "0"
        else -> null // حالت ناشناخته
    }

    // حالا بر اساس وضعیت، ظاهر رو برمی‌گردونیم
    return when (isOk) {
        true -> AlarmUIConfig(
            painter = painterResource(id = R.drawable.ok),
            tint = colorResource(id = R.color.green)
        )
        false -> AlarmUIConfig(
            painter = painterResource(id = R.drawable.error),
            tint = colorResource(id = R.color.dark_yellow)
        )
        null -> AlarmUIConfig( // معادل همون else توی کد خودته
            painter = painterResource(id = R.drawable.dashed_line),
            tint = Color.Gray
        )
    }
}