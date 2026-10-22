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
import com.example.data.Item
import com.example.ui.navigation.Routes

data class CategoryItemInfo(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val filterQuery: String
)

@Composable
fun CollectionCategoriesSection(
    items: List<Item>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryItemInfo("TCG", Icons.Default.Style, Color(0xFF6366F1), "Trading Cards"),
        CategoryItemInfo("Diecast", Icons.Default.DirectionsCar, Color(0xFFEF4444), "Carrinhos / Diecast"),
        CategoryItemInfo("Action Figures", Icons.Default.SmartToy, Color(0xFFA855F7), "Action Figures"),
        CategoryItemInfo("Moedas", Icons.Default.MonetizationOn, Color(0xFFEAB308), "Moedas"),
        CategoryItemInfo("Colecionáveis", Icons.Default.Extension, Color(0xFFEC4899), "Colecionáveis"),
        CategoryItemInfo("Outros", Icons.Default.Category, Color(0xFF10B981), "Outros")
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Categorias da Coleção",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Visualização Isolada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        for (i in categories.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val cat1 = categories[i]
                val count1 = items.count {
                    if (cat1.title == "TCG") it.isCard
                    else if (cat1.title == "Diecast") it.isDiecast
                    else it.category.contains(cat1.filterQuery, ignoreCase = true)
                }
                CategoryCard(
                    category = cat1,
                    count = count1,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.COLLECTION) }
                )

                if (i + 1 < categories.size) {
                    val cat2 = categories[i + 1]
                    val count2 = items.count {
                        if (cat2.title == "TCG") it.isCard
                        else if (cat2.title == "Diecast") it.isDiecast
                        else it.category.contains(cat2.filterQuery, ignoreCase = true)
                    }
                    CategoryCard(
                        category = cat2,
                        count = count2,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.COLLECTION) }
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItemInfo,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = category.color.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            category.icon,
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(category.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("$count itens", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
