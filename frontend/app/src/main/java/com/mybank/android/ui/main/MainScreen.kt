package com.mybank.android.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.mybank.android.data.DefaultDataRepository
import com.mybank.android.theme.MyBankTheme

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel =
        viewModel { MainScreenViewModel(DefaultDataRepository()) }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state) {
        MainScreenUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is MainScreenUiState.Success -> {
            MainScreen(
                data = (state as MainScreenUiState.Success).data,
                modifier = modifier
            )
        }

        is MainScreenUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Não foi possível carregar os dados.")
            }
        }
    }
}

@Composable
internal fun MainScreen(
    data: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "MyBank",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Olá, Lucas!",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Bem-vindo à sua conta",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Saldo disponível",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "R$ 893,01",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Valor fictício para desenvolvimento",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Acesso rápido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text("PIX")
            }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text("Transferir")
            }
        }

        Text(
            text = "Últimas movimentações",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        TransactionItem(
            title = "Compra no débito",
            description = "Cartão final 9634",
            amount = "- R$ 1,00"
        )

        TransactionItem(
            title = "PIX enviado",
            description = "Transferência",
            amount = "- R$ 25,00"
        )

        TransactionItem(
            title = "Dinheiro recebido",
            description = "Transferência recebida",
            amount = "+ R$ 100,00",
            isIncome = true
        )

        Text(
            text = if (data.isNotEmpty()) {
                "Dados demonstrativos"
            } else {
                "Nenhum dado carregado"
            },
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TransactionItem(
    title: String,
    description: String,
    amount: String,
    isIncome: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
               color = if (isIncome) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyBankTheme {
        MainScreen(data = listOf("Android"))
    }
}
