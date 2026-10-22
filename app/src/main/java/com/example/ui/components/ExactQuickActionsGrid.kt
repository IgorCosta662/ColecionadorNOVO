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

data class QuickActionItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val cardBgColor: Color,
    val route: String
)

@Composable
fun ExactQuickActionsGrid(
    itemCount: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickActionItem(
            title = "Escanear",
            subtitle = "Identificar com IA",
            icon = Icons.Default.CameraAlt,
            iconBgColor = Color(0xFFA78BFA).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF312E81).copy(alpha = 0.45f),
            route = Routes.SCANNER
        ),
        QuickActionItem(
            title = "Adicionar",
            subtitle = "Manual por Catego...",
            icon = Icons.Default.Add,
            iconBgColor = Color(0xFF64748B).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF1E293B).copy(alpha = 0.65f),
            route = Routes.ADD_ITEM
        ),
        QuickActionItem(
            title = "Pesquisar",
            subtitle = "Busca no Acervo",
            icon = Icons.Default.Search,
            iconBgColor = Color(0xFF475569).copy(alpha = 0.35f),
            cardBgColor = Color(0xFF1E293B).copy(alpha = 0.65f),
            route = Routes.COLLECTION
        ),
        QuickActionItem(
            title = "Ver Coleção",
            subtitle = "Todos os $itemCount itens",
            icon = Icons.Default.PhotoLibrary,
            iconBgColor = Color(0xFFF43F5E).copy(alpha = 0.25f),
            cardBgColor = Color(0xFF4C1D24).copy(alpha = 0.45f),
            route = Routes.COLLECTION
        ),
        QuickActionItem(
            title = "Catálogo",
            subtitle = "Cartas & Carrinhos",
            icon = Icons.Default.MenuBook,
            iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
            cardBgColor = Color(0xFF451A03).copy(alpha = 0.40f),
            route = Routes.MARKETPLACE
        ),
        QuickActionItem(
            title = "Checklists",
            subtitle = "Progresso de Sets",
            icon = Icons.Default.Checklist,
            iconBgColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
            cardBgColor = Color(0xFF0C4A6E).copy(alpha = 0.40f),
            route = Routes.MISSING_CARDS
        ),
        QuickActionItem(
            title = "Mercado",
            subtitle = "Radar & Cotações",
            icon = Icons.Default.TrendingUp,
            iconBgColor = Color(0xFF34D399).copy(alpha = 0.25f),
            cardBgColor = Color(0xFF064E3B).copy(alpha = 0.40f),
            route = Routes.MARKETPLACE
        ),
        QuickActionItem(
            title = "Estatísticas",
            subtitle = "Gráficos & Análise",
            icon = Icons.Default.BarChart,
            iconBgColor = Color(0xFFA855F7).copy(alpha = 0.25f),
            cardBgColor = Color(0xFF3B0764).copy(alpha = 0.40f),
            route = Routes.STATISTICS
        )
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Ações Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        for (i in actions.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickActionCard(action = actions[i], modifier = Modifier.weight(1f), onClick = { navController.navigate(actions[i].route) })
                if (i + 1 < actions.size) {
                    QuickActionCard(action = actions[i + 1], modifier = Modifier.weight(1f), onClick = { navController.navigate(actions[i + 1].route) })
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    action: QuickActionItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = action.cardBgColor)
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
                color = action.iconBgColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        action.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    action.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    action.subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}
