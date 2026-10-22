package com.example.data

import com.example.api.ScryfallApiClient

data class CardRuleInfo(
    val name: String,
    val game: String,
    val cardType: String,
    val manaCostOrStats: String,
    val effectPt: String,
    val oracleEn: String = "",
    val strategyTip: String = "",
    val imageUrl: String = ""
)

object CardRulesEncyclopedia {

    private val database: Map<String, CardRuleInfo> = mapOf(
        // === FINAL FANTASY & MAGIC: THE GATHERING ===
        "Cloud, Planet's Champion" to CardRuleInfo(
            name = "Cloud, Planet's Champion",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Humano Soldado Herói",
            manaCostOrStats = "{3}{R}{W} • 4/4",
            effectPt = "Iniciativa, Vigilância.\nSempre que Cloud entra no campo de batalha ou ataca equipado com um Equipamento, você pode escolher um:\n• Procure em seu grimório por um card de Equipamento ou Criatura Lendária, revele-o e coloque-o em sua mão.\n• Cause dano igual ao poder de Cloud a qualquer alvo.\n• Suas criaturas equipadas ganham Golpe Duplo até o final do turno.",
            oracleEn = "First strike, vigilance. Whenever Cloud enters the battlefield or attacks while equipped, choose one: Search your library for an Equipment or Legendary creature card, or deal damage equal to Cloud's power to any target.",
            strategyTip = "O comandante e atacante supremo do deck. Equipe-o rapidamente com a Buster Sword ou Samurai's Katana para ativar sua busca e causar dano massivo nos oponentes.",
            imageUrl = ""
        ),
        "Sephiroth, Planet's Heir" to CardRuleInfo(
            name = "Sephiroth, Planet's Heir",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Humano Avatar Vilão",
            manaCostOrStats = "{3}{U}{B} • 5/5",
            effectPt = "Voar, Ameaçar.\nQuando Sephiroth entra no campo de batalha, destrua a criatura alvo que um oponente controla.\nSempre que uma criatura que um oponente controla morre, coloque um marcador +1/+1 em Sephiroth e cada oponente perde 2 pontos de vida.\n{4}{B}{B}: Conjure 'Supernova' — destrua todas as criaturas sem marcadores.",
            oracleEn = "Flying, menace. When Sephiroth enters, destroy target creature. Whenever a creature an opponent controls dies, put a +1/+1 counter on Sephiroth and each opponent loses 2 life.",
            strategyTip = "O líder de controle e finalizador. Use remoções e contramágicas para limpar a mesa e alimentar os marcadores de Sephiroth até finalizar a partida.",
            imageUrl = ""
        ),
        "Buster Sword" to CardRuleInfo(
            name = "Buster Sword",
            game = "Magic: The Gathering",
            cardType = "Artefato — Equipamento Lendário",
            manaCostOrStats = "{3} • Equipar {2}",
            effectPt = "A criatura equipada recebe +3/+1, Atropelar e 'Sempre que esta criatura causar dano de combate a um jogador, reduza em {2} o custo de mana da próxima mágica que você conjurar neste turno'.\nEquipar Lendário {1}.",
            oracleEn = "Equipped creature gets +3/+1, trample, and 'Whenever this creature deals combat damage to a player, reduce the mana cost of your next spell by {2}'.",
            strategyTip = "A lendária espada de Cloud e Zack. Concede atropelar para furar bloqueios e gera redução de custo a cada ataque bem-sucedido.",
            imageUrl = ""
        ),
        "Masamune" to CardRuleInfo(
            name = "Masamune",
            game = "Magic: The Gathering",
            cardType = "Artefato — Equipamento Lendário",
            manaCostOrStats = "{3} • Equipar {2}",
            effectPt = "A criatura equipada recebe +4/+0, Toque Mortífero e 'Criaturas que bloquearem ou forem bloqueadas por esta criatura não podem ser regeneradas e perdem Indestrutível até o final do turno'.",
            oracleEn = "Equipped creature gets +4/+0, deathtouch. Creatures blocking or blocked by this creature lose indestructible and cannot regenerate.",
            strategyTip = "A icônica katana de Sephiroth. Torna qualquer atacante letal e anula defesas indestrutíveis do oponente.",
            imageUrl = ""
        ),
        "Barret Wallace" to CardRuleInfo(
            name = "Barret Wallace",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Humano Rebelde",
            manaCostOrStats = "{2}{R} • 3/3",
            effectPt = "Alcance.\n{T}, Descarte um card: Barret causa 3 de dano a qualquer criatura com voar ou jogador. Se o card descartado for um Equipamento ou Artefato, desvire Barret.",
            oracleEn = "Reach. {T}, Discard a card: Barret deals 3 damage to any target with flying or player.",
            strategyTip = "Excelente controle de mesa e resposta contra criaturas voadoras inimigas.",
            imageUrl = ""
        ),
        "Beatrix, Loyal General" to CardRuleInfo(
            name = "Beatrix, Loyal General",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Humano Cavaleiro",
            manaCostOrStats = "{2}{W} • 3/3",
            effectPt = "Golpe Duplo, Vigilância.\nSuas outras criaturas Guerreiro e Cavaleiro recebem +1/+1. Sempre que Beatrix ataca sozinha, você ganha 3 pontos de vida.",
            oracleEn = "Double strike, vigilance. Other Knight and Warrior creatures you control get +1/+1.",
            strategyTip = "Uma potência em combate individual, ativando dano em dobro e protegendo seus pontos de vida.",
            imageUrl = ""
        ),
        "Ultimecia, Temporal Threat" to CardRuleInfo(
            name = "Ultimecia, Temporal Threat",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Feiticeira Vilã",
            manaCostOrStats = "{4}{U}{B} • 4/5",
            effectPt = "Voar.\nCompressão Temporal: Sempre que você conjurar sua segunda mágica a cada turno, exile a mágica do topo do grimório do oponente alvo. Você pode jogá-la sem pagar seu custo de mana.",
            oracleEn = "Flying. Time Compression: Whenever you cast your second spell each turn, exile the top card of target opponent's library. You may cast it without paying its mana cost.",
            strategyTip = "Gera vantagem de cartas avassaladora ao roubar os melhores feitiços e criaturas dos oponentes.",
            imageUrl = ""
        ),
        "Jenova, Ancient Calamity" to CardRuleInfo(
            name = "Jenova, Ancient Calamity",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Horror Alienígena",
            manaCostOrStats = "{3}{B}{B} • 5/4",
            effectPt = "Ameaçar.\nQuando Jenova entra no campo de batalha ou morre, cada oponente sacrifica uma criatura e descarta um card. Você ganha vida igual ao número de cards descartados dessa forma.",
            oracleEn = "Menace. When Jenova enters or dies, each opponent sacrifices a creature and discards a card.",
            strategyTip = "Força os oponentes a perderem recursos cruciais tanto na entrada quanto se tentarem destruí-la.",
            imageUrl = ""
        ),
        "Meteor" to CardRuleInfo(
            name = "Meteor",
            game = "Magic: The Gathering",
            cardType = "Feitiço Lendário — Calamidade",
            manaCostOrStats = "{5}{B}{B}",
            effectPt = "Destrua todas as criaturas e terrenos não básicos. Cada jogador perde 5 pontos de vida. Se você controlar Sephiroth, esta mágica não pode ser anulada.",
            oracleEn = "Destroy all creatures and nonbasic lands. Each player loses 5 life. If you control Sephiroth, this spell can't be countered.",
            strategyTip = "O botão de reset definitivo para garantir que apenas Sephiroth e sua presença dominem a mesa.",
            imageUrl = ""
        ),
        "Tifa Lockhart" to CardRuleInfo(
            name = "Tifa Lockhart",
            game = "Magic: The Gathering",
            cardType = "Criatura Lendária — Humano Monge Guerreira",
            manaCostOrStats = "{2}{R}{W} • 4/3",
            effectPt = "Ímpeto, Iniciativa.\nSempre que Tifa causa dano de combate a um jogador, coloque um marcador de Combo nela. Para cada marcador de Combo, criaturas atacantes que você controla recebem +1/+0.",
            oracleEn = "Haste, first strike. Whenever Tifa deals combat damage to a player, put a Combo counter on her.",
            strategyTip = "Atacante rápida de curva média que acelera o poder de ataque de todo o exército a cada turno.",
            imageUrl = ""
        ),
        "Sol Ring" to CardRuleInfo(
            name = "Sol Ring",
            game = "Magic: The Gathering",
            cardType = "Artefato",
            manaCostOrStats = "{1}",
            effectPt = "{T}: Adicione {C}{C} (dois mana incolores).\nO artefato de aceleração mais icônico de todo o Magic.",
            oracleEn = "{T}: Add {C}{C}.",
            strategyTip = "Permite jogar mágicas de custo 4 no turno 2, garantindo liderança precoce na partida.",
            imageUrl = ""
        ),
        "Arcane Signet" to CardRuleInfo(
            name = "Arcane Signet",
            game = "Magic: The Gathering",
            cardType = "Artefato",
            manaCostOrStats = "{2}",
            effectPt = "{T}: Adicione um mana de qualquer cor na identidade de cor do seu comandante.",
            oracleEn = "{T}: Add one mana of any color in your commander's color identity.",
            strategyTip = "Garante correção de mana perfeita para decks bicolores ou multicolores.",
            imageUrl = ""
        ),
        "Command Tower" to CardRuleInfo(
            name = "Command Tower",
            game = "Magic: The Gathering",
            cardType = "Terreno",
            manaCostOrStats = "Terreno",
            effectPt = "{T}: Adicione um mana de qualquer cor na identidade de cor do seu comandante.",
            oracleEn = "{T}: Add one mana of any color in your commander's color identity.",
            strategyTip = "Entra desvirado e gera qualquer cor necessária sem penalidades.",
            imageUrl = ""
        ),

        // === YU-GI-OH! TCG ===
        "Fallen of Albaz" to CardRuleInfo(
            name = "Fallen of Albaz",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Efeito — Dragão / Escuridão",
            manaCostOrStats = "Nível 4 • ATK 1800 / DEF 0",
            effectPt = "Se este card for Invocado por Invocação-Normal ou Especial (exceto durante a Etapa de Dano): você pode descartar 1 card; Invoque por Invocação-Fusão 1 Monstro de Fusão do seu Deck Adicional, usando monstros em qualquer campo como Matéria de Fusão, incluindo este card.",
            oracleEn = "If normal or special summoned: discard 1 card; Fusion Summon 1 Fusion Monster using monsters on either field, including this card.",
            strategyTip = "O motor central do deck Branded. Use o monstro do oponente como matéria de fusão para quebrar defesas e invocar Mirrorjade ou Lubellion.",
            imageUrl = ""
        ),
        "Mirrorjade the Iceblade Dragon" to CardRuleInfo(
            name = "Mirrorjade the Iceblade Dragon",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Fusão / Efeito — Wyrm / Escuridão",
            manaCostOrStats = "Nível 10 • ATK 3000 / DEF 2500",
            effectPt = "(Efeito Rápido): Você pode enviar 1 Monstro de Fusão do seu Deck Adicional para o Cemitério que mencione 'Caído de Albaz'; bana 1 monstro no campo.\nSe este card com a face para cima sob o controle do seu dono for destruído pelo adversário: destrua todos os monstros que ele controla durante a Fase Final.",
            oracleEn = "Quick Effect: Send 1 Fusion Monster that mentions 'Fallen of Albaz' from Extra Deck to GY; banish 1 monster on the field. If leaves the field by opponent: wipe enemy field at End Phase.",
            strategyTip = "O chefe absoluto do deck Albaz. Bana monstros sem destruir e puna o adversário caso ele tente tirá-lo de campo.",
            imageUrl = ""
        ),
        "Branded Fusion" to CardRuleInfo(
            name = "Branded Fusion",
            game = "Yu-Gi-Oh!",
            cardType = "Card de Magia Normal",
            manaCostOrStats = "Magia Normal",
            effectPt = "Invoque por Invocação-Fusão 1 Monstro de Fusão que liste 'Caído de Albaz' como matéria do seu Deck Adicional, usando 2 monstros da sua mão, Deck ou campo como Matéria de Fusão. Pelo restante deste turno, você não pode Invocar monstros do Deck Adicional, exceto Monstros de Fusão.",
            oracleEn = "Fusion Summon 1 Fusion Monster that mentions 'Fallen of Albaz' using 2 materials from your hand, Deck, or field.",
            strategyTip = "A melhor carta de fusão do jogo. Funda diretamente do Deck enviando Albaz e monstros poderosos como Tragédia Despian ou Dragão da Luz.",
            imageUrl = ""
        ),
        "Lubellion the Searing Dragon" to CardRuleInfo(
            name = "Lubellion the Searing Dragon",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Fusão / Efeito — Dragão / Luz",
            manaCostOrStats = "Nível 8 • ATK 2500 / DEF 3000",
            effectPt = "Se este card for Invocado por Fusão: descarte 1 card; Invoque por Fusão 1 Monstro de Fusão de Nível 8 ou menor do seu Deck Adicional embaralhando matérias do seu campo, Cemitério e/ou banidos de volta ao Deck.",
            oracleEn = "If Fusion Summoned: discard 1; Fusion Summon by shuffling materials from field, GY, or banishment into Deck.",
            strategyTip = "Degrau intermediário perfeito para reciclar Albaz e trazer imediatamente o Mirrorjade.",
            imageUrl = ""
        ),
        "Ash Blossom & Joyous Spring" to CardRuleInfo(
            name = "Ash Blossom & Joyous Spring",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro Regulador / Efeito — Zumbi / Fogo",
            manaCostOrStats = "Nível 3 • ATK 0 / DEF 1800",
            effectPt = "(Efeito Rápido): Quando o oponente ativar um efeito que adicione um card do Deck à mão, Invoque por Invocação-Especial do Deck ou envie um card do Deck para o Cemitério: descarte este card; negue esse efeito.",
            oracleEn = "Discard this card from hand to negate an effect that touches the deck (add, summon, or send to GY).",
            strategyTip = "A 'Hand Trap' defensiva mais importante do jogo para parar o turno do adversário antes que ele inicie combos.",
            imageUrl = ""
        ),
        "Fire King High Avatar Garunix" to CardRuleInfo(
            name = "Fire King High Avatar Garunix",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Efeito — Besta Alada / Fogo",
            manaCostOrStats = "Nível 8 • ATK 2700 / DEF 1700",
            effectPt = "Se este card for destruído por efeito de card e enviado ao Cemitério: Invoque-o por Invocação-Especial durante a próxima Fase de Apoio. Quando Invocado desta forma: destrua todos os outros monstros no campo.\nQuando for destruído em batalha: Invoque 1 monstro 'Reis de Fogo' do Deck.",
            oracleEn = "If destroyed by card effect: Special Summon next Standby Phase, then wipe all other monsters on the field.",
            strategyTip = "O ciclo infinito de destruição. Destrua Garunix com seus próprios efeitos para limpar o campo todo turno.",
            imageUrl = ""
        ),
        "Legendary Fire King Ponix" to CardRuleInfo(
            name = "Legendary Fire King Ponix",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Efeito — Besta Alada / Fogo",
            manaCostOrStats = "Nível 1 • ATK 500 / DEF 200",
            effectPt = "Se seu monstro de FOGO for destruído: Invoque este card por Invocação-Especial da mão. Se Invocado: adicione 1 Magia/Armadilha 'Reis de Fogo' do Deck à mão. Durante a próxima Fase de Apoio após ser destruído: adicione este card do Cemitério à mão.",
            oracleEn = "Special Summon if FIRE monster is destroyed. Search 1 Fire King Spell/Trap. Recycles to hand.",
            strategyTip = "O buscador inicial e reciclador contínuo do deck Fire Kings.",
            imageUrl = ""
        ),
        "Grapha, Dragon Lord of Dark World" to CardRuleInfo(
            name = "Grapha, Dragon Lord of Dark World",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro de Efeito — Demônio / Escuridão",
            manaCostOrStats = "Nível 8 • ATK 2700 / DEF 1800",
            effectPt = "Você pode Invocar este card por Invocação-Especial do Cemitério retornando 1 monstro 'Mundo Sombrio' do campo para a mão.\nSe este card for descartado por efeito de card: destrua 1 card que seu oponente controla.",
            oracleEn = "Special summon from GY by bouncing 1 Dark World monster. If discarded: destroy 1 card opponent controls.",
            strategyTip = "Invocação gratuita recursiva do cemitério que destrói cartas do oponente a cada descarte.",
            imageUrl = ""
        ),
        "Red Dragon Archfiend" to CardRuleInfo(
            name = "Red Dragon Archfiend",
            game = "Yu-Gi-Oh!",
            cardType = "Monstro Sincro / Efeito — Dragão / Escuridão",
            manaCostOrStats = "Nível 8 • ATK 3000 / DEF 2000",
            effectPt = "1 Regulador + 1+ monstros não-Reguladores.\nDepois do cálculo de dano, se este card atacou um monstro em Posição de Defesa do oponente: destrua todos os monstros em Posição de Defesa que seu adversário controla.\nDurante a sua Fase Final: destrua todos os outros monstros que você controla que não declararam um ataque neste turno.",
            oracleEn = "Destroys all defense position monsters opponent controls after attacking one. 3000 ATK powerhouse.",
            strategyTip = "Ataque impiedoso contra monstros defensivos do adversário, base do deck de Jack Atlas.",
            imageUrl = ""
        ),

        // === ONE PIECE CARD GAME ===
        "Monkey.D.Luffy" to CardRuleInfo(
            name = "Monkey.D.Luffy",
            game = "One Piece Card Game",
            cardType = "Líder / Personagem — Vermelho",
            manaCostOrStats = "Vida 5 • Poder 5000 • Atributo Golpe",
            effectPt = "[Ativar: Principal] [1 vez por turno] Você pode dar a este Líder ou a 1 dos seus Personagens até 1 carta de DON!! descansada.\n[DON!! x2] [Ao Atacar] Este Personagem ganha <Rush> e +1000 de Poder durante este turno.",
            oracleEn = "[Activate: Main] [Once Per Turn] Give this Leader or 1 of your Characters up to 1 rested DON!! card. [DON!! x2] [When Attacking] Gains Rush.",
            strategyTip = "Acelera cartas de DON!! para atacar com força máxima todo turno, colocando pressão implacável na Vida do oponente.",
            imageUrl = ""
        ),
        "Roronoa Zoro" to CardRuleInfo(
            name = "Roronoa Zoro",
            game = "One Piece Card Game",
            cardType = "Personagem — Vermelho (Bando do Chapéu de Palha)",
            manaCostOrStats = "Custo 3 • Poder 5000 • Counter +1000",
            effectPt = "<Rush> (Este Personagem pode atacar no mesmo turno em que é jogado).\n[DON!! x1] Este Personagem recebe +1000 de Poder durante o seu turno.",
            oracleEn = "<Rush>. [DON!! x1] This Character gets +1000 power during your turn.",
            strategyTip = "O melhor atacante veloz do jogo. Entre atacando direto no Líder do oponente no turno 2.",
            imageUrl = ""
        ),
        "Nami" to CardRuleInfo(
            name = "Nami",
            game = "One Piece Card Game",
            cardType = "Personagem — Vermelho (Bando do Chapéu de Palha)",
            manaCostOrStats = "Custo 1 • Poder 1000 • Counter +1000",
            effectPt = "[Ao Jogar] Olhe até 5 cartas do topo do seu deck; revele até 1 carta com a característica {Bando do Chapéu de Palha} diferente de [Nami] e adicione-a à sua mão. Em seguida, coloque as demais no fundo do deck.",
            oracleEn = "[On Play] Look at 5 cards from the top of your deck; reveal up to 1 {Straw Hat Crew} type card other than [Nami] and add it to your hand.",
            strategyTip = "A buscadora principal de turno 1 para encontrar Luffy, Zoro, Chopper ou Jet Pistol.",
            imageUrl = ""
        ),
        "Trafalgar Law" to CardRuleInfo(
            name = "Trafalgar Law",
            game = "One Piece Card Game",
            cardType = "Líder — Vermelho / Roxo (Piratas do Coração)",
            manaCostOrStats = "Vida 4 • Poder 5000 • Atributo Corte",
            effectPt = "[Ativar: Principal] [DON!! -3] Coloque até 1 Personagem do seu adversário com Poder de 3000 ou menos no fundo do deck do seu dono e, em seguida, jogue até 1 Personagem com custo 4 ou menor da sua mão.",
            oracleEn = "[Activate: Main] [DON!! -3] Place 1 opponent character with 3000 power or less to bottom of deck, then play 1 cost 4 or less character from hand.",
            strategyTip = "Remoção de mesa aliada a desenvolvimento simultâneo de criaturas. Uma das maiores forças de controle do jogo.",
            imageUrl = ""
        ),
        "Eustass \"Captain\" Kid" to CardRuleInfo(
            name = "Eustass \"Captain\" Kid",
            game = "One Piece Card Game",
            cardType = "Líder — Verde (Piratas do Kid)",
            manaCostOrStats = "Vida 5 • Poder 5000 • Atributo Especial",
            effectPt = "[Ao Atacar] [DON!! -1] e descarte 1 card da mão: Coloque este Líder como ativo.\n(Permite ao Líder atacar duas vezes no mesmo turno com poder amplificado de DON!!).",
            oracleEn = "[When Attacking] [DON!! -1] and discard 1 card: Set this Leader as active (attacks twice per turn).",
            strategyTip = "Permite desferir dois ataques fulminantes com o Líder todo turno, drenando rapidamente os bloqueadores e contadores da mão adversária.",
            imageUrl = ""
        ),
        "Portgas.D.Ace" to CardRuleInfo(
            name = "Portgas.D.Ace",
            game = "One Piece Card Game",
            cardType = "Líder — Amarelo (Três Irmãos)",
            manaCostOrStats = "Vida 4 • Poder 5000 • Atributo Especial",
            effectPt = "[Ao Atacar] Você pode olhar as cartas da sua Vida e reordená-las. Se você tiver 2 ou menos cartas de Vida, este Líder e todos os seus Personagens ganham +1000 de Poder durante este turno.",
            oracleEn = "[When Attacking] Look and reorder your Life. If at 2 or less Life, gains +1000 power.",
            strategyTip = "Manipula gatilhos (Triggers) secretos na Vida para surpreender o oponente durante ataques.",
            imageUrl = ""
        ),
        "Sabo" to CardRuleInfo(
            name = "Sabo",
            game = "One Piece Card Game",
            cardType = "Personagem — Amarelo (Exército Revolucionário)",
            manaCostOrStats = "Custo 5 • Poder 6000 • <Bloqueador>",
            effectPt = "<Bloqueador> (Após o oponente declarar ataque, você pode descansar este card para torná-lo o alvo).\n[Ao Jogar] Compre 2 cartas e descarte 2 cartas. Até o final do próximo turno do seu adversário, seus Personagens não podem ser nocauteados por efeitos.",
            oracleEn = "<Blocker>. [On Play] Draw 2, trash 2. Your characters cannot be K.O.'d by effects until end of opponent's next turn.",
            strategyTip = "O defensor perfeito. Protege todo o seu campo contra remoções enquanto filtra sua mão.",
            imageUrl = ""
        ),
        "Gum-Gum Jet Pistol" to CardRuleInfo(
            name = "Gum-Gum Jet Pistol",
            game = "One Piece Card Game",
            cardType = "Card de Evento — Vermelho",
            manaCostOrStats = "Custo 4",
            effectPt = "[Principal] Nocauteie até 1 Personagem do seu adversário com Poder de 6000 ou menos.\n[Gatilho (Trigger)] Ative o efeito [Principal] deste card imediatamente sem pagar o custo.",
            oracleEn = "[Main] K.O. up to 1 of your opponent's Characters with 6000 power or less. [Trigger] Activate this card's Main effect.",
            strategyTip = "A principal remoção vermelha contra ameaças de meio de jogo. Letal quando ativada direto da Vida.",
            imageUrl = ""
        ),

        // === POKÉMON TCG ===
        "Charizard ex" to CardRuleInfo(
            name = "Charizard ex",
            game = "Pokémon TCG",
            cardType = "Pokémon ex — Estágio 2 (Tera Escuridão)",
            manaCostOrStats = "HP 330 • Fraqueza: Grama",
            effectPt = "Habilidade: Reino Infernal.\nQuando você joga este Pokémon da sua mão para evoluir 1 dos seus Pokémon durante o seu turno, você pode procurar no seu baralho por até 3 cartas de Energia Básica de Fogo e ligá-las aos seus Pokémon como desejar.\nAtaque: Explosão Ardente (180+ de dano):\nCausa 30 de dano a mais para cada carta de Prêmio que o seu oponente pegou.",
            oracleEn = "Ability: Infernal Reign. Search deck for up to 3 Basic Fire Energy and attach. Burning Darkness (180+): Does 30 more damage for each Prize card opponent has taken.",
            strategyTip = "O atacante de virada mais temido do formato. Conforme o adversário pega prêmios, seu ataque escala até mais de 330 de dano, nocauteando qualquer Pokémon ex com 1 golpe.",
            imageUrl = ""
        ),
        "Gardevoir ex" to CardRuleInfo(
            name = "Gardevoir ex",
            game = "Pokémon TCG",
            cardType = "Pokémon ex — Estágio 2 (Psíquico)",
            manaCostOrStats = "HP 315 • Fraqueza: Escuridão",
            effectPt = "Habilidade: Abraço Psíquico.\nQuantas vezes desejar durante o seu turno, você pode ligar 1 carta de Energia Básica Psíquica da sua pilha de descarte a 1 dos seus Pokémon Psíquicos. Se fizer isso, coloque 2 contadores de dano naquele Pokémon.\nAtaque: Força Milagrosa (190 de dano):\nCura todas as Condições Especiais deste Pokémon.",
            oracleEn = "Ability: Psychic Embrace. Attach basic Psychic Energy from discard as often as you like, placing 2 damage counters. Miracle Force: 190 damage.",
            strategyTip = "Energização infinita vinda do descarte para alimentar atacantes psíquicos como Scream Tail, Drifloon e Mew ex.",
            imageUrl = ""
        ),
        "Miraidon ex" to CardRuleInfo(
            name = "Miraidon ex",
            game = "Pokémon TCG",
            cardType = "Pokémon ex — Básico (Elétrico)",
            manaCostOrStats = "HP 220 • Fraqueza: Luta",
            effectPt = "Habilidade: Unidade Tandem.\nUma vez durante o seu turno, você pode procurar no seu baralho por até 2 Pokémon Básicos de Energia Elétrica e colocá-los no seu Banco.\nAtaque: Impulso de Fótons (220 de dano):\nDurante o seu próximo turno, este Pokémon não pode atacar.",
            oracleEn = "Ability: Tandem Unit. Search deck for up to 2 Basic Lightning Pokémon and put them onto your Bench.",
            strategyTip = "Garante consistência absoluta de turno 1, enchendo o banco instantaneamente sem gastar cartas de Apoiador.",
            imageUrl = ""
        ),
        "Pidgeot ex" to CardRuleInfo(
            name = "Pidgeot ex",
            game = "Pokémon TCG",
            cardType = "Pokémon ex — Estágio 2 (Incolor)",
            manaCostOrStats = "HP 280 • Fraqueza: Elétrico",
            effectPt = "Habilidade: Busca Veloz.\nUma vez durante o seu turno, você pode procurar no seu baralho por qualquer 1 carta e colocá-la na sua mão. Em seguida, embaralhe o seu baralho.\nAtaque: Ventania Cortante (120 de dano).",
            oracleEn = "Ability: Quick Search. Search your deck for any 1 card and add it to your hand once per turn.",
            strategyTip = "O motor de busca perfeito: coloque em campo e pegue a carta exata que você precisa todo turno (Ordem da Chefia, Iono, Raro Doce).",
            imageUrl = ""
        )
    )

    fun getCardInfo(cardName: String, gameHint: String = "Magic: The Gathering"): CardRuleInfo {
        database[cardName]?.let { return it }

        val clean = cardName.trim().lowercase()
        val entry = database.entries.find { (key, _) ->
            key.lowercase() == clean || clean.contains(key.lowercase()) || key.lowercase().contains(clean)
        }
        if (entry != null) {
            val base = entry.value
            return if (base.imageUrl.isNotBlank()) base
            else base.copy(imageUrl = CardImageResolver.resolveImageUrl(base.name, base.game))
        }

        val g = when {
            gameHint.contains("One Piece", ignoreCase = true) -> "One Piece Card Game"
            gameHint.contains("Yu-Gi-Oh", ignoreCase = true) || gameHint.contains("Yugioh", ignoreCase = true) -> "Yu-Gi-Oh!"
            gameHint.contains("Pok", ignoreCase = true) -> "Pokémon TCG"
            else -> "Magic: The Gathering"
        }

        val type = when {
            g == "Magic: The Gathering" && (clean.contains("land") || clean.contains("terreno") || clean.contains("mountain") || clean.contains("island") || clean.contains("forest") || clean.contains("plains") || clean.contains("swamp")) -> "Terreno Básico"
            g == "Magic: The Gathering" && (clean.contains("sword") || clean.contains("blade") || clean.contains("katana") || clean.contains("ring") || clean.contains("signet")) -> "Artefato"
            g == "Magic: The Gathering" && (clean.contains("bolt") || clean.contains("spell") || clean.contains("path") || clean.contains("swords")) -> "Mágica Instantânea"
            g == "Yu-Gi-Oh!" && (clean.contains("dragon") || clean.contains("albaz") || clean.contains("garunix")) -> "Monstro de Efeito"
            g == "Yu-Gi-Oh!" && (clean.contains("fusion") || clean.contains("magia")) -> "Card de Magia"
            g == "One Piece Card Game" && (clean.contains("luffy") || clean.contains("zoro") || clean.contains("law")) -> "Personagem"
            g == "One Piece Card Game" && clean.contains("pistol") -> "Card de Evento"
            g == "Pokémon TCG" && (clean.contains("energia") || clean.contains("energy")) -> "Energia Básica"
            g == "Pokémon TCG" && (clean.contains("bola") || clean.contains("doce") || clean.contains("ordem") || clean.contains("professor")) -> "Treinador / Item"
            else -> "Carta de Baralho ($g)"
        }

        return CardRuleInfo(
            name = cardName,
            game = g,
            cardType = type,
            manaCostOrStats = "Padrão de Jogo",
            effectPt = "Esta carta integra a estratégia principal do baralho. Utilize-a em conjunto com as peças de combo e aceleração para maximizar seu impacto em campo e avançar rumo à condição de vitória.",
            oracleEn = "Card included in this deck strategy.",
            strategyTip = "Peça fundamental para a curva e sinergia do deck.",
            imageUrl = CardImageResolver.resolveImageUrl(cardName, g)
        )
    }

    suspend fun fetchScryfallEnrichment(cardName: String): CardRuleInfo? {
        return try {
            val match = ScryfallApiClient.getBestCardMatch(cardName) ?: return null
            CardRuleInfo(
                name = match.name,
                game = "Magic: The Gathering",
                cardType = match.typeLine ?: "Mágica / Criatura",
                manaCostOrStats = match.manaCost ?: match.typeLine ?: "Padrão",
                effectPt = match.oracleText ?: "Habilidade oficial ativa de acordo com as regras de Magic.",
                oracleEn = match.oracleText ?: "",
                strategyTip = "Coleção: [${match.setCode.uppercase()}] ${match.setName} • N° ${match.collectorNumber} • Raridade: ${match.rarity}",
                imageUrl = match.imageUrlLarge
            )
        } catch (_: Throwable) {
            null
        }
    }
}
