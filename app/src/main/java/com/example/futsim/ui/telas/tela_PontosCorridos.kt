package com.example.futsim.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.futsim.model.Time
import com.example.futsim.model.TimeTabela
import com.example.futsim.navigation.BottomNavItem
import com.example.futsim.ui.viewmodel.LocalFutSimViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource // Importação adicionada
import com.example.futsim.R // Importação adicionada
import androidx.annotation.StringRes // Importação adicionada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPontosCorridos(navHostController: NavHostController, campeonatoId: Int) {
    val viewModel = LocalFutSimViewModel.current
    val times by viewModel.times.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Estados para os diálogos
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var timeSelecionado by remember { mutableStateOf<Time?>(null) }

    // Estados para os campos de texto do diálogo
    var nome by remember { mutableStateOf("") }
    var vitorias by remember { mutableStateOf("") }
    var empates by remember { mutableStateOf("") }
    var derrotas by remember { mutableStateOf("") }
    var golsPro by remember { mutableStateOf("") }
    var golsContra by remember { mutableStateOf("") }

    // Carrega os times do campeonato ao entrar na tela
    LaunchedEffect(campeonatoId) {
        viewModel.carregarTimesPorCampeonato(campeonatoId)
    }

    fun limparCampos() {
        nome = ""
        vitorias = ""
        empates = ""
        derrotas = ""
        golsPro = ""
        golsContra = ""
        timeSelecionado = null
    }

    // --- DIÁLOGO DE ADICIONAR/EDITAR TIME ---
    if (showAddOrEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddOrEditDialog = false
                limparCampos()
            },
            title = { Text(if (timeSelecionado == null) stringResource(R.string.adicionar_time_dialog_titulo) else stringResource(R.string.editar_time_dialog_titulo)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text(stringResource(R.string.nome_do_time)) })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = vitorias, onValueChange = { vitorias = it }, label = { Text(stringResource(R.string.vitorias_abreviado)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = empates, onValueChange = { empates = it }, label = { Text(stringResource(R.string.empates_abreviado)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = derrotas, onValueChange = { derrotas = it }, label = { Text(stringResource(R.string.derrotas_abreviado)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = golsPro, onValueChange = { golsPro = it }, label = { Text(stringResource(R.string.gols_pro_abreviado)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = golsContra, onValueChange = { golsContra = it }, label = { Text(stringResource(R.string.gols_contra_abreviado)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                }
            }, ///
            confirmButton = {
                Button(onClick = {
                    val camposValidos = nome.isNotBlank() && vitorias.toIntOrNull() != null && empates.toIntOrNull() != null && derrotas.toIntOrNull() != null && golsPro.toIntOrNull() != null && golsContra.toIntOrNull() != null
                    if (camposValidos) {
                        val v = vitorias.toInt()
                        val e = empates.toInt()
                        val d = derrotas.toInt()
                        val gp = golsPro.toInt()
                        val gc = golsContra.toInt()

                        if (timeSelecionado == null) { // Adicionar novo time
                            val time = Time(nome = nome, campeonatoId = campeonatoId, vitorias = v, empates = e, derrotas = d, golsPro = gp, golsContra = gc)
                            viewModel.inserirTime(time)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Time adicionado!") }
                        } else { // Atualizar time existente
                            val timeAtualizado = timeSelecionado!!.copy(nome = nome, vitorias = v, empates = e, derrotas = d, golsPro = gp, golsContra = gc)
                            viewModel.atualizarTime(timeAtualizado)
                            coroutineScope.launch { snackbarHostState.showSnackbar("Time atualizado!") }
                        }
                        showAddOrEditDialog = false
                        limparCampos()
                    } else {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Preencha todos os campos corretamente.") }
                    }
                }) { Text(stringResource(R.string.salvar)) }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showAddOrEditDialog = false
                    limparCampos()
                }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }

    // --- DIÁLOGO DE CONFIRMAÇÃO DE EXCLUSÃO ---
    if (showDeleteDialog && timeSelecionado != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                limparCampos()
            },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.excluir_time_dialog_titulo)) },
            text = { Text(stringResource(R.string.excluir_time_dialog_texto, timeSelecionado?.nome ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletarTime(timeSelecionado!!)
                        coroutineScope.launch { snackbarHostState.showSnackbar("Time excluído!") }
                        showDeleteDialog = false
                        limparCampos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.excluir)) }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showDeleteDialog = false
                    limparCampos()
                }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddOrEditDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.adicionar_time_content_desc))
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.pontos_corridos_titulo), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController.navigate(BottomNavItem.Campeonatos.route) {
                            popUpTo(navHostController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voltar_content_desc))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val timesOrdenados = times
                .map {
                    TimeTabela(posicao = 0, nome = it.nome, pontos = it.vitorias * 3 + it.empates, jogos = it.vitorias + it.empates + it.derrotas, vitorias = it.vitorias, empates = it.empates, derrotas = it.derrotas, golsPro = it.golsPro, golsContra = it.golsContra)
                }
                .sortedWith(compareByDescending<TimeTabela> { it.pontos }.thenByDescending { it.vitorias }.thenByDescending { it.saldoGols }.thenByDescending { it.golsPro })
                .mapIndexed { index, time -> time.copy(posicao = index + 1) }

            if (times.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.nenhum_time_adicionado_mensagem),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item { HeaderTabelaPontosCorridos() }

                    items(timesOrdenados, key = { it.nome }) { timeTabela ->
                        LinhaTabelaPontosCorridos(
                            time = timeTabela,
                            onEditClick = {
                                val timeOriginal = times.find { it.nome == timeTabela.nome }
                                if (timeOriginal != null) {
                                    timeSelecionado = timeOriginal
                                    nome = timeOriginal.nome
                                    vitorias = timeOriginal.vitorias.toString()
                                    empates = timeOriginal.empates.toString()
                                    derrotas = timeOriginal.derrotas.toString()
                                    golsPro = timeOriginal.golsPro.toString()
                                    golsContra = timeOriginal.golsContra.toString()
                                    showAddOrEditDialog = true
                                }
                            },
                            onDeleteClick = {
                                timeSelecionado = times.find { it.nome == timeTabela.nome }
                                showDeleteDialog = true
                            }
                        )
                        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderTabelaPontosCorridos() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val headerStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(stringResource(R.string.tabela_posicao_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tabela_time), modifier = Modifier.weight(2f).padding(start = 8.dp), style = headerStyle)
        Text(stringResource(R.string.tabela_pontos_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tabela_jogos_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.vitorias_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.empates_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.derrotas_abreviado), modifier = Modifier.width(30.dp), style = headerStyle, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tabela_saldo_gols_abreviado), modifier = Modifier.width(35.dp), style = headerStyle, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.width(72.dp)) // Espaço para os botões de ação
    }
    Divider(color = MaterialTheme.colorScheme.outline)
}

@Composable
fun LinhaTabelaPontosCorridos(
    time: TimeTabela,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val position = time.posicao
    val isLeader = position == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Fundo neutro
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val cellStyle = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "${time.posicao}",
            modifier = Modifier.width(30.dp),
            style = cellStyle,
            textAlign = TextAlign.Center,
            fontWeight = if(isLeader) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = time.nome,
            modifier = Modifier.weight(2f).padding(start = 8.dp),
            style = cellStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if(isLeader) FontWeight.Bold else FontWeight.Normal
        )
        Text("${time.pontos}", modifier = Modifier.width(30.dp), style = cellStyle.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
        Text("${time.jogos}", modifier = Modifier.width(30.dp), style = cellStyle, textAlign = TextAlign.Center)
        Text("${time.vitorias}", modifier = Modifier.width(30.dp), style = cellStyle, textAlign = TextAlign.Center)
        Text("${time.empates}", modifier = Modifier.width(30.dp), style = cellStyle, textAlign = TextAlign.Center)
        Text("${time.derrotas}", modifier = Modifier.width(30.dp), style = cellStyle, textAlign = TextAlign.Center)
        Text("${time.saldoGols}", modifier = Modifier.width(35.dp), style = cellStyle, textAlign = TextAlign.Center)

        Row(modifier = Modifier.width(72.dp)) {
            IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.editar), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.excluir), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}