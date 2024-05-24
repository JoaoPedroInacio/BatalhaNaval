import java.io.File

const val MENU_PRINCIPAL = 100
const val MENU_DEFINIR_TABULEIRO = 101
const val MENU_DEFINIR_NAVIOS = 102
const val MENU_JOGAR = 103
const val MENU_LER_FICHEIRO = 104
const val MENU_GRAVAR_FICHEIRO = 105
const val SAIR = 106

var numLinhas = -1
var numColunas = -1

var tabuleiroHumano: Array<Array<Char?>> = emptyArray()
var tabuleiroComputador: Array<Array<Char?>> = emptyArray()

var tabuleiroPalpitesDoHumano: Array<Array<Char?>> = emptyArray()
var tabuleiroPalpitesDoComputador: Array<Array<Char?>> = emptyArray()

var menuJogarOk = 0

fun menuPrincipal(): Int {
    println()
    println("""> > Batalha Naval < <
        |
        |1 - Definir Tabuleiro e Navios
        |2 - Jogar
        |3 - Gravar
        |4 - Ler
        |0 - Sair
        |
    """.trimMargin())
    var menuEscolhido: Int?
    do {
        menuEscolhido = readln().toIntOrNull()
        if (menuEscolhido == null || menuEscolhido >= 5 || menuEscolhido < -1) {
            println("!!! Opcao invalida, tente novamente")
        }
    } while (menuEscolhido == null || menuEscolhido >= 5 || menuEscolhido < -1)

    when (menuEscolhido) {
        1 -> return MENU_DEFINIR_TABULEIRO
        2 -> return MENU_JOGAR
        3 -> return MENU_GRAVAR_FICHEIRO
        4 -> return MENU_LER_FICHEIRO
        -1 -> return MENU_PRINCIPAL
        0 -> return SAIR
    }
    println()
    return menuEscolhido
}

fun menuDefinirTabuleiro(): Int {
    println()
    println("""> > Batalha Naval < <
        |
        |Defina o tamanho do tabuleiro:
    """.trimMargin())

    numLinhas = pedeLinhas()
    when (numLinhas) {
        -1 -> return MENU_PRINCIPAL
        0 -> return SAIR
    }
    numColunas = pedeColunas()
    when (numColunas) {
        -1 -> return MENU_PRINCIPAL
        0 -> return SAIR
    }
    if (!tamanhoTabuleiroValido(numLinhas, numColunas)) {
        println("!!! Tamanho de tabuleiro invalido")
        return MENU_DEFINIR_TABULEIRO
    }


    tabuleiroHumano = criaTabuleiroVazio(numLinhas, numColunas)
    tabuleiroComputador = criaTabuleiroVazio(numLinhas, numColunas)
    tabuleiroPalpitesDoComputador = criaTabuleiroVazio(numLinhas, numColunas)
    tabuleiroPalpitesDoHumano = criaTabuleiroVazio(numLinhas, numColunas)


    return MENU_DEFINIR_NAVIOS
}

fun menuDefinirNavios(): Int {

    var orientacao: String? = "E"

    var mapaHumano = obtemMapa(tabuleiroHumano, true)
    for (linha in mapaHumano) println(linha)

    val numDeNavios = calculaNumNavios(numLinhas, numColunas)
    val tiposDeNavios = arrayOf("submarino", "contra-torpedeiro", "navio-tanque", "porta-avioes")


    for (posicao in 0 until numDeNavios.size) { // 0,1,2,3
        if (numDeNavios[posicao] != 0) {
            for (count in numDeNavios[posicao] downTo 1) { //0 até numero de vezes que esse navio tem de ser inserido
                var sucessoDaInsercao = false
                var coordenadas: String?

                do {
                    println("""Insira as coordenadas de um ${tiposDeNavios[posicao]}:
        |Coordenadas? (ex: 6,G)
    """.trimMargin())

                    do {
                        coordenadas = readlnOrNull()
                        if (coordenadas != null && coordenadas != "") {
                            if (coordenadas.toIntOrNull() == -1) return MENU_PRINCIPAL
                            if (coordenadas.toIntOrNull() == 0) return SAIR
                        }
                        val validadeDasCoordenadas = processaCoordenadas(coordenadas, numLinhas, numColunas)
                                ?: Pair(-1, -1)
                    } while (validadeDasCoordenadas == Pair(-1, -1))

                    val coordenadasNum = processaCoordenadas(coordenadas, numLinhas, numColunas)
                            ?: Pair(-1, -1) // retorna um Pair em coordenadas normais

                    if (posicao > 0) {
                        println("""Insira a orientacao do navio:
            |Orientacao? (N, S, E, O)
        """.trimMargin())

                        orientacao = pedeOrientacaoEValida()
                        when {
                            orientacao.toIntOrNull() == -1 -> return MENU_PRINCIPAL
                            orientacao.toIntOrNull() == 0 -> return SAIR
                        }
                    }
                    if (orientacao != null) {
                        sucessoDaInsercao = insereNavio(tabuleiroHumano, coordenadasNum.first, coordenadasNum.second, orientacao, posicao + 1)
                        if (!sucessoDaInsercao) println("!!! Posicionamento invalido, tente novamente")
                    }
                } while (!sucessoDaInsercao)

                mapaHumano = obtemMapa(tabuleiroHumano, true)
                for (linha in mapaHumano) println(linha)
            }
        }
    }

    preencheTabuleiroComputador(tabuleiroComputador, calculaNumNavios(numLinhas, numColunas))
    val mapaComputador = obtemMapa(tabuleiroComputador, true)
    println("Pretende ver o mapa gerado para o Computador? (S/N)")
    do {
        var resposta: String? = readlnOrNull()
        when {
            resposta == "-1" -> return MENU_PRINCIPAL
            resposta == "0" -> return SAIR
            resposta == null || resposta != "S" && resposta != "N" -> {
                println("!!!Resposta invalida, responda S ou N)")
                resposta = null
            }

            resposta == "S" -> for (linha in mapaComputador) println(linha)
        }
    } while (resposta == null)

    menuJogarOk = 1

    return MENU_PRINCIPAL
}

fun menuJogar(): Int {

    if (menuJogarOk == 1) {

        while (true) {
            val mapaPalpitesHumano = obtemMapa(tabuleiroPalpitesDoHumano, false)
            for (linha in mapaPalpitesHumano) println(linha)

            println("""Indique a posição que pretende atingir
        |Coordenadas? (ex: 6,G)
    """.trimMargin())

            var tiro: String?

            do {
                tiro = readlnOrNull()
                if (tiro != null && tiro != "") {
                    if (tiro.toIntOrNull() == -1) return MENU_PRINCIPAL
                    if (tiro.toIntOrNull() == 0) return SAIR
                }
            } while (processaCoordenadas(tiro, numLinhas, numColunas) == null)

            val conversaoTiroHumano = processaCoordenadas(tiro, numLinhas, numColunas) ?: Pair(-1, -1)


            print(">>> HUMANO >>>${lancarTiro(tabuleiroComputador, tabuleiroPalpitesDoHumano, conversaoTiroHumano)}")
            if (navioCompleto(tabuleiroPalpitesDoHumano, conversaoTiroHumano.first, conversaoTiroHumano.second)) {
                println(" Navio ao fundo!")
            } else println()
            if (venceu(tabuleiroPalpitesDoHumano)) {
                println("PARABENS! Venceu o jogo!")
                do {
                    println("Prima enter para voltar ao menu principal")
                    val continua: String? = readlnOrNull()
                    if (continua != null && continua != "") {
                        if (continua.toIntOrNull() == -1) return MENU_PRINCIPAL
                        if (continua.toIntOrNull() == 0) return SAIR
                    }
                } while (continua != "")
                return MENU_PRINCIPAL
            }
            val tiroDoComputador = geraTiroComputador(tabuleiroPalpitesDoComputador)
            println("Computador lancou tiro para a posicao $tiroDoComputador")
            println(">>> COMPUTADOR >>>${lancarTiro(tabuleiroHumano, tabuleiroPalpitesDoComputador, tiroDoComputador)}")

            if (venceu(tabuleiroPalpitesDoComputador)) {
                println("OPS! O computador venceu o jogo!")
                do {
                    println("Prima enter para voltar ao menu principal")
                    val continua: String? = readlnOrNull()
                    if (continua != null && continua != "") {
                        if (continua.toIntOrNull() == -1) return MENU_PRINCIPAL
                        if (continua.toIntOrNull() == 0) return SAIR
                    }
                } while (continua != "")
            }

            do {
                println("Prima enter para continuar")
                val continua: String? = readlnOrNull()
                if (continua != null && continua != "") {
                    if (continua.toIntOrNull() == -1) return MENU_PRINCIPAL
                    if (continua.toIntOrNull() == 0) return SAIR
                }
            } while (continua != "")
        }

    } else {
        print("!!! Tem que primeiro definir o tabuleiro do jogo, tente novamente")
    }
    println()
    return MENU_PRINCIPAL
}

fun menuLerFicheiro(): Int {

    var nomeDoFicheiro: String?

    do {
        println("Introduza o nome do ficheiro (ex: jogo.txt)")
        nomeDoFicheiro = readlnOrNull()
        if (nomeDoFicheiro != null && nomeDoFicheiro != "") {
            if (nomeDoFicheiro.toIntOrNull() == -1) return MENU_PRINCIPAL
            if (nomeDoFicheiro.toIntOrNull() == 0) return SAIR
        }
    } while (nomeDoFicheiro == null || nomeDoFicheiro == "")

    tabuleiroHumano = lerJogo(nomeDoFicheiro, 1)
    println("Tabuleiro ${numLinhas}x${numColunas} lido com sucesso")
    val mapaHumano = obtemMapa(tabuleiroHumano, true)
    for (linha in mapaHumano) println(linha)


    tabuleiroPalpitesDoHumano = lerJogo(nomeDoFicheiro, 2)
    tabuleiroComputador = lerJogo(nomeDoFicheiro, 3)
    tabuleiroPalpitesDoComputador = lerJogo(nomeDoFicheiro, 4)


    return MENU_PRINCIPAL
}

fun menuGravarFicheiro(): Int {

    println("Introduza o nome do ficheiro (ex: jogo.txt)")
    var nomeDoFicheiro: String?
    do {
        nomeDoFicheiro = readlnOrNull()
        if (nomeDoFicheiro != null && nomeDoFicheiro != "") {
            if (nomeDoFicheiro.toIntOrNull() == -1) return MENU_PRINCIPAL
            if (nomeDoFicheiro.toIntOrNull() == 0) return SAIR
        }
    } while (nomeDoFicheiro == null)

    gravarJogo(nomeDoFicheiro, tabuleiroHumano, tabuleiroPalpitesDoHumano, tabuleiroComputador, tabuleiroPalpitesDoComputador)

    println("Tabuleiro ${numLinhas}x$numColunas gravado com sucesso")

    return MENU_PRINCIPAL
}

fun tamanhoTabuleiroValido(numLinhas: Int?, numColunas: Int?): Boolean {
    return when {
        numColunas == 4 && numLinhas == 4 -> true
        numColunas == 5 && numLinhas == 5 -> true
        numColunas == 7 && numLinhas == 7 -> true
        numColunas == 8 && numLinhas == 8 -> true
        numColunas == 10 && numLinhas == 10 -> true
        else -> false
    }
}

fun pedeOrientacaoEValida(): String {
    var orientacao: String?
    val orientacaoValida = arrayOf("E", "S", "O", "N", "0", "-1")
    do {
        orientacao = readlnOrNull()
        when {
            orientacao == null || orientacao !in (orientacaoValida) -> {
                orientacao = null
                println("""!!! Orientacao invalida, tente novamente
                                  |Orientacao? (N, S, E, O)
                                     """.trimMargin())
            }
        }
    } while (orientacao == null)
    return orientacao
}

fun pedeLinhas(): Int {

    var linhas: Int?
    do {
        println("Quantas linhas?")
        linhas = readln().toIntOrNull()
        if (linhas == null) println("!!! Número de linhas invalidas, tente novamente")
    } while (linhas == null)
    return linhas
}

fun pedeColunas(): Int {
    var colunas: Int?
    do {
        println("Quantas colunas?")
        colunas = readln().toIntOrNull()
        if (colunas == null) println("!!! Número de colunas invalidas, tente novamente")
    } while (colunas == null)
    return colunas
}


fun processaCoordenadas(coordenadas: String?, numLinhas: Int, numColunas: Int): Pair<Int, Int>? {


    if (coordenadas == null || coordenadas.length < 3 || coordenadas.length > 4) {
//        println("""!!! Coordenadas invalidas, tente novamente
//            |Coordenadas? (ex: 6,G)
//        """.trimMargin())
        return null
    } else {

        val colunaCode = coordenadas[coordenadas.length - 1].code
        var linha = coordenadas[0].toString().toIntOrNull()

        if (coordenadas.length == 3 && coordenadas[1] == ',' && colunaCode in 65..90) {
            if (linha != null) {
                return when {
                    linha == 0 -> null
                    linha <= numLinhas && colunaCode <= numColunas + 64 -> Pair(linha, colunaCode - 64)
                    else -> null
                }
            }
            println("""!!! Coordenadas invalidas, tente novamente
                |Coordenadas? (ex: 6,G)
            """.trimMargin())
            return null
        }
        if (coordenadas.length == 4 && coordenadas[2] == ',' && colunaCode in 65..90) {

            linha = (coordenadas[0].toString() + coordenadas[1].toString()).toInt()
            return when {
                linha == 0 -> null
                linha <= numLinhas && colunaCode <= numColunas + 64 -> Pair(linha, colunaCode - 64)
                else -> null
            }

        }else return null

    }
}


fun criaLegendaHorizontal(numColunas: Int): String {
    var count = 0
    var codigoAsciiLetraA = 65
    var legendaHorizontal = ""
    while (count < numColunas) {
        legendaHorizontal += if (count == numColunas - 1) {
            "${codigoAsciiLetraA.toChar()}"
        } else {
            "${codigoAsciiLetraA.toChar()} | "
        }
        count++
        codigoAsciiLetraA++
    }
    return legendaHorizontal
}

fun criaTerreno(numLinhas: Int, numColunas: Int): String {
    var linha = 0
    var terreno = ""
    terreno += "\n| "
    terreno += (criaLegendaHorizontal(numColunas))
    terreno += " |\n"
    while (linha < numLinhas) {
        var coluna = 0
        while (coluna < numColunas) {
            terreno += "|   "

            if (coluna == numColunas - 1) {
                terreno += "| ${linha + 1}"
            }
            coluna++
        }
        terreno += "\n"
        linha++
    }
    return terreno
}


fun calculaNumNavios(numLinhas: Int, numColunas: Int): Array<Int> {

    val numNavios: Array<Int> = when {
        numLinhas == 4 && numColunas == 4 -> arrayOf(2, 0, 0, 0)
        numLinhas == 5 && numColunas == 5 -> arrayOf(1, 1, 1, 0)
        numLinhas == 7 && numColunas == 7 -> arrayOf(2, 1, 1, 1)
        numLinhas == 8 && numColunas == 8 -> arrayOf(2, 2, 1, 1)
        numLinhas == 10 && numColunas == 10 -> arrayOf(3, 2, 1, 1)
        else -> emptyArray()
    }
    return numNavios
}


fun criaTabuleiroVazio(numLinhas: Int, numColunas: Int): Array<Array<Char?>> {
    return Array(numLinhas) { Array(numColunas) { null } }
}


fun coordenadaContida(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {
    return linha in 1..tabuleiro.size && coluna in 1..tabuleiro[0].size
}


fun limparCoordenadasVazias(coordenadas: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
    var coordenadasLimpas = emptyArray<Pair<Int, Int>>()

    for (coordenada in coordenadas) if (coordenada != Pair(0, 0)) {
        coordenadasLimpas += coordenada
    }
    return coordenadasLimpas
}


fun juntarCoordenadas(coordenadas: Array<Pair<Int, Int>>, coordenadas2: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
    var coordenadasJuntas = coordenadas

    for (coordenada in coordenadas2) coordenadasJuntas += coordenada

    return coordenadasJuntas
}

fun gerarCoordenadasNavio(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int, orientacao: String, dimensao: Int): Array<Pair<Int, Int>> {

    var coordenadasNavio = emptyArray<Pair<Int, Int>>()
    var possiveisCoordenadasNavio = emptyArray<Pair<Int, Int>>()

    if (coordenadaContida(tabuleiro, linha, coluna)) {
        when (orientacao) {
            "E" -> for (colunas in coluna..coluna + 3) {
                if (coordenadaContida(tabuleiro, linha, colunas)) {
                    possiveisCoordenadasNavio += Pair(linha, colunas)
                }
            }

            "N" -> for (linhas in linha downTo linha - 3) {
                if (coordenadaContida(tabuleiro, linhas, coluna)) {
                    possiveisCoordenadasNavio += Pair(linhas, coluna)
                }
            }

            "O" -> for (colunas in coluna downTo coluna - 3) {
                if (coordenadaContida(tabuleiro, linha, colunas)) {
                    possiveisCoordenadasNavio += Pair(linha, colunas)
                }
            }

            "S" -> {
                for (linhas in linha..linha + 3) {
                    if (coordenadaContida(tabuleiro, linhas, coluna)) {
                        possiveisCoordenadasNavio += Pair(linhas, coluna)
                    }
                }
            }
        }
    } else return emptyArray()

    when {
        possiveisCoordenadasNavio.size < dimensao -> return coordenadasNavio
        possiveisCoordenadasNavio.isEmpty() -> return coordenadasNavio
        else -> {
            for (posicao in 0 until dimensao) coordenadasNavio += possiveisCoordenadasNavio[posicao]
        }

    }
    return coordenadasNavio
}

fun gerarCoordenadasFronteira(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int, orientacao: String, dimensao: Int): Array<Pair<Int, Int>> {

    var coordenadasFronteira: Array<Pair<Int, Int>> = emptyArray()
    val coordenadasNavio = gerarCoordenadasNavio(tabuleiro, linha, coluna, orientacao, dimensao)

    if (coordenadasNavio.isNotEmpty()) {
        for (coordenadas in coordenadasNavio) {

            val linhaNavioNaMatriz = coordenadas.first
            val colunaNavioNaMatriz = coordenadas.second

            for (i in linhaNavioNaMatriz - 1..linhaNavioNaMatriz + 1) {
                for (j in colunaNavioNaMatriz - 1..colunaNavioNaMatriz + 1) {

                    if (coordenadaContida(tabuleiro, i, j)) {
                        if ((Pair(i, j) !in coordenadasNavio) && (Pair(i, j) !in coordenadasFronteira)) {
                            coordenadasFronteira += Pair(i, j)
                        }
                    }

                }
            }
        }
        return coordenadasFronteira
    }
    return emptyArray()
}


fun estaLivre(tabuleiro: Array<Array<Char?>>, conjuntoDeCoordenadas: Array<Pair<Int, Int>>): Boolean {
    for (coordenada in conjuntoDeCoordenadas) {
        if (tabuleiro[coordenada.first - 1][coordenada.second - 1] != null) return false
    }
    return true
}

fun insereNavioSimples(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int, dimensao: Int): Boolean {

    val coordenadasNavio = gerarCoordenadasNavio(tabuleiro, linha, coluna, "E", dimensao)
    val coordenadasFronteira = gerarCoordenadasFronteira(tabuleiro, linha, coluna, "E", dimensao)
    val coordenadasNavioEFronteira = juntarCoordenadas(coordenadasNavio, coordenadasFronteira)

    if (coordenadasNavioEFronteira.isNotEmpty() && (estaLivre(tabuleiro, coordenadasNavioEFronteira))) {
        for (coordenada in coordenadasNavio) {
            tabuleiro[coordenada.first - 1][coordenada.second - 1] = dimensao.toString().first()
        }
        return true
    }
    return false
}


fun insereNavio(tabuleiro: Array<Array<Char?>>, linha: Int, coluna: Int, orientacao: String, dimensao: Int): Boolean {
    if (orientacao == "E") {
        return insereNavioSimples(tabuleiro, linha, coluna, dimensao)
    }

    val coordenadasNavio = gerarCoordenadasNavio(tabuleiro, linha, coluna, orientacao, dimensao)
    val coordenadasFronteira = gerarCoordenadasFronteira(tabuleiro, linha, coluna, orientacao, dimensao)
    val coordenadasNavioEFronteira = juntarCoordenadas(coordenadasNavio, coordenadasFronteira)

    if (coordenadasNavioEFronteira.isNotEmpty() && estaLivre(tabuleiro, coordenadasNavioEFronteira)) {
        for (coordenada in coordenadasNavio) {
            tabuleiro[coordenada.first - 1][coordenada.second - 1] = dimensao.toString().first()
        }
        return true
    } else {
        return false
    }
}


fun preencheTabuleiroComputador(tabuleiro: Array<Array<Char?>>, numeroDeNavios: Array<Int>): Array<Array<Char?>> {

    var sucessoInsercao: Boolean

    for (posicao in 3 downTo 0) {
        if (numeroDeNavios[posicao] != 0) {
            for (count in numeroDeNavios[posicao] downTo 1) { //0 até numero de vezes que esse navio tem de ser inserido
                do {
                    val linhaAleatoria = (1..numLinhas).random()
                    val colunaAleatoria = (1..numColunas).random()
                    val orientacao = when (posicao) {
                        0 -> "E"
                        else -> arrayOf("E", "N", "O", "S").random()
                    }
                    sucessoInsercao = insereNavio(tabuleiro, linhaAleatoria, colunaAleatoria, orientacao, posicao + 1)

                } while (!sucessoInsercao)
            }
        }
    }
    return tabuleiro
}


fun navioCompleto(tabuleiroPalpites: Array<Array<Char?>>, linha: Int, coluna: Int): Boolean {

    var coordenadasIguaisPosicaoAtingida = emptyArray<Pair<Int, Int>>()

    if(coordenadaContida(tabuleiroPalpites,linha,coluna)){

        when (val posicaoAtingida = tabuleiroPalpites[linha-1][coluna-1]) {
            null, 'X' -> return false
            '1' -> return true
            '2', '3', '4' -> {
                val dimensaoMenos1 = posicaoAtingida.toString().toInt() - 1

                for (colunas in coluna - dimensaoMenos1..coluna + dimensaoMenos1) {
                    if (coordenadaContida(tabuleiroPalpites, linha, colunas)) {
                        if (tabuleiroPalpites[linha-1][colunas-1] == posicaoAtingida &&
                                Pair(linha, colunas) !in coordenadasIguaisPosicaoAtingida) {
                            coordenadasIguaisPosicaoAtingida += Pair(linha, colunas)
                        }
                    }
                }
                if (coordenadasIguaisPosicaoAtingida.size == posicaoAtingida.toString().toInt()) return true

                for (linhas in linha - dimensaoMenos1..linha + dimensaoMenos1) {
                    if (coordenadaContida(tabuleiroPalpites, linhas, coluna)) {
                        if (tabuleiroPalpites[linhas-1][coluna-1] == posicaoAtingida &&
                                Pair(linhas, coluna) !in coordenadasIguaisPosicaoAtingida) {
                            coordenadasIguaisPosicaoAtingida += Pair(linhas, coluna)
                        }

                    }
                }
                return coordenadasIguaisPosicaoAtingida.size == posicaoAtingida.toString().toInt()
            }
        }
    }
    return false
}

fun obtemMapa(tabuleiroReal: Array<Array<Char?>>, eTabuleiroReal: Boolean): Array<String> {

    val mapa = Array(tabuleiroReal.size + 1) { "|" }

    val codAscii2 = '\u2082'
    val codAscii3 = '\u2083'
    val codAscii4 = '\u2084'


    mapa[0] += " ${criaLegendaHorizontal(tabuleiroReal[0].size)} |"

    for (linha in 0 until tabuleiroReal.size) {
        var linhaDoMapa = "|"
        for (coluna in 0 until tabuleiroReal[linha].size) {

            val valor = tabuleiroReal[linha][coluna]

            when (tabuleiroReal[linha][coluna]) {
                null -> {
                    linhaDoMapa += if (eTabuleiroReal) " ~ |" else " ? |"
                }
                'X' -> linhaDoMapa += " X |"
                '1', '2', '3', '4' -> {
                    if (eTabuleiroReal) linhaDoMapa += " $valor |" else {
                        if (navioCompleto(tabuleiroReal, linha+1, coluna+1)) {
                            linhaDoMapa += " $valor |"
                        } else {
                            when (valor) {
                                '2' -> linhaDoMapa += " $codAscii2 |"
                                '3' -> linhaDoMapa += " $codAscii3 |"
                                '4' -> linhaDoMapa += " $codAscii4 |"
                            }
                        }
                    }
                }

            }
            if (coluna == tabuleiroReal[linha].size - 1) linhaDoMapa += " ${linha + 1}"
        }
        mapa[linha + 1] = linhaDoMapa
    }
    return mapa
}

fun lancarTiro(tabuleiroReal: Array<Array<Char?>>, tabuleiroPalpites: Array<Array<Char?>>, linhaEColuna: Pair<Int, Int>): String {

    val mensagem: String

    when (tabuleiroReal[linhaEColuna.first - 1][linhaEColuna.second - 1]) {
        null -> {
            tabuleiroPalpites[linhaEColuna.first - 1][linhaEColuna.second - 1] = 'X'
            mensagem = "Agua."
        }

        '1' -> {
            tabuleiroPalpites[linhaEColuna.first - 1][linhaEColuna.second - 1] = '1'
            mensagem = "Tiro num submarino."
        }

        '2' -> {
            tabuleiroPalpites[linhaEColuna.first - 1][linhaEColuna.second - 1] = '2'
            mensagem = "Tiro num contra-torpedeiro."
        }

        '3' -> {
            tabuleiroPalpites[linhaEColuna.first - 1][linhaEColuna.second - 1] = '3'
            mensagem = "Tiro num navio-tanque."
        }

        '4' -> {
            tabuleiroPalpites[linhaEColuna.first - 1][linhaEColuna.second - 1] = '4'
            mensagem = "Tiro num porta-avioes."
        }

        else -> mensagem = "Agua."
    }

    return mensagem
}

fun geraTiroComputador(tabuleiroPalpitesComputador: Array<Array<Char?>>): Pair<Int, Int> {

    var coordenadasNull = emptyArray<Pair<Int, Int>>()

    for (linha in 0 until tabuleiroPalpitesComputador.size) {
        for (coluna in 0 until tabuleiroPalpitesComputador[linha].size) {
            if (tabuleiroPalpitesComputador[linha][coluna] == null) coordenadasNull += Pair(linha + 1, coluna + 1)
        }
    }
    val tiroComputador = coordenadasNull.random()

    return tiroComputador
}

fun contarNaviosDeDimensao(tabuleiro: Array<Array<Char?>>, dimensao: Int): Int {

    var count = 0

    for (linha in 0 until tabuleiro.size) {
        for (coluna in 0 until tabuleiro[0].size) {
            if (tabuleiro[linha][coluna] == dimensao.toString().first()) {
                if (navioCompleto(tabuleiro, linha+1, coluna+1)) count++
            }
        }
    }
    when (dimensao) {
        2 -> count /= 2
        3 -> count /= 3
        4 -> count /= 4
    }

    return count
}

fun venceu(tabuleiro: Array<Array<Char?>>): Boolean {

    val numeroDeNavios = calculaNumNavios(tabuleiro.size, tabuleiro[0].size)

    return (numeroDeNavios[0] == contarNaviosDeDimensao(tabuleiro, 1) &&
            numeroDeNavios[1] == contarNaviosDeDimensao(tabuleiro, 2) &&
            numeroDeNavios[2] == contarNaviosDeDimensao(tabuleiro, 3) &&
            numeroDeNavios[3] == contarNaviosDeDimensao(tabuleiro, 4)
            )
}

fun lerJogo(nomeDoFicheiro: String, tipoDeTabuleiro: Int): Array<Array<Char?>> {

    val ficheiro = File(nomeDoFicheiro).readLines()
    numLinhas = ficheiro[0][0].toString().toInt()
    numColunas = ficheiro[0][2].toString().toInt()
    val tabuleiro = criaTabuleiroVazio(numLinhas, numColunas)
    var count = 0

    when (tipoDeTabuleiro) {
        1 -> {
            for (linha in 4 until 4 + numLinhas) {
                val partes = ficheiro[linha].split(",")
                for (i in 0 until partes.size) {
                    if (partes[i] != "") {
                        tabuleiro[count][i] = partes[i].trim().first()
                    }
                }
                count++
            }
        }

        2 -> {
            for (linha in numLinhas + 7 until numLinhas * 2 + 7) {
                val partes = ficheiro[linha].split(",")
                for (i in 0 until partes.size) {
                    if (partes[i] != "") {
                        tabuleiro[count][i] = partes[i].trim().first()
                    }
                }
                count++
            }
        }

        3 -> {
            for (linha in numLinhas * 2 + 10 until numLinhas * 3 + 10) {
                val partes = ficheiro[linha].split(",")
                for (i in 0 until partes.size) {
                    if (partes[i] != "") {
                        tabuleiro[count][i] = partes[i].trim().first()
                    }
                }
                count++
            }
        }

        4 -> {
            for (linha in numLinhas * 3 + 13 until numLinhas * 4 + 13) {
                val partes = ficheiro[linha].split(",")
                for (i in 0 until partes.size) {
                    if (partes[i] != "") {
                        tabuleiro[count][i] = partes[i].trim().first()
                    }
                }
                count++
            }
        }
    }

    return tabuleiro
}

fun gravarJogo(nomeDoFicheiro: String,
               tabuleiroRealHumano: Array<Array<Char?>>,
               tabuleiroPalpitesHumano: Array<Array<Char?>>,
               tabuleiroRealComputador: Array<Array<Char?>>,
               tabuleiroPalpitesComputador: Array<Array<Char?>>) {

    val tabuleiros = arrayOf(tabuleiroRealHumano, tabuleiroPalpitesHumano, tabuleiroRealComputador, tabuleiroPalpitesComputador)

    val fileprinter = File(nomeDoFicheiro).printWriter()
    var caracter: String?
    val codAscii2 = '\u2082'
    val codAscii3 = '\u2083'
    val codAscii4 = '\u2084'

    fileprinter.println("${tabuleiroRealHumano.size},${tabuleiroRealHumano[0].size}\n") // tamanho do tabuleiro

    for (tabuleiro in tabuleiros) {
        when (tabuleiro) {
            tabuleiroRealHumano -> fileprinter.println("Jogador\nReal")
            tabuleiroPalpitesHumano -> fileprinter.println("\nJogador\nPalpites")
            tabuleiroRealComputador -> fileprinter.println("\nComputador\nReal")
            tabuleiroPalpitesComputador -> fileprinter.println("\nComputador\nPalpites")
        }
        for (linha in 0 until tabuleiro.size) {
            for (coluna in 0 until tabuleiro[0].size) {
                caracter = when (tabuleiro[linha][coluna]) {
                    '1', '2', '3', '4', 'X' -> tabuleiro[linha][coluna].toString()
                    codAscii2 -> "2"
                    codAscii3 -> "3"
                    codAscii4 -> "4"
                    else -> ""
                }
                fileprinter.print(caracter)
                if (coluna == tabuleiro[0].size - 1) fileprinter.print("\n") else fileprinter.print(',')
            }
        }
    }
    fileprinter.close()
}

fun main() {
    var menuAtual = MENU_PRINCIPAL
    while (true) {
        menuAtual = when (menuAtual) {
            MENU_PRINCIPAL -> menuPrincipal()
            MENU_DEFINIR_TABULEIRO -> menuDefinirTabuleiro()
            MENU_DEFINIR_NAVIOS -> menuDefinirNavios()
            MENU_JOGAR -> menuJogar()
            MENU_LER_FICHEIRO -> menuLerFicheiro()
            MENU_GRAVAR_FICHEIRO -> menuGravarFicheiro()
            SAIR -> return
            else -> return
        }
    }
}
