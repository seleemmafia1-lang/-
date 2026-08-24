package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QualityRule
import com.example.ui.theme.ColorBadText
import com.example.ui.theme.RaneenNavy
import com.example.ui.viewmodel.QualityViewModel

@Composable
fun RulesManagementScreen(
    viewModel: QualityViewModel,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.rules.collectAsState()
    val searchQuery by viewModel.rulesSearchQuery.collectAsState()

    var newCode by remember { mutableStateOf("") }
    var newSection by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }

    var ruleToDelete by remember { mutableStateOf<QualityRule?>(null) }

    val filteredRules = remember(rules, searchQuery) {
        if (searchQuery.isBlank()) rules
        else rules.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.section.contains(searchQuery, ignoreCase = true) ||
                    it.code.contains(searchQuery, ignoreCase = true)
        }
    }

    val sections = remember(filteredRules) {
        filteredRules.map { it.section }.distinct()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Add Rule Form
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚙️ إضافة معيار جودة جديد",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newCode,
                            onValueChange = { newCode = it },
                            label = { Text("كود المعيار") },
                            placeholder = { Text("Q-${String.format("%03d", rules.size + 1)}") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("new_rule_code"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newSection,
                            onValueChange = { newSection = it },
                            label = { Text("القسم / التصنيف *") },
                            placeholder = { Text("مثال: النظافة أو التسعير") },
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("new_rule_section"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم ونص المعيار المطلوب *") },
                        placeholder = { Text("اكتب ضابط الجودة المطلوب فحصه...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_rule_name"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.addNewRule(newCode, newSection, newName) { success ->
                                if (success) {
                                    newCode = ""
                                    newSection = ""
                                    newName = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_rule_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "➕ إضافة ضابط الجودة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateRulesSearch(it) },
                placeholder = { Text("🔎 بحث في معايير الجودة (${rules.size} معيار)...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateRulesSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rules_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // List of Rules grouped by section
        sections.forEach { sec ->
            val secRules = filteredRules.filter { it.section == sec }

            item(key = "section_$sec") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = sec, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "${secRules.size} معيار", fontSize = 12.sp)
                    }
                }
            }

            items(secRules, key = { it.code }) { rule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_manage_item_${rule.code}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = RaneenNavy.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = rule.code,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = RaneenNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = rule.section,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rule.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { ruleToDelete = rule },
                            modifier = Modifier.testTag("delete_rule_${rule.code}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف المعيار",
                                tint = ColorBadText
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Rule Confirm Dialog
    ruleToDelete?.let { r ->
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            title = { Text("حذف معيار الجودة", fontWeight = FontWeight.Bold) },
            text = { Text("هل ترغب في حذف المعيار [${r.code}] ${r.name}؟ (لا يمكن حذف المعايير المستخدمة في تقارير سابقة)") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRule(r.code)
                        ruleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
