package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Routes

data class ExactToolItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val cardBgColor: Color,
    val route: String
)

@Composable
fun ExactCollectorToolsGrid(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ExactToolItem(
            title = "O Que Falta?",
            subtitle = "Checklist por Set",
            icon = Icons.Default.Checklist,
            iconBgColor = Color(0xFF38BDF8).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF0F2B48).copy(alpha = 0.5f),
            route = Routes.MISSING_CARDS
        ),
        ExactToolItem(
            title = "Inventário Físico",
            subtitle = "Pastas & Caixas",
            icon = Icons.Default.Inventory2,
            iconBgColor = Color(0xFF34D399).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF064E3B).copy(alpha = 0.4f),
            route = Routes.PHYSICAL_INVENTORY
        ),
        ExactToolItem(
            title = "Duplicatas",
            subtitle = "Controle de Cópias",
            icon = Icons.Default.ContentCopy,
            iconBgColor = Color(0xFFA78BFA).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF2E1065).copy(alpha = 0.45f),
            route = Routes.DUPLICATES
        ),
        ExactToolItem(
            title = "Agentes de IA",
            subtitle = "7 Especialistas",
            icon = Icons.Default.AutoAwesome,
            iconBgColor = Color(0xFFFBBF24).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF451A03).copy(alpha = 0.45f),
            route = Routes.AI_ASSISTANT
        ),
        ExactToolItem(
            title = "Alertas de Preço",
            subtitle = "Detector Suspeito",
            icon = Icons.Default.NotificationsActive,
            iconBgColor = Color(0xFFF87171).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF4C0519).copy(alpha = 0.45f),
            route = Routes.PRICE_ALERTS
        ),
        ExactToolItem(
            title = "Patrimônio & B...",
            subtitle = "PIN, CSV & PDF",
            icon = Icons.Default.Security,
            iconBgColor = Color(0xFF60A5FA).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF1E3A5F).copy(alpha = 0.5f),
            route = Routes.PATRIMONY_BACKUP
        ),
        ExactToolItem(
            title = "Decks & Forma...",
            subtitle = "Calcular & Montar",
            icon = Icons.Default.FolderSpecial,
            iconBgColor = Color(0xFF818CF8).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF1E1B4B).copy(alpha = 0.55f),
            route = Routes.DECKS_LIST
        ),
        ExactToolItem(
            title = "Catálogo Oficial",
            subtitle = "Preços & Sets",
            icon = Icons.Default.MenuBook,
            iconBgColor = Color(0xFFF472B6).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF4A044E).copy(alpha = 0.45f),
            route = Routes.MARKETPLACE
        ),
        ExactToolItem(
            title = "Produtos Selad...",
            subtitle = "Decks MTG & Prec...",
            icon = Icons.Default.Archive,
            iconBgColor = Color(0xFFC084FC).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF3B0764).copy(alpha = 0.45f),
            route = Routes.SEALED_PRODUCTS
        ),
        ExactToolItem(
            title = "Guia de Efeitos",
            subtitle = "Traduzir Tudo (PT-...",
            icon = Icons.Default.Translate,
            iconBgColor = Color(0xFF4ADE80).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF064E3B).copy(alpha = 0.4f),
            route = Routes.EFFECTS_GUIDE
        )
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ferramentas do Colecionador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Gestão Avançada",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA78BFA),
                fontWeight = FontWeight.SemiBold
            )
        }

        for (i in tools.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExactToolCard(tool = tools[i], modifier = Modifier.weight(1f), onClick = { navController.navigate(tools[i].route) })
                if (i + 1 < tools.size) {
                    ExactToolCard(tool = tools[i + 1], modifier = Modifier.weight(1f), onClick = { navController.navigate(tools[i + 1].route) })
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Import Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { navController.navigate(Routes.IMPORT_CATALOG) },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Importar Catálogo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        "Direto do PDF, Excel (.xlsx) & CSV",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExactToolCard(
    tool: ExactToolItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = tool.cardBgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tool.iconBgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        tool.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tool.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    tool.subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}
