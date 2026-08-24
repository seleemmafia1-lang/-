package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.QualityViewModel
import com.example.ui.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    LoginScreenInternal(
        onLogin = { username, password, role, onResult ->
            userViewModel.login(username, password, role, onResult = onResult)
        },
        onRegisterUser = { username, fullName, role, branch, password, onResult ->
            userViewModel.registerNewUser(username, fullName, role, branch, password, onResult)
        },
        modifier = modifier
    )
}

@Composable
fun LoginScreen(
    viewModel: QualityViewModel,
    modifier: Modifier = Modifier
) {
    LoginScreenInternal(
        onLogin = { username, password, role, onResult ->
            viewModel.login(username, password, role, onResult = onResult)
        },
        onRegisterUser = null,
        modifier = modifier
    )
}

@Composable
private fun LoginScreenInternal(
    onLogin: (String, String, UserRole, (Boolean, String) -> Unit) -> Unit,
    onRegisterUser: ((String, String, UserRole, String, String, (Boolean, String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var selectedRole by remember { mutableStateOf(UserRole.INSPECTOR) }
    var username by remember { mutableStateOf("inspector") }
    var password by remember { mutableStateOf("123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    fun applyDemoCredentials(role: UserRole) {
        selectedRole = role
        errorMessage = null
        when (role) {
            UserRole.INSPECTOR -> {
                username = "inspector"
                password = "123"
            }
            UserRole.QUALITY_MANAGER -> {
                username = "manager"
                password = "123"
            }
            UserRole.BRANCH_MANAGER -> {
                username = "branch"
                password = "123"
            }
            UserRole.ADMIN -> {
                username = "admin"
                password = "123"
            }
        }
    }

    fun handleLogin() {
        errorMessage = null
        val cleanUsername = username.trim()
        if (cleanUsername.isBlank()) {
            errorMessage = "يرجى إدخال اسم المستخدم أو الرقم الوظيفي"
            return
        }
        if (password.isBlank()) {
            errorMessage = "يرجى إدخال كلمة المرور"
            return
        }

        isLoading = true
        onLogin(cleanUsername, password, selectedRole) { success, msg ->
            isLoading = false
            if (!success) {
                errorMessage = msg
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header Banner with Brand Styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(RaneenNavyDark, RaneenNavy, RaneenNavyLight)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.5.dp, RaneenOrange.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "شعار رنين للجودة",
                            tint = RaneenOrange,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "شركة رنين — إدارة الجودة",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "نظام التفتيش والرقابة الفنية الميدانية (Room DB)",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Section: Role Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "١. اختيار الدور الوظيفي (Role)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = RaneenNavy
                        )
                        Text(
                            text = "حدد دورك للتحقق من الصلاحيات المسجلة",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RaneenOrange.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, RaneenOrange.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "قاعدة بيانات محلية",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RaneenOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Role Selection Cards Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleSelectionCard(
                            role = UserRole.INSPECTOR,
                            isSelected = selectedRole == UserRole.INSPECTOR,
                            icon = Icons.Filled.Engineering,
                            onClick = { applyDemoCredentials(UserRole.INSPECTOR) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleSelectionCard(
                            role = UserRole.QUALITY_MANAGER,
                            isSelected = selectedRole == UserRole.QUALITY_MANAGER,
                            icon = Icons.Filled.AssignmentTurnedIn,
                            onClick = { applyDemoCredentials(UserRole.QUALITY_MANAGER) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleSelectionCard(
                            role = UserRole.BRANCH_MANAGER,
                            isSelected = selectedRole == UserRole.BRANCH_MANAGER,
                            icon = Icons.Filled.Business,
                            onClick = { applyDemoCredentials(UserRole.BRANCH_MANAGER) },
                            modifier = Modifier.weight(1f)
                        )
                        RoleSelectionCard(
                            role = UserRole.ADMIN,
                            isSelected = selectedRole == UserRole.ADMIN,
                            icon = Icons.Filled.AdminPanelSettings,
                            onClick = { applyDemoCredentials(UserRole.ADMIN) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Login Credentials Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "٢. التحقق من بيانات الدخول",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = RaneenNavy
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RaneenNavy.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, RaneenNavy.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = selectedRole.title,
                                    color = RaneenNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Username Field
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text("اسم المستخدم (Username)") },
                            placeholder = { Text("مثال: inspector أو manager") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "اسم المستخدم",
                                    tint = RaneenNavy
                                )
                            },
                            trailingIcon = {
                                if (username.isNotEmpty()) {
                                    IconButton(onClick = { username = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "مسح",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("كلمة المرور (Password)") },
                            placeholder = { Text("أدخل كلمة المرور") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "كلمة المرور",
                                    tint = RaneenNavy
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    handleLogin()
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Remember Me Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RaneenNavy
                                )
                            )
                            Text(
                                text = "تذكر تسجيل الدخول في الذاكرة المحلية",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Prominent Error message with animation
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ColorBadBg,
                                border = BorderStroke(1.2.dp, ColorBadBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = "تنبيه خطأ",
                                        tint = ColorBadText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "فشل في تسجيل الدخول",
                                            color = ColorBadText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = errorMessage ?: "",
                                            color = ColorBadText.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                handleLogin()
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RaneenOrange,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "التحقق وتسجيل الدخول",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Demo Accounts Fill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "حسابات جاهزة في قاعدة البيانات (Demo):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (onRegisterUser != null) {
                        TextButton(
                            onClick = { showRegisterDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = RaneenNavy
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "إضافة مستخدم",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RaneenNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DemoChip(
                        title = "مفتش\ninspector",
                        onClick = { applyDemoCredentials(UserRole.INSPECTOR) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        title = "مدير جودة\nmanager",
                        onClick = { applyDemoCredentials(UserRole.QUALITY_MANAGER) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        title = "مدير فرع\nbranch",
                        onClick = { applyDemoCredentials(UserRole.BRANCH_MANAGER) },
                        modifier = Modifier.weight(1f)
                    )
                    DemoChip(
                        title = "مدير نظام\nadmin",
                        onClick = { applyDemoCredentials(UserRole.ADMIN) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "نظام الجودة الشاملة © شركة رنين للتجارة والتوزيع",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showRegisterDialog && onRegisterUser != null) {
        RegisterUserDialog(
            onDismiss = { showRegisterDialog = false },
            onConfirm = { uName, fName, uRole, bName, pHash ->
                onRegisterUser(uName, fName, uRole, bName, pHash) { success, _ ->
                    if (success) {
                        showRegisterDialog = false
                        username = uName
                        password = pHash
                        selectedRole = uRole
                    }
                }
            }
        )
    }
}

@Composable
private fun RoleSelectionCard(
    role: UserRole,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) RaneenNavy.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) RaneenNavy else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) RaneenOrange else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = role.title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) RaneenNavy else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = role.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DemoChip(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = RaneenNavy,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )
    }
}

@Composable
fun RegisterUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, UserRole, String, String) -> Unit
) {
    var regUsername by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("123") }
    var regBranch by remember { mutableStateOf("فرع رنين - الهرم") }
    var regRole by remember { mutableStateOf(UserRole.INSPECTOR) }
    var dialogError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "تسجيل مستخدم جديد في Room DB",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )
                Text(
                    text = "سيتم حفظ الحساب مباشرة في جدول المستخدمين المحلي",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = regUsername,
                    onValueChange = { regUsername = it; dialogError = null },
                    label = { Text("اسم المستخدم (Username)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regFullName,
                    onValueChange = { regFullName = it; dialogError = null },
                    label = { Text("الاسم الكامل") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it; dialogError = null },
                    label = { Text("كلمة المرور") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regBranch,
                    onValueChange = { regBranch = it; dialogError = null },
                    label = { Text("الفرع / الإدارة") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "الدور الوظيفي:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaneenNavy
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserRole.values().forEach { role ->
                        FilterChip(
                            selected = regRole == role,
                            onClick = { regRole = role },
                            label = { Text(role.title, fontSize = 11.sp) }
                        )
                    }
                }

                if (dialogError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dialogError ?: "",
                        color = ColorBadText,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (regUsername.isBlank() || regFullName.isBlank() || regPassword.isBlank()) {
                                dialogError = "يرجى تعبئة كافة الحقول"
                                return@Button
                            }
                            onConfirm(regUsername, regFullName, regRole, regBranch, regPassword)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RaneenNavy)
                    ) {
                        Text("حفظ الحساب")
                    }
                }
            }
        }
    }
}
