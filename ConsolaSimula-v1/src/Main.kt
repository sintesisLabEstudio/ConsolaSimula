//PARA JSON
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

//------- CLASES
data class Usuario(
    var idUsuario: Int = 0,
    var nikUsuario: String = "",
    var passUsuario: String = ""
)

data class Item(
    var idItem: Int,
    var idUsuario: Int,
    var nomItem: String,
    var valorItem: Int
)

data class Simula(
    var idUsuario: Int = 0,
    var idSim: Int = 0,
    var nomSim: String = "",
    var monTot: Int = 0,
    var totItems: Int = 0,
    var mesesDisp: Int = 0,
    var diasRest: Int = 0,
    var dif: Int = 0
)

// -- LISTAS GLOBALES
val listaUsuarios = mutableListOf<Usuario>()
val listaItems = mutableListOf<Item>()
val listaSimulaciones = mutableListOf<Simula>()

val gson = Gson()
val archivoUsuarios = File("usuarios.json")
var indiceUsuarioActivo=0
val archivoTotales = File("totales.json")

//------- FUNCIONES BASE
fun defineListaUsuarios(){
    listaUsuarios.add(Usuario(1,"jorge","clave"))
    listaUsuarios.add(Usuario(2, "maria", "amelia"))

}

fun mostrarUsuarios() {
    println("--- Lista de Usuarios ---")
    listaUsuarios.forEach { usuario ->
        println("ID: ${usuario.idUsuario} | Nick: ${usuario.nikUsuario}")
    }
}

fun validaUsuario(): Usuario ?{
    println("Nombre de Usuario")
    val usuarioNom:String = readln()
    val encontrado = listaUsuarios.find { it.nikUsuario == usuarioNom }
    //si no encuentra el nombre sale de inmediato
    if(encontrado == null){
        println("no existe este usuario")
        return null
    } else {
        println("clave de Usuario")
        val usuarioPass= readln()
        //obtiene clave correcta
        if(encontrado.passUsuario==usuarioPass){
            indiceUsuarioActivo=encontrado.idUsuario
            println("clave correcta")
            return encontrado
        } else {
            println("clave incorrecta")
            return null
        }
    }
}

fun ingresaItems(){
    println("-------------------------------------------------------\n")
    println("ingreso de los items de gastos mensuales y sus valores")
    println("-------------------------------------------------------\n")
    //LOOP
    while (true){
        //NOMBRE ITEM
        var itemNombre: String
        while (true){
            println("ingrese Nombre del Item")
            itemNombre = readln()
            if (itemNombre.isNotBlank() && itemNombre.toIntOrNull() == null) {
                // permitir textos que contengan letras y números mezclados (como "Dpto 101"
                break
            } else {
                println("Error: No puede ingresar un número como nombre.")
            }
        }
        //VALOR ITEM
        println("ingrese valor del Item")
        val itemValor = readln().toIntOrNull() ?: 0

        //INDICE LISTA
        val indiceLista: Int=listaItems.size + 1

        //INGRESA ITEM LISTA
        if(itemNombre != null && itemValor>=0){
            listaItems.add(Item(indiceLista,indiceUsuarioActivo,itemNombre,itemValor))
        }

        //CONSULTA SALIDA
        println("---------------------\n")
        println("Desea continuar s/n")
        val continuar = readln()
        if(continuar=="n" || continuar=="N"){
            break
        }
    }
    //guarda en json archivo
    if(listaItems.isNotEmpty()){
        guardarItemsEnJsonManual()
    }
}

fun presentaItems(){
    println("Presenta los items de gastos mensuales y sus valores")
    println("-------------------------------------------------------\n")
    val itemsUsuario = listaItems.filter { it.idUsuario==indiceUsuarioActivo }
    if(itemsUsuario.isEmpty()){
        println("No hay items ingresados")
    }else {
        itemsUsuario.forEach{
            item -> println("ID: ${item.idItem} | ITEM: ${item.nomItem} | VALOR: ${item.valorItem}")
        }
    }
    val total =sumaItemsLista()
    println("El valor total de los items es: $total")
}

fun sumaItemsLista(): Int {
    if(listaItems.isEmpty()){
        return 0
    }else {
        var total: Int =0
        listaItems.forEach{
                item -> total+=item.valorItem
        }
        return total
    }
}

fun guardarItemsEnJsonManual() {
    val jsonString = listaItems.joinToString(
        prefix = "[\n",
        postfix = "\n]",
        separator = ",\n"
    ) { item ->
        """  {
    "idItem": ${item.idItem},
    "idUsuario": ${item.idUsuario},
    "nomItem": "${item.nomItem}",
    "valorItem": ${item.valorItem}
  }"""
    }
    // Guarda el String generado en el archivo
    File("items.json").writeText(jsonString)
    println("¡Lista guardada en items.json!")

    //RUTA
    val archivo = File("items.json")
    archivo.writeText(jsonString)
    // Imprime la ruta absoluta exacta (ej: C:\Usuarios\TuNombre\IdeaProjects\MiProyecto\items.json)
    println("Archivo guardado en: ${archivo.absolutePath}")
}

fun cargaListaItemsJson() {
    val archivo = File("items.json")

    if (archivo.exists()) {
        val json = archivo.readText()
        val tipoLista = object : TypeToken<MutableList<Item>>() {}.type

        val listaCargada: MutableList<Item> = gson.fromJson(json, tipoLista)

        listaItems.clear()
        listaItems.addAll(listaCargada)

        println("=== LISTA DE ITEMS CARGADOS DESDE DISCO ===")
        listaItems.forEach { item ->
            println("ID Item: ${item.idItem} | ID Usuario: ${item.idUsuario} | Nombre: ${item.nomItem} | VALOR: ${item.valorItem}")
        }
    } else {
        println("Error: No se encontró el archivo items.json en el disco.")
    }
}

fun guardarUsuarios() {
    val jsonString = gson.toJson(listaUsuarios)
    archivoUsuarios.writeText(jsonString)
    println("Lista de usuarios guardada en JSON.")
}

fun cargarUsuarios() {
    if (archivoUsuarios.exists()) {
        val jsonString = archivoUsuarios.readText()
        val tipoLista = object : TypeToken<MutableList<Usuario>>() {}.type
        val listaCargada: MutableList<Usuario> = gson.fromJson(jsonString, tipoLista)
        listaUsuarios.clear()
        listaUsuarios.addAll(listaCargada)
        // DESPLIEGUE EN CONSOLA
        println("=== LISTA DE USUARIOS CARGADOS DESDE DISCO ===")
        listaUsuarios.forEach { u ->
            println("ID: ${u.idUsuario} | Nick: ${u.nikUsuario}")
        }
    } else {
        println("No se encontró el archivo JSON. Se iniciará con lista por defecto.")
        defineListaUsuarios()
    }
}

fun inputMontoDisponible() {
    val itemsTotal = sumaItemsLista()
    if (itemsTotal <= 0) {
        println("El total de items debe ser mayor a 0.")
        return
    }

    // 1. Pedir un nombre para la simulación
    println("Ingrese un nombre para esta simulación:")
    val nombreSim = readln()

    println("Ingrese el Monto total disponible:")
    val montoTotal: Int = readln().toIntOrNull() ?: 0

    // Cálculos
    val meses: Int = montoTotal / itemsTotal
    val diferencia: Int = montoTotal % itemsTotal
    val gastoDiarioBase: Double = itemsTotal / 30.0
    val dias: Int = if (gastoDiarioBase > 0) (diferencia / gastoDiarioBase).toInt() else 0
    val montoDiario: Double = if (dias > 0) diferencia.toDouble() / dias else 0.0

    // Presentación
    println("\n--- RESULTADO ---")
    println("Meses disponibles: $meses")
    println("Diferencia sobrante: $$diferencia")
    if (dias > 0) {
        val diarioFormateado = String.format("%.2f", montoDiario)
        println("La diferencia de $$diferencia se distribuye en $dias días con un monto de $$diarioFormateado diario.")
    } else if (diferencia > 0) {
        println("La diferencia de $$diferencia no alcanza para cubrir un día completo.")
    }

    // 2. CREAR EL OBJETO CON LOS DATOS CALCULADOS
    val nuevaSimulacion = Simula(
        idUsuario = indiceUsuarioActivo,
        idSim = listaSimulaciones.size + 1,
        nomSim = nombreSim,
        monTot = montoTotal,
        totItems = itemsTotal,
        mesesDisp = meses,
        diasRest = dias,
        dif = diferencia
    )

    // 3. GUARDAR EL OBJETO EN LA LISTA GLOBAL
    listaSimulaciones.add(nuevaSimulacion)

    // 4. AHORA SÍ GUARDAR EN EL ARCHIVO JSON
    grabaTotalesJson()
}

fun grabaTotalesJson() {
    val jsonString = gson.toJson(listaSimulaciones)
    archivoTotales.writeText(jsonString)
    println("-> Totales guardados correctamente en totales.json")
}

fun cargaTotalesJson() {
    if (archivoTotales.exists() && archivoTotales.length() > 0) {
        val jsonString = archivoTotales.readText()
        val tipoLista = object : TypeToken<MutableList<Simula>>() {}.type

        val listaCargada: MutableList<Simula> = gson.fromJson(jsonString, tipoLista)

        listaSimulaciones.clear()
        listaSimulaciones.addAll(listaCargada)

        println("\n=== LISTA DE TOTALES / SIMULACIONES CARGADAS ===")
        listaSimulaciones.forEach { s ->
            println("Simulación: ${s.nomSim} (ID: ${s.idSim}) | Usuario ID: ${s.idUsuario}")
            println("   Monto Total: $${s.monTot} | Gastos Mensuales: $${s.totItems}")
            println("   Meses Disponibles: ${s.mesesDisp} | Días Extra: ${s.diasRest} días | Diferencia: $${s.dif}")
            println("--------------------------------------------------")
        }
    } else {
        println("-> El archivo totales.json no existe o está vacío.")
    }
}

fun main() {
    //CARGA LISTA USUARIOS
    defineListaUsuarios()
    guardarUsuarios()

    // INTENTOS 3
    for (intento in 1..3) {
        println("\n--- Intento $intento de 3 ---")
        val datosUsuario: Usuario?= validaUsuario()
        if(datosUsuario!=null){
            println("ingresando a ${datosUsuario.nikUsuario} a SIMULA ")
            break
        }
    }
    //SI EL USUARIO NO ESTA ACTIVO SALE DE INMEDIATO
    if(indiceUsuarioActivo<=0){
        println("Usuario no activo")
        return
    }

    //INGRESO ITEMS GASTOS MES
    ingresaItems()

    //PRESENTA ITEM DATOS
    presentaItems()

    //INPUT Y CALCULO MESES
    inputMontoDisponible()

    //presenta usuarios
    cargarUsuarios()

    //presenta items
    cargaListaItemsJson()

    //presenta totales
    cargaTotalesJson()
}




