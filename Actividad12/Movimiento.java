package Actividad12;

public class Movimiento {
    String dni;
    String tipo; // INGRESO o RETIRADA
    double cantidad;
    String fecha; // formato dd/MM/yyyy HH:mm:ss
    String concepto;

    public Movimiento(String dni, String tipo, double cantidad, String fecha, String concepto) {
        this.dni = dni;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.concepto = concepto;
    }
}
