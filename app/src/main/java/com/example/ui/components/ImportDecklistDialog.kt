package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecklistParser
import com.example.data.PreconCardTemplate

@Composable
fun ImportDecklistDialog(
    initialGame: String = "Magic: The Gathering",
    onDismiss: () -> Unit,
    onImportCards: (List<PreconCardTemplate>) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedGame by remember { mutableStateOf(initialGame) }

    val parsedCards by remember(rawText, selectedGame) {
        derivedStateOf {
            DecklistParser.parseDecklistText(rawText, selectedGame)
        }
    }

    val totalCardCount by remember(parsedCards) {
        derivedStateOf {
            parsedCards.sumOf { it.quantity }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Colar Lista de Cartas (Decklist)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Cole a lista de cartas no formato padrão (ex: Arena, Moxfield, LigaMagic, Pokémon TCG Live).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilledTonalButton(
                            onClick = {
                                selectedGame = "Magic: The Gathering"
                                rawText = """
1 Sol Ring
1 Arcane Signet
1 Command Tower
1 Cultivate
1 Kodama's Reach
1 Swords to Plowshares
1 Lightning Greaves
1 Beast Within
4 Lightning Bolt
10 Mountain
10 Forest
8 Plains
                                """.trimIndent()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ex: MTG", fontSize = 11.sp)
                        }
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                selectedGame = "Magic: The Gathering"
                                rawText = """
1 Cloud, Planet's Champion
1 Barret Wallace
1 Beatrix, Loyal General
2 Adelbert Steiner
3 Cloudbound Moogle
1 Tifa Lockhart
1 Buster Sword
2 Samurai's Katana
2 Warrior's Sword
2 Fate of the Sun-Cryst
1 Rabanastre, Royal City
2 Lightning Bolt
1 Swords to Plowshares
1 Path to Exile
2 Boros Signet
1 Arcane Signet
1 Sol Ring
4 Wind-Scarred Crag
2 Boros Garrison
1 Command Tower
16 Mountain
13 Plains
                                """.trimIndent()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ex: Final Fantasy", fontSize = 11.sp)
                        }
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                selectedGame = "One Piece Card Game"
                                rawText = """
1 Monkey.D.Luffy
4 Roronoa Zoro
4 Nami
4 Tony Tony.Chopper
4 Sanji
4 Usopp
4 Brook
4 Nico Robin
4 Franky
4 Jinbe
4 Gum-Gum Jet Pistol
4 Diable Jambe
4 Guard Point
                                """.trimIndent()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ex: One Piece", fontSize = 11.sp)
                        }
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                selectedGame = "Yu-Gi-Oh!"
                                rawText = """
3 Fallen of Albaz
1 Mirrorjade the Iceblade Dragon
1 Lubellion the Searing Dragon
1 Albion the Branded Dragon
3 Branded Fusion
2 Tri-Brigade Mercourier
2 Springans Kitt
1 Ash Blossom & Joyous Spring
2 Effect Veiler
1 Ghost Ogre & Snow Rabbit
2 Dark Ruler No More
1 Pot of Extravagance
1 Called by the Grave
                                """.trimIndent()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ex: Yu-Gi-Oh!", fontSize = 11.sp)
                        }
                    }

                    item {
                        FilledTonalButton(
                            onClick = {
                                selectedGame = "Pokémon TCG"
                                rawText = """
3 Charizard ex
2 Pidgeot ex
4 Charmander
4 Raro Doce
4 Ultra Bola
4 Bola Ninho
3 Ordem da Chefia
16 Energia Básica de Fogo
                                """.trimIndent()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Ex: Pokémon", fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Lista de cartas (1x Nome da Carta por linha)") },
                    placeholder = {
                        Text(
                            "1 Sol Ring\n1 Command Tower\n4 Lightning Bolt\n24 Mountain...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    minLines = 6,
                    maxLines = 10
                )

                // Parsed Summary Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (parsedCards.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                if (parsedCards.isEmpty()) "Nenhuma carta identificada ainda"
                                else "$totalCardCount carta(s) em ${parsedCards.size} linha(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (rawText.isNotEmpty()) {
                            TextButton(onClick = { rawText = "" }) {
                                Text("Limpar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedCards.isNotEmpty()) {
                        onImportCards(parsedCards)
                    }
                },
                enabled = parsedCards.isNotEmpty()
            ) {
                Text("Adicionar $totalCardCount Carta(s)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
