package com.aalavandaanus.kabaliassistant.features.familytree

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

@Composable
fun FamilyTreeScreen() {
    val context = LocalContext.current
    val store = remember { FamilyIdentityStore(context) }
    val people = FamilyTreeData.people
    var identityId by remember { mutableStateOf(store.getPersonId()) }
    var search by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf<FamilyPerson?>(null) }
    var identifyDialog by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    val identity = FamilyTreeData.person(identityId)
    val matches = if (search.isBlank()) emptyList() else people.filter {
        it.fullName.contains(search.trim(), true) || it.displayName.contains(search.trim(), true)
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        if (FamilyTreeData.IS_PREVIEW_DATA) {
            Card(Modifier.fillMaxWidth()) {
                Text("PREVIEW DATA — fictional sample people. Replace FamilyTreeData.kt before using real family records.", Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (identity == null) "Identify yourself" else "You are: ${identity.displayName}", fontWeight = FontWeight.SemiBold)
                Text("Family tree · ${people.size} people", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { identifyDialog = true }) { Text(if (identity == null) "Identify" else "Change") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search family") })
        if (matches.isNotEmpty()) {
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 140.dp)) {
                items(matches, key = { it.id }) { person ->
                    Row(Modifier.fillMaxWidth().clickable { profile = person; search = "" }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(person.displayName)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(person.displayName, fontWeight = FontWeight.Medium)
                            Text(FamilyTreeData.relationshipLabel(identity, person), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            OutlinedButton(onClick = { zoom = 1f; panX = 0f; panY = 0f }) { Text("Fit") }
            OutlinedButton(onClick = { zoom = min(zoom * 1.2f, 3.5f) }) { Text("+") }
            OutlinedButton(onClick = { zoom = max(zoom / 1.2f, 0.55f) }) { Text("−") }
        }
        TreeCanvas(people, identityId, profile?.id, zoom, panX, panY,
            onTransform = { scale, dx, dy -> zoom = (zoom * scale).coerceIn(0.55f, 3.5f); panX += dx; panY += dy },
            onSelect = { profile = FamilyTreeData.person(it) }, modifier = Modifier.weight(1f).fillMaxWidth())
        Text("Pinch to zoom · drag to pan · tap a person for their profile", Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodySmall)
    }

    if (identifyDialog) {
        IdentifyDialog(people, identityId, onDismiss = { identifyDialog = false }, onChoose = {
            store.setPersonId(it.id); identityId = it.id; identifyDialog = false; profile = it
        }, onClear = { store.clear(); identityId = null; identifyDialog = false })
    }
    profile?.let { person ->
        PersonDialog(person, identityId, onDismiss = { profile = null }, onOpenRelative = { profile = it }, onIdentify = {
            store.setPersonId(person.id); identityId = person.id; profile = null
        })
    }
}

@Composable
private fun TreeCanvas(
    people: List<FamilyPerson>, identityId: String?, selectedId: String?, zoom: Float, panX: Float, panY: Float,
    onTransform: (Float, Float, Float) -> Unit, onSelect: (String) -> Unit, modifier: Modifier = Modifier
) {
    val cardW = 150f
    val cardH = 68f
    val rowGap = 150f
    val positions = remember(people) {
        val map = mutableMapOf<String, Offset>()
        people.groupBy { it.generation }.toSortedMap().forEach { (generation, members) ->
            val gap = 18f
            val rowWidth = members.size * cardW + max(0, members.size - 1) * gap
            val left = max(12f, (900f - rowWidth) / 2f)
            members.forEachIndexed { index, p -> map[p.id] = Offset(left + index * (cardW + gap), 28f + generation * rowGap) }
        }
        map
    }
    Canvas(Modifier.then(modifier).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        .pointerInput(positions) { detectTransformGestures { _, pan, scale, _ -> onTransform(scale, pan.x, pan.y) } }
        .pointerInput(positions, zoom, panX, panY) {
            detectTapGestures { tap ->
                val x = (tap.x - panX) / zoom; val y = (tap.y - panY) / zoom
                positions.entries.firstOrNull { (_, p) -> x in p.x..(p.x + cardW) && y in p.y..(p.y + cardH) }?.key?.let(onSelect)
            }
        }) {
        val canvas = drawContext.canvas.nativeCanvas
        val checkpoint = canvas.save()
        canvas.translate(panX, panY); canvas.scale(zoom, zoom)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(140, 148, 158); strokeWidth = 2.5f }
        people.forEach { p ->
            val from = positions[p.id] ?: return@forEach
            p.childIds.forEach childLoop@{ childId ->
                val to = positions[childId] ?: return@childLoop
                canvas.drawLine(from.x + cardW / 2, from.y + cardH, to.x + cardW / 2, to.y, line)
            }
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(30, 37, 47); textSize = 16f; typeface = android.graphics.Typeface.create("sans-serif-medium", Typeface.NORMAL) }
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb(88, 96, 107); textSize = 12f }
        people.forEach { person ->
            val p = positions[person.id] ?: return@forEach
            val isMe = person.id == identityId; val selected = person.id == selectedId
            val fill = when { isMe -> android.graphics.Color.rgb(218, 237, 255); selected -> android.graphics.Color.rgb(255, 238, 199); else -> android.graphics.Color.WHITE }
            val stroke = when { isMe -> android.graphics.Color.rgb(31, 111, 190); selected -> android.graphics.Color.rgb(194, 132, 24); else -> android.graphics.Color.rgb(195, 202, 211) }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill; style = Paint.Style.FILL }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = stroke; style = Paint.Style.STROKE; strokeWidth = if (isMe || selected) 3.5f else 1.5f }
            canvas.drawRoundRect(p.x, p.y, p.x + cardW, p.y + cardH, 12f, 12f, fillPaint)
            canvas.drawRoundRect(p.x, p.y, p.x + cardW, p.y + cardH, 12f, 12f, borderPaint)
            val name = person.displayName.let { if (it.length > 17) it.take(16) + "…" else it }
            canvas.drawText(name, p.x + 9, p.y + 28, titlePaint)
            canvas.drawText(if (isMe) "YOU" else "Generation ${person.generation + 1}", p.x + 9, p.y + 49, detailPaint)
        }
        canvas.restoreToCount(checkpoint)
    }
}

@Composable
private fun Avatar(name: String) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
        Text(name.split(" ").filter(String::isNotBlank).take(2).mapNotNull { it.firstOrNull() }.joinToString(""), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun IdentifyDialog(people: List<FamilyPerson>, currentId: String?, onDismiss: () -> Unit, onChoose: (FamilyPerson) -> Unit, onClear: () -> Unit) {
    var search by remember { mutableStateOf("") }
    val results = people.filter { it.displayName.contains(search, true) || it.fullName.contains(search, true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Identify yourself") }, text = {
        Column {
            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search by name") })
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.height(250.dp)) {
                items(results, key = { it.id }) { person ->
                    Row(Modifier.fillMaxWidth().clickable { onChoose(person) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(person.displayName); Spacer(Modifier.width(10.dp)); Column {
                            Text(person.displayName, fontWeight = FontWeight.Medium)
                            Text("Generation ${person.generation + 1}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }, confirmButton = {}, dismissButton = {
        Row {
            if (currentId != null) TextButton(onClick = onClear) { Text("Clear identity") }
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    })
}

@Composable
private fun PersonDialog(person: FamilyPerson, identityId: String?, onDismiss: () -> Unit, onOpenRelative: (FamilyPerson) -> Unit, onIdentify: () -> Unit) {
    val dateFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val parents = FamilyTreeData.parents(person); val spouses = FamilyTreeData.spouses(person)
    val children = FamilyTreeData.children(person); val others = FamilyTreeData.otherRelatives(person)
    AlertDialog(onDismissRequest = onDismiss, title = {
        Column {
            Text(person.displayName, fontWeight = FontWeight.Bold)
            if (person.preferredName != null && person.preferredName != person.fullName) Text(person.fullName, style = MaterialTheme.typography.bodySmall)
        }
    }, text = {
        LazyColumn(Modifier.heightIn(max = 360.dp)) {
            item {
                Text("Generation ${person.generation + 1}", style = MaterialTheme.typography.bodySmall)
                person.birthDate?.let { Text("Born: ${dateFormat.format(it)}") }
                person.deathDate?.let { Text("Died: ${dateFormat.format(it)}") }
                person.birthplace?.let { Text("Birthplace: $it") }
                person.note?.let { Text(it, Modifier.padding(top = 6.dp)) }
            }
            item { RelativeSection("Parents", parents, onOpenRelative) }
            item { RelativeSection("Spouse", spouses, onOpenRelative) }
            item { RelativeSection("Children", children, onOpenRelative) }
            item { RelativeSection("Other relatives", others, onOpenRelative) }
        }
    }, confirmButton = {
        Row {
            if (identityId != person.id) TextButton(onClick = onIdentify) { Text("Identify as me") }
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    })
}

@Composable
private fun RelativeSection(title: String, people: List<FamilyPerson>, onOpen: (FamilyPerson) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        if (people.isEmpty()) Text("Not listed", style = MaterialTheme.typography.bodySmall)
        else people.forEach { person -> Text("• ${person.displayName}", Modifier.fillMaxWidth().clickable { onOpen(person) }.padding(vertical = 5.dp), color = MaterialTheme.colorScheme.primary) }
    }
}
