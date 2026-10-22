package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawerSheet(
    navController: NavController,
    onCloseDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Drawer Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Style,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Column {
                    Text(
                        "Colecionador",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Menu de Ferramentas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("PRINCIPAIS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            DrawerItem(
                icon = Icons.Default.Dashboard,
                label = "Painel / Início",
                onClick = {
                    navController.navigate(Routes.DASHBOARD)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Style,
                label = "Minha Coleção",
                onClick = {
                    navController.navigate(Routes.COLLECTION)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.QrCodeScanner,
                label = "Escanear & Identificar Carta",
                onClick = {
                    navController.navigate(Routes.SCANNER)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.FolderSpecial,
                label = "Gerenciador de Decks",
                onClick = {
                    navController.navigate(Routes.DECKS_LIST)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.BarChart,
                label = "Estatísticas & Patrimônio",
                onClick = {
                    navController.navigate(Routes.STATISTICS)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("MERCADO & FERRAMENTAS PRO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            DrawerItem(
                icon = Icons.Default.Storefront,
                label = "Mercado Oficial (LigaMagic/LigaPokémon)",
                onClick = {
                    navController.navigate(Routes.MARKETPLACE)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.CompareArrows,
                label = "Comparador de Cartas",
                onClick = {
                    navController.navigate(Routes.COMPARE)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Bookmarks,
                label = "Lista de Desejos (Wishlist)",
                onClick = {
                    navController.navigate(Routes.WISHLIST)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.EmojiEvents,
                label = "Conquistas & Nível",
                badge = "XP",
                onClick = {
                    navController.navigate(Routes.ACHIEVEMENTS)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("GESTÃO AVANÇADA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            DrawerItem(
                icon = Icons.Default.Checklist,
                label = "O Que Falta? (Checklists)",
                onClick = {
                    navController.navigate(Routes.MISSING_CARDS)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Inventory2,
                label = "Inventário Físico (Pastas/Caixas)",
                onClick = {
                    navController.navigate(Routes.PHYSICAL_INVENTORY)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.ContentCopy,
                label = "Duplicatas (Troca/Venda)",
                onClick = {
                    navController.navigate(Routes.DUPLICATES)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.AutoAwesome,
                label = "Agentes de IA (Especialistas)",
                badge = "7 IAs",
                onClick = {
                    navController.navigate(Routes.AI_ASSISTANT)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.NotificationsActive,
                label = "Alertas de Preço & Radar",
                onClick = {
                    navController.navigate(Routes.PRICE_ALERTS)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Security,
                label = "Patrimônio & Backup (CSV/PDF)",
                onClick = {
                    navController.navigate(Routes.PATRIMONY_BACKUP)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Archive,
                label = "Produtos Selados & Caixas",
                onClick = {
                    navController.navigate(Routes.SEALED_PRODUCTS)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.Translate,
                label = "Guia de Efeitos (Traduzir PT-BR)",
                onClick = {
                    navController.navigate(Routes.EFFECTS_GUIDE)
                    onCloseDrawer()
                }
            )

            DrawerItem(
                icon = Icons.Default.CloudUpload,
                label = "Importar Catálogo (PDF/Excel)",
                onClick = {
                    navController.navigate(Routes.IMPORT_CATALOG)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("SISTEMA & CONFIGURAÇÕES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            DrawerItem(
                icon = Icons.Default.Settings,
                label = "Configurações & Chave de IA",
                badge = "IA",
                onClick = {
                    navController.navigate(Routes.SETTINGS)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald
                ) {
                    Text(
                        badge,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
