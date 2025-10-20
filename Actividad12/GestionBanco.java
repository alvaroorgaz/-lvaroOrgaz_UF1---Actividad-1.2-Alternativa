package Actividad12;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/*
 * GestionBanco.java
 Aplicación para gestionar múltiples cuentas bancarias con persistencia en disco.
 */

public class GestionBanco {

    // Limites fijos (según requerimiento: usar arrays)
    static final int MAX_CUENTAS = 300;
    static final int MAX_MOVIMIENTOS = 2000;

    // Estructuras principales
    static Cuenta[] cuentas = new Cuenta[MAX_CUENTAS];
    static int numCuentas = 0;

    static Movimiento[] movimientos = new Movimiento[MAX_MOVIMIENTOS];
    static int numMovimientos = 0;

    // Rutas de datos
    static final String CARPETA_DATOS = "datos";
    static final String FICHERO_CUENTAS = CARPETA_DATOS + File.separator + "cuentas.txt";
    static final String FICHERO_MOVIMIENTOS = CARPETA_DATOS + File.separator + "movimientos.txt";

    // Scanner para entrada por consola (debe usarse)
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        inicializarSistema();
        menuPrincipal();
        sc.close();
    }

    // Inicialización: carpeta y ficheros; carga con Scanner
    static void inicializarSistema() {
        try {
            File carpeta = new File(CARPETA_DATOS);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
                System.out.println("Creada carpeta: " + CARPETA_DATOS);
            }

            File fCuentas = new File(FICHERO_CUENTAS);
            File fMovs = new File(FICHERO_MOVIMIENTOS);

            if (!fCuentas.exists()) {
                fCuentas.createNewFile();
                System.out.println("Creado fichero: " + FICHERO_CUENTAS);
            }
            if (!fMovs.exists()) {
                fMovs.createNewFile();
                System.out.println("Creado fichero: " + FICHERO_MOVIMIENTOS);
            }

            // Cargar cuentas
            cargarCuentasDesdeFichero();
            // Cargar movimientos
            cargarMovimientosDesdeFichero();

        } catch (IOException e) {
            System.err.println("Error al inicializar sistema: " + e.getMessage());
        }
    }

    static void cargarCuentasDesdeFichero() {
        try {
            Scanner f = new Scanner(new File(FICHERO_CUENTAS));
            while (f.hasNextLine()) {
                String linea = f.nextLine().trim();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split(";");
                // Formato: DNI;NOMBRE_COMPLETO;NUMERO_CUENTA;SALDO
                if (partes.length < 4) continue; // línea malformada, se ignora
                String dni = partes[0];
                String nombre = partes[1];
                String numCuenta = partes[2];
                double saldo = 0.0;
                try {
                    saldo = Double.parseDouble(partes[3]);
                } catch (NumberFormatException ex) {
                    // si no se puede parsear, dejar saldo 0
                }
                if (numCuentas < MAX_CUENTAS) {
                    cuentas[numCuentas++] = new Cuenta(dni, nombre, numCuenta, saldo);
                }
            }
            f.close();
            System.out.println("Cargadas " + numCuentas + " cuentas desde " + FICHERO_CUENTAS);
        } catch (FileNotFoundException e) {
            System.err.println("Fichero de cuentas no encontrado: " + e.getMessage());
        }
    }

    static void cargarMovimientosDesdeFichero() {
        try {
            Scanner f = new Scanner(new File(FICHERO_MOVIMIENTOS));
            while (f.hasNextLine()) {
                String linea = f.nextLine().trim();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split(";");
                // Formato: DNI;TIPO;CANTIDAD;FECHA;CONCEPTO
                if (partes.length < 5) continue;
                String dni = partes[0];
                String tipo = partes[1];
                double cantidad = 0.0;
                try {
                    cantidad = Double.parseDouble(partes[2]);
                } catch (NumberFormatException ex) {
                    // dejar en 0
                }
                String fecha = partes[3];
                // El concepto puede contener puntos y comas si el split hubiese partido; reconstruir
                String concepto = partes[4];
                if (partes.length > 5) {
                    // Unir el resto con ";" original por si contenía ; en el concepto
                    StringBuilder sb = new StringBuilder(concepto);
                    for (int i = 5; i < partes.length; i++) {
                        sb.append(";").append(partes[i]);
                    }
                    concepto = sb.toString();
                }
                if (numMovimientos < MAX_MOVIMIENTOS) {
                    movimientos[numMovimientos++] = new Movimiento(dni, tipo, cantidad, fecha, concepto);
                }
            }
            f.close();
            System.out.println("Cargados " + numMovimientos + " movimientos desde " + FICHERO_MOVIMIENTOS);
        } catch (FileNotFoundException e) {
            System.err.println("Fichero de movimientos no encontrado: " + e.getMessage());
        }
    }

    // Menú principal
    static void menuPrincipal() {
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("--- BANCO - MENÚ PRINCIPAL ---");
            System.out.println("1. Listar todas las cuentas");
            System.out.println("2. Crear nueva cuenta");
            System.out.println("3. Seleccionar cuenta para operar");
            System.out.println("4. Ver estad\u00edsticas generales");
            System.out.println("5. Salir (guardar)");
            System.out.print("Elige una opción: ");
            String opcion = sc.nextLine().trim();
            switch (opcion) {
                case "1": listarCuentas(); break;
                case "2": crearNuevaCuenta(); break;
                case "3": seleccionarCuenta(); break;
                case "4": verEstadisticas(); break;
                case "5": guardarYSalir(); salir = true; break;
                default: System.out.println("Opción no válida.");
            }
        }
    }

    static void listarCuentas() {
        System.out.println("--- LISTADO DE CUENTAS ---");
        if (numCuentas == 0) {
            System.out.println("No hay cuentas registradas.");
            return;
        }
        for (int i = 0; i < numCuentas; i++) {
            Cuenta c = cuentas[i];
            System.out.printf("%d) DNI: %s | Nombre: %s | Saldo: %.2f\n", i+1, c.dni, c.nombre, c.saldo);
        }
    }

    static void crearNuevaCuenta() {
        System.out.println("--- CREAR NUEVA CUENTA ---");
        System.out.print("DNI: ");
        String dni = sc.nextLine().trim();
        if (dni.isEmpty()) {
            System.out.println("DNI vacío."); return; }
        if (buscarIndicePorDNI(dni) != -1) {
            System.out.println("Ya existe una cuenta con ese DNI.");
            return;
        }
        System.out.print("Nombre completo: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("Nombre vacío."); return; }
        if (numCuentas >= MAX_CUENTAS) {
            System.out.println("Límite de cuentas alcanzado.");
            return;
        }
        String numeroCuenta = generarNumeroCuenta();
        Cuenta nueva = new Cuenta(dni, nombre, numeroCuenta, 0.0);
        cuentas[numCuentas++] = nueva;
        System.out.println("Cuenta creada con número: " + numeroCuenta);
        // Guardar inmediatamente cambios en fichero de cuentas (para persistencia intermedia)
        guardarCuentasEnFichero();
    }

    static void seleccionarCuenta() {
        System.out.println("--- SELECCIONAR CUENTA ---");
        System.out.print("Introduce DNI del titular: ");
        String dni = sc.nextLine().trim();
        int idx = buscarIndicePorDNI(dni);
        if (idx == -1) {
            System.out.println("DNI no encontrado.");
            return;
        }
        menuCuenta(idx);
    }

    static void verEstadisticas() {
        System.out.println("--- ESTADISTICAS GENERALES ---");
        System.out.println("Número total de cuentas: " + numCuentas);
        double total = 0.0;
        double saldoPos = 0.0;
        double saldoNeg = 0.0;
        for (int i = 0; i < numCuentas; i++) {
            total += cuentas[i].saldo;
            if (cuentas[i].saldo >= 0) saldoPos += cuentas[i].saldo; else saldoNeg += cuentas[i].saldo;
        }
        System.out.printf("Saldo total del banco: %.2f\n", total);
        System.out.printf("Saldo acumulado (>=0): %.2f\n", saldoPos);
        System.out.printf("Saldo negativo total (<0): %.2f\n", saldoNeg);
    }

    static void guardarYSalir() {
        System.out.println("Guardando datos...");
        guardarCuentasEnFichero();
        // Los movimientos nuevos ya se escribieron al hacer cada movimiento en modo append.
        System.out.println("Datos guardados. Saliendo.");
    }

    static void guardarCuentasEnFichero() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FICHERO_CUENTAS, false)); // sobrescribe
            for (int i = 0; i < numCuentas; i++) {
                Cuenta c = cuentas[i];
                // Formato: DNI;NOMBRE_COMPLETO;NUMERO_CUENTA;SALDO
                pw.println(c.dni + ";" + c.nombre + ";" + c.numeroCuenta + ";" + String.format("%.2f", c.saldo));
            }
            pw.close();
        } catch (IOException e) {
            System.err.println("Error al guardar cuentas: " + e.getMessage());
        }
    }

    // Menú para operar sobre la cuenta seleccionada
    static void menuCuenta(int indice) {
        Cuenta c = cuentas[indice];
        boolean volver = false;
        while (!volver) {
            System.out.println();
            System.out.println("--- OPERACIONES - " + c.nombre + " (DNI: " + c.dni + ") ---");
            System.out.println("1. Consultar saldo");
            System.out.println("2. Ver datos del titular");
            System.out.println("3. Ver historial de movimientos");
            System.out.println("4. Realizar ingreso");
            System.out.println("5. Realizar retirada");
            System.out.println("6. Volver al menú principal");
            System.out.print("Elige una opción: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1": System.out.printf("Saldo actual: %.2f\n", c.saldo); break;
                case "2": System.out.println("Nombre: " + c.nombre + " | DNI: " + c.dni + " | Cuenta: " + c.numeroCuenta); break;
                case "3": mostrarMovimientosDeDNI(c.dni); break;
                case "4": realizarIngreso(c); break;
                case "5": realizarRetirada(c); break;
                case "6": volver = true; break;
                default: System.out.println("Opcion no valida.");
            }
        }
    }

    static void mostrarMovimientosDeDNI(String dni) {
        System.out.println("--- HISTORIAL DE MOVIMIENTOS de " + dni + " ---");
        boolean encontrado = false;
        for (int i = 0; i < numMovimientos; i++) {
            if (movimientos[i].dni.equals(dni)) {
                System.out.printf("%s | %s | %.2f | %s\n", movimientos[i].tipo, movimientos[i].concepto, movimientos[i].cantidad, movimientos[i].fecha);
                encontrado = true;
            }
        }
        if (!encontrado) System.out.println("No hay movimientos para este titular.");
    }

    static void realizarIngreso(Cuenta c) {
        System.out.print("Cantidad a ingresar: ");
        String linea = sc.nextLine().trim();
        double cantidad = 0.0;
        try {
            cantidad = Double.parseDouble(linea);
        } catch (NumberFormatException e) {
            System.out.println("Cantidad no válida."); return;
        }
        if (cantidad <= 0) { System.out.println("La cantidad debe ser mayor que 0."); return; }
        System.out.print("Concepto: ");
        String concepto = sc.nextLine().trim();
        c.saldo += cantidad;
        Movimiento m = new Movimiento(c.dni, "INGRESO", cantidad, fechaActual(), concepto);
        if (numMovimientos < MAX_MOVIMIENTOS) movimientos[numMovimientos++] = m;
        // Escribir movimiento al fichero en modo append
        escribirMovimientoEnFichero(m);
        // Actualizar fichero cuentas (guardar saldo)
        guardarCuentasEnFichero();
        System.out.println("Ingreso realizado. Nuevo saldo: " + String.format("%.2f", c.saldo));
    }

    static void realizarRetirada(Cuenta c) {
        System.out.print("Cantidad a retirar: ");
        String linea = sc.nextLine().trim();
        double cantidad = 0.0;
        try {
            cantidad = Double.parseDouble(linea);
        } catch (NumberFormatException e) {
            System.out.println("Cantidad no válida."); return;
        }
        if (cantidad <= 0) { System.out.println("La cantidad debe ser mayor que 0."); return; }
        if (c.saldo < cantidad) { System.out.println("Saldo insuficiente."); return; }
        System.out.print("Concepto: ");
        String concepto = sc.nextLine().trim();
        c.saldo -= cantidad;
        Movimiento m = new Movimiento(c.dni, "RETIRADA", cantidad, fechaActual(), concepto);
        if (numMovimientos < MAX_MOVIMIENTOS) movimientos[numMovimientos++] = m;
        escribirMovimientoEnFichero(m);
        guardarCuentasEnFichero();
        System.out.println("Retirada realizada. Nuevo saldo: " + String.format("%.2f", c.saldo));
    }

    static void escribirMovimientoEnFichero(Movimiento m) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(FICHERO_MOVIMIENTOS, true)); // append
            // Formato: DNI;TIPO;CANTIDAD;FECHA;CONCEPTO
            pw.println(m.dni + ";" + m.tipo + ";" + String.format("%.2f", m.cantidad) + ";" + m.fecha + ";" + m.concepto);
            pw.close();
        } catch (IOException e) {
            System.err.println("Error al escribir movimiento: " + e.getMessage());
        }
    }

    // Utilidades
    static int buscarIndicePorDNI(String dni) {
        for (int i = 0; i < numCuentas; i++) {
            if (cuentas[i].dni.equalsIgnoreCase(dni)) return i;
        }
        return -1;
    }

    static String generarNumeroCuenta() {
        // Genera un número de cuenta tipo ES + 22 dígitos aleatorios
        StringBuilder sb = new StringBuilder("ES");
        for (int i = 0; i < 22; i++) {
            sb.append((int)(Math.random() * 10));
        }
        // Aseguramos unicidad simple comprobando que no exista
        String candidato = sb.toString();
        boolean unico = true;
        for (int i = 0; i < numCuentas; i++) {
            if (cuentas[i].numeroCuenta.equals(candidato)) { unico = false; break; }
        }
        if (unico) return candidato; else return generarNumeroCuenta();
    }

    static String fechaActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date());
    }


    }


