package com.tivanco.hymaco.appUI.settingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tivanco.hymaco.R
import com.tivanco.hymaco.utils.ClearFocusOnKeyboardClose
import com.tivanco.hymaco.utils.PrefsManager
import com.tivanco.hymaco.utils.responsiveSizes

@Composable
fun Settings(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    customName: MutableState<TextFieldValue>,
    ssid: MutableState<TextFieldValue>,
    pass: MutableState<TextFieldValue>,
    interval: MutableState<TextFieldValue>,
    mqttHost: MutableState<TextFieldValue>,
    mqttPort: MutableState<TextFieldValue>
) {
    val context = LocalContext.current
    // خوندن آیدی سخت‌افزاری فقط برای نمایش
    val imei = PrefsManager.getImei(context)
    var isPassShown by remember { mutableStateOf(false) }

    val ssidFocusRequester = remember { FocusRequester() }
    val passFocusRequester = remember { FocusRequester() }
    val intervalFocusRequester = remember { FocusRequester() }
    val hostFocusRequester = remember { FocusRequester() }
    val portFocusRequester = remember { FocusRequester() }

    ClearFocusOnKeyboardClose(
        onKeyboardClosed = {
            focusManager.clearFocus()
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //-------------------- DEVICE SETTINGS CARD (جدید) --------------------//
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderText("شناسه دستگاه (جرثقیل)")

                // نمایش آیدی سخت افزاری
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (imei == "") {
                        Text(
                            text = "لطفا برای دریافت شناسه سخت افزاری(IMEI) یک بار به مودم متصل شوید",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                            color = Color.Gray,
                            style = TextStyle(
                                fontSize = (responsiveSizes().fontSize.value - 2f).sp,
                                textDirection = TextDirection.Rtl
                            )
                        )
                    } else {
                        Text(
                            text = imei,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                            color = Color.Gray,
                            style = TextStyle(
                                fontSize = (responsiveSizes().fontSize.value - 2f).sp,
                                textDirection = TextDirection.Ltr
                            )
                        )
                        Text(
                            text = "شناسه سخت‌افزاری (ثابت):",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                            color = Color.Gray,
                            style = TextStyle(
                                fontSize = (responsiveSizes().fontSize.value - 2f).sp,
                                textDirection = TextDirection.Rtl
                            )
                        )
                    }
                }

                // فیلد ورود نام دلخواه
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                    value = customName.value,
                    onValueChange = { customName.value = it },
                    label = {
                        Text(
                            "نام نمایشی",
                            color = Color(0xff9a9a9a),
                            fontSize = responsiveSizes().labelFontSize
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { ssidFocusRequester.requestFocus() }
                    ),
                    textStyle = TextStyle(
                        textDirection = TextDirection.Rtl,
                        fontSize = responsiveSizes().fontSize
                    )
                )
            }
        }

        //-------------------- MODEM SETTINGS CARD --------------------//
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderText("تنظیمات مودم")
                //-------------------- MODEM SSID --------------------//
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .focusRequester(ssidFocusRequester), // اضافه شدن فوکوس به اینجا
                    value = ssid.value,
                    onValueChange = { ssid.value = it },
                    label = {
                        Text(
                            "SSID",
                            color = Color(0xff9a9a9a),
                            fontSize = responsiveSizes().labelFontSize
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passFocusRequester.requestFocus() }
                    ),
                    textStyle = TextStyle(fontSize = responsiveSizes().fontSize)
                )
                //-------------------- MODEM PASS --------------------//
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .focusRequester(passFocusRequester),
                    value = pass.value,
                    onValueChange = { pass.value = it },
                    label = {
                        Text(
                            "Password",
                            color = Color(0xff9a9a9a),
                            fontSize = responsiveSizes().labelFontSize
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (isPassShown) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPassShown = !isPassShown }) {
                            Icon(
                                painter = painterResource(if (isPassShown) R.drawable.pass_hide else R.drawable.pass_show),
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { intervalFocusRequester.requestFocus() }
                    ),
                    textStyle = TextStyle(fontSize = responsiveSizes().fontSize)
                )
                //-------------------- INTERVAL --------------------//
                OutlinedTextField(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .fillMaxWidth()
                        .focusRequester(intervalFocusRequester),
                    value = interval.value,
                    onValueChange = { interval.value = it },
                    label = {
                        Text(
                            "Interval(ms)",
                            color = Color(0xff9a9a9a),
                            fontSize = responsiveSizes().labelFontSize
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { hostFocusRequester.requestFocus() }
                    ),
                    textStyle = TextStyle(fontSize = responsiveSizes().fontSize)
                )
            }
        }
        //-------------------- MQTT SETTINGS CARD --------------------//
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderText("بروکر MQTT")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    //-------------------- MQTT HOST --------------------//
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(0.7f)
                            .focusRequester(hostFocusRequester),
                        value = mqttHost.value,
                        onValueChange = { mqttHost.value = it },
                        placeholder = {
                            Text(
                                "Host",
                                color = Color(0xff9a9a9a),
                                fontSize = responsiveSizes().labelFontSize
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { portFocusRequester.requestFocus() }
                        ),
                        textStyle = TextStyle(
                            fontSize = responsiveSizes().fontSize,
                            textAlign = TextAlign.Start
                        ),
                    )

                    Text(
                        text = ":",
                        fontSize = responsiveSizes().fontSize
                    )

                    //-------------------- MQTT PORT --------------------//
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(0.3f)
                            .focusRequester(portFocusRequester),
                        value = mqttPort.value,
                        onValueChange = { mqttPort.value = it },
                        placeholder = {
                            Text(
                                "Port",
                                color = Color(0xff9a9a9a),
                                fontSize = responsiveSizes().labelFontSize
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        textStyle = TextStyle(
                            fontSize = responsiveSizes().fontSize,
                            textAlign = TextAlign.Center
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderText(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFD5D5D5))
            .padding(10.dp),
        text = text,
        fontSize = responsiveSizes().fontSize,
        color = Color(0xFF4B4B4B),
        textAlign = TextAlign.Center,
        style = TextStyle(textDirection = TextDirection.Rtl)
    )
}