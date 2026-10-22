package com.example.ui.components

import androidx.compose.foundation.background
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

data class ToolGridItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val route: String
)

@Composable
fun CollectorToolsSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val tools = listOf(
        ToolGridItem("O Que Falta?", "Checklist por Set", Icons.Default.Checklist, Color(0xFF38BDF8).copy(alpha = 0.2f), Routes.MISSING_CARDS),
        ToolGridItem("Inventário Físico", "Pastas & Caixas", Icons.Default.Inventory2, Color(0xFF34D399).copy(alpha = 0.2f), Routes.PHYSICAL_INVENTORY),
        ToolGridItem("Duplicatas", "Controle de Cópias", Icons.Default.ContentCopy, Color(0xFFA78BFA).copy(alpha = 0.2f), Routes.DUPLICATES),
        ToolGridItem("Assistente IA", "Perguntas da Coleção", Icons.Default.AutoAwesome, Color(0xFFFBBF24).copy(alpha = 0.2f), Routes.AI_ASSISTANT),
        ToolGridItem("Alertas de Preço", "Detector Suspeito", Icons.Default.NotificationsActive, Color(0xFFF87171).copy(alpha = 0.2f), Routes.PRICE_ALERTS),
        ToolGridItem("Patrimônio & Backup", "PIN, CSV & PDF", Icons.Default.Security, Color(0xFF60A5FA).copy(alpha = 0.2f), Routes.PATRIMONY_BACKUP),
        ToolGridItem("Decks & Formatos", "Calcular & Montar", Icons.Default.FolderSpecial, Color(0xFF818CF8).copy(alpha = 0.2f), Routes.DECKS_LIST),
        ToolGridItem("Catálogo Oficial", "Preços & Sets", Icons.Default.MenuBook, Color(0xFFF472B6).copy(alpha = 0.2f), Routes.MARKETPLACE),
        ToolGridItem("Produtos Selados", "Decks MTG & Caixas", Icons.Default.Archive, Color(0xFFC084FC).copy(alpha = 0.2f), Routes.SEALED_PRODUCTS),
        ToolGridItem("Guia de Efeitos", "Traduzir Tudo (PT-BR)", Icons.Default.Translate, Color(0xFF4ADE80).copy(alpha = 0.2f), Routes.EFFECTS_GUIDE)
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Two columns grid
        for (i in tools.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolGridCard(tool = tools[i], modifier = Modifier.weight(1f), onClick = { navController.navigate(tools[i].route) })
                if (i + 1 < tools.size) {
                    ToolGridCard(tool = tools[i + 1], modifier = Modifier.weight(1f), onClick = { navController.navigate(tools[i + 1].route) })
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Import Banner as in screenshot
        ImportCatalogBanner(onClick = { navController.navigate(Routes.IMPORT_CATALOG) })
    }
}

@Composable
fun ToolGridCard(
    tool: ToolGridItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tool.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    tool.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tool.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    tool.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ImportCatalogBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Importar Catálogo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    "Direto do PDF, Excel (.xlsx) & CSV",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
